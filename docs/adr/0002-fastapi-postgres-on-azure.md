# FastAPI + Postgres on Azure Container Apps

The backend is FastAPI with Postgres (Azure Database for PostgreSQL Flexible Server, B1ms). The API and the TanStack Start frontend (Nitro Node build) each run as an Azure Container App. SQLite and Render were simpler; the team chose Azure and Postgres to match what a judge's engineering team would run. Near-miss photos are stored in Postgres (`bytea`, max 2 MB, resized on the client) rather than Blob Storage, to keep one data store for the hackathon.
