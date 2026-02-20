package com.customer.main.entity;

/**
 * Customer lifecycle status in Home Loan context.
 * PENDING_KYC: Registered, documents under verification
 * ACTIVE: KYC approved, eligible for loan application
 * INACTIVE: Deactivated/suspended
 */
public enum CustomerStatus {
    PENDING_KYC,
    ACTIVE,
    INACTIVE
}
