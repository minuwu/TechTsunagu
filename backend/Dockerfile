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

FROM python:3.12-slim-bookworm
RUN useradd --create-home --uid 10001 wakatta
WORKDIR /app
COPY --from=build --chown=wakatta:wakatta /app /app
ENV PATH="/app/.venv/bin:$PATH" PYTHONUNBUFFERED=1
USER wakatta
EXPOSE 8000
# shortcut: migrations at start-up are safe only with one replica (§15.1); move to a Container Apps Job to scale out.
# --forwarded-allow-ips='*' trusts X-Forwarded-For because the container is reachable only through Azure's ingress.
CMD ["sh", "-c", "alembic upgrade head && exec uvicorn app.main:create_app --factory --host 0.0.0.0 --port 8000 --no-access-log --proxy-headers --forwarded-allow-ips='*'"]
