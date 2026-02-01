package com.customer.main.exception;

public class CustomerNotFoundException extends RuntimeException {
	public CustomerNotFoundException(long id) {
		super("Customer not found with ID: " + id);
	}

}
