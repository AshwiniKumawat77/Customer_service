package com.customer.main.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer entity for Home Loan domain.
 * Used for KYC registration and loan application eligibility.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(unique = true, nullable = false, updatable = false, length = 36)
    private String customerUuid;

    @NotBlank(message = "First name is mandatory")
    private String firstName;
    @NotBlank(message = "Last name is mandatory")
    private String lastName;
    @NotBlank(message = "Gender is mandatory")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotNull
    @Column(unique = true)
    private Long mobile;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is mandatory")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
    @Column(unique = true)
    private String panNumber;
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits")
    @Column(unique = true)
    private String aadhaarNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.PENDING_KYC;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmploymentDetails employmentDetails;

    
    
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



	public String getGender() {
		return gender;
	}



	public void setGender(String gender) {
		this.gender = gender;
	}



	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}



	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}



	public Long getMobile() {
		return mobile;
	}



	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
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



	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}



	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}



	public List<Address> getAddresses() {
		return addresses;
	}



	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}



	public EmploymentDetails getEmploymentDetails() {
		return employmentDetails;
	}



	public void setEmploymentDetails(EmploymentDetails employmentDetails) {
		this.employmentDetails = employmentDetails;
	}



	@Override
	public String toString() {
		return "Customer [customerId=" + customerId + ", customerUuid=" + customerUuid + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", gender=" + gender + ", dateOfBirth=" + dateOfBirth + ", mobile="
				+ mobile + ", email=" + email + ", panNumber=" + panNumber + ", aadhaarNumber=" + aadhaarNumber
				+ ", status=" + status + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate
				+ ", addresses=" + addresses + ", employmentDetails=" + employmentDetails + "]";
	}
    
    
}
