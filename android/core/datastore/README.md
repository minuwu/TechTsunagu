# :core:datastore
Owner work package: WP-0B

Encrypted session storage and application preferences.
- `EncryptedSessionStore`: backed by Preferences DataStore + Tink AEAD with master key.
- `DataStoreSettingsStore`: UI and app settings (dark mode, text size, offline hints, notifications, onboarding).
- `DataStorePanelTokenStore`: storage for panel machine token.
