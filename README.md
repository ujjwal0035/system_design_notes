# System Design Interview Preparation Tracker

# Goal

Crack strong product-based and FAANG-level system design interviews through structured learning, deep dives, and mock practice.

---

# Progress Legend

* [ ] Not Started
* [/] In Progress
* [ ] Completed
* [R] Revision Needed
* [M] Mock Interview Completed

---

# Phase 1 — Foundation Systems

---

# 1. URL Shortener System (TinyURL / Bitly)

Status: [ ]

## Concepts To Learn

### Functional Requirements

* [ ] URL shortening
* [ ] Redirect support
* [ ] Expiry URLs
* [ ] Custom aliases
* [ ] Analytics

### Non-Functional Requirements

* [ ] Scalability
* [ ] High availability
* [ ] Low latency

### Core Design Topics

* [ ] Base62 encoding
* [ ] Unique ID generation
* [ ] Database schema design
* [ ] Read-heavy optimization
* [ ] Caching with Redis
* [ ] Cache invalidation
* [ ] Rate limiting
* [ ] Sharding strategies
* [ ] Consistent hashing
* [ ] Hot key handling

### Deep Dive

* [ ] Analytics pipeline
* [ ] Click tracking
* [ ] Async processing with Kafka
* [ ] Multi-region deployment

### Practice

* [ ] Draw architecture diagram
* [ ] Explain tradeoffs
* [ ] Solve without notes
* [ ] Mock interview

### Notes

*

---

# 2. Rate Limiter

Status: [ ]

## Concepts To Learn

### Algorithms

* [ ] Token bucket
* [ ] Leaky bucket
* [ ] Fixed window
* [ ] Sliding window log
* [ ] Sliding window counter

### Core Topics

* [ ] Distributed counters
* [ ] Redis atomic operations
* [ ] API throttling
* [ ] Per-user limits
* [ ] Per-IP limits
* [ ] Global rate limits

### Deep Dive

* [ ] Distributed rate limiter
* [ ] Horizontal scaling
* [ ] Retry handling
* [ ] Burst traffic handling

### Practice

* [ ] Design APIs
* [ ] Compare algorithms
* [ ] Mock interview

### Notes

*

---

# 3. Notification System

Status: [ ]

## Concepts To Learn

### Channels

* [ ] Push notifications
* [ ] Email notifications
* [ ] SMS notifications
* [ ] In-app notifications

### Architecture

* [ ] Event-driven architecture
* [ ] Queue-based processing
* [ ] Worker services
* [ ] Retry mechanisms
* [ ] Dead letter queues

### Core Topics

* [ ] Kafka
* [ ] RabbitMQ
* [ ] Idempotency
* [ ] Retry strategies
* [ ] Notification preferences
* [ ] Scheduling notifications

### Deep Dive

* [ ] Priority notifications
* [ ] Exactly-once delivery
* [ ] Multi-region delivery

### Practice

* [ ] Design end-to-end architecture
* [ ] Explain scaling path
* [ ] Mock interview

### Notes

*

---

# 4. Chat System (WhatsApp / Messenger)

Status: [ ]

## Concepts To Learn

### Realtime Communication

* [ ] WebSockets
* [ ] Long polling
* [ ] Sticky sessions
* [ ] Pub-sub architecture

### Messaging

* [ ] Message ordering
* [ ] Read receipts
* [ ] Offline sync
* [ ] Delivery guarantees

### Scaling

* [ ] Group chat scaling
* [ ] Presence system
* [ ] Message storage
* [ ] Fanout architecture

### Deep Dive

* [ ] End-to-end encryption
* [ ] Distributed messaging
* [ ] Multi-device sync

### Practice

* [ ] Design group chat
* [ ] Explain ordering logic
* [ ] Mock interview

### Notes

*

---

# 5. News Feed System (Instagram / Twitter)

Status: [ ]

## Concepts To Learn

### Feed Generation

* [ ] Push model
* [ ] Pull model
* [ ] Hybrid model

### Core Topics

* [ ] Feed ranking
* [ ] Fanout-on-write
* [ ] Fanout-on-read
* [ ] Celebrity problem
* [ ] Feed caching

### Deep Dive

* [ ] Recommendation systems
* [ ] Machine learning ranking
* [ ] Timeline generation

### Practice

* [ ] Compare feed models
* [ ] Handle celebrity users
* [ ] Mock interview

### Notes

*

---

# 6. Video Streaming System (YouTube / Netflix)

Status: [ ]

## Concepts To Learn

### Video Pipeline

* [ ] Video upload
* [ ] Video transcoding
* [ ] Metadata service

### Streaming

* [ ] CDN
* [ ] Chunk streaming
* [ ] Adaptive bitrate streaming

### Storage

* [ ] Blob storage
* [ ] Distributed storage
* [ ] Replication

### Deep Dive

* [ ] Recommendation engine
* [ ] Live streaming
* [ ] Global CDN strategy

### Practice

* [ ] Design upload flow
* [ ] Explain streaming architecture
* [ ] Mock interview

### Notes

*

---

# 7. Ride Sharing System (Uber / Ola)

Status: [ ]

## Concepts To Learn

### Realtime Tracking

* [ ] Driver location updates
* [ ] Rider matching
* [ ] ETA calculation

### Geo Topics

* [ ] GeoHash
* [ ] QuadTree
* [ ] Spatial indexing

### Scaling

* [ ] Realtime event processing
* [ ] Surge pricing
* [ ] Dispatch systems

### Deep Dive

* [ ] Route optimization
* [ ] Dynamic pricing
* [ ] Distributed location services

### Practice

* [ ] Design matching system
* [ ] Explain scaling
* [ ] Mock interview

### Notes

*

---

# 8. Distributed Cache (Redis-like System)

Status: [ ]

## Concepts To Learn

### Core Concepts

* [ ] Caching strategies
* [ ] TTL management
* [ ] Cache invalidation

### Distributed Systems

* [ ] Replication
* [ ] Partitioning
* [ ] Consistent hashing
* [ ] Leader-follower replication

### Eviction Policies

* [ ] LRU
* [ ] LFU
* [ ] FIFO

### Deep Dive

* [ ] Split brain problem
* [ ] Replication lag
* [ ] Distributed locking

### Practice

* [ ] Design cache cluster
* [ ] Compare eviction strategies
* [ ] Mock interview

### Notes

*

---

# 9. Search Autocomplete System

Status: [ ]

## Concepts To Learn

### Search Basics

* [ ] Trie
* [ ] Prefix search
* [ ] Ranking systems

### Optimization

* [ ] Memory optimization
* [ ] Top-K suggestions
* [ ] Caching

### Deep Dive

* [ ] Personalized search
* [ ] Real-time analytics
* [ ] Distributed trie

### Practice

* [ ] Design autocomplete engine
* [ ] Explain ranking
* [ ] Mock interview

### Notes

*

---

# 10. Distributed Job Scheduler

Status: [ ]

## Concepts To Learn

### Scheduling

* [ ] Delayed jobs
* [ ] Cron jobs
* [ ] Retry handling

### Distributed Coordination

* [ ] Leader election
* [ ] Distributed locking
* [ ] Worker coordination

### Reliability

* [ ] Failover handling
* [ ] Exactly-once execution
* [ ] Idempotent jobs

### Deep Dive

* [ ] Kubernetes jobs
* [ ] Airflow architecture
* [ ] Priority scheduling

### Practice

* [ ] Design scheduler
* [ ] Explain failover
* [ ] Mock interview

### Notes

*

---

# Weekly Revision Tracker

## Week 1

* [ ]

## Week 2

* [ ]

## Week 3

* [ ]

## Week 4

* [ ]

---

# Mock Interview Tracker

| Topic               | Date | Performance | Weak Areas |
| ------------------- | ---- | ----------- | ---------- |
| URL Shortener       |      |             |            |
| Rate Limiter        |      |             |            |
| Notification System |      |             |            |
| Chat System         |      |             |            |
| Feed System         |      |             |            |
| Video Streaming     |      |             |            |
| Uber System         |      |             |            |
| Distributed Cache   |      |             |            |
| Autocomplete        |      |             |            |
| Job Scheduler       |      |             |            |

---

# Final Goal Checklist

* [ ] Able to estimate system scale confidently
* [ ] Able to explain tradeoffs clearly
* [ ] Able to design under pressure
* [ ] Able to handle deep dive questions
* [ ] Able to discuss bottlenecks
* [ ] Able to scale systems from 1k → millions of users
* [ ] Comfortable with distributed systems concepts
* [ ] Ready for FAANG-level interviews

---


---

# Kafka Deep Dive

Status: [/] In Progress

## Notes

* [x] Topic vs Partition
* [x] Partition ordering and parallelism
* [x] Custom partition keys
* [x] Hot partitions
* [x] Partition count considerations
* [x] Consumer groups
* [x] Offsets and committed offsets
* [x] At-least-once processing
* [x] Idempotent consumers
* [x] Replication factor
* [x] Leader and follower replicas
* [x] ISR (In-Sync Replicas)
* [x] acks
* [x] min.insync.replicas
* [ ] Leader election failure scenarios
* [ ] Consumer rebalancing
* [ ] Producer internals
* [ ] Consumer internals
* [ ] Transactions / exactly-once
* [ ] Retention / log segments / compaction
* [ ] Kafka performance and production troubleshooting

## Detailed Notes

[Kafka Deep Dive Notes — Part 1](./kafka/Kafka-Deep-Dive-Notes-Part-1.md)
