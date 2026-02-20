package com.customer.main.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.customer.main.constant.CustomerConstants;
import com.customer.main.entity.MaskingUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom implementation for native SQL operations on Customer entity with comprehensive logging.
 * Provides INSERT and UPDATE operations with real-time SQL execution logging.
 */
@Repository
public class CustomerRepositoryImpl {

    private static final Logger log = LoggerFactory.getLogger(CustomerRepositoryImpl.class);

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Save customer with custom SQL INSERT and comprehensive logging
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveCustomerNative(String firstName, String lastName, String gender, LocalDate dateOfBirth,
            String email, Long mobile, String panNumber, String aadhaarNumber, String customerUuid,
            String status, LocalDateTime createdDate, LocalDateTime updatedDate) {

        log.info("========== REPOSITORY: saveCustomerNative EXECUTED ==========");
        log.info("Query: INSERT_CUSTOMER");
        log.info("SQL Query: {}", formatSql(CustomerConstants.INSERT_CUSTOMER));
        log.info("Parameters: firstName={}, lastName={}, gender={}, dateOfBirth={}, email={}, mobile={}, panNumber={}, aadhaarNumber={}, customerUuid={}, status={}, createdDate={}, updatedDate={}",
                firstName, lastName, gender, dateOfBirth, email, mobile,
                MaskingUtil.maskPan(panNumber), MaskingUtil.maskAadhaar(aadhaarNumber), customerUuid, status,
                createdDate, updatedDate);

        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("firstName", firstName);
            params.put("lastName", lastName);
            params.put("gender", gender);
            params.put("dateOfBirth", dateOfBirth);
            params.put("email", email);
            params.put("mobile", mobile);
            params.put("panNumber", panNumber);
            params.put("aadhaarNumber", aadhaarNumber);
            params.put("customerUuid", customerUuid);
            params.put("status", status);
            params.put("createdDate", createdDate);
            params.put("updatedDate", updatedDate);

            namedParameterJdbcTemplate.update(CustomerConstants.INSERT_CUSTOMER, params);

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Status: SUCCESS | Execution Time: {}ms | customerUuid: {} | Record Inserted", executionTime, customerUuid);
            log.info("========== REPOSITORY: saveCustomerNative END ==========");
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Status: FAILED | Exception: {} | Message: {} | Execution Time: {}ms",
                    e.getClass().getSimpleName(), e.getMessage(), executionTime);
            log.error("Stack Trace: ", e);
            log.info("========== REPOSITORY: saveCustomerNative END (WITH ERROR) ==========");
            throw new RuntimeException("Failed to save customer", e);
        }
    }

    /**
     * Update customer with custom SQL UPDATE and comprehensive logging
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateCustomerNative(Long customerId, String firstName, String lastName, String gender,
            LocalDate dateOfBirth, String email, Long mobile, String panNumber, String aadhaarNumber,
            String status, LocalDateTime updatedDate) {

        log.info("========== REPOSITORY: updateCustomerNative EXECUTED ==========");
        log.info("SQL Constant: UPDATE_CUSTOMER");
        log.info("SQL Query: {}", formatSql(CustomerConstants.UPDATE_CUSTOMER));
        log.info("Parameters: customerId={}, firstName={}, lastName={}, gender={}, dateOfBirth={}, email={}, mobile={}, panNumber={}, aadhaarNumber={}, status={}, updatedDate={}",
                customerId, firstName, lastName, gender, dateOfBirth, email, mobile,
                MaskingUtil.maskPan(panNumber), MaskingUtil.maskAadhaar(aadhaarNumber), status, updatedDate);

        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("customerId", customerId);
            params.put("firstName", firstName);
            params.put("lastName", lastName);
            params.put("gender", gender);
            params.put("dateOfBirth", dateOfBirth);
            params.put("email", email);
            params.put("mobile", mobile);
            params.put("panNumber", panNumber);
            params.put("aadhaarNumber", aadhaarNumber);
            params.put("status", status);
            params.put("updatedDate", updatedDate);

            int rowsAffected = namedParameterJdbcTemplate.update(CustomerConstants.UPDATE_CUSTOMER, params);

            long executionTime = System.currentTimeMillis() - startTime;
            
            if (rowsAffected > 0) {
                log.info("Status: SUCCESS | Rows Affected: {} | Execution Time: {}ms | customerId: {} | Record Updated", rowsAffected, executionTime, customerId);
            } else {
                log.warn("Status: NO ROWS AFFECTED | Rows Affected: 0 | Execution Time: {}ms | customerId: {} | No matching record found", executionTime, customerId);
            }
            
            log.info("========== REPOSITORY: updateCustomerNative END ==========");
            return rowsAffected;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Status: FAILED | Exception: {} | Message: {} | Execution Time: {}ms | customerId: {}",
                    e.getClass().getSimpleName(), e.getMessage(), executionTime, customerId);
            log.error("Stack Trace: ", e);
            log.info("========== REPOSITORY: updateCustomerNative END (WITH ERROR) ==========");
            throw new RuntimeException("Failed to update customer", e);
        }
    }

    /**
     * Format SQL for better readability in logs
     */
    private String formatSql(String sql) {
        if (sql == null) return "";
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * Count customers by PAN number with logging
     */
    @Transactional(readOnly = true)
    public long countByPanNumber(String panNumber) {
        log.debug("ENTER :: countByPanNumber | panNumber={}", MaskingUtil.maskPan(panNumber));
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("panNumber", panNumber);
            
            Long count = namedParameterJdbcTemplate.queryForObject(
                    CustomerConstants.COUNT_BY_PAN_NUMBER, params, Long.class);
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("EXIT :: countByPanNumber | panNumber={} | count={} | executionTime={}ms", 
                    MaskingUtil.maskPan(panNumber), count != null ? count : 0, executionTime);
            return count != null ? count : 0;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("ERROR :: countByPanNumber | panNumber={} | Exception: {} | ExecutionTime={}ms",
                    MaskingUtil.maskPan(panNumber), e.getClass().getSimpleName(), executionTime);
            return 0;
        }
    }

    /**
     * Check if customer exists by PAN number with logging
     */
    @Transactional(readOnly = true)
    public boolean existsByPanNumber(String panNumber) {
        log.debug("ENTER :: existsByPanNumber | panNumber={}", MaskingUtil.maskPan(panNumber));
        boolean exists = countByPanNumber(panNumber) > 0;
        log.debug("EXIT :: existsByPanNumber | panNumber={} | exists={}", MaskingUtil.maskPan(panNumber), exists);
        return exists;
    }

    /**
     * Count customers by email with logging
     */
    @Transactional(readOnly = true)
    public long countByEmail(String email) {
        log.debug("ENTER :: countByEmail | email={}", email);
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("email", email);
            
            Long count = namedParameterJdbcTemplate.queryForObject(
                    CustomerConstants.COUNT_BY_EMAIL, params, Long.class);
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("EXIT :: countByEmail | email={} | count={} | executionTime={}ms", 
                    email, count != null ? count : 0, executionTime);
            return count != null ? count : 0;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("ERROR :: countByEmail | email={} | Exception: {} | ExecutionTime={}ms",
                    email, e.getClass().getSimpleName(), executionTime);
            return 0;
        }
    }

    /**
     * Check if customer exists by email with logging
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("ENTER :: existsByEmail | email={}", email);
        boolean exists = countByEmail(email) > 0;
        log.debug("EXIT :: existsByEmail | email={} | exists={}", email, exists);
        return exists;
    }

    /**
     * Count customers by mobile number with logging
     */
    @Transactional(readOnly = true)
    public long countByMobile(Long mobile) {
        log.debug("ENTER :: countByMobile | mobile={}", mobile);
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("mobile", mobile);
            
            Long count = namedParameterJdbcTemplate.queryForObject(
                    CustomerConstants.COUNT_BY_MOBILE, params, Long.class);
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("EXIT :: countByMobile | mobile={} | count={} | executionTime={}ms", 
                    mobile, count != null ? count : 0, executionTime);
            return count != null ? count : 0;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("ERROR :: countByMobile | mobile={} | Exception: {} | ExecutionTime={}ms",
                    mobile, e.getClass().getSimpleName(), executionTime);
            return 0;
        }
    }

    /**
     * Check if customer exists by mobile number with logging
     */
    @Transactional(readOnly = true)
    public boolean existsByMobile(Long mobile) {
        log.debug("ENTER :: existsByMobile | mobile={}", mobile);
        boolean exists = countByMobile(mobile) > 0;
        log.debug("EXIT :: existsByMobile | mobile={} | exists={}", mobile, exists);
        return exists;
    }
}
