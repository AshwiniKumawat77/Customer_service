package com.customer.main.event;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.customer.main.entity.CustomerOutboxEvent;
import com.customer.main.repository.CustomerOutboxEventRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@@Slf4j
public class CustomerOutboxProcessor {

	 private static final Logger log = LoggerFactory.getLogger(CustomerOutboxProcessor.class); 
    private final CustomerOutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
    	 	  
        List<CustomerOutboxEvent> events = repository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        for (CustomerOutboxEvent event : events) {
            try {
                kafkaTemplate.send("customer-topic",
                        event.getAggregateId(),
                        event.getPayload());

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());

                log.info("Event sent to Kafka | id={}", event.getId());

            } catch (Exception ex) {
                log.error("Kafka publish failed | id={} | will retry", event.getId(), ex);
            }
        }
    }
}
