#!/bin/bash
# Runs once, automatically, on first container start (gvenzl/oracle-free
# executes every .sh/.sql file placed in /container-entrypoint-initdb.d/,
# in alphabetical order, after APP_USER has already been created from the
# compose env vars). Shell scripts (unlike plain .sql files) have access to
# the container's environment variables, which is why this is a .sh wrapper
# around the actual SQL rather than a .sql file.
#
# Creates a second, fully isolated schema/user dedicated to integration
# tests (AbstractIntegrationTest), so Flyway's "clean" command can freely
# drop and recreate this schema on every test run without ever touching
# the main application schema (APP_USER).

set -euo pipefail

sqlplus -s / as sysdba <<EOF
ALTER SESSION SET CONTAINER = FREEPDB1;

CREATE USER oip_test IDENTIFIED BY "${TEST_USER_PASSWORD}";

GRANT CONNECT, RESOURCE TO oip_test;
GRANT UNLIMITED TABLESPACE TO oip_test;

EXIT;
EOF
