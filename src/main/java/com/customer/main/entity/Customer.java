package com.customer.main.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
	 	
		@Id 
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long customerId;
		
		@NotBlank(message = "First name is mandatory")
	    private String firstName;
		@NotBlank(message = "Last name is mandatory")
	    private String lastName;
		@NotBlank(message = "Gender is mandatory")
	    private String gender;
		@Past(message = "Date of birth must be in the past")
		@JsonFormat(pattern = "dd-MM-YYYY") 
	    private LocalDate dateOfBirth;
		@NotNull
		@Column(unique = true)
		@Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
	    private Long mobile;
		@Email(message = "Invalid email format")
		@NotBlank(message = "Email is mandatory")
		@Column(unique = true)
	    private String email;
		
		@NotBlank(message = "PAN number is required")
		@Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}",message = "Invalid PAN format")
		@Column(unique = true)
		private String panNumber;
		@NotBlank(message = "Aadhaar number is required")
		@Pattern(regexp = "\\d{12}",message = "Aadhaar must be 12 digits")
		@Column(unique = true)
		private String aadhaarNumber;
		
		private Boolean active;
        private LocalDateTime createdDate;
}
