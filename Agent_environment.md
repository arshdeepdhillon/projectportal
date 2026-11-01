# Agent Environment (Draft)

This document records the configuration visible for the current autonomous AI
developer-agent session in the Project Portal repository, alongside the
project's documented environment targets. Session and host values are
observations, not portable repository defaults. Settings that are not exposed
by the session or documented by the project remain **Not specified (TBD)**.

## 1. Agent runtime

The current session is running through GitHub Copilot. The repository itself
does not configure an agent runtime; session facts below describe this
environment only.

| Parameter                                      | Required value                                                                                     | Status / source                                                                          |
|------------------------------------------------|----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| Agent product/runtime                          | GitHub Copilot                                                                                     | Current session environment.                                                             |
| Agent/runtime version                          | 1.0.96                                                                                             | Current session environment; not a repository-pinned dependency.                         |
| Underlying agent framework / implementation    | Not specified (TBD)                                                                                | Not exposed in the session.                                                              |
| Agent process mode and lifecycle               | API-accessed interactive coding session                                                            | Current session; backend process/lifecycle details are not exposed.                      |
| Workspace / repository root                    | `/cs763/project/project-portal`                                                                    | Current session environment.                                                             |
| File-system access policy                      | Not specified (TBD)                                                                                | Available workspace path is known; full host permission policy is not exposed.           |
| Human approval or autonomous action boundaries | Not specified (TBD)                                                                                | No execution policy is configured here.                                                  |
| Agent logging or session persistence           | Session state is available for this conversation; runtime retention policy is not specified (TBD). | Current session tools expose session artifacts; broader retention policy is not exposed. |

## 2. LLM settings

The model-selection mode for this session is **Auto**: the system selects the
underlying model dynamically. The exact model and low-level inference
configuration are not exposed. These observations describe this session, not
portable repository defaults.

| Parameter                                               | Required value                   | Status                                                                     |
|---------------------------------------------------------|----------------------------------|----------------------------------------------------------------------------|
| Agent-facing AI service                                 | GitHub Copilot                   | Current session environment.                                               |
| Model-selection mode                                    | Auto; model selected dynamically | Current session environment.                                               |
| Underlying LLM provider/vendor                          | Not specified (TBD)              | Not exposed by the session.                                                |
| Exact model identifier and revision                     | Not specified (TBD)              | Auto mode selects the concrete model dynamically; its name is not exposed. |
| API endpoint / deployment                               | Not specified (TBD)              | Managed service endpoint is not exposed.                                   |
| Credential reference                                    | Not specified (TBD)              | Session authentication mechanism and secrets are not exposed.              |
| Context-window limit                                    | Not specified (TBD)              | No limit is documented.                                                    |
| Maximum output tokens                                   | Not specified (TBD)              | No limit is documented.                                                    |
| Sampling parameters (for example, temperature or top-p) | Not specified (TBD)              | No values are documented.                                                  |
| Request timeout, retry policy, and rate limits          | Not specified (TBD)              | No values are documented.                                                  |
| Fallback model or provider                              | Not specified (TBD)              | No fallback is documented.                                                 |

## 3. Tool settings

The current session provides the following tool interfaces. Their availability
is session-specific and does not constitute a repository tool manifest or a
general host permission policy.

| Tool setting                                  | Required value                                                                       | Status                                                               |
|-----------------------------------------------|--------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Agent tool interfaces                         | Shell commands, repository file read/search/edit, GitHub MCP, and IDE debugger tools | Available in the current session.                                    |
| Shell command utilities confirmed available   | `git` 2.38.1; `curl` 8.7.1                                                           | Current host command versions.                                       |
| Shell / command execution scope               | Not specified (TBD)                                                                  | A shell tool is available; complete execution policy is not exposed. |
| Git operations and permitted remotes          | Not specified (TBD)                                                                  | Git is installed; remote permissions are not specified.              |
| Network access, domains, and API integrations | Not specified (TBD)                                                                  | `curl` is installed; network policy is not exposed.                  |
| Per-tool approval requirements                | Not specified (TBD)                                                                  | Not exposed by the session.                                          |
| Tool timeouts, concurrency, and output limits | Not specified (TBD)                                                                  | No general invocation limits are exposed.                            |

The project build uses the Gradle wrapper; its configured distribution is
Gradle 9.6.0 (`gradle/wrapper/gradle-wrapper.properties`). This is a project
build-tool setting, not an agent permission or tool allowlist.

## 4. Host and project environment

### Specification targets

The following are required environment targets stated by `spec.md`. They
describe the intended service stack and are not all configured by the current
build.

| Parameter             | Specification target                                     | Source                                        |
|-----------------------|----------------------------------------------------------|-----------------------------------------------|
| Java runtime          | Java 17 LTS                                              | `spec.md`, “System Architecture & Tech Stack” |
| Application framework | Spring Boot 3.2+                                         | `spec.md`, “System Architecture & Tech Stack” |
| Persistence           | Spring Data JPA / Hibernate 6 with a relational database | `spec.md`, “System Architecture & Tech Stack” |
| Data validation       | Jakarta Bean Validation (`jakarta.validation`)           | `spec.md`, “System Architecture & Tech Stack” |
| JSON processing       | Jackson 2.x                                              | `spec.md`, “System Architecture & Tech Stack” |
| Local fixture data    | JSON fixtures loaded under `dev` or `test` profiles      | `spec.md`, “Data Seed Strategy”               |

The exact relational database product, database connection settings, and
profile activation mechanism are not specified.

### Current repository build configuration

| Parameter                               | Current value                     | Source                                                                       |
|-----------------------------------------|-----------------------------------|------------------------------------------------------------------------------|
| Build system                            | Gradle wrapper                    | `gradlew`, `build.gradle`                                                    |
| Gradle wrapper distribution             | Gradle 9.6.0 binary distribution  | `gradle/wrapper/gradle-wrapper.properties`                                   |
| Gradle plugins                          | Java plugin                       | `build.gradle`                                                               |
| Project group / version                 | `com.bu` / `1.0-SNAPSHOT`         | `build.gradle`                                                               |
| Declared application Java compatibility | Not specified                     | `build.gradle` does not set a Java toolchain or source/target compatibility. |
| Spring Boot plugin or dependencies      | Not declared in the current build | `build.gradle`                                                               |

The Java 17 and Spring Boot 3.2+ specification targets should not be read as
proof that the current build enforces or packages that stack. The current
`build.gradle` applies the Java plugin and declares JUnit test dependencies;
it does not declare Spring Boot, JPA, validation, or Jackson dependencies.

### Current host details

| Host parameter                                        | Value                                                              |
|-------------------------------------------------------|--------------------------------------------------------------------|
| Operating system                                      | macOS (Darwin)                                                     |
| Operating-system release                              | Not specified (TBD); only the Darwin kernel/platform was queried.  |
| CPU architecture                                      | x86_64                                                             |
| Container or virtual-machine image                    | Not specified (TBD)                                                |
| Memory and CPU allocation                             | Not specified (TBD)                                                |
| Installed Java runtime                                | OpenJDK 23.0.1, 64-bit Server VM                                   |
| Java target for the specified service                 | Java 17 LTS; current build does not declare/enforce that target.   |
| Database engine, address, and credentials reference   | Not specified (TBD); specification requires a relational database. |
| Required environment variables and their sources      | Not specified (TBD)                                                |
| Deployment-specific paths, ports, and resource limits | Not specified (TBD)                                                |

## 5. Configuration sources and completion

Project targets are sourced from `spec.md`; current build settings are sourced
from `build.gradle` and the Gradle wrapper properties. Session-specific agent
and host observations were supplied by the current execution environment or
queried from the host. Values marked TBD are not available through these
sources and must be obtained from the relevant service or host administrator
if a complete deployment configuration is required.
