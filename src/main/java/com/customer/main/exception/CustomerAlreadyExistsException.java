package com.customer.main.exception;

public class CustomerAlreadyExistsException extends RuntimeException{
	public CustomerAlreadyExistsException(String pan) {
        super("Customer already exists with " + pan);
    }
}
