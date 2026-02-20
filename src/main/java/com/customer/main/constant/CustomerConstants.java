package com.customer.main.constant;

/**
 * All SQL queries for Customer repository (Home Loan Customer Service).
 * Table name: customer (JPA entity default).
 */
public final class CustomerConstants {

    private CustomerConstants() {}

    // ---------- Insert/Save Customer ----------
    public static final String INSERT_CUSTOMER =
    	    "INSERT INTO customer " +
    	    "(first_name, last_name, gender, date_of_birth, email, mobile, " +
    	    "pan_number, aadhaar_number, customer_uuid, status, created_date, updated_date) " +
    	    "VALUES " +
    	    "(:firstName, :lastName, :gender, :dateOfBirth, :email, :mobile, " +
    	    ":panNumber, :aadhaarNumber, :customerUuid, :status, :createdDate, :updatedDate)";

    // ---------- Update Customer ----------
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

    // ---------- Exists checks (use count; repository exposes boolean via default method) ----------
    public static final String COUNT_BY_PAN_NUMBER =
            "SELECT COUNT(*) FROM customer WHERE pan_number = :panNumber";

    public static final String COUNT_BY_UID =
            "SELECT COUNT(*) FROM customer WHERE aadhaar_number = :aadhaarnumber";
    public static final String COUNT_BY_EMAIL =
            "SELECT COUNT(*) FROM customer WHERE email = :email";

    public static final String COUNT_BY_MOBILE =
            "SELECT COUNT(*) FROM customer WHERE mobile = :mobile";

    // ---------- Find by unique keys ----------
    public static final String FIND_BY_PAN_NUMBER =
            "SELECT * FROM customer WHERE pan_number = :panNumber";

    public static final String FIND_BY_EMAIL =
            "SELECT * FROM customer WHERE email = :email";

    public static final String FIND_BY_CUSTOMER_UUID =
            "SELECT * FROM customer WHERE customer_uuid = :customerUuid";

    public static final String FIND_BY_ID =
            "SELECT * FROM customer WHERE customer_id = :id";

    // ---------- Find by status (paginated; use with Pageable) ----------
    public static final String FIND_BY_STATUS =
            "SELECT * FROM customer WHERE status = :status ORDER BY created_date DESC";

    public static final String COUNT_BY_STATUS =
            "SELECT COUNT(*) FROM customer WHERE status = :status";

    // ---------- Search by name, PAN, or email ----------
    public static final String SEARCH_BY_NAME_PAN_EMAIL =
            "SELECT * FROM customer WHERE " +
            "LOWER(first_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(last_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "pan_number LIKE CONCAT('%', :q, '%') OR " +
            "email LIKE CONCAT('%', :q, '%') " +
            "ORDER BY first_name, last_name";

    public static final String COUNT_SEARCH_BY_NAME_PAN_EMAIL =
            "SELECT COUNT(*) FROM customer WHERE " +
            "LOWER(first_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(last_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "pan_number LIKE CONCAT('%', :q, '%') OR " +
            "email LIKE CONCAT('%', :q, '%')";
}
