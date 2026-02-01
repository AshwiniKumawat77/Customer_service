package com.customer.main.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotNull(message = "Address type is required")
    @Enumerated(EnumType.STRING)
    private AddressType type;

    @NotBlank(message = "House number is required")
    private String houseNo;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Invalid pincode")
    private String pincode;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
   }
