package com.customer.main.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.customer.main.dto.CustomerEnquiryRequestDto;
import com.customer.main.dto.CustomerRequestDto;
import com.customer.main.dto.CustomerResponseDto;
import com.customer.main.dto.PageResponseDto;
import com.customer.main.entity.CustomerStatus;
import com.customer.main.service.CustomerService;

import jakarta.validation.Valid;

/**
 * REST API for Home Loan Customer Service.
 * Real-time industry-level endpoints for customer KYC and lifecycle.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    // ---------- Step 1: Customer enquiry / basic registration (pre-KYC, pre-loan) ----------
    @PostMapping("/enquiry")
    public ResponseEntity<CustomerResponseDto> createCustomerEnquiry(@RequestBody @Valid CustomerEnquiryRequestDto dto) {
        log.info("POST /api/customers/enquiry | pan={}", dto.getPanNumber());
        CustomerResponseDto response = customerService.createCustomerEnquiry(dto);
        log.debug("POST /api/customers/enquiry | created customerId={}", response.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------- Step 2: Complete KYC & enrich customer ----------
    @PutMapping("/{id}/kyc")
    public ResponseEntity<CustomerResponseDto> completeKyc(
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequestDto dto) {
        log.info("PUT /api/customers/{}/kyc | pan={}", id, dto.getPanNumber());
        return ResponseEntity.ok(customerService.completeKyc(id, dto));
    }

    // ---------- Legacy full create (direct KYC) ----------
    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@RequestBody @Valid CustomerRequestDto dto) {
        log.info("POST /api/customers | pan={}", dto.getPanNumber());
        CustomerResponseDto response = customerService.createCustomer(dto);
        log.debug("POST /api/customers | created customerId={}", response.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---------- Update ----------
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody @Valid CustomerRequestDto dto) {
        log.info("PUT /api/customers/{} | pan={}", id, dto.getPanNumber());
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    // ---------- Get by ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long id) {
        log.debug("GET /api/customers/{}", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // ---------- Get by UUID (for external/loan service lookup) ----------
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<CustomerResponseDto> getCustomerByUuid(@PathVariable String uuid) {
        log.debug("GET /api/customers/uuid/{}", uuid);
        return ResponseEntity.ok(customerService.getCustomerByUuid(uuid));
    }

    // ---------- Get by PAN (used during loan application) ----------
    @GetMapping("/pan/{pan}")
    public ResponseEntity<CustomerResponseDto> getCustomerByPan(@PathVariable String pan) {
        log.debug("GET /api/customers/pan/{}", pan);
        return ResponseEntity.ok(customerService.getCustomerByPan(pan));
    }

    // ---------- Get by Email ----------
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponseDto> getCustomerByEmail(@PathVariable String email) {
        log.debug("GET /api/customers/email/{}", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    // ---------- List all (paginated) ----------
    @GetMapping
    public ResponseEntity<PageResponseDto<CustomerResponseDto>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/customers | page={} size={}", page, size);
        return ResponseEntity.ok(customerService.getAllCustomers(page, size));
    }

    // ---------- List by status (e.g. ACTIVE, PENDING_KYC) ----------
    @GetMapping("/status/{status}")
    public ResponseEntity<PageResponseDto<CustomerResponseDto>> getCustomersByStatus(
            @PathVariable CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/customers/status/{} | page={} size={}", status, page, size);
        return ResponseEntity.ok(customerService.getCustomersByStatus(status, page, size));
    }

    // ---------- Search (name, PAN, email) ----------
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<CustomerResponseDto>> searchCustomers(
            @RequestParam("q") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/customers/search?q={} | page={} size={}", searchTerm, page, size);
        return ResponseEntity.ok(customerService.searchCustomers(searchTerm, page, size));
    }

    // ---------- Check PAN exists (for duplicate check before registration) ----------
    @GetMapping("/check-pan")
    public ResponseEntity<Boolean> checkPanExists(@RequestParam("pan") String pan) {
        log.debug("GET /api/customers/check-pan?pan={}", pan);
        return ResponseEntity.ok(customerService.existsByPan(pan));
    }

    // ---------- Update status (activate/deactivate) ----------
    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomerResponseDto> updateCustomerStatus(
            @PathVariable Long id,
            @RequestParam("status") CustomerStatus status) {
        log.info("PATCH /api/customers/{}/status?status={}", id, status);
        return ResponseEntity.ok(customerService.updateCustomerStatus(id, status));
    }
}
