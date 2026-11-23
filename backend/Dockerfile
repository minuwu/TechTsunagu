# Wakatta? API image (architecture §15). Multi-stage, locked dependencies, non-root.
# The container runs the migration as the schema owner, then serves as the app role (both URLs are secrets).

FROM python:3.12-slim-bookworm AS build
COPY --from=ghcr.io/astral-sh/uv:0.11.28 /uv /bin/uv
WORKDIR /app
ENV UV_COMPILE_BYTECODE=1 UV_LINK_MODE=copy UV_PYTHON_DOWNLOADS=never
COPY pyproject.toml uv.lock ./
RUN uv sync --frozen --no-dev --no-install-project
COPY alembic.ini ./
COPY migrations ./migrations
COPY app ./app
COPY jobs ./jobs

FROM python:3.12-slim-bookworm
RUN useradd --create-home --uid 10001 wakatta
WORKDIR /app
COPY --from=build --chown=wakatta:wakatta /app /app
ENV PATH="/app/.venv/bin:$PATH" PYTHONUNBUFFERED=1
USER wakatta
EXPOSE 8000
# jobs/start.py: first-boot database setup, migrations and an optional demo reset, then uvicorn (express Container
# Apps environments have no Jobs). It trusts X-Forwarded-For because the container is reachable only via Azure's ingress.
CMD ["python", "/app/jobs/start.py"]
