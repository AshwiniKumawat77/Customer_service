# Quick Reference: Custom SQL Save Implementation

## Files Modified

### 1. **CustomerConstants.java**
Added UPDATE_CUSTOMER SQL constant for update operations

### 2. **CustomerRepository.java**
Added two new methods:
- `saveCustomerNative()` - Native SQL INSERT with all parameters
- `updateCustomerNative()` - Native SQL UPDATE with all parameters

### 3. **CustomerServiceImpl.java**
Added two private helper methods:
- `saveCustomerWithLogging()` - Wraps INSERT with comprehensive logging
- `updateCustomerWithLogging()` - Wraps UPDATE with comprehensive logging

Updated three service methods to use custom SQL:
- `createCustomerEnquiry()` 
- `completeKyc()`
- `createCustomer()`
- `updateCustomer()`
- `updateCustomerStatus()`

---

## Method Signatures

### Save Method (in Repository)
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
    @Param("updatedDate") LocalDateTime updatedDate
);
```

### Update Method (in Repository)
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
    @Param("updatedDate") LocalDateTime updatedDate
);
```

---

## Logging Levels

| Operation | Entry Log | Debug Log | Exit Log | Error Log |
|-----------|-----------|-----------|----------|-----------|
| **SAVE** | UUID, Name | All params + masked PAN | ExecutionTime, Success | Exception details + stack |
| **UPDATE** | ID, UUID, Email | Name, Status, Mobile | RowsAffected, ExecutionTime | Exception details + stack |

---

## Parameter Details for Save

| Parameter | Type | Example | Notes |
|-----------|------|---------|-------|
| firstName | String | "John" | Max 100 chars |
| lastName | String | "Doe" | Max 100 chars |
| gender | String | "M" / "F" / "OTHER" | Single character |
| dateOfBirth | LocalDate | "1990-05-15" | Must be in past |
| email | String | "john@example.com" | Must be unique |
| mobile | Long | 9876543210 | Must be unique |
| panNumber | String | "ABCDE1234F" | Must be unique |
| aadhaarNumber | String | "123456789012" | Must be unique |
| customerUuid | String | "550e8400-e29b..." | Auto-generated UUID |
| status | String | "PENDING_KYC" | Enum name as string |
| createdDate | LocalDateTime | "2026-02-15T10:30:00" | Current timestamp |
| updatedDate | LocalDateTime | "2026-02-15T10:30:00" | Current timestamp |

---

## Parameter Details for Update

| Parameter | Type | Example | Notes |
|-----------|------|---------|-------|
| customerId | Long | 101 | Primary key, required |
| firstName | String | "John" | Updated value |
| lastName | String | "Doe" | Updated value |
| gender | String | "M" | Updated value |
| dateOfBirth | LocalDate | "1990-05-15" | Updated value |
| email | String | "john.new@example.com" | Updated value, check uniqueness |
| mobile | Long | 9876543210 | Updated value, check uniqueness |
| panNumber | String | "ABCDE1234F" | Updated value, check uniqueness |
| aadhaarNumber | String | "123456789012" | Updated value, check uniqueness |
| status | String | "ACTIVE" | Enum name as string |
| updatedDate | LocalDateTime | "2026-02-15T11:00:00" | Current timestamp |

---

## SQL Queries

### INSERT Query
```sql
INSERT INTO customer 
(first_name, last_name, gender, date_of_birth, email, mobile, 
 pan_number, aadhaar_number, customer_uuid, status, created_date, updated_date) 
VALUES 
(:firstName, :lastName, :gender, :dateOfBirth, :email, :mobile, 
 :panNumber, :aadhaarNumber, :customerUuid, :status, :createdDate, :updatedDate)
```

### UPDATE Query
```sql
UPDATE customer SET 
    first_name = :firstName, 
    last_name = :lastName, 
    gender = :gender, 
    date_of_birth = :dateOfBirth, 
    email = :email, 
    mobile = :mobile, 
    pan_number = :panNumber, 
    aadhaar_number = :aadhaarNumber, 
    status = :status, 
    updated_date = :updatedDate 
WHERE customer_id = :customerId
```

---

## Log Output Examples

### Save Operation - Success
```
[INFO ] ENTER :: saveCustomerWithLogging | uuid=550e8400-e29b-41d4-a716-446655440000 | firstName=John | lastName=Doe
[DEBUG] saveCustomerWithLogging | Details: pan=XXXXX9999X | email=john@example.com | mobile=9876543210 | status=PENDING_KYC | dob=1990-05-15
[INFO ] EXIT :: saveCustomerWithLogging | uuid=550e8400-e29b-41d4-a716-446655440000 | executionTime=111ms | status=SUCCESS
```

### Update Operation - Success
```
[INFO ] ENTER :: updateCustomerWithLogging | customerId=101 | uuid=550e8400-e29b-41d4-a716-446655440000 | email=john@example.com
[DEBUG] updateCustomerWithLogging | Details: firstName=John | lastName=Doe | status=ACTIVE | mobile=9876543210
[INFO ] EXIT :: updateCustomerWithLogging | customerId=101 | rowsAffected=1 | executionTime=56ms | status=SUCCESS
```

### Update Operation - No Rows Updated
```
[WARN ] EXIT :: updateCustomerWithLogging | customerId=999 | rowsAffected=0 | executionTime=12ms | status=NO_ROWS_UPDATED
```

### Error Case
```
[ERROR] ERROR :: saveCustomerWithLogging failed | uuid=550e8400-e29b-41d4-a716-446655440001 | pan=XXXXX0001X | errorMessage=Duplicate entry for key 'email' | errorType=DataIntegrityViolationException
```

---

## Usage Example in Service

```java
@Override
@Transactional(rollbackFor = Exception.class)
@CacheEvict(cacheNames = CacheConfig.CACHE_CUSTOMERS, allEntries = true)
public CustomerResponseDto createCustomerEnquiry(CustomerEnquiryRequestDto dto) {
    // ... validation code ...
    
    Customer customer = new Customer();
    customer.setFirstName(dto.getFirstName());
    customer.setLastName(dto.getLastName());
    customer.setGender(dto.getGender());
    customer.setDateOfBirth(dto.getDateOfBirth());
    customer.setEmail(dto.getEmail());
    customer.setMobile(Long.parseLong(dto.getMobileNumber()));
    customer.setPanNumber(dto.getPanNumber());
    customer.setAadhaarNumber(dto.getAadhaarNumber());
    customer.setCustomerUuid(UUID.randomUUID().toString());
    customer.setStatus(CustomerStatus.PENDING_KYC);
    customer.setCreatedDate(LocalDateTime.now());
    customer.setUpdatedDate(LocalDateTime.now());

    // Use custom SQL save with logging
    saveCustomerWithLogging(customer);
    log.info("EXIT :: createCustomerEnquiry | customerId={} | uuid={} | status={}", 
            customer.getCustomerId(), customer.getCustomerUuid(), customer.getStatus());

    eventPublisher.publishCustomerRegistered(customer);
    return mapEntityToResponse(customer);
}
```

---

## Key Features

✅ **No JpaRepository.save()** - Full native SQL control  
✅ **Explicit Parameters** - All 12 fields for save, 11 for update  
✅ **Comprehensive Logging** - Entry, Exit, Debug, Error levels  
✅ **Performance Tracking** - Execution time in milliseconds  
✅ **Data Security** - Sensitive data masking (PAN, Aadhaar)  
✅ **Error Handling** - Full stack trace logging  
✅ **Row Tracking** - Number of affected rows for updates  
✅ **Audit Trail** - Complete tracking for compliance  

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "No rows updated" warning | Verify customer ID exists before calling update |
| Duplicate key exception | Check email, PAN, mobile, aadhaar uniqueness before insert |
| Slow execution time | Check database indexes on email, pan_number, mobile |
| Missing logs | Ensure SLF4J logger is configured with appropriate log level |
| NULL pointer exception | Verify all entity fields are populated before calling save/update |

---

## Testing the Implementation

### Test Save Operation
```java
CustomerEnquiryRequestDto dto = new CustomerEnquiryRequestDto();
dto.setFirstName("Test");
dto.setLastName("User");
dto.setPanNumber("ABCDE1234F");
dto.setEmail("test@example.com");
// ... set other fields ...

CustomerResponseDto response = customerService.createCustomerEnquiry(dto);
// Check logs for ENTER, DEBUG, and EXIT messages with timing
```

### Test Update Operation
```java
CustomerRequestDto updateDto = new CustomerRequestDto();
updateDto.setFirstName("Updated");
// ... set other fields ...

CustomerResponseDto response = customerService.updateCustomer(101L, updateDto);
// Check logs for ENTER, DEBUG, EXIT, and rowsAffected
```

---

## Documentation Files

- **CUSTOM_SQL_SAVE_IMPLEMENTATION.md** - Detailed technical documentation
- **QUICK_REFERENCE.md** - This file with quick lookup information
