package com.customer.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.customer.main.entity.Customer;
import com.customer.main.entity.CustomerRequestDto;
import com.customer.main.service.CustomerService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("api/customers")
public class CustomerController {
	
	@Autowired
	private final CustomerService customerService;
	
	
	@PostMapping
	public ResponseEntity<Customer> createCustomer(@RequestBody @Valid CustomerRequestDto dto){
		 return new ResponseEntity<>(
	                customerService.createCustomer(dto),
	                HttpStatus.CREATED
	        );
	}
	
	@GetMapping
	public ResponseEntity<List<Customer>> getAllCustomer(){
		return new ResponseEntity<List<Customer>>(customerService.getAllCustomers(),HttpStatus.OK);
		
	}
	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
		return new ResponseEntity<>(customerService.getCustomerByID(id),HttpStatus.OK);
		
	}
	

}
