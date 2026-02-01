package com.customer.main.entity.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.customer.main.entity.Customer;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CustomerDao {

    private final JdbcTemplate jdbcTemplate;

    public int save(Customer customer) {
        return jdbcTemplate.update(SqlProperty.INSERT_CUSTOMER,
                customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getGender(),
                customer.getDateOfBirth(), customer.getMobile());
    }

    public List<Customer> findAll() {
        return jdbcTemplate.query(SqlProperty.GET_ALL_CUSTOMERS, new CustomerRowMapper());
    }

    public Customer findById(Long id) {
        return jdbcTemplate.queryForObject(SqlProperty.GET_CUSTOMER_BY_ID, new CustomerRowMapper(), id);
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update(SqlProperty.DELETE_CUSTOMER_BY_ID, id);
    }

    public int update(Long id, Customer c) {
        return jdbcTemplate.update(SqlProperty.UPDATE_CUSTOMER_BY_ID,
                c.getFirstName(), c.getLastName(), c.getEmail(),
                c.getGender(), c.getDateOfBirth(), c.getMobile(), id);
    }
}

