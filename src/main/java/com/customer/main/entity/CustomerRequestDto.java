package com.customer.main.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDto {

    @NotBlank
    private String firstName;

    private String lastName;

    @Email
    private String email;

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String panNumber;

    @NotBlank
    private String aadhaarNumber;
}
