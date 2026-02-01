package com.customer.main.entity.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CustomerRequestDto {

    @NotBlank
    private String firstName;

    private String lastName;
    private String gender;
	@Past(message = "Date of birth must be in the past")
	@JsonFormat(pattern = "dd-MM-YYYY") 
    private LocalDate dateOfBirth;

    @Email
    private String email;

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String panNumber;

    @NotBlank
    private String aadhaarNumber;
}
