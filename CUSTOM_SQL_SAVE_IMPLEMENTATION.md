# Custom SQL Save & Update Implementation with Comprehensive Logging

## Overview
This document explains the custom SQL implementation for Customer entity save and update operations, replacing the default JpaRepository.save() method with explicit native SQL queries and comprehensive logging for better tracking and debugging.

---

## Why Custom SQL Instead of JPA Save?

### Benefits:
1. **Full Control**: Explicit control over SQL execution
2. **Performance**: Direct SQL without ORM overhead for bulk operations
3. **Debugging**: Easier to trace exact SQL and parameters being executed
4. **Audit Trail**: Comprehensive logging for compliance and troubleshooting
5. **Security**: Better parameter binding and query control
6. **Database-Specific**: Ability to leverage database-specific features

---

## Implementation Architecture

### 1. Database Constants (CustomerConstants.java)

```java
// INSERT Customer with all parameters
public static final String INSERT_CUSTOMER =
    "INSERT INTO customer " +
    "(first_name, last_name, gender, date_of_birth, email, mobile, " +
    "pan_number, aadhaar_number, customer_uuid, status, created_date, updated_date) " +
    "VALUES " +
    "(:firstName, :lastName, :gender, :dateOfBirth, :email, :mobile, " +
    ":panNumber, :aadhaarNumber, :customerUuid, :status, :createdDate, :updatedDate)";

// UPDATE Customer with all parameters
public static final String UPDATE_CUSTOMER =
    "UPDATE customer SET " +
    "first_name = :firstName, " +
    "last_name = :lastName, " +
    "gender = :gender, " +
    "date_of_birth = :dateOfBirth, " +
    "email = :email, " +
    "mobile = :mobile, " +
    "pan_number = :panNumber, " +
    "aadhaar_number = :aadhaarNumber, " +
    "status = :status, " +
    "updated_date = :updatedDate " +
    "WHERE customer_id = :customerId";
```

### 2. Repository Methods (CustomerRepository.java)

#### Save Method
```java
@Modifying
@Query(value = CustomerConstants.INSERT_CUSTOMER, nativeQuery = true)
void saveCustomerNative(
    @Param("firstName") String firstName,
    @Param("lastName") String lastName,
    @Param("gender") String gender,
    @Param("dateOfBirth") LocalDate dateOfBirth,
    @Param("email") String email,
    @Param("mobile") Long mobile,
    @Param("panNumber") String panNumber,
    @Param("aadhaarNumber") String aadhaarNumber,
    @Param("customerUuid") String customerUuid,
    @Param("status") String status,
    @Param("createdDate") LocalDateTime createdDate,
    @Param("updatedDate") LocalDateTime updatedDate);
```

#### Update Method
```java
@Modifying
@Query(value = CustomerConstants.UPDATE_CUSTOMER, nativeQuery = true)
int updateCustomerNative(
    @Param("customerId") Long customerId,
    @Param("firstName") String firstName,
    @Param("lastName") String lastName,
    @Param("gender") String gender,
    @Param("dateOfBirth") LocalDate dateOfBirth,
    @Param("email") String email,
    @Param("mobile") Long mobile,
    @Param("panNumber") String panNumber,
    @Param("aadhaarNumber") String aadhaarNumber,
    @Param("status") String status,
    @Param("updatedDate") LocalDateTime updatedDate);
```

**Key Points:**
- `@Modifying` - Indicates this query modifies the database (INSERT/UPDATE)
- `nativeQuery = true` - Executes raw SQL instead of JPQL
- All parameters are explicitly named for clarity
- Return type: `void` for INSERT, `int` for UPDATE (rows affected)

---

## Service Implementation (CustomerServiceImpl.java)

### saveCustomerWithLogging() Method

This method wraps the native SQL INSERT with comprehensive logging:

```java
private void saveCustomerWithLogging(Customer customer) {
    // ENTRY LOG
    log.info("ENTER :: saveCustomerWithLogging | uuid={} | firstName={} | lastName={}", 
            customer.getCustomerUuid(), customer.getFirstName(), customer.getLastName());
    
    // DETAIL DEBUG LOG
    log.debug("saveCustomerWithLogging | Details: pan={} | email={} | mobile={} | status={} | dob={}", 
            MaskingUtil.maskPan(customer.getPanNumber()), customer.getEmail(), customer.getMobile(), 
            customer.getStatus(), customer.getDateOfBirth());

    try {
        // TIMING START
        long startTime = System.currentTimeMillis();
        
        // EXECUTE NATIVE SQL
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
        
        // SUCCESS LOG WITH TIMING
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("EXIT :: saveCustomerWithLogging | uuid={} | executionTime={}ms | status=SUCCESS", 
                customer.getCustomerUuid(), executionTime);
        
    } catch (Exception e) {
        // ERROR LOG WITH FULL CONTEXT
        log.error("ERROR :: saveCustomerWithLogging failed | uuid={} | pan={} | errorMessage={} | errorType={}", 
                customer.getCustomerUuid(), MaskingUtil.maskPan(customer.getPanNumber()), 
                e.getMessage(), e.getClass().getSimpleName(), e);
        throw e;
    }
}
```

### updateCustomerWithLogging() Method

```java
private void updateCustomerWithLogging(Customer customer) {
    // ENTRY LOG
    log.info("ENTER :: updateCustomerWithLogging | customerId={} | uuid={} | email={}", 
            customer.getCustomerId(), customer.getCustomerUuid(), customer.getEmail());
    
    // DETAIL DEBUG LOG
    log.debug("updateCustomerWithLogging | Details: firstName={} | lastName={} | status={} | mobile={}", 
            customer.getFirstName(), customer.getLastName(), customer.getStatus(), customer.getMobile());

    try {
        // TIMING START
        long startTime = System.currentTimeMillis();
        
        // EXECUTE NATIVE SQL
        int rowsAffected = customerRepository.updateCustomerNative(
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
        
        // SUCCESS/WARNING LOG WITH TIMING AND ROWS AFFECTED
        long executionTime = System.currentTimeMillis() - startTime;
        
        if (rowsAffected > 0) {
            log.info("EXIT :: updateCustomerWithLogging | customerId={} | rowsAffected={} | executionTime={}ms | status=SUCCESS", 
                    customer.getCustomerId(), rowsAffected, executionTime);
        } else {
            log.warn("EXIT :: updateCustomerWithLogging | customerId={} | rowsAffected=0 | executionTime={}ms | status=NO_ROWS_UPDATED", 
                    customer.getCustomerId(), executionTime);
        }
        
    } catch (Exception e) {
        // ERROR LOG WITH FULL CONTEXT
        log.error("ERROR :: updateCustomerWithLogging failed | customerId={} | uuid={} | errorMessage={} | errorType={}", 
                customer.getCustomerId(), customer.getCustomerUuid(), 
                e.getMessage(), e.getClass().getSimpleName(), e);
        throw e;
    }
}
```

---

## Logging Strategy

### Log Levels Used:

| Level | Usage | Example |
|-------|-------|---------|
| **INFO** | Major flow entry/exit and success | ENTER/EXIT, Status changes |
| **DEBUG** | Detailed parameters and values | Individual field values |
| **WARN** | Unexpected but non-critical issues | No rows updated |
| **ERROR** | Failures that need investigation | Exceptions with stack trace |

### Logged Information:

#### Entry Point (INFO)
- Method name
- Customer UUID
- Key identifiers (firstName, lastName)

#### Debug Details
- All parameter values
- Masked sensitive data (PAN, Aadhaar)
- Status information
- Date of birth

#### Exit Point (INFO/WARN)
- UUID or ID
- Execution time in milliseconds
- Number of rows affected (for updates)
- Success/Warning status

#### Error Cases (ERROR)
- Customer UUID/ID
- Masked PAN
- Full error message
- Exception type
- Full stack trace

---

## Usage in Service Methods

### Example 1: createCustomerEnquiry()
```java
// ... validation and setup ...
Customer customer = new Customer();
// ... populate fields ...
customer.setCustomerUuid(UUID.randomUUID().toString());
customer.setStatus(CustomerStatus.PENDING_KYC);
customer.setCreatedDate(LocalDateTime.now());
customer.setUpdatedDate(LocalDateTime.now());

// Use custom SQL save with logging
saveCustomerWithLogging(customer);
log.info("EXIT :: createCustomerEnquiry | customerId={} | uuid={} | status={}", 
        customer.getCustomerId(), customer.getCustomerUuid(), customer.getStatus());
```

### Example 2: completeKyc()
```java
// ... validation and enrichment ...
customer.setStatus(CustomerStatus.ACTIVE);
customer.setUpdatedDate(LocalDateTime.now());

// Use custom SQL update with logging
updateCustomerWithLogging(customer);
log.info("EXIT :: completeKyc | id={} | status={} | email={}", 
        id, customer.getStatus(), customer.getEmail());
```

### Example 3: updateCustomer()
```java
// ... validation ...
customer.setFirstName(dto.getFirstName());
customer.setLastName(dto.getLastName());
// ... other field updates ...
customer.setUpdatedDate(LocalDateTime.now());

// Use custom SQL update with logging
updateCustomerWithLogging(customer);
log.info("updateCustomer success | id={} | email={}", id, customer.getEmail());
```

---

## Log Output Examples

### Successful Save
```
2026-02-15 10:30:45.123 [INFO ] - ENTER :: saveCustomerWithLogging | uuid=550e8400-e29b-41d4-a716-446655440000 | firstName=John | lastName=Doe
2026-02-15 10:30:45.125 [DEBUG] - saveCustomerWithLogging | Details: pan=XXXXX9999X | email=john@example.com | mobile=9876543210 | status=PENDING_KYC | dob=1990-05-15
2026-02-15 10:30:45.234 [INFO ] - EXIT :: saveCustomerWithLogging | uuid=550e8400-e29b-41d4-a716-446655440000 | executionTime=111ms | status=SUCCESS
```

### Successful Update
```
2026-02-15 10:35:22.456 [INFO ] - ENTER :: updateCustomerWithLogging | customerId=101 | uuid=550e8400-e29b-41d4-a716-446655440000 | email=john.doe@example.com
2026-02-15 10:35:22.458 [DEBUG] - updateCustomerWithLogging | Details: firstName=John | lastName=Doe | status=ACTIVE | mobile=9876543210
2026-02-15 10:35:22.512 [INFO ] - EXIT :: updateCustomerWithLogging | customerId=101 | rowsAffected=1 | executionTime=56ms | status=SUCCESS
```

### Error Case
```
2026-02-15 10:40:10.789 [INFO ] - ENTER :: saveCustomerWithLogging | uuid=550e8400-e29b-41d4-a716-446655440001 | firstName=Jane | lastName=Smith
2026-02-15 10:40:10.790 [DEBUG] - saveCustomerWithLogging | Details: pan=XXXXX0001X | email=jane@example.com | mobile=9876543211 | status=PENDING_KYC | dob=1992-03-20
2026-02-15 10:40:10.850 [ERROR] - ERROR :: saveCustomerWithLogging failed | uuid=550e8400-e29b-41d4-a716-446655440001 | pan=XXXXX0001X | errorMessage=Duplicate entry for key 'email' | errorType=DataIntegrityViolationException
java.org.springframework.dao.DataIntegrityViolationException: ... stack trace ...
```

---

## Database Schema Requirements

Ensure your `customer` table has these columns:

```sql
CREATE TABLE customer (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_uuid VARCHAR(36) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mobile BIGINT UNIQUE NOT NULL,
    pan_number VARCHAR(10) UNIQUE NOT NULL,
    aadhaar_number VARCHAR(12) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_KYC',
    created_date DATETIME NOT NULL,
    updated_date DATETIME NOT NULL,
    
    INDEX idx_status (status),
    INDEX idx_email (email),
    INDEX idx_pan (pan_number)
);
```

---

## Transaction Management

All save/update operations are wrapped in `@Transactional` at the service method level:

```java
@Override
@Transactional(rollbackFor = Exception.class)
@CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
public CustomerResponseDto createCustomerEnquiry(CustomerEnquiryRequestDto dto) {
    // ... method implementation ...
}
```

**Benefits:**
- Automatic rollback on exceptions
- Atomicity of operations
- Cache invalidation on successful write
- Proper connection management

---

## Performance Considerations

1. **Execution Timing**: All methods log execution time in milliseconds
2. **Batch Operations**: For bulk inserts/updates, consider creating batch methods
3. **Index Usage**: Ensure indexes exist on frequently queried columns (email, pan_number, status)
4. **Connection Pooling**: Verify proper connection pool configuration in application.properties

---

## Security & Data Masking

Sensitive information is automatically masked in logs:

```java
// PAN Masking Example
log.debug("Details: pan={}", MaskingUtil.maskPan(customer.getPanNumber()));
// Output: pan=XXXXX9999X (last 4 digits visible)

// Aadhaar Masking Example
log.debug("Details: aadhaar={}", MaskingUtil.maskAadhaar(customer.getAadhaarNumber()));
// Output: aadhaar=XXXX XXXX 0123 (last 4 digits visible)
```

---

## Troubleshooting

### Issue: "No rows updated" warning
- Verify customer ID exists before update
- Check if data actually changed
- Review cascading updates if related entities exist

### Issue: Execution time is slow
- Check database indexes
- Review query execution plan
- Consider connection pool settings
- Monitor database server load

### Issue: DataIntegrityViolationException
- Verify uniqueness constraints (email, PAN, mobile)
- Check for concurrent updates
- Review foreign key relationships

---

## Summary

This custom SQL implementation provides:
- ✅ Full control over database operations
- ✅ Comprehensive audit trail through logging
- ✅ Explicit parameter binding for security
- ✅ Performance metrics (execution time)
- ✅ Better debugging capabilities
- ✅ Database-specific optimization potential
- ✅ Sensitive data masking in logs
- ✅ Clear entry/exit flow tracking
