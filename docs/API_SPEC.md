# API Specification (Shared Contract)

**Version** 0.1.0 · **Updated** YYYY-MM-DD · **Owner** <Backend owner>

This is the **single source of truth** for the API between Frontend, Backend and AI. Build against it now using mocks. Any change goes through an `XREQ` in [AGENTS.md](../AGENTS.md). Never rename a field silently.

A machine-readable copy lives in [openapi.yaml](openapi.yaml) (OpenAPI 3.1). Change both in the same PR.

**Product in one paragraph:** <who uses it, what they do, the one demo moment>.

## 1. Conventions

| Item | Rule |
| :--- | :--- |
| Base URL (local) | `http://localhost:8000` |
| Contract file | [openapi.yaml](openapi.yaml), validated with `npx -y @redocly/cli lint docs/openapi.yaml` |
| Mock API (no backend) | `npx -y @stoplight/prism-cli mock docs/openapi.yaml` → `http://localhost:4010` |
| Format | JSON, UTF-8, `snake_case` keys |
| Auth | `Authorization: Bearer <token>` on every endpoint not marked **public** |
| Versioning | No version in the path during the hackathon. Breaking changes only through an `XREQ`. |
| Timestamps | ISO 8601 in UTC, e.g. `2026-01-01T04:30:00Z` |
| Nulls | A nullable field is always present and set to `null`. |
| Lists | Query `page` (from 1) and `page_size` (default 20, max 100); `data` is `{ "items": [...], "page": 1, "page_size": 20, "total": 57 }` |

### 1.1 Response envelope

Every JSON response, success or error, uses this envelope.

```json
{ "success": true, "data": {}, "message": "ok" }
```

```json
{
  "success": false,
  "error": { "code": "RESOURCE_NOT_FOUND", "message": "Item not found", "details": {} }
}
```

## 2. Roles and permissions

| Role | Can |
| :--- | :--- |
| <role> | <actions> |

## 3. Endpoint index

| Method | Path | Auth | Owner | Status |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | public | Backend | `PLANNED` |

Status: `PLANNED` → `MOCKED` → `LIVE`.

## 4. Endpoint details

### GET /api/health
**Response (200):** `{ "success": true, "data": { "status": "ok" }, "message": "ok" }`

## 5. How to update this file

1. Need a new endpoint or field? **File an `XREQ`** in [AGENTS.md](../AGENTS.md) with the exact JSON.
2. Backend implements it, updates §3 status, and marks the `XREQ` `[RESOLVED]`.
3. Once Frontend has checked the integration, set the status to `LIVE`.
4. Change [openapi.yaml](openapi.yaml) in the same PR and lint it.

### Changelog

| Version | Date | Change |
| :--- | :--- | :--- |
| 0.1.0 | YYYY-MM-DD | First contract. |
