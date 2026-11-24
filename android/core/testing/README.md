# :core:testing

Test-support library for Wakatta unit and integration tests.

## Contents
- `MainDispatcherRule`: JUnit rule setting `Dispatchers.Main` to a `TestDispatcher` (default `UnconfinedTestDispatcher`).
- `JsonFixture.load("name")`: Reads JSON fixtures from `src/main/resources/fixtures/`.
- `MockWebServer` helpers: Helper utilities for mocking backend responses with standard envelope `{success, data, message}`.
