package com.customer.main.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EmploymentDetails {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employmentId;

    private String employmentType; // SALARIED / SELF_EMPLOYED
    private String companyName;
    private Double monthlyIncome;
    private Integer totalExperience;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

}
