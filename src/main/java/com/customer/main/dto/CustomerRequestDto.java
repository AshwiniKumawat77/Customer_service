package com.customer.main.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating/updating Customer (KYC registration for Home Loan).
 */

@NoArgsConstructor
public class CustomerRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobileNumber;

    @NotBlank(message = "PAN is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
    private String panNumber;

    @NotBlank(message = "Aadhaar is required")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits")
    private String aadhaarNumber;

    @Valid
    @NotNull
    private AddressDto address;

    @Valid
    @NotNull
    private EmploymentDetailsDto employmentDetails;
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

	
	public AddressDto getAddress() {
		return address;
	}

	public void setAddress(AddressDto address) {
		this.address = address;
	}

	public EmploymentDetailsDto getEmploymentDetails() {
		return employmentDetails;
	}

	public void setEmploymentDetails(EmploymentDetailsDto employmentDetails) {
		this.employmentDetails = employmentDetails;
	}
	
	

	public CustomerRequestDto(@NotBlank(message = "First name is required") String firstName,
			@NotBlank(message = "Last name is required") String lastName,
			@NotBlank(message = "Gender is required") String gender,
			@NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
			@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email,
			@NotBlank(message = "Mobile number is required") @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number") String mobileNumber,
			@NotBlank(message = "PAN is required") @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format") String panNumber,
			@NotBlank(message = "Aadhaar is required") @Pattern(regexp = "\\d{12}", message = "Aadhaar must be 12 digits") String aadhaarNumber,
			@Valid @NotNull AddressDto address, @Valid @NotNull EmploymentDetailsDto employmentDetails) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.email = email;
		this.mobileNumber = mobileNumber;
		this.panNumber = panNumber;
		this.aadhaarNumber = aadhaarNumber;
		this.address = address;
		this.employmentDetails = employmentDetails;
	}

	@Override
	public String toString() {
		return "CustomerRequestDto [firstName=" + firstName + ", lastName=" + lastName + ", gender=" + gender
				+ ", dateOfBirth=" + dateOfBirth + ", email=" + email + ", mobileNumber=" + mobileNumber
				+ ", panNumber=" + panNumber + ", aadhaarNumber=" + aadhaarNumber + ", address=" + address
				+ ", employmentDetails=" + employmentDetails + ", getFirstName()=" + getFirstName() + ", getLastName()="
				+ getLastName() + ", getGender()=" + getGender() + ", getDateOfBirth()=" + getDateOfBirth()
				+ ", getEmail()=" + getEmail() + ", getMobileNumber()=" + getMobileNumber() + ", getPanNumber()="
				+ getPanNumber() + ", getAadhaarNumber()=" + getAadhaarNumber() + ", getAddress()=" + getAddress()
				+ ", getEmploymentDetails()=" + getEmploymentDetails() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}

	
    
}
