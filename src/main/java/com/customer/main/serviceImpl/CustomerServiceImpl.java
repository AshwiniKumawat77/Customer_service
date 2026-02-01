package com.customer.main.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.customer.main.entity.Customer;
import com.customer.main.entity.CustomerRequestDto;
import com.customer.main.exception.CustomerAlreadyExistsException;
import com.customer.main.repository.CustomerRepository;
import com.customer.main.service.CustomerService;

public class CustomerServiceImpl implements CustomerService{
	
	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public Customer createCustomer(CustomerRequestDto dto) {
		if(existsByPan(dto.getPanNumber())) {
			throw new CustomerAlreadyExistsException(dto.getPanNumber());
		}
			Customer customer = new Customer();
			customer.setFirstName(dto.getFirstName());
			customer.setLastName(dto.getLastName());
			customer.setGender(dto.get);
		return null;
		
	}

	@Override
	public Customer getCustomerByID(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean existsByPan(String panNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Customer> getAllCustomers() {
		// TODO Auto-generated method stub
		return null;
	}

}
