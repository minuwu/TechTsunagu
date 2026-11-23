# :core:sync
Owner work package: WP-0D

Outbox synchronization and network monitoring.
- `OutboxDecision`: pure decision function mapping HTTP codes and `Retry-After` to `OutboxDecision` (Done, Drop, PauseSession, RetryLater, Backoff).
- `OutboxSender`: contract for sending enqueued requests over the network.
- `OutboxWorker`: WorkManager worker skeleton that drains the outbox queue according to `OutboxDecision`.
- `OutboxWriterImpl`: implementation of `:core:model:OutboxWriter` backed by `OutboxStore`.
- `ConnectivityObserver` and `NetworkConnectivityObserver`: observes network connectivity state via `ConnectivityManager`.
