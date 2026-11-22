"""Alembic environment. Migrations run as the schema owner (MIGRATION_DATABASE_URL), never as the app role."""

import os
from logging.config import fileConfig

from alembic import context
from sqlalchemy import Connection, create_engine, pool

from app.models import Base

config = context.config
if config.config_file_name is not None and config.attributes.get("configure_logger", True):
    fileConfig(config.config_file_name)

target_metadata = Base.metadata


def _run(connection: Connection) -> None:
    context.configure(connection=connection, target_metadata=target_metadata, compare_type=True)
    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    supplied: Connection | None = config.attributes.get("connection")
    if supplied is not None:
        _run(supplied)
        return
    url = os.environ.get("MIGRATION_DATABASE_URL")
    if not url:
        raise RuntimeError("MIGRATION_DATABASE_URL is not set (the schema owner's connection URL)")
    engine = create_engine(url, poolclass=pool.NullPool)
    with engine.connect() as connection:
        _run(connection)


if context.is_offline_mode():
    raise RuntimeError("Offline (SQL script) migrations are not used; run against a database.")
run_migrations_online()
