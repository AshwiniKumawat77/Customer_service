package com.customer.main.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.customer.main.config.CacheConfig;
import com.customer.main.dto.AddressDto;
import com.customer.main.dto.CustomerEnquiryRequestDto;
import com.customer.main.dto.CustomerRequestDto;
import com.customer.main.dto.CustomerResponseDto;
import com.customer.main.dto.EmploymentDetailsDto;
import com.customer.main.dto.PageResponseDto;
import com.customer.main.entity.Address;
import com.customer.main.entity.AddressType;
import com.customer.main.entity.Customer;
import com.customer.main.entity.CustomerOutboxEvent;
import com.customer.main.entity.CustomerStatus;
import com.customer.main.entity.EmploymentDetails;
import com.customer.main.entity.MaskingUtil;
import com.customer.main.exception.BusinessException;
import com.customer.main.exception.CustomerAlreadyExistsException;
import com.customer.main.exception.CustomerNotFoundException;
import com.customer.main.exception.DatabaseException;
import com.customer.main.repository.CustomerOutboxEventRepository;
import com.customer.main.repository.CustomerRepository;
import com.customer.main.repository.CustomerRepositoryImpl;
import com.customer.main.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of Home Loan Customer Service.
 * Enforces business rules: age 21–65, unique PAN/email/mobile, status lifecycle.
 * Uses cache for reads, @Transactional for writes and read-only optimisation.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private static final int MIN_AGE_HOME_LOAN = 21;
    private static final int MAX_AGE_HOME_LOAN = 65;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerRepositoryImpl customerRepositoryImpl;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CustomerEventPublisher eventPublisher;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired CustomerOutboxEventRepository customerOutboxEventRepository;

    // ---------- Step 1: customer enquiry / basic registration ----------
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
    public CustomerResponseDto createCustomerEnquiry(CustomerEnquiryRequestDto dto) {
    	log.info("ENTER :: createCustomerEnquiry | pan={} | email={}", 	MaskingUtil.maskPan(dto.getPanNumber()), dto.getEmail());

        // Uniqueness checks (same as full create)
        if (existsByPan(dto.getPanNumber())) {
        	log.warn("createCustomerEnquiry failed | duplicate PAN | pan={}", MaskingUtil.maskPan(dto.getPanNumber()));
            throw new CustomerAlreadyExistsException("Pan: " + dto.getPanNumber());
        }

        if (exixstByUId(dto.getAadhaarNumber())) {
            log.warn("createCustomerEnquiry failed | duplicate UID | UID={}", dto.getAadhaarNumber());
            throw new CustomerAlreadyExistsException("Aadhaar no : " + MaskingUtil.maskAadhaar(dto.getAadhaarNumber()));
        }

        if (existsByEmail(dto.getEmail())) {
            log.warn("createCustomerEnquiry failed | duplicate email | email={}", dto.getEmail());
            throw new CustomerAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        Long mobile = Long.parseLong(dto.getMobileNumber());
        if (customerRepository.existsByMobile(mobile)) {
        	log.warn("createCustomerEnquiry failed | duplicate PAN | pan={}", MaskingUtil.maskPan(dto.getPanNumber()));

            throw new CustomerAlreadyExistsException("Mobile already registered: " + dto.getMobileNumber());
        }

        try {
            Customer customer = new Customer();
            customer.setFirstName(dto.getFirstName());
            customer.setLastName(dto.getLastName());
            customer.setGender(dto.getGender());
            customer.setDateOfBirth(dto.getDateOfBirth());
            customer.setEmail(dto.getEmail());
            customer.setMobile(mobile);
            customer.setPanNumber(dto.getPanNumber());
            customer.setAadhaarNumber(dto.getAadhaarNumber());
            customer.setCustomerUuid(UUID.randomUUID().toString());
            customer.setStatus(CustomerStatus.PENDING_KYC);
            customer.setCreatedDate(LocalDateTime.now());
            customer.setUpdatedDate(LocalDateTime.now());

            // Custom SQL Save with logging (REPLACE JpaRepository.save)
            customerRepositoryImpl.saveCustomerNative(
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getGender(),
                    customer.getDateOfBirth(),
                    customer.getEmail(),
                    customer.getMobile(),
                    customer.getPanNumber(),
                    customer.getAadhaarNumber(),
                    customer.getCustomerUuid(),
                    customer.getStatus().name(),
                    customer.getCreatedDate(),
                    customer.getUpdatedDate()
            );
            log.info("Customer enquiry saved successfully | uuid={}", customer.getCustomerUuid());

            // 🔥 OUTBOX SAVE
            saveOutboxEvent(customer, "CUSTOMER_REGISTERED");
            return mapEntityToResponse(customer);
        } catch (Exception e) {
        	log.error("ERROR :: createCustomerEnquiry | pan={} | message={}",  MaskingUtil.maskPan(dto.getPanNumber()),
        	        e.getMessage(), e);
            throw new DatabaseException("Failed to register customer enquiry", e);
        }
    }

 // ============================================================
    // 🔥 OUTBOX SAVE METHOD (CORE OF PATTERN)
    // ============================================================
   private void saveOutboxEvent(Customer customer, String eventType) {

    log.info("Creating outbox event | type={} | uuid={}",
            eventType, customer.getCustomerUuid());

    String payload;
    try {
        payload = objectMapper.writeValueAsString(customer);
    } catch (Exception e) {
        throw new RuntimeException("Failed to serialize customer", e);
    }

    CustomerOutboxEvent outbox = new CustomerOutboxEvent();
    outbox.setAggregateId(customer.getCustomerUuid());
    outbox.setEventType(eventType);
    outbox.setPayload(payload);
    outbox.setStatus("NEW");   // Better: use ENUM
    outbox.setRetryCount(0);
    outbox.setCreatedAt(LocalDateTime.now());

    customerOutboxEventRepository.save(outbox);

    log.info("Outbox event saved successfully | type={} | uuid={}",
            eventType, customer.getCustomerUuid());
}


	// ---------- Step 2: complete KYC & enrich ----------
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
    public CustomerResponseDto completeKyc(Long id, CustomerRequestDto dto) {
    	log.info("ENTER :: completeKyc | id={} | pan={}", id, MaskingUtil.maskPan(dto.getPanNumber()));
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        validateAgeForHomeLoan(dto.getDateOfBirth());

        // Reuse uniqueness checks if fields are being changed
        if (!customer.getPanNumber().equals(dto.getPanNumber()) && existsByPan(dto.getPanNumber())) {
            log.warn("completeKyc failed | duplicate PAN | id={} | pan={}", id,  MaskingUtil.maskPan(dto.getPanNumber()));
            throw new CustomerAlreadyExistsException(dto.getPanNumber());
        }
        if (!customer.getEmail().equals(dto.getEmail()) && existsByEmail(dto.getEmail())) {
            log.warn("completeKyc failed | duplicate email | id={} | email={}", id, dto.getEmail());
            throw new CustomerAlreadyExistsException("Email already registered: " + dto.getEmail());
        }
        Long mobile = Long.parseLong(dto.getMobileNumber());
        if (!customer.getMobile().equals(mobile) && customerRepository.existsByMobile(mobile)) {
            log.warn("completeKyc failed | duplicate mobile | id={} | mobile={}", id, dto.getMobileNumber());
            throw new CustomerAlreadyExistsException("Mobile already registered: " + dto.getMobileNumber());
        }

        try {
            // Basic fields
            customer.setFirstName(dto.getFirstName());
            customer.setLastName(dto.getLastName());
            customer.setGender(dto.getGender());
            customer.setDateOfBirth(dto.getDateOfBirth());
            customer.setEmail(dto.getEmail());
            customer.setMobile(mobile);
            customer.setPanNumber(dto.getPanNumber());
            customer.setAadhaarNumber(dto.getAadhaarNumber());

            // Address and employment details (full KYC)
            applyAddressAndEmployment(dto, customer);

            // Mark customer as ACTIVE after successful KYC completion
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setUpdatedDate(LocalDateTime.now());

            // Custom SQL Update with logging (REPLACE JpaRepository.save)
            customerRepositoryImpl.updateCustomerNative(
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getGender(),
                    customer.getDateOfBirth(),
                    customer.getEmail(),
                    customer.getMobile(),
                    customer.getPanNumber(),
                    customer.getAadhaarNumber(),
                    customer.getStatus().name(),
                    customer.getUpdatedDate()
            );
            return mapEntityToResponse(customer);
        } catch (Exception e) {
        	log.error("ERROR :: completeKyc | id={} | message={} | errorType={}", id, e.getMessage(), e.getClass().getSimpleName(), e);
            throw new DatabaseException("Failed to complete KYC for customer", e);
        }
        
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
    public CustomerResponseDto createCustomer(CustomerRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CustomerRequestDto must not be null");
        }
        if (dto.getDateOfBirth() == null) {
            throw new BusinessException("DATE_OF_BIRTH_REQUIRED", "Date of birth is required");
        }
        log.info("createCustomer started | pan={}", MaskingUtil.maskPan(dto.getPanNumber()));
        validateAgeForHomeLoan(dto.getDateOfBirth());

        if (existsByPan(dto.getPanNumber())) {
            log.warn("createCustomer failed | duplicate PAN | pan={}", MaskingUtil.maskPan(dto.getPanNumber()));
            throw new CustomerAlreadyExistsException("Pan: "+dto.getPanNumber());
        }
        
        if(exixstByUId(dto.getAadhaarNumber())) {
        	log.warn("createCustomer failed | duplicate UID | UID={}", MaskingUtil.maskAadhaar(dto.getAadhaarNumber()));
            throw new CustomerAlreadyExistsException("Aadhaar no : "+dto.getAadhaarNumber());
        }
        
        if (existsByEmail(dto.getEmail())) {
            log.warn("createCustomer failed | duplicate email | email={}", dto.getEmail());
            throw new CustomerAlreadyExistsException("Email already registered: " + dto.getEmail());
        }
        Long mobile = Long.parseLong(dto.getMobileNumber());
        if (customerRepository.existsByMobile(mobile)) {
            log.warn("createCustomer failed | duplicate mobile | mobile={}", dto.getMobileNumber());
            throw new CustomerAlreadyExistsException("Mobile already registered: " + dto.getMobileNumber());
        }

        try {
            Customer customer = mapRequestToEntity(dto);
            customer.setCustomerUuid(UUID.randomUUID().toString());
            customer.setStatus(CustomerStatus.PENDING_KYC);
            customer.setCreatedDate(LocalDateTime.now());
            customer.setUpdatedDate(LocalDateTime.now());
            
            // Custom SQL Save with logging (REPLACE JpaRepository.save)
            log.debug("Saving customer with SQL | uuid={} | firstName={} | lastName={} | pan={} | email={} | mobile={} | status={}", 
                    customer.getCustomerUuid(), customer.getFirstName(), customer.getLastName(),
                    MaskingUtil.maskPan(customer.getPanNumber()), customer.getEmail(), customer.getMobile(), customer.getStatus());
            
            long startTime = System.currentTimeMillis();
            customerRepository.saveCustomerNative(
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getGender(),
                    customer.getDateOfBirth(),
                    customer.getEmail(),
                    customer.getMobile(),
                    customer.getPanNumber(),
                    customer.getAadhaarNumber(),
                    customer.getCustomerUuid(),
                    customer.getStatus().name(),
                    customer.getCreatedDate(),
                    customer.getUpdatedDate()
            );
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("createCustomer success | customerId={} | uuid={} | executionTime={}ms", customer.getCustomerId(), customer.getCustomerUuid(), executionTime);

            // Also publish event so downstream services can react if this path is used
            saveOutboxEvent(customer, "CUSTOMER_CREATED");
            
            return mapEntityToResponse(customer);
        } catch (Exception e) {
            log.error("createCustomer failed | pan={} | error={}", MaskingUtil.maskPan(dto.getPanNumber()), e.getMessage());
            throw new DatabaseException("Failed to create customer", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto dto) {
        log.info("updateCustomer started | id={} | pan={}", id, MaskingUtil.maskPan(dto.getPanNumber()));
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        validateAgeForHomeLoan(dto.getDateOfBirth());

        if (!customer.getPanNumber().equals(dto.getPanNumber()) && existsByPan(dto.getPanNumber())) {
            log.warn("updateCustomer failed | duplicate PAN | id={} | pan={}", id, MaskingUtil.maskPan(dto.getPanNumber()));
            throw new CustomerAlreadyExistsException(dto.getPanNumber());
        }
        if (!customer.getEmail().equals(dto.getEmail()) && existsByEmail(dto.getEmail())) {
            log.warn("updateCustomer failed | duplicate email | id={} | email={}", id, dto.getEmail());
            throw new CustomerAlreadyExistsException("Email already registered: " + dto.getEmail());
        }
        Long mobile = Long.parseLong(dto.getMobileNumber());
        if (!customer.getMobile().equals(mobile) && customerRepository.existsByMobile(mobile)) {
            log.warn("updateCustomer failed | duplicate mobile | id={} | mobile={}", id, dto.getMobileNumber());
            throw new CustomerAlreadyExistsException("Mobile already registered: " + dto.getMobileNumber());
        }

        try {
            customer.setFirstName(dto.getFirstName());
            customer.setLastName(dto.getLastName());
            customer.setGender(dto.getGender());
            customer.setDateOfBirth(dto.getDateOfBirth());
            customer.setEmail(dto.getEmail());
            customer.setMobile(mobile);
            customer.setPanNumber(dto.getPanNumber());
            customer.setAadhaarNumber(dto.getAadhaarNumber());
            customer.setUpdatedDate(LocalDateTime.now());
            
            // Custom SQL Update with logging (REPLACE JpaRepository.save)
            log.debug("Updating customer with SQL | customerId={} | email={} | firstName={} | lastName={} | pan={} | mobile={}", 
                    id, customer.getEmail(), customer.getFirstName(), customer.getLastName(),
                    MaskingUtil.maskPan(customer.getPanNumber()), customer.getMobile());
            
            customerRepositoryImpl.updateCustomerNative(
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getGender(),
                    customer.getDateOfBirth(),
                    customer.getEmail(),
                    customer.getMobile(),
                    customer.getPanNumber(),
                    customer.getAadhaarNumber(),
                    customer.getStatus().name(),
                    customer.getUpdatedDate()
            );
            return mapEntityToResponse(customer);
        } catch (Exception e) {
            log.error("updateCustomer failed | id={} | error={} | errorType={}", id, e.getMessage(), e.getClass().getSimpleName(), e);
            throw new DatabaseException("Failed to update customer", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_CUSTOMERS, key = "'id:' + #id")
    public CustomerResponseDto getCustomerById(Long id) {
    	log.info("ENTER :: getCustomerById | id={}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        log.info("EXIT :: getCustomerById | id={} found", id);
        return mapEntityToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_CUSTOMERS, key = "'uuid:' + #uuid")
    public CustomerResponseDto getCustomerByUuid(String uuid) {
        log.debug("getCustomerByUuid | uuid={}", MaskingUtil.maskPan(uuid));
        Customer customer = customerRepository.findByCustomerUuid(uuid)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with UUID: " + MaskingUtil.maskPan(uuid)));
        return mapEntityToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_CUSTOMERS, key = "'pan:' + #panNumber")
    public CustomerResponseDto getCustomerByPan(String panNumber) {
        log.debug("getCustomerByPan | pan={}", MaskingUtil.maskPan(panNumber));
        Customer customer = customerRepository.findByPanNumber(panNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with PAN: " + MaskingUtil.maskPan(panNumber)));
        return mapEntityToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_CUSTOMERS, key = "'email:' + #email")
    public CustomerResponseDto getCustomerByEmail(String email) {
        log.debug("getCustomerByEmail | email={}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return mapEntityToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CustomerResponseDto> getAllCustomers(int page, int size) {
        log.debug("getAllCustomers | page={} | size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return buildPageResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CustomerResponseDto> getCustomersByStatus(CustomerStatus status, int page, int size) {
        log.debug("getCustomersByStatus | status={} | page={} | size={}", status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Customer> customerPage = customerRepository.findByStatus(status, pageable);
        return buildPageResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CustomerResponseDto> searchCustomers(String searchTerm, int page, int size) {
        log.debug("searchCustomers | q={} | page={} | size={}", searchTerm, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName", "lastName"));
        Page<Customer> customerPage = customerRepository.searchByNameOrPanOrEmail(searchTerm.trim(), pageable);
        return buildPageResponse(customerPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
    public CustomerResponseDto updateCustomerStatus(Long id, CustomerStatus status) {
    	 log.info("ENTER :: updateCustomerStatus | id={} | status={}", id, status);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.setStatus(status);
        customer.setUpdatedDate(LocalDateTime.now());
        
        // Custom SQL Update with logging (REPLACE JpaRepository.save)
        log.debug("Updating customer status with SQL | customerId={} | newStatus={}", id, status);
        
        customerRepositoryImpl.updateCustomerNative(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getGender(),
                customer.getDateOfBirth(),
                customer.getEmail(),
                customer.getMobile(),
                customer.getPanNumber(),
                customer.getAadhaarNumber(),
                customer.getStatus().name(),
                customer.getUpdatedDate()
        );
        return mapEntityToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPan(String panNumber) {
    	log.debug("ENTER :: existsByPan | pan={}", panNumber);
    	boolean exists = customerRepository.existsByPanNumber(panNumber);
        log.debug("EXIT :: existsByPan | pan={} | exists={}", panNumber, exists);
        return exists;
    }
    @Override
	@Transactional(readOnly = true)
	public boolean exixstByUId(String uid) {
    	log.debug("ENTER :: exixstByUId | uid={}", uid);
        boolean exists = customerRepository.exixstByUId(uid);
        log.debug("EXIT :: exixstByUId | uid={} | exists={}", uid, exists);
        return exists;
	}
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
    	log.debug("ENTER :: existsByEmail | email={}", email);
        boolean exists = customerRepository.existsByEmail(email);
        log.debug("EXIT :: existsByEmail | email={} | exists={}", email, exists);
        return exists;
    }

    private void validateAgeForHomeLoan(LocalDate dateOfBirth) {
    	 log.debug("Validating age for DOB={}", dateOfBirth);
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < MIN_AGE_HOME_LOAN || age > MAX_AGE_HOME_LOAN) {
        	log.warn("Age validation failed | age={}", age);
            throw new BusinessException("AGE_NOT_ELIGIBLE",
                    "Age must be between " + MIN_AGE_HOME_LOAN + " and " + MAX_AGE_HOME_LOAN + " for home loan eligibility");
        }
    }

    private Customer mapRequestToEntity(CustomerRequestDto dto) {
        Customer customer = modelMapper.map(dto, Customer.class);
        customer.setMobile(Long.parseLong(dto.getMobileNumber()));

        // Map address and employment details explicitly
        applyAddressAndEmployment(dto, customer);

        return customer;
    }

    /**
     * Helper to copy address and employment details from DTO into Customer entity
     * and maintain proper bi-directional relationships.
     */
    private void applyAddressAndEmployment(CustomerRequestDto dto, Customer customer) {
        // Address (convert single DTO to list of addresses)
        AddressDto addressDto = dto.getAddress();
        List<Address> addresses = new ArrayList<>();
        if (addressDto != null) {
            Address address = new Address();
            address.setType(AddressType.CURRENT);
            address.setHouseNo(addressDto.getHouseNo());
            address.setCity(addressDto.getCity());
            address.setState(addressDto.getState());
            address.setPincode(addressDto.getPincode());
            address.setCustomer(customer);
            addresses.add(address);
        }
        customer.setAddresses(addresses);

        // Employment details
        EmploymentDetailsDto empDto = dto.getEmploymentDetails();
        if (empDto != null) {
            EmploymentDetails employmentDetails = customer.getEmploymentDetails();
            if (employmentDetails == null) {
                employmentDetails = new EmploymentDetails();
            }
            employmentDetails.setEmploymentType(empDto.getEmploymentType());
            employmentDetails.setCompanyName(empDto.getCompanyName());
            employmentDetails.setMonthlyIncome(empDto.getMonthlyIncome());
            employmentDetails.setTotalExperience(empDto.getTotalExperience());
            employmentDetails.setCustomer(customer);
            customer.setEmploymentDetails(employmentDetails);
        } else {
            customer.setEmploymentDetails(null);
        }
    }

    private CustomerResponseDto mapEntityToResponse(Customer customer) {
    	 log.debug("Mapping entity to response | customerId={}", customer.getCustomerId());
        CustomerResponseDto dto = modelMapper.map(customer, CustomerResponseDto.class);
        dto.setMobileNumber(customer.getMobile() != null ? customer.getMobile().toString() : null);
        dto.setActive(customer.getStatus() == CustomerStatus.ACTIVE);
        dto.setPanNumber(MaskingUtil.maskPan(customer.getPanNumber()));
        dto.setAadhaarNumber(MaskingUtil.maskAadhaar(customer.getAadhaarNumber()));
        return dto;
    }

    private PageResponseDto<CustomerResponseDto> buildPageResponse(Page<Customer> page) {
        return new PageResponseDto<>(
                page.getContent().stream().map(this::mapEntityToResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

	
}
