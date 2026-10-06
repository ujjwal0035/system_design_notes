# Kafka — Deep Dive Notes (Part 1)

> Interview-focused notes covering topics, partitions, keys, consumer groups, offsets, idempotency, replicas, leaders, ISR, acknowledgements, and durability.

## 1. What problem does Kafka solve?

Kafka is best understood as a **distributed, durable, append-only event log** that decouples producers from consumers.

Instead of:
Producer -> Service A / Service B / Service C

Use:
Producer -> Kafka -> Service A
                 -> Service B
                 -> Service C

Benefits:
- Decoupling
- Durable buffering
- Multiple independent consumers
- Replay from retained data
- Horizontal scaling through partitions

Kafka is more than a traditional message queue: multiple consumer groups can independently consume the same retained data.

## 2. Topic

A **topic** is a logical stream/category of events.

Examples:
- call-events
- payment-events
- notification-events

A topic is divided into **partitions**. Partitions are the actual ordered logs where records are stored.

Topic != one physical log.

## 3. Partition

A partition is an **ordered, append-only log**.

Example:

| Offset | Event |
|---:|---|
| 0 | CALL_STARTED |
| 1 | CALL_CONNECTED |
| 2 | CALL_COMPLETED |

Important:
- Ordering is guaranteed **within a partition**.
- Kafka does **not** provide global ordering across partitions.
- Partitions provide parallelism and horizontal scalability.
- A partition is assigned to only one consumer within a consumer group at a time.

### Why partitions?

If a topic has 4 partitions:

P0 -> Consumer 1
P1 -> Consumer 2
P2 -> Consumer 3
P3 -> Consumer 4

This enables parallel processing.

Maximum active consumers for one topic in one consumer group is bounded by partition count.

Example: 4 partitions + 6 consumers means 2 consumers are idle.

## 4. Producer -> Partition Selection

A record can have a key and value. When a key is supplied, the producer's partitioning strategy maps the key to a partition.

Conceptually:

partition = hash(key) % number_of_partitions

Example:

key = CALL-123
hash(CALL-123) % 4 = 2
=> Partition 2

The exact partitioner behavior depends on the Kafka client/configuration, so the formula is a mental model rather than a promise of the exact implementation in every version.

## 5. Choosing a Partition Key

Use a key based on the **entity whose events must remain ordered**.

| Requirement | Good key |
|---|---|
| Same call must remain ordered | callId |
| Same customer must remain ordered | customerId |
| Same agent must remain ordered | agentId |
| Same order must remain ordered | orderId |

### CallPlus example

If the requirement is:

CALL_STARTED -> CALL_CONNECTED -> CALL_COMPLETED

for the same call, use:

key = callId

All events for the same call are routed to the same partition, preserving their relative ordering.

**Interview statement:**
> Kafka guarantees ordering within a partition. If I need ordering for a particular entity, I use that entity's ID as the partition key.

## 6. Key Selection Trade-off: Hot Partitions

Suppose one customer or agent generates most of the traffic.

If customerId maps Customer A to P7:

P7 -> very high traffic
P0 -> low
P1 -> low
P2 -> low

This is a **hot partition**.

Therefore:

> Choose a key that satisfies the ordering requirement while providing reasonable distribution.

Useful rule:
> Choose the smallest business entity whose events actually need ordering.

## 7. No Key

If no business key is provided, the producer can distribute records using its partitioning strategy.

This can improve distribution, but there is no entity-level ordering guarantee.

Use a key when business ordering matters.

## 8. Increasing Partition Count

Partitions can be increased.

Example:
10 partitions -> 20 partitions

But increasing partitions can change how keys map to partitions.

Conceptually:

hash(key) % 10

can become:

hash(key) % 20

So a key that previously went to P3 may later map elsewhere.

Therefore:
> Do not treat partition-count increases as completely free; consider key distribution and ordering requirements before changing the count.

## 9. How to Choose Number of Partitions

Consider:
1. Producer throughput
2. Consumer throughput
3. Required consumer parallelism
4. Broker capacity
5. Replication overhead
6. Expected future growth
7. Ordering requirements

Example:
If one partition safely handles about 20 MB/s and the topic needs about 100 MB/s:

100 / 20 = about 5 partitions

This is only a starting estimate. Benchmarking and growth planning are required.

More partitions != automatically better.

## 10. Consumer Group

A **consumer group** is a logical group of consumers that jointly consume a topic.

Example:

Topic: call-events

P0 -> C1
P1 -> C2
P2 -> C3
P3 -> C4

Consumer Group: call-processing

Within a consumer group:
> A partition is assigned to only one consumer at a time.

But different consumer groups can consume the same topic independently.

Example:

call-events
  |-- call-processing group
  |     |-- C1
  |     |-- C2
  |     |-- C3
  |
  |-- analytics group
        |-- A1
        |-- A2
        |-- A3

Each consumer group has its own consumption progress/offsets.

## 11. Offset

Every record in a partition has an offset.

Partition 0:
0 -> A
1 -> B
2 -> C
3 -> D

The offset is the record's position within that partition.

### Current position vs committed offset

A consumer may have:

current position  = 103
committed offset = 102

The committed offset tells Kafka where the consumer group should resume.

### Important

The committed offset represents the **next record to consume**, not the last successfully processed record.

If offset 101 was successfully processed:

committed offset = 102

## 12. At-Least-Once Processing

Scenario:

Offset 100 -> Event A

Consumer:
1. Processes A successfully.
2. DB operation succeeds.
3. Kafka offset commit fails.
4. Consumer crashes.

After restart, offset 100 can be processed again.

Therefore:

First attempt  -> processed
Second attempt -> processed again

This is **at-least-once processing**.

Duplicates are possible.

## 13. Idempotent Consumer

A consumer should often be designed so processing the same event multiple times produces the same final business result.

### Bad example

PAYMENT_CREATED
    |
    v
charge ₹100

If processed twice:
₹100 + ₹100 = ₹200  ❌

### Better: Event ID

Example event:

{
  eventId: EVT-12345,
  type: PAYMENT_CREATED,
  paymentId: PAY-999
}

Maintain a deduplication/inbox record:

processed_events
- event_id
- processed_at

Flow:

Kafka event
    |
    v
Check eventId
    |
    +--> already processed -> skip
    |
    +--> new -> process -> record eventId

### Database-level idempotency

Often the best approach is to make the business operation itself idempotent.

Examples:
- Unique constraint on event_id
- Conditional UPDATE
- Upsert
- State transition validation

Example:

UPDATE calls
SET status = 'COMPLETED'
WHERE call_id = 'C123'
  AND status <> 'COMPLETED';

Processing the same event again does not create a different final state.

## 14. Exactly-Once Caveat

Kafka exactly-once semantics do **not** automatically mean every external database/API side effect happens exactly once.

For external systems, you may still need:
- Idempotency
- Deduplication
- Transactions where appropriate
- Inbox/outbox patterns where appropriate

Do not casually claim "Kafka guarantees exactly once for my entire distributed system."

## 15. Replication

A partition can have multiple replicas.

Example:

Topic: call-events
Partitions = 6
Replication Factor = 3

Total partition replicas = 6 × 3 = 18.

Important:
- **Partitions** = logical parallelism/order units
- **Replicas** = copies for fault tolerance

## 16. Leader and Followers

For each partition, one replica is the leader.

Example:

P0
- Broker 1 -> Leader
- Broker 2 -> Follower
- Broker 3 -> Follower

Normally:
- Producer writes to the leader.
- Followers replicate the leader's log.
- Consumers normally read from the partition leader, subject to Kafka/client settings.

The leader provides a single authoritative write order for the partition.

## 17. Broker Failure

Suppose:

P0
- B1 -> Leader -> offset 100
- B2 -> Replica -> offset 100
- B3 -> Replica -> offset 100

B1 fails.

Kafka can elect an eligible replica as the new leader:

B2 -> New Leader
B3 -> Replica

This provides fault tolerance.

## 18. ISR — In-Sync Replicas

**ISR = In-Sync Replicas.**

It is the set of replicas that Kafka currently considers sufficiently caught up with the leader.

Example:

P0
- B1 -> Leader  -> offset 100
- B2 -> Replica -> offset 100
- B3 -> Replica -> offset 97

If B3 falls sufficiently behind:

ISR = {B1, B2}

B3 still exists as a replica, but it is outside the ISR.

**Interview statement:**
> ISR is the set of replicas that Kafka considers sufficiently caught up with the leader and therefore relevant for ISR-based durability and leader-election decisions.

Do not define ISR simply as "replicas containing exactly the same bytes."

## 19. Replication Factor vs ISR

Replication Factor = how many replicas exist.

ISR = how many replicas are currently in-sync.

Example:

Replication Factor = 3
- B1 -> Leader
- B2 -> ISR
- B3 -> out of sync

ISR count = 2.

## 20. Producer acks

### acks=0
Producer does not wait for broker acknowledgement.

- Lower acknowledgement overhead
- Weaker durability guarantee

### acks=1
Leader acknowledges after accepting the record locally.

Producer -> Leader -> ACK

Followers may still be catching up.

### acks=all
Producer waits for the leader to acknowledge the record according to the in-sync replica rules.

This is commonly paired with min.insync.replicas for stronger durability.

## 21. min.insync.replicas

Suppose:

Replication Factor = 3
min.insync.replicas = 2

If:
ISR = {B1, B2}

then there are 2 in-sync replicas and a write using the appropriate acknowledgement configuration can succeed.

If:
ISR = {B1}

then only one in-sync replica remains.

With acks=all and min.insync.replicas=2, Kafka can reject the write because the durability requirement cannot be met.

## 22. Complete Event Flow

Example CallPlus event:

CALL_COMPLETED
callId = C123

Step 1 — Producer
Producer
key = C123
value = CALL_COMPLETED

Step 2 — Partition selection
hash(C123) -> P7

Step 3 — Leader lookup
P7 leader -> Broker 2

Step 4 — Append
Broker 2
P7
offset 9812 -> CALL_COMPLETED

Step 5 — Replication
B2 -> Leader
B1 -> Replica
B3 -> Replica

Step 6 — Consumer Group
call-processing-group
P7 -> Consumer-4

Step 7 — Processing
Consumer -> DB/business operation

Step 8 — Commit
committed offset = 9813

Meaning offset 9813 is the next record to consume.

## 23. CallPlus Example

Possible event pipeline:

Dialer
  |
  v
CallPlus
  |
  v
Kafka / event stream
  |
  +------------------+
  |                  |
  v                  v
Call Processing     Analytics
Consumer Group      Consumer Group

Possible design:
- Topic: call-events
- Partitions: sized from throughput + parallelism requirements
- Key: callId
- Replication Factor: 3

Why callId?

CALL_STARTED -> CALL_CONNECTED -> CALL_ON_HOLD -> CALL_RESUMED -> CALL_COMPLETED

for the same call needs ordering.

Monitor for hot keys/partitions.

## 24. Interview Cheat Sheet

| Concept | Remember |
|---|---|
| Topic | Logical stream/category |
| Partition | Ordered append-only log |
| Offset | Position inside a partition |
| Key | Routes related events to a partition |
| Ordering | Guaranteed within a partition |
| Consumer | Process that reads/processes records |
| Consumer Group | Logical group sharing partitions and offsets |
| Replication Factor | Number of replicas |
| Leader | Authoritative replica for a partition |
| Follower | Replica that follows the leader |
| ISR | Replicas sufficiently caught up with leader |
| Hot Partition | Partition receiving disproportionately high traffic |
| acks=0 | No producer acknowledgement wait |
| acks=1 | Leader acknowledgement |
| acks=all | Stronger ISR-based acknowledgement |
| min.insync.replicas | Minimum ISR required for acks=all writes |
| Idempotency | Reprocessing produces the same final business result |

## 25. Senior-Level Rules

1. Ordering is a partition-level property, not a topic-level property.
2. Consumer parallelism is bounded by partition count.
3. Use a business key when entity-level ordering is required.
4. Choose the smallest entity that actually needs ordering.
5. Consider hot partitions when choosing a key.
6. More partitions improve parallelism but add operational and replication overhead.
7. Replication factor and ISR are different concepts.
8. At-least-once processing means duplicate processing is possible.
9. Design consumers to be idempotent when duplicates are possible.
10. Kafka exactly-once semantics do not automatically make external DB/API side effects exactly once.
11. acks=all + min.insync.replicas can provide stronger durability guarantees.
12. A committed offset represents the next record the consumer group should resume from.

---

## Next Kafka Topics

1. Leader election + ISR failure scenarios
2. Consumer polling
3. Consumer groups in depth
4. Rebalancing
5. Heartbeats and session.timeout.ms
6. max.poll.interval.ms
7. Consumer lag
8. Producer internals
9. Batching / linger.ms / compression
10. Retries and idempotent producer
11. Delivery semantics
12. Kafka transactions / exactly-once
13. Retention and log segments
14. Log compaction
15. Kafka performance internals
16. Production failure scenarios
