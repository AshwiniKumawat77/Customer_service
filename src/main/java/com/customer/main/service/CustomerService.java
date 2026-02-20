package com.customer.main.service;

import com.customer.main.dto.CustomerEnquiryRequestDto;
import com.customer.main.dto.CustomerRequestDto;
import com.customer.main.dto.CustomerResponseDto;
import com.customer.main.dto.PageResponseDto;
import com.customer.main.entity.CustomerStatus;

/**
 * Service contract for Home Loan Customer Service.
 */
public interface CustomerService {

    /**
     * Step 1: Customer enquiry / basic registration (pre-KYC, pre-loan).
     * Saves basic identity data; CIBIL service will listen to events and perform bureau checks.
     */
    CustomerResponseDto createCustomerEnquiry(CustomerEnquiryRequestDto dto);

    /**
     * Step 2: Complete KYC and enrich customer with address and employment details.
     * Typically called by bank-side application after positive CIBIL / in-branch KYC.
     */
    CustomerResponseDto completeKyc(Long id, CustomerRequestDto dto);

    @Deprecated(since = "1.0", forRemoval = false)
    CustomerResponseDto createCustomer(CustomerRequestDto dto);

    CustomerResponseDto updateCustomer(Long id, CustomerRequestDto dto);

    CustomerResponseDto getCustomerById(Long id);

    CustomerResponseDto getCustomerByUuid(String uuid);

    CustomerResponseDto getCustomerByPan(String panNumber);

    CustomerResponseDto getCustomerByEmail(String email);

    PageResponseDto<CustomerResponseDto> getAllCustomers(int page, int size);

    PageResponseDto<CustomerResponseDto> getCustomersByStatus(CustomerStatus status, int page, int size);

    PageResponseDto<CustomerResponseDto> searchCustomers(String searchTerm, int page, int size);

    CustomerResponseDto updateCustomerStatus(Long id, CustomerStatus status);

    boolean existsByPan(String panNumber);

    boolean existsByEmail(String email);
    boolean exixstByUId(String uid);
}
