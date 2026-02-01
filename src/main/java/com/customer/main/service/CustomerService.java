package com.customer.main.service;

import java.util.List;

import com.customer.main.entity.Customer;
import com.customer.main.entity.CustomerRequestDto;

public interface CustomerService {
	
	Customer createCustomer(CustomerRequestDto customerRequestDto);
	Customer getCustomerByID(long id);
	boolean existsByPan(String panNumber);
	List<Customer> getAllCustomers();

}
