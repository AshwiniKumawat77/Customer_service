package com.customer.main.event;

import java.time.LocalDateTime;

import com.customer.main.entity.CustomerStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a customer is minimally registered (pre-KYC).
 * This can be consumed by:
 * - CIBIL service (to trigger bureau check)
 * - Notification service (to send email/SMS/OTP)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisteredEvent {

    public CustomerRegisteredEvent(Long customerId2, String customerUuid2, String firstName2, String lastName2,
			String panNumber2, String aadhaarNumber2, String email2, Object object, CustomerStatus status2,
			LocalDateTime createdDate2) {
		// TODO Auto-generated constructor stub
	}
	private Long customerId;
    private String customerUuid;
    private String firstName;
    private String lastName;
    private String panNumber;
    private String aadhaarNumber;
    private String email;
    private String mobileNumber;
    private CustomerStatus status;
    private LocalDateTime createdDate;
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public String getCustomerUuid() {
		return customerUuid;
	}
	public void setCustomerUuid(String customerUuid) {
		this.customerUuid = customerUuid;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPanNumber() {
		return panNumber;
	}
	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}
	public String getAadhaarNumber() {
		return aadhaarNumber;
	}
	public void setAadhaarNumber(String aadhaarNumber) {
		this.aadhaarNumber = aadhaarNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public CustomerStatus getStatus() {
		return status;
	}
	public void setStatus(CustomerStatus status) {
		this.status = status;
	}
	public LocalDateTime getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}
    
    
    
}

