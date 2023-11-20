FROM mysql:8.0

ENV MYSQL_ROOT_PASSWORD=testUser

RUN echo 'Ejecución scripts'

COPY 00_grant_database.sql /docker-entrypoint-initdb.d/00_grant_database.sql
RUN chmod 644 /docker-entrypoint-initdb.d/00_grant_database.sql

COPY 01_create_database.sql /docker-entrypoint-initdb.d/01_create_database.sql
RUN chmod 644 /docker-entrypoint-initdb.d/01_create_database.sql
