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
import com.customer.main.serviceImpl.CustomerServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * Polls the outbox table and publishes NEW events to Kafka.
 * Runs every 5 seconds. Status must match saveOutboxEvent: "NEW".
 */
@Component
@Slf4j
public class CustomerOutboxProcessor {

	 private static final Logger log = LoggerFactory.getLogger(CustomerOutboxProcessor.class);
	 
    private static final String OUTBOX_STATUS_PENDING = "NEW";
    private static final String OUTBOX_STATUS_SENT = "SENT";
    private static final String KAFKA_TOPIC = "customer-topic";

    private final CustomerOutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CustomerOutboxProcessor(CustomerOutboxEventRepository repository,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<CustomerOutboxEvent> events = repository.findTop50ByStatusOrderByCreatedAtAsc(OUTBOX_STATUS_PENDING);
        if (events.isEmpty()) {
            return;
        }

        for (CustomerOutboxEvent event : events) {
            try {
                kafkaTemplate.send(KAFKA_TOPIC, event.getAggregateId(), event.getPayload());
                event.setStatus(OUTBOX_STATUS_SENT);
                event.setProcessedAt(LocalDateTime.now());
                repository.save(event);
                log.info("Event sent to Kafka | id={} | aggregateId={}", event.getId(), event.getAggregateId());
            } catch (Exception ex) {
                log.error("Kafka publish failed | id={} | will retry", event.getId(), ex);
            }
        }
    }
}
