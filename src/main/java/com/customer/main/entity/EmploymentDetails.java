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

	public Long getEmploymentId() {
		return employmentId;
	}

	public void setEmploymentId(Long employmentId) {
		this.employmentId = employmentId;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Double getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Double monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public Integer getTotalExperience() {
		return totalExperience;
	}

	public void setTotalExperience(Integer totalExperience) {
		this.totalExperience = totalExperience;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
    
    

}
