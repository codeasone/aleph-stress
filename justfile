#!/usr/bin/env just

clean:
  clj -T:build clean

build:
  clj -T:build uber
  docker-compose build

db-create:
  PGPASSWORD=insecure psql -U postgres -h localhost -p 5432 -tc "SELECT 1 FROM pg_database WHERE datname = 'aleph_stress'" | grep -q 1 || PGPASSWORD=insecure psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE aleph_stress;"

db-create-migration:
  clj -M:migrate create

db-migrate:
  clj -M:migrate migrate

db-rollback:
  clj -M:migrate rollback

db-pending:
  clj -M:migrate pending-list

db-connect:
  PGPASSWORD=insecure psql -h 127.0.01 -p 5432 -U postgres -d aleph_stress

db-drop:
  PGPASSWORD=insecure psql -U postgres -h localhost -p 5432 -c "DROP DATABASE IF EXISTS aleph_stress;"

db-reset:
  PGPASSWORD=insecure psql -U postgres -h localhost -p 5432 -d aleph_stress -c "DELETE FROM submission;"

format:
  cljfmt fix
