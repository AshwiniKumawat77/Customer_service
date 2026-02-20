package com.customer.main.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.customer.main.constant.CustomerConstants;
import com.customer.main.entity.Customer;
import com.customer.main.entity.CustomerStatus;

/**
 * Repository for Customer entity. All SQL is defined in {@link CustomerConstants}.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
	@Modifying
	@Transactional
	@Query(value = CustomerConstants.INSERT_CUSTOMER, nativeQuery = true)
	void saveCustomerNative(
	        @Param("firstName") String firstName,
	        @Param("lastName") String lastName,
	        @Param("gender") String gender,
	        @Param("dateOfBirth") LocalDate dateOfBirth,
	        @Param("email") String email,
	        @Param("mobile") Long mobile,
	        @Param("panNumber") String panNumber,
	        @Param("aadhaarNumber") String aadhaarNumber,
	        @Param("customerUuid") String customerUuid,
	        @Param("status") String status,
	        @Param("createdDate") LocalDateTime createdDate,
	        @Param("updatedDate") LocalDateTime updatedDate
	);

	@Modifying
	@Transactional
	@Query(value = CustomerConstants.UPDATE_CUSTOMER, nativeQuery = true)
	int updateCustomerNative(
	        @Param("customerId") Long customerId,
	        @Param("firstName") String firstName,
	        @Param("lastName") String lastName,
	        @Param("gender") String gender,
	        @Param("dateOfBirth") LocalDate dateOfBirth,
	        @Param("email") String email,
	        @Param("mobile") Long mobile,
	        @Param("panNumber") String panNumber,
	        @Param("aadhaarNumber") String aadhaarNumber,
	        @Param("status") String status,
	        @Param("updatedDate") LocalDateTime updatedDate
	);


    // ---------- Exists (using COUNT constants + default method) ----------
    @Query(value = CustomerConstants.COUNT_BY_PAN_NUMBER, nativeQuery = true)
    long countByPanNumber(@Param("panNumber") String panNumber);

    default boolean existsByPanNumber(String panNumber) {
        return countByPanNumber(panNumber) > 0;
    }

    @Query(value = CustomerConstants.COUNT_BY_EMAIL, nativeQuery = true)
    long countByEmail(@Param("email") String email);

    default boolean existsByEmail(String email) {
        return countByEmail(email) > 0;
    }

    @Query(value = CustomerConstants.COUNT_BY_MOBILE, nativeQuery = true)
    long countByMobile(@Param("mobile") Long mobile);

    default boolean existsByMobile(Long mobile) {
        return countByMobile(mobile) > 0;
    }

    // ---------- Find by unique keys ----------
    @Query(value = CustomerConstants.FIND_BY_PAN_NUMBER, nativeQuery = true)
    Optional<Customer> findByPanNumber(@Param("panNumber") String panNumber);

    @Query(value = CustomerConstants.FIND_BY_EMAIL, nativeQuery = true)
    Optional<Customer> findByEmail(@Param("email") String email);

    @Query(value = CustomerConstants.FIND_BY_CUSTOMER_UUID, nativeQuery = true)
    Optional<Customer> findByCustomerUuid(@Param("customerUuid") String customerUuid);

    // ---------- Find by status (paginated) ----------
    @Query(value = CustomerConstants.FIND_BY_STATUS,
           countQuery = CustomerConstants.COUNT_BY_STATUS,
           nativeQuery = true)
    Page<Customer> findByStatus(@Param("status") String status, Pageable pageable);

    // ---------- Search by name, PAN, or email ----------
    @Query(value = CustomerConstants.SEARCH_BY_NAME_PAN_EMAIL,
           countQuery = CustomerConstants.COUNT_SEARCH_BY_NAME_PAN_EMAIL,
           nativeQuery = true)
    Page<Customer> searchByNameOrPanOrEmail(@Param("q") String searchTerm, Pageable pageable);

    // Helper: pass enum name for native query (status column is string)
    default Page<Customer> findByStatus(CustomerStatus status, Pageable pageable) {
        return findByStatus(status.name(), pageable);
    }

    @Query(value = CustomerConstants.COUNT_BY_UID, nativeQuery = true)
    long countByUId(@Param("aadhaarnumber") String uid);
	      default  boolean exixstByUId(String uid) {
			return countByUId(uid) > 0;
		}
}
