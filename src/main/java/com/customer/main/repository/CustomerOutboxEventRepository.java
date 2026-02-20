package com.customer.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.customer.main.entity.CustomerOutboxEvent;

public interface CustomerOutboxEventRepository extends JpaRepository<CustomerOutboxEvent, Long> {
	
	List<CustomerOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(String status);

}
