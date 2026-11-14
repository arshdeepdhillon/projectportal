---
 AI Contribution Weight: [ 92% ] AI Generated / [8%] Human Refactored
---

# Agent Environment

This document records configuration visible for the current GitHub Copilot CLI session and the Project Portal
environment documented in the repository. Session and host facts are observations, not portable repository defaults.
Values unavailable from the session or repository are marked **Not exposed** or **Not specified** rather than inferred.

## 1. Agent runtime

| Parameter                                      | Current value                                                                                                     | Status / source                                                                            |
|------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| Agent product/runtime                          | GitHub Copilot CLI                                                                                                | Current session environment.                                                               |
| Agent/runtime version                          | 1.0.112                                                                                                           | Current session environment; not a repository-pinned dependency.                           |
| Agent process mode and lifecycle               | API-accessed interactive coding session                                                                           | Current session; backend process/lifecycle details are not exposed.                        |
| Workspace / repository root                    | `\cs763\project\project-portal`                                                                                   | Current session environment.                                                               |
| File-system access policy                      | Workspace read, search, and edit tools are available; broader host policy is not exposed                          | Current session tools.                                                                     |
| Human approval or autonomous action boundaries | Repository instructions require explicit user confirmation before creating a commit; other limits are not exposed | `.github/copilot-instructions.md` and current session.                                     |
| Agent logging or session persistence           | Session state is available for this conversation; retention policy is not exposed                                 | Current session tools expose session artifacts; broader retention details are unavailable. |

## 2. LLM settings

The model-selection mode for this session is **Auto**: the system selects the
underlying model dynamically. These observations describe this session, not
portable repository defaults.

| Parameter               | Current value                    | Status                       |
|-------------------------|----------------------------------|------------------------------|
| Agent-facing AI service | GitHub Copilot                   | Current session environment. |
| Model-selection mode    | Auto; model selected dynamically | Current session environment. |

## 3. Tool settings

The following tool interfaces are available in this session. Their availability
is session-specific and does not constitute a repository tool manifest or a
general host permission policy.

| Tool setting                                  | Current value                                                                      | Status                                                                |
|-----------------------------------------------|------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| Agent tool interfaces                         | PowerShell; repository read/search/edit; GitHub MCP; IDE selection and diagnostics | Available in the current session.                                     |
| Shell command utilities confirmed available   | Git 2.39.1.windows.1; curl 8.13.0                                                  | Versions reported by the current host.                                |
| Shell / command execution scope               | Workspace commands are available; broader execution policy is not exposed          | Current session provides PowerShell; complete host policy is unknown. |
| Git operations and permitted remotes          | Git is installed; remote authorization is not exposed                              | Current host and session.                                             |
| Network access, domains, and API integrations | curl is installed; permitted network destinations are not exposed                  | Current host and session.                                             |
| Tool timeouts, concurrency, and output limits | Tool-specific limits apply; no complete global configuration is exposed            | Current tool interfaces.                                              |

The project uses the Gradle wrapper with Gradle 9.6.0 (`gradle/wrapper/gradle-wrapper.properties`). The current build
declares the Java, Spring Boot 3.3.5, dependency-management 1.1.6, and SonarQube 7.5.0.8588 plugins in `build.gradle`.
These are project build settings, not agent tool permissions.

## 4. Host and project environment

### Specification targets

The following are intended environment targets stated by `spec.md`; they are not proof that every dependency is
currently configured in the build.

| Parameter             | Specification target                                     | Source                                        |
|-----------------------|----------------------------------------------------------|-----------------------------------------------|
| Java runtime          | Java 17 LTS                                              | `spec.md`, “System Architecture & Tech Stack” |
| Application framework | Spring Boot 3.2+                                         | `spec.md`, “System Architecture & Tech Stack” |
| Persistence           | Spring Data JPA / Hibernate 6 with a relational database | `spec.md`, “System Architecture & Tech Stack” |
| Data validation       | Jakarta Bean Validation (`jakarta.validation`)           | `spec.md`, “System Architecture & Tech Stack” |
| JSON processing       | Jackson 2.x                                              | `spec.md`, “System Architecture & Tech Stack” |
| Local fixture data    | JSON fixtures loaded under `dev` or `test` profiles      | `spec.md`, “Data Seed Strategy”               |

### Current repository build configuration

| Parameter                   | Current value                                                               | Source                                                     |
|-----------------------------|-----------------------------------------------------------------------------|------------------------------------------------------------|
| Build system                | Gradle wrapper                                                              | `gradlew`, `build.gradle`                                  |
| Gradle wrapper distribution | Gradle 9.6.0 binary distribution                                            | `gradle/wrapper/gradle-wrapper.properties`                 |
| Gradle plugins              | Java, Spring Boot 3.3.5, dependency-management 1.1.6, SonarQube 7.5.0.8588  | `build.gradle`                                             |
| Project group / version     | `com.bu` / `1.0.0`                                                          | `build.gradle`                                             |
| Java toolchain              | Azul vendor, Java 17                                                        | `build.gradle` configures the compiler and test toolchain. |
| Spring dependencies         | Spring Boot JPA and web starters; Flyway; PostgreSQL and H2 runtime drivers | `build.gradle`                                             |
| Database migrations         | Flyway enabled; Hibernate schema mode is `validate`                         | `src/main/resources/application.properties`                |
| Validation dependency       | No Bean Validation starter explicitly declared                              | `build.gradle`                                             |

The build is configured for Java 17 and Spring Boot 3.3.5. This is newer than the minimum Spring Boot 3.2+ target stated
in `spec.md`. The specification mentions Jakarta Bean Validation, but the current build does not explicitly declare a
validation starter.

### Current host and application configuration

| Host or application parameter             | Current value                                                                   | Source / status                                                    |
|-------------------------------------------|---------------------------------------------------------------------------------|--------------------------------------------------------------------|
| Operating system                          | Windows 10 Pro, version 10.0.19045                                              | Current host query.                                                |
| CPU architecture                          | x64                                                                             | Current host query.                                                |
| Java runtime on host `PATH`               | `java` was not found on `PATH` during inspection                                | Current host query; separate from the configured Gradle toolchain. |
| Java build toolchain                      | Azul vendor, Java 17                                                            | `build.gradle`.                                                    |
| Java target for the specified service     | Java 17 LTS                                                                     | `spec.md`; matches the configured toolchain.                       |
| Local development database                | File-backed H2 in PostgreSQL compatibility mode at `build/dev-db/projectportal` | `application-dev.properties`.                                      |
| Test database                             | In-memory H2 in PostgreSQL compatibility mode                                   | `src/functionaltest/resources/application-test.properties`.        |
| Compose database                          | PostgreSQL 16 Alpine, service name `database`, port 5432                        | `docker-compose.yml`.                                              |
| Application port                          | 8080                                                                            | `docker-compose.yml`, `Dockerfile`.                                |
| Database connection environment variables | `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `SPRING_DATASOURCE_URL`                | `docker-compose.yml`, `application.properties`.                    |
| Profile selection                         | Compose sets `SPRING_PROFILES_ACTIVE=postgres`; IDE `dev` profile is explicit   | `docker-compose.yml`, `README.md`.                                 |

`application.properties` supplies a local PostgreSQL URL and default database user values, which can be overridden with
environment variables. Docker Compose uses the `database` service and a named volume; deployment credentials should be
supplied through the deployment environment rather than treated as agent settings.

## 5. Configuration sources and limits

Project targets and application settings above are sourced from `spec.md`,`build.gradle`, Gradle wrapper properties,
application properties,`docker-compose.yml`, `Dockerfile`, and `README.md`. Session-specific facts come from the current
execution environment or host queries. Agent internals, managed-service configuration, broader host permissions, and
deployment values not defined in the repository are not exposed or specified; they are not inferred here.

## 6. Repository agent instructions

The repository instructions in `.github/copilot-instructions.md` require contribution-weight headers on source files,
contribution history for source changes, and explicit user confirmation immediately before creating a commit. They also
describe Project Portal's service boundaries and architectural constraints. They do not specify the runtime, model
internals, or tool permissions listed as unavailable above.