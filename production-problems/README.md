# Production Problems Tracker

A running engineering log of production problems, investigations, experiments, solutions, trade-offs, and final implementation decisions.

## Tracking Format

Each production problem should capture:

1. Problem / symptom
2. Production context and scale
3. Root-cause hypotheses
4. Evidence / observations
5. Research and investigation performed
6. Solutions considered
7. Solution selected
8. Why the solution was selected
9. Implementation status
10. Results / metrics
11. Remaining risks and follow-ups
12. Interview / system-design takeaway

## Current Problems

| # | Problem | Area | Status |
|---|---|---|---|
| 1 | SSE/live-connection imbalance during Kubernetes scale-out | Kubernetes / Realtime / SSE | Investigating |

## Production Scale Context

- Current live connections: ~15,000
- Default pods: 3
- Peak pods observed: 6
- HPA scale-out trigger: 70% CPU
- Average dialer/SSE request/event rate: ~100 req/sec
- Peak windows vary by workload; commonly 10 AM–1 PM and 2 PM–4 PM

## Key Principle

Do not treat pod count as equivalent to workload capacity when long-lived connections are pinned to existing pods. A newly created pod can remain mostly idle while older pods continue carrying thousands of live SSE connections.

---

## Files

- `01-sse-connection-imbalance.md` — detailed investigation of the current 15K live-connection scaling problem.
