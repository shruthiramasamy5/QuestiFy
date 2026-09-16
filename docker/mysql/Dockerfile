FROM mysql:8.0

# Automatically run initialization script to create all microservice databases on startup
COPY init.sql /docker-entrypoint-initdb.d/init.sql

EXPOSE 3306
