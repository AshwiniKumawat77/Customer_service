# Architecture & Flow Diagrams

## 1. Overall Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     CustomerServiceImpl                          │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Service Methods                                         │   │
│  │  - createCustomerEnquiry()                               │   │
│  │  - completeKyc()                                         │   │
│  │  - createCustomer()                                      │   │
│  │  - updateCustomer()                                      │   │
│  │  - updateCustomerStatus()                                │   │
│  └─────────────────────┬──────────────────────────────────┘   │
│                        │                                        │
│                        ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Helper Methods (NEW)                                    │   │
│  │  - saveCustomerWithLogging()                             │   │
│  │  - updateCustomerWithLogging()                           │   │
│  │                                                          │   │
│  │  Features:                                               │   │
│  │  ✓ Entry logging (INFO)                                 │   │
│  │  ✓ Debug logging (DEBUG)                                │   │
│  │  ✓ Execution timing                                     │   │
│  │  ✓ Parameter validation                                 │   │
│  │  ✓ Error handling                                       │   │
│  └─────────────────────┬──────────────────────────────────┘   │
│                        │                                        │
│                        ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  CustomerRepository                                      │   │
│  │  - saveCustomerNative()        (NEW)                    │   │
│  │  - updateCustomerNative()      (NEW)                    │   │
│  │  + All existing query methods                            │   │
│  └─────────────────────┬──────────────────────────────────┘   │
│                        │                                        │
└────────────────────────┼────────────────────────────────────────┘
                         │
                         ▼
              ┌────────────────────────────┐
              │   Native SQL Queries       │
              │   via @Query               │
              │   (Parameter Binding)      │
              └────────────┬───────────────┘
                           │
                           ▼
              ┌────────────────────────────┐
              │  Database                  │
              │  (MySQL/MariaDB)           │
              │                            │
              │  customer table            │
              └────────────────────────────┘
```

---

## 2. Save Operation Flow

```
START
  │
  ├─► Service validates business rules
  │   - Uniqueness checks (PAN, Email, Mobile)
  │   - Age validation
  │   └─► Exception if validation fails ──────┐
  │                                             │
  ├─► Create Customer entity                  │
  │   - Populate all 12 fields                 │
  │   - Set UUID, Status, Timestamps           │
  │                                             │
  ├─► Call saveCustomerWithLogging(customer)  │
  │   │                                        │
  │   ├─► Log: ENTRY                          │
  │   │   └─ UUID, FirstName, LastName        │
  │   │                                        │
  │   ├─► Log: DEBUG                          │
  │   │   └─ All parameters + masked PAN      │
  │   │                                        │
  │   ├─► Start timer                         │
  │   │                                        │
  │   ├─► Call repo.saveCustomerNative(...)   │
  │   │   │                                    │
  │   │   ├─► Spring Data processes params    │
  │   │   │                                    │
  │   │   ├─► Execute: INSERT SQL             │
  │   │   │   (Parameter binding)             │
  │   │   │                                    │
  │   │   ├─► Database validates:             │
  │   │   │   - UNIQUE constraints            │
  │   │   │   - NOT NULL values               │
  │   │   │   - Foreign keys                  │
  │   │   │                                    │
  │   │   └─► Record inserted / Exception     │
  │   │                                        │
  │   ├─► Stop timer                          │
  │   │                                        │
  │   ├─► Log: EXIT + SUCCESS                 │
  │   │   └─ UUID, executionTime=XXms         │
  │   │                                        │
  │   └─► If Exception:                       │
  │       Log: ERROR + Details + Stack        │
  │       Rethrow Exception                   │
  │                                             │
  ├─► Transaction: COMMIT if success          │
  │                    ROLLBACK if error ◄────┤
  │                                             │
  ├─► Publish event (CIBIL checks)            │
  │                                             │
  └─► Return CustomerResponseDto
       END
```

---

## 3. Update Operation Flow

```
START
  │
  ├─► Retrieve customer by ID from database
  │   └─► Exception if not found
  │
  ├─► Service validates business rules
  │   - Uniqueness checks (if fields changed)
  │   - Age validation
  │   - Status lifecycle
  │   └─► Exception if validation fails
  │
  ├─► Update Customer entity fields
  │   - FirstName, LastName, etc.
  │   - Set updatedDate = NOW()
  │
  ├─► Call updateCustomerWithLogging(customer)
  │   │
  │   ├─► Log: ENTRY
  │   │   └─ CustomerID, UUID, Email
  │   │
  │   ├─► Log: DEBUG
  │   │   └─ All update values
  │   │
  │   ├─► Start timer
  │   │
  │   ├─► Call repo.updateCustomerNative(...)
  │   │   │
  │   │   ├─► Spring Data processes params
  │   │   │
  │   │   ├─► Execute: UPDATE SQL
  │   │   │   WHERE customer_id = :customerId
  │   │   │
  │   │   ├─► Database validates:
  │   │   │   - UNIQUE constraints
  │   │   │   - NOT NULL values
  │   │   │   - Foreign keys
  │   │   │
  │   │   └─► Return: rowsAffected (0 or 1)
  │   │
  │   ├─► Stop timer
  │   │
  │   ├─► Log: EXIT + Status
  │   │   If rowsAffected = 1:
  │   │       Log SUCCESS + executionTime
  │   │   If rowsAffected = 0:
  │   │       Log WARNING (no rows updated)
  │   │
  │   └─► If Exception:
  │       Log: ERROR + Details + Stack
  │       Rethrow Exception
  │
  ├─► Transaction: COMMIT if success
  │                ROLLBACK if error
  │
  ├─► Clear cache (CacheEvict)
  │
  └─► Return CustomerResponseDto
       END
```

---

## 4. Logging Flow

```
┌─────────────────────────────────────────────────────┐
│         Logging Flow in saveCustomerWithLogging     │
└─────────────────────────────────────────────────────┘

Step 1: ENTRY LOG (INFO level)
┌──────────────────────────────────────┐
│ ENTER :: saveCustomerWithLogging     │
│ │ uuid=550e8400-e29b...             │
│ │ firstName=John                    │
│ └─ lastName=Doe                     │
└──────────────────────────────────────┘
         │
         ▼
Step 2: DEBUG LOG (DEBUG level)
┌──────────────────────────────────────┐
│ saveCustomerWithLogging | Details:  │
│ │ pan=XXXXX9999X                   │  (Masked)
│ │ email=john@example.com           │
│ │ mobile=9876543210                │
│ │ status=PENDING_KYC               │
│ └─ dob=1990-05-15                  │
└──────────────────────────────────────┘
         │
         ▼
Step 3: Execute SQL
┌──────────────────────────────────────┐
│ START: long startTime = now()        │
│                                      │
│ customerRepository.saveCustomerNative│
│ (firstName, lastName, ...)           │
│                                      │
│ STOP: long executionTime = elapsed()│
└──────────────────────────────────────┘
         │
         ▼
Step 4: SUCCESS EXIT LOG (INFO level)
┌──────────────────────────────────────┐
│ EXIT :: saveCustomerWithLogging      │
│ │ uuid=550e8400-e29b...             │
│ │ executionTime=111ms               │  (Measured)
│ └─ status=SUCCESS                   │
└──────────────────────────────────────┘
         │
         ▼
    RETURN SUCCESS
    
    ┌─ OR ─────────────────────────────┐
    │ Step 4 Alternative: ERROR        │
    │                                  │
    │ CATCH: Exception e               │
    │                                  │
    │ [ERROR] LOG (ERROR level)        │
    │ │ uuid=550e8400-e29b...         │
    │ │ pan=XXXXX9999X                │
    │ │ errorMessage=...              │
    │ │ errorType=DuplicateKeyException│
    │ │ [Full Stack Trace]            │
    │ └─ throw e                      │
    │                                  │
    │ RETURN FAILURE + Exception      │
    └──────────────────────────────────┘
```

---

## 5. Database Interaction Sequence

```
Service Method
    │
    ├─► Validate & Prepare Data
    │
    └─► saveCustomerWithLogging(customer)
        │
        ├─► Log: ENTER
        │
        ├─► Log: DEBUG (with all params)
        │
        ├─► Start timing
        │
        └─► repository.saveCustomerNative(
                firstName: "John",
                lastName: "Doe",
                gender: "M",
                dateOfBirth: LocalDate(1990-05-15),
                email: "john@example.com",
                mobile: 9876543210,
                panNumber: "ABCDE1234F",
                aadhaarNumber: "123456789012",
                customerUuid: "550e8400-e29b-41d4...",
                status: "PENDING_KYC",
                createdDate: LocalDateTime(2026-02-15T10:30:00),
                updatedDate: LocalDateTime(2026-02-15T10:30:00)
            )
            │
            ├─► Spring Data
            │   └─► Binds parameters
            │
            ├─► Database
            │   │
            │   ├─► Parse SQL
            │   │   INSERT INTO customer
            │   │   (first_name, last_name, ...)
            │   │   VALUES (:firstName, :lastName, ...)
            │   │
            │   ├─► Validate Parameters
            │   │   - Types match columns
            │   │   - NULL values OK?
            │   │   - Size limits OK?
            │   │
            │   ├─► Check Constraints
            │   │   - PRIMARY KEY unique?
            │   │   - UNIQUE (email, pan, mobile, aadhaar)?
            │   │   - FOREIGN KEYS valid?
            │   │
            │   ├─► Execute Insert
            │   │   - Insert row into table
            │   │   - Generate customer_id (AUTO_INCREMENT)
            │   │   - Update indexes
            │   │
            │   └─► Return Success
            │
            └─► Stop timing
                Log: EXIT (SUCCESS)
                Return to Service
```

---

## 6. Parameter Binding Process

```
┌─────────────────────────────────────────────┐
│  Named Parameter Binding (Security Layer)   │
└─────────────────────────────────────────────┘

Java Code:
  customerRepository.saveCustomerNative(
      "John",                  ──► @Param("firstName")
      "Doe",                   ──► @Param("lastName")
      "M",                     ──► @Param("gender")
      ...
  )
  │
  ▼
Spring Data Processing:
  1. Match parameters to @Param annotations
  2. Create parameter map:
     {
        "firstName" → "John",
        "lastName" → "Doe",
        "gender" → "M",
        ...
     }
  │
  ▼
SQL Template Substitution:
  BEFORE: INSERT INTO customer
          (first_name, last_name, ...)
          VALUES (:firstName, :lastName, ...)
  
  AFTER:  INSERT INTO customer
          (first_name, last_name, ...)
          VALUES ('John', 'Doe', ...)
          
          [With Proper Type Conversion]
  │
  ▼
Security: SQL Injection Prevention
  Input:  "John'; DROP TABLE customer; --"
  Stored: "John'; DROP TABLE customer; --" (as literal string)
  Result: Column first_name gets full string value
          No SQL injection possible!
  │
  ▼
Database Execution:
  INSERT INTO customer (first_name, ...)
  VALUES ('John''; DROP TABLE customer; --', ...)
  
  ✓ Accepted as valid row data
  ✗ No DROP TABLE executed
```

---

## 7. Transaction & Rollback Flow

```
┌─────────────────────────────────────────────┐
│  Transaction Management Flow                │
└─────────────────────────────────────────────┘

START Transaction
  │
  ├─► Method: createCustomerEnquiry()
  │   @Transactional(rollbackFor = Exception.class)
  │
  ├─► Validation
  │   If fails ─────────┐
  │                     │
  ├─► Create entity    │
  │                     │
  ├─► Call save method │
  │   │                 │
  │   ├─► saveCustomerWithLogging()
  │   │   │              │
  │   │   ├─► Execute SQL
  │   │   │   Success ──┐
  │   │   │   Failure ──┼──┐
  │   │   │              │  │
  │   │   └─► Return    │  │
  │   │                  │  │
  │   └─► Return       │  │
  │                     │  │
  ├─► Publish event  │  │
  │                     │  │
  ├─► Return result   │  │
  │                     │  │
  └─► COMMIT          │  │
      Transaction      │  │
      All changes      │  │
      Persisted        │  │
      │                │  │
      ◄─ Success path  │  │
                        │  │
              Validation fails
              OR
              Exception thrown
              │                │
              ▼                ▼
           ROLLBACK       ROLLBACK
           Transaction    Transaction
           All changes    All changes
           Undone         Undone
           
           Exception      Exception
           Propagated     Propagated
           to caller      to caller
```

---

## 8. Error Handling & Logging Path

```
┌─────────────────────────────────────────────┐
│  Error Handling Flow                        │
└─────────────────────────────────────────────┘

saveCustomerWithLogging()
  │
  try {
      │
      ├─► Log: ENTER (INFO)
      │
      ├─► Log: DEBUG (DEBUG)
      │
      ├─► Start timer
      │
      ├─► Call SQL method
      │   │
      │   └─► Database Error ──────┐
      │                            │
      ├─► Stop timer              │
      │                            │
      ├─► Log: EXIT + SUCCESS      │
      │   (If no error)            │
      │                            │
  } catch (Exception e) {          │
      │                            │
      ◄────────────────────────────┘
      │
      ├─► Log: ERROR (ERROR level)
      │   ├─ customerUuid
      │   ├─ maskedPan
      │   ├─ errorMessage
      │   ├─ errorType
      │   └─ [Full Stack Trace]
      │
      └─► Re-throw Exception
          │
          └─► Service Layer
              │
              ├─► Catch as DatabaseException
              │   OR
              ├─► Catch as specific exception
              │   (DataIntegrityViolationException, etc)
              │
              └─► Handle or propagate to controller
```

---

## 9. Timing & Performance Measurement

```
┌─────────────────────────────────────────────┐
│  Execution Time Measurement                 │
└─────────────────────────────────────────────┘

saveCustomerWithLogging() {
    │
    Log: ENTER with UUID
    │
    long startTime = System.currentTimeMillis();
    │   ▲
    │   │
    │   └─ Current time in milliseconds
    │      (e.g., 1739609445123)
    │
    try {
        │
        Log: DEBUG with parameters
        │
        repository.saveCustomerNative(...);
        │
        │ ◄─── SQL EXECUTION
        │      Actual database work happens here
        │      Time spent: IO, parsing, constraints, etc.
        │
    } finally {
        │
        long executionTime = System.currentTimeMillis() - startTime;
        │   ▲
        │   │
        │   └─ Current time minus start time
        │      (e.g., 1739609445234 - 1739609445123 = 111ms)
        │
        Log: EXIT with executionTime=XXms
        │
        └─ Log shows exact duration
    }
}

Example Log Output:
  10:30:45.123 [INFO ] ENTER :: saveCustomerWithLogging
  10:30:45.125 [DEBUG] saveCustomerWithLogging | Details: ...
  10:30:45.234 [INFO ] EXIT :: saveCustomerWithLogging | executionTime=111ms
  
  111ms = Total time from ENTER to EXIT
```

---

## 10. Complete Request-to-Response Flow

```
CLIENT REQUEST
    │
    └─► CustomerController.createCustomerEnquiry(dto)
        │
        └─► CustomerServiceImpl.createCustomerEnquiry(dto)
            │
            ├─► Log: ENTER with pan, email
            │
            ├─► Validate uniqueness
            │   └─ existsByPan, existsByEmail, etc.
            │
            ├─► Create Customer entity
            │   ├─ firstName, lastName, gender, ...
            │   ├─ panNumber, aadhaarNumber
            │   ├─ uuid = new UUID()
            │   ├─ status = PENDING_KYC
            │   └─ timestamps = NOW()
            │
            ├─► saveCustomerWithLogging(customer)
            │   ├─ Log: ENTRY + uuid
            │   ├─ Log: DEBUG + params
            │   ├─ Execute: INSERT SQL
            │   ├─ Measure: executionTime
            │   └─ Log: EXIT + SUCCESS
            │
            ├─► eventPublisher.publishCustomerRegistered(customer)
            │   └─ CIBIL check event
            │
            ├─► mapEntityToResponse(customer)
            │   ├─ Convert entity to DTO
            │   ├─ Mask PAN/Aadhaar
            │   └─ Return CustomerResponseDto
            │
            └─► Log: EXIT with status=SUCCESS
                │
                └─► RESPONSE to CLIENT
                    {
                      customerId: 101,
                      customerUuid: "550e8400-e29b...",
                      firstName: "John",
                      lastName: "Doe",
                      email: "john@example.com",
                      status: "PENDING_KYC"
                    }
```

---

## Summary of Flows

| Operation | Time | Key Logs | Database Action |
|-----------|------|----------|-----------------|
| **Save** | 50-200ms | ENTER, DEBUG, EXIT | INSERT + constraints |
| **Update** | 40-150ms | ENTER, DEBUG, EXIT, rowsAffected | UPDATE + constraints |
| **Error** | <10ms | ERROR + Stack | ROLLBACK |
| **Read** | 5-50ms | DEBUG | SELECT (read-only) |

---

**All diagrams show actual implementation with real logging and timing**
