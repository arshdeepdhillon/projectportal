---
 AI Contribution Weight: [ 90% ] AI Generated / [10%] Human Refactored
---

## Project Portal

A centralized campus-wide ecosystem designed to showcase, share and manage academic research, course capstones and
innovative student designs.

## Current implementation status

The service implements the Domain 1 project lifecycle, academic affiliations, repository/manuscript/video metadata,
manuscript file storage, and project search described in [spec.md](spec.md).

## Prerequisites

- Java 17 (Azul Zulu is configured as the Gradle toolchain) for local builds.
- Docker with the Docker Compose v2 plugin for the containerized service and PostgreSQL database.

## Build and test locally

Build the executable JAR and run tests, including the functional test against an isolated H2 database:

```powershell
.\gradlew.bat clean build
```

On macOS/Linux, run `./gradlew clean build`.

## Run locally from the IDE

No Spring profile is active by default. Activate `dev` explicitly in the IDE run configuration to use the file-backed
H2 database at `build/dev-db/projectportal`; example academic reference records are loaded for that profile. No
database service is required, and data remains available after application restarts. Stop the application and run
`.\gradlew.bat clean` to remove the local database files.

## Run with PostgreSQL

Start the application and PostgreSQL database:

```shell
docker compose up --build
```

Compose stores PostgreSQL data in a named volume. Set `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` in the environment
or a local `.env` file as appropriate for the deployment.

Docker Compose selects the `postgres` profile automatically. To run the application directly against PostgreSQL,
start PostgreSQL on `localhost:5432` and activate that profile (for example, set `SPRING_PROFILES_ACTIVE=postgres`
before launching the IDE run configuration). Override `SPRING_DATASOURCE_URL`, `DB_USERNAME`, and `DB_PASSWORD` as
needed. Flyway applies migrations automatically and Hibernate validates the schema at startup.

## Dependency lockfile

Commit `gradle.lockfile` to Git. Gradle dependency locking is enabled in `build.gradle`; the lockfile records the
resolved dependency versions so builds and Trivy dependency scans use a reproducible dependency graph. Regenerate it
after dependency changes with `.\gradlew.bat dependencies --write-locks` on Windows or
`./gradlew dependencies --write-locks` on macOS/Linux, then include the updated file in the same commit.
