package com.customer.main.constant;

public class CustomerConstants {

    public static final String INSERT_CUSTOMER ="INSERT INTO customers " +
        "(first_name, last_name, email, mobile_number, pan_number, aadhaar_number, active, created_date) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String GET_CUSTOMER_BY_ID ="SELECT customer_id, first_name, last_name, email, mobile_number, pan_number, aadhaar_number, active, created_date " +
        "FROM customers WHERE customer_id = ?";

    public static final String CHECK_PAN_EXISTS ="SELECT COUNT(1) FROM customers WHERE pan_number = ?";
    public static final String GET_ALL_CUSTOMERS ="SELECT * FROM customer";
}
