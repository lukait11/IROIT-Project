-- Each microservice owns its own database (database-per-service). This runs
-- automatically on the postgres container's first startup only (mounted into
-- /docker-entrypoint-initdb.d/), since the official Postgres image skips this
-- directory entirely once the data directory already has data in it.
CREATE DATABASE "user-service";
CREATE DATABASE "order-service";
CREATE DATABASE "payment-service";
CREATE DATABASE "notification-service";
