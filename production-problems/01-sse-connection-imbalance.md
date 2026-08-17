# Production Problem #1 — SSE Connection Imbalance During Kubernetes Scale-Out

**Status:** Investigating  
**Area:** Kubernetes / Realtime / SSE / HPA / Redis / Kafka  
**Production scale:** ~15,000 live connections

## 1. Problem

During peak calling windows, the CRM service receives substantially more workload. CPU reaches the HPA scale-out threshold of 70%, causing Kubernetes to add pods.

The problem is that the newly created pods do not receive a proportional share of the existing long-lived SSE connections. Existing agents remain connected to the original pods.

As a result, Kubernetes increases pod count without actually redistributing the dominant connection workload.

## 2. Production Context

- Default pods: 3
- Peak pods observed: 6
- Live connections: ~15,000
- HPA scale-out trigger: 70% CPU
- Average dialer/SSE request/event rate: ~100 req/sec
- Peak workload windows vary by business conditions; commonly 10 AM–1 PM and 2 PM–4 PM.

Peak workload is not only calling traffic. The service also participates in campaign creation, campaign data preparation, filtering, compliance checks, dialer uploads, calling activity, dialer events, and SSE event delivery.

## 3. Observed Production Evidence

A representative peak observation:

| Metric | Pod 1 | Pod 2 | Pod 3 | Pod 4 | Pod 5 | Pod 6 |
|---|---:|---:|---:|---:|---:|---:|
| Live connections | 4,900 | 5,100 | 5,000 | 20 | 15 | 10 |
| CPU | 92% | 95% | 91% | 18% | 15% | 14% |

This is strong evidence of connection/workload imbalance.

The first three pods continue carrying almost all 15K live connections while the newly added pods remain nearly idle from a connection perspective.

## 4. Current Request/Event Routing Model

The current architecture maintains knowledge of which pod owns an agent's live connection. When a request/event reaches a pod that does not own the agent connection, Redis is used to identify the connection-owning pod and the event is forwarded there.

Conceptually:

```text
Request/Event
    |
    v
New/Random Pod
    |
    v
Redis lookup
    |
    v
Connection-owning Pod
    |
    v
SSE connection
    |
    v
Agent
```

This preserves connection correctness, but it does not automatically redistribute existing live connections when Kubernetes adds pods.

## 5. Why CPU Spikes During Peak

The CPU spike is not attributed to one isolated operation. Multiple workloads overlap during peak windows:

```text
Calling pace increases
        |
        +--> More requests
        +--> Campaign processing
        +--> Filtering
        +--> Compliance checks
        +--> Dialer upload / integration
        +--> More dialer events
        +--> More SSE activity
        |
        v
     DB load increases
        |
        v
     DB latency increases
        |
        v
Calling/event processing becomes delayed
        |
        v
SSE event delivery is delayed
        |
        v
More concurrent/in-flight work and application memory pressure
        |
        v
CPU + memory increase
```

A potential feedback loop is:

```text
Traffic increase
    -> DB load increase
    -> DB latency increase
    -> event processing delay
    -> more work remains in-flight
    -> memory/GC pressure
    -> CPU increase
    -> slower processing
    -> more backlog
```

This needs to be validated with production metrics rather than assumed to be the only CPU cause.

## 6. Investigation / Research Completed

### Finding A — HPA is reactive

The current HPA starts scale-out at 70% CPU. By the time a new pod is scheduled, started, and becomes Ready, the original pods may already be under significant pressure.

### Finding B — Adding pods does not move existing SSE connections

A long-lived SSE connection is already established with a specific pod. Adding another Kubernetes pod does not migrate that existing TCP/HTTP stream.

### Finding C — New pods can remain almost idle

The observed 4,900 / 5,100 / 5,000 connection distribution versus 20 / 15 / 10 demonstrates that scale-out is not equivalent to connection redistribution.

### Finding D — Redis routing solves ownership, not horizontal connection balancing

Redis can answer `agent -> connection-owning pod`, allowing an event received by another pod to be forwarded correctly. However, that mechanism still leaves the original connection-owning pod responsible for the live SSE connection.

### Finding E — The service contains multiple workload types

Campaign processing, filtering, compliance, dialer integration, event processing, DB access, and SSE handling are currently part of the same scaling boundary. Therefore CPU-based pod scaling may scale all workloads together even when only one workload is the immediate bottleneck.

## 7. Solutions Considered

### Solution 1 — Increase HPA max replicas

**Idea:** Allow Kubernetes to create more pods.

**Why it does not solve the core problem:** If existing agents remain connected to the first three pods, additional pods can remain almost idle.

**Decision:** Not sufficient as a standalone solution.

### Solution 2 — Lower the HPA threshold

**Idea:** Scale before CPU reaches 70%.

**Benefit:** Provides more headroom during the peak ramp.

**Limitation:** It does not redistribute existing SSE connections.

**Decision:** Useful as a supporting optimization, not the architectural fix.

### Solution 3 — Pre-scale before predictable peak windows

**Idea:** Increase the minimum/desired pod count before the known peak periods around 10 AM–1 PM and 2 PM–4 PM.

**Benefit:** Avoids waiting for CPU to reach the threshold while pods start and become Ready.

**Limitation:** Still does not move existing connections to the newly available pods.

**Decision:** Strong operational improvement, but not sufficient alone.

### Solution 4 — Rebalance live agent connections

**Idea:** When new capacity appears, move some agents to newly available pods by closing/re-establishing connections.

**Benefit:** Can distribute the 15K connections more evenly.

**Risks:** Reconnection storms, temporary disconnects, duplicate sessions, event loss, and additional load during rebalance.

**Decision:** Possible, but requires careful connection-draining/reconnect design.

### Solution 5 — Separate connection management from event processing

**Idea:** Make connection ownership and business/event processing independent scaling dimensions.

Conceptually:

```text
             Connection Layer
          +------+------+------+
          |      |      |      |
        Conn   Conn   Conn   Conn
          |      |      |      |
          +------+------+------+
                     |
                  Kafka
                     |
          +----------+----------+
          |          |          |
       Worker-1   Worker-2   Worker-3
```

**Benefit:** Processing capacity can scale independently from connection capacity. Kafka can distribute event-processing work while a connection registry routes the final event to the connection owner.

**Limitation:** Larger architectural change and requires careful ownership/routing semantics.

**Decision:** Strong candidate for the long-term architecture if measurement confirms processing and connection workloads need independent scaling.

## 8. Current Production Solution / Existing Design

The existing system uses Redis-based connection/event routing so that an event can be routed to the pod owning the agent's live connection.

This was useful for solving the original cross-pod event-delivery/connection-ownership problem.

The system has also moved toward Kafka for event processing as Redis usage grew with active users.

**Important distinction:** The current Redis/Kafka architecture should not be described as solving the new HPA connection-imbalance problem. It solves event ownership/routing and scalable event processing, while the current investigation is focused on why live connections remain concentrated on the original pods after HPA scale-out.

## 9. Why the Current Design Still Has the New Problem

The current model effectively becomes:

```text
Pod 1 -> ~5K connections -> high CPU
Pod 2 -> ~5K connections -> high CPU
Pod 3 -> ~5K connections -> high CPU
Pod 4 -> almost no connections -> low CPU
Pod 5 -> almost no connections -> low CPU
Pod 6 -> almost no connections -> low CPU
```

When an event for an agent connected to Pod 1 arrives at Pod 4:

```text
Pod 4
  -> Redis
  -> Pod 1
  -> SSE
  -> Agent
```

Therefore the new pod can perform routing work without relieving the connection ownership load on Pod 1.

## 10. Next Investigation

Before changing the architecture, identify the exact load-balancing path for a new SSE connection.

Check:

```bash
kubectl get svc -A
kubectl get ingress -A
kubectl get ingressclass
kubectl get svc <service-name> -n <namespace> -o yaml
kubectl describe svc <service-name> -n <namespace>
kubectl describe ingress <ingress-name> -n <namespace>
```

Specifically verify:

- Cloud load balancer type
- Ingress controller
- Kubernetes Service type
- `sessionAffinity`
- Sticky-session annotations/cookies
- How new SSE connections are distributed after Pod 4/5/6 become Ready

## 11. Metrics Required Before Final Architecture Decision

Capture these per pod during a complete peak window:

| Metric | Why |
|---|---|
| Live SSE connections | Proves connection distribution |
| CPU | Shows processing/connection pressure |
| Memory | Shows connection/event buffering pressure |
| Requests/sec | Shows request distribution |
| Dialer events/sec | Shows event-processing distribution |
| SSE write latency | Shows delivery pressure |
| Event processing latency | Shows application backlog |
| DB latency | Validates DB feedback-loop hypothesis |
| DB connections | Detects DB pool pressure |
| Kafka consumer lag | Detects asynchronous processing bottleneck |
| Redis ops/sec/latency | Detects routing/cache pressure |
| JVM GC activity | Determines whether memory pressure is contributing to CPU |

## 12. Success Criteria

The first target is not simply to increase the number of pods. The target is to make capacity actually usable.

For example, after scale-out from 3 to 6 pods, a healthier connection distribution would be approximately:

```text
Pod 1 -> ~2.5K
Pod 2 -> ~2.5K
Pod 3 -> ~2.5K
Pod 4 -> ~2.5K
Pod 5 -> ~2.5K
Pod 6 -> ~2.5K
```

The exact target should be determined from measured safe connection capacity per pod rather than assuming 2.5K is the final limit.

## 13. Current Decision

**Current status: Investigation in progress.**

The production evidence confirms that the immediate scaling problem is **connection/workload imbalance after HPA scale-out**.

The current working direction is:

1. Identify the actual Kubernetes load-balancing path.
2. Verify whether sticky/session affinity affects new SSE connection distribution.
3. Measure per-pod connection, CPU, memory, event, DB, Redis, and Kafka metrics during peak.
4. Evaluate pre-scaling as an immediate mitigation.
5. Evaluate controlled connection redistribution and/or separation of connection and event-processing workloads as the architectural solution.

No final architectural solution is marked as implemented for this specific imbalance problem yet.

## 14. System Design / Interview Takeaway

> Horizontal pod scaling does not automatically provide horizontal capacity when the workload contains long-lived, stateful connections.

For realtime systems, distinguish between:

- **request load balancing**
- **connection ownership**
- **event processing**
- **state/routing registry**
- **database bottlenecks**
- **pod scaling**

A scalable design must ensure that the unit Kubernetes scales is aligned with the unit that actually consumes the constrained resource.
