# :core:data

Shared data repositories for the Wakatta Android application.

## Responsibilities
Provides implementations of the six core shared repositories:
- `MachineRepository` (`FakeMachineRepository`)
- `SignOffRepository` (`FakeSignOffRepository`)
- `WorkerRepository` (`FakeWorkerRepository`)
- `GlossaryRepository` (`FakeGlossaryRepository`)
- `CheckRepository` (`FakeCheckRepository`)
- `ReceiptRepository` (`FakeReceiptRepository`)

## Fake Implementations
Each fake implementation reads seed data from `:core:model` `DemoSeed` and simulates 200 ms latency.
Worker responses never contain `correctIndex`; check questions are graded inside `FakeCheckRepository`.

## Real Implementations
Real implementations will be added in package `com.techtsunagu.wakatta.core.data.real` when `:core:network` endpoints are wired.
