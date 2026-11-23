# :core:database
Owner work package: WP-0D

Room database persistence for offline caching and outbox.
- `AppDatabase`: Room database containing `OutboxEntity` and `CacheEntryEntity`.
- `OutboxStore`: interface and `RoomOutboxStore` implementation.
  * REQUESTED CHANGES: `OutboxStore` interface is currently defined module-privately here because it is missing from `:core:model`. Once added to `:core:model`, `RoomOutboxStore` can bind to the shared contract directly.
- `RoomCacheStore`: implements `:core:model:CacheStore`.
