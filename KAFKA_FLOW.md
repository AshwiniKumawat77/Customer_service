# Kafka & Outbox Flow (Beginner-Friendly)

## What is Kafka?

**Kafka** is a **message broker**: one service (producer) sends messages, another (consumer) reads them. Think of it like a **post office**:

- **Producer** = sender (your Customer Service)
- **Topic** = mailbox name (e.g. `customer-topic`)
- **Consumer** = receiver (e.g. Notification Service, Analytics Service)

Messages stay in the topic until consumers read them. If a consumer is down, messages are not lost.

---

## Why Use an "Outbox" Table?

We don’t send to Kafka **directly** inside the same database transaction as “create customer”. Reason:

1. **Database and Kafka are two different systems.** If we:
   - save customer to DB ✅  
   - then send to Kafka ❌ (Kafka is down / network error)  
   we might **roll back** the whole transaction and lose the customer, or we might commit and **lose the event**. Both are bad.

2. **Outbox pattern**:
   - In the **same transaction** as “create customer”, we **only** insert a row into `customer_outbox_event` (same DB).
   - A **separate job** (e.g. every 5 seconds) reads **NEW** rows from this table, sends them to Kafka, then marks them **SENT**.
   - So: **one transaction** = customer + outbox row. No Kafka in that transaction. Kafka is handled later by the processor.

So: **event is “generated”** when the row is inserted into `customer_outbox_event`. It is **sent to Kafka** a few seconds later by `CustomerOutboxProcessor`.

---

## Flow in This Project

```
┌─────────────────────────────────────────────────────────────────────────┐
│  1. POST /customers (createCustomer)                                     │
│     → Save customer to DB (same transaction)                             │
│     → Save one row to customer_outbox_event (status = "NEW")             │
│     → Return response                                                    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  2. Every 5 seconds: CustomerOutboxProcessor runs                        │
│     → SELECT * FROM customer_outbox_event WHERE status = 'NEW'           │
│     → For each row: send payload to Kafka topic "customer-topic"         │
│     → Set status = 'SENT', set processedAt, save row                     │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  3. Kafka                                                                 │
│     → Topic: customer-topic                                               │
│     → Other services can consume these events (e.g. send email, analytics)│
└─────────────────────────────────────────────────────────────────────────┘
```

---

## What Was Wrong & What We Fixed

| Issue | Why it broke | Fix |
|-------|----------------|-----|
| Error near `setCreatedAt` / `setProcessedAt` | Entity had no `processedAt` field; or we were overwriting `createdAt` (wrong). | Added `processedAt` to entity; processor sets it when sent and then **saves** the entity. |
| Repository / KafkaTemplate `null` | They were never injected (no constructor injection). | Used `@RequiredArgsConstructor` so Spring injects them. |
| Events “not generated” | 1) Processor looked for status **"PENDING"**, but we save **"NEW"**. 2) No `repository.save(event)` after marking SENT, so DB never updated. 3) `@EnableScheduling` was missing, so the scheduled job might not run. | Query by **"NEW"**; call **repository.save(event)** after setStatus/setProcessedAt; added **@EnableScheduling** on the main application. |
| Event “not sent” to Kafka | Kafka might not be running, or wrong address. | See “Kafka setup” below. |

---

## Do You Have Kafka Installed / Running?

Your app is configured to use Kafka at **localhost:9092** (`application.properties`).

- If Kafka is **not installed** or **not running**, the outbox processor will throw errors when it tries to send (e.g. connection refused). The **outbox row is still created** when you create a customer; only the “send to Kafka” step fails.

### Option A: Install and run Kafka locally (e.g. Windows)

1. Download Kafka (e.g. from https://kafka.apache.org/downloads).
2. Unzip and from the Kafka root directory run (in two separate terminals):
   - Start Zookeeper: `bin\windows\zookeeper-server-start.bat config\zookeeper.properties`
   - Start Kafka: `bin\windows\kafka-server-start.bat config\server.properties`
3. Optional: create topic manually:  
   `bin\windows\kafka-topics.bat --create --topic customer-topic --bootstrap-server localhost:9092`

### Option B: Use Docker

```bash
docker run -d --name kafka -p 9092:9092 apache/kafka
```

(Exact image and port may vary; check image docs.)

### Option C: Run without Kafka (for development)

You can disable the outbox processor so the app doesn’t try to connect to Kafka:

- Comment out or remove `@Scheduled` from `CustomerOutboxProcessor.processOutboxEvents()`, **or**
- Don’t start Kafka and accept that the processor will log errors every 5 seconds until Kafka is available.

The **event is still “generated”** (row in `customer_outbox_event`); it just won’t be sent until Kafka is up and the processor runs successfully.

---

## Quick checklist

- [ ] MySQL running; DB and table `customer_outbox_event` exist (JPA can create table with `ddl-auto=update`).
- [ ] After POST createCustomer: row appears in `customer_outbox_event` with `status = 'NEW'`.
- [ ] `@EnableScheduling` is on your main application class so the processor runs every 5 seconds.
- [ ] Kafka is running on `localhost:9092` (or you changed `spring.kafka.bootstrap-servers` to match your setup).
- [ ] After the processor runs: same row has `status = 'SENT'` and `processedAt` set.

If you want, we can next add a small Kafka consumer in this project to verify that messages are received on `customer-topic`.
