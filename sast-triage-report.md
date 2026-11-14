> **AI Deliverable Metadata**
> Conceptual contribution: **0% AI-assisted / 100% human-directed**,

# Assignment 2 SAST Triage Report

## 1. Summary

| Classification                                      | Count  | Criteria & Disposition                                                                                                                                                                                                                                  |
|:----------------------------------------------------|:-------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **True Positives - Source-Verified & Reachable**    | **31** | **26** reachable dependency CVEs actively invoked in `ArtifactController`, `AcademicUnitFixtureLoader`, embedded Tomcat, and PostgreSQL JDBC, plus **5** concrete application-level vulnerabilities verified directly in `src/main` and `build.gradle`. |
| **Needs Verification - Undetermined / Gate**        | **48** | **47** Semgrep Supply Chain findings in runtime classpath libraries (`spring-boot-autoconfigure`, `spring-webmvc`, `logback`) requiring runtime configuration verification, plus **1** SonarQube Quality Gate failure (`QUALITY GATE STATUS: FAILED`).  |
| **False Positives - Unreachable / Non-Exploitable** | **65** | Vulnerable library methods (`spring-expression` SpEL flaws, `tomcat-embed-core` CGI/AJP/FileStore bugs) present in `gradle.lockfile` whose vulnerable execution paths are absent from `src/main`.                                                       |

## Part 1: True Positives - Multi-Layered Vulnerability Validation

Inspecting the actual source code (`build.gradle`, `ArtifactController.java`, `ArtifactService.java`,
`ManuscriptFileData.java`, `ProjectService.java`, and `V2__create_project_artifact_catalog.sql`) reveals **five distinct
vulnerabilities** spanning **Input/Deserialization, Authorization/Access Control, Database/Persistence, State Machine,
and Transport/Driver** layers.

### 1. Authorization & Access Control Layer - Complete Absence of Authentication & Broken Object-Level Authorization

* **Source Evidence:**
    * `build.gradle` (Lines 27?38): `spring-boot-starter-security` is completely absent from `dependencies`.
    * `src/main/java/com/bu/service/repositoryindexer/controller/ProjectController.java`
    * `src/main/java/com/bu/service/repositoryindexer/controller/ArtifactController.java`
    * `src/main/java/com/bu/service/repositoryindexer/api/ProjectApi.java` & `ArtifactApi.java`
* **Software Layer:** Authorization & Access Control Layer
* **CWE Mapping:** **CWE-306** (*Missing Authentication for Critical Function*) & **CWE-639** (*Authorization Bypass
  Through User-Controlled Key*)
* **Technical Root Cause:**
    * Despite `spec.md` defining role-based access rules (`faculty`, `student`, `administrator`) and `ProjectMember`
      entities tracking ownership (`ProjectRole`), the application has zero authentication or authorization middleware.
      Every REST endpoint-including `POST /api/projects`, `DELETE /api/projects/{id}`,
      `POST /api/projects/{projectId}/artifacts`, and `DELETE /api/projects/{projectId}/artifacts/{artifactId}` is
      publicly accessible without credentials, allowing any unauthenticated caller to modify, or delete arbitrary
      projects and manuscript artifacts.
* **Remediation:** Add `org.springframework.boot:spring-boot-starter-security`, configure a `SecurityFilterChain`
  enforcing authentication on `/api/**`, and implement `@PreAuthorize` / service-layer ownership checks against
  `ProjectMember` records.

### 2. Input & Deserialization Layer - Unbounded Multipart File Upload & Reachable Jackson Resource Exhaustion

* **Source Evidence:**
    * `src/main/resources/application.properties`: No `spring.servlet.multipart.max-file-size` or `max-request-size`
      overrides configured (or unbounded handling in controller).
    * `src/main/java/com/bu/service/repositoryindexer/controller/ArtifactController.java` (Lines 45, 76): Direct
      invocation of `ObjectMapper.readValue(payload, ...)` on raw `@RequestPart("payload") String payload` inputs
      alongside `@RequestPart("file") MultipartFile file`.
    * `src/main/java/com/bu/service/repositoryindexer/featureservice/ArtifactService.java`: Calls `file.getBytes()`
      directly into an in-memory `byte[]` array without streaming or size bounds.
    * **Reachable CVEs:** `CVE-2026-68497`, `CVE-2026-89425`, `CVE-2026-68498` (`jackson-databind:2.17.2` &
      `jackson-core:2.17.2`).
* **Software Layer:** Input Validation & Deserialization Layer
* **CWE Mapping:** **CWE-400** (*Uncontrolled Resource Consumption*) & **CWE-434** (*Unrestricted Upload of File with
  Dangerous Type*)
* **Technical Root Cause:**
    * `ArtifactController` manually parses raw JSON strings via `objectMapper.readValue(...)` (bypassing Spring's
      standard `HttpMessageConverter` controls and triggering Semgrep's reachable path into Jackson `2.17.2` parser
      exhaustion flaws) while simultaneously buffering uploaded `MultipartFile` contents entirely into JVM heap memory (
      `file.getBytes()`) without enforcing strict MIME-type allowlists or file size caps.
* **Remediation:**
    1. Upgrade Spring Boot to `3.5.15+` (bumping Jackson to `2.18.11+`).
    2. Enforce explicit `spring.servlet.multipart.max-file-size` and validate file magic bytes/MIME types in
       `ArtifactService` before allocating `byte[]` buffers.

### 3. Database & Persistence Layer - Unbounded Binary Large Object (BLOB) Storage & Unauthenticated Database Defaults

* **Source Evidence:**
    * `src/main/java/com/bu/service/repositoryindexer/entity/ManuscriptFileData.java`
    * `src/main/resources/db/migration/V2__create_project_artifact_catalog.sql`
    * `src/main/resources/application.properties` & `docker-compose.yml`
    * **Reachable CVE:** `CVE-2026-42198` (`org.postgresql:postgresql:42.7.4`).
* **Software Layer:** Database & Persistence Layer
* **CWE Mapping:** **CWE-770** (*Allocation of Resources Without Limits or Throttling*) & **CWE-798** (*Use of
  Hard-coded Credentials*)
* **Technical Root Cause:**
    * `ManuscriptFileData` persists raw manuscript binaries directly inside a PostgreSQL `BYTEA` column (
      `content BYTEA NOT NULL` in `V2__create_project_artifact_catalog.sql`), loading full file payloads into JPA
      persistence context memory on every read/write transaction. Combined with hardcoded fallback credentials in
      `application.properties` and `docker-compose.yml`'s unbounded iteration loop (`CVE-2026-42198`), database
      connections are vulnerable to authentication DoS.
* **Remediation:** Upgrade `org.postgresql:postgresql` to `42.7.11+`, externalize all database credentials via secret
  injection, and offload manuscript binaries to object storage.

### 4. Business Logic & State Machine Layer - Non-Atomic Check-Then-Act & Unenforced Artifact Lifecycle Transitions

* **Source Evidence:**
    * `src/main/java/com/bu/service/repositoryindexer/featureservice/ArtifactService.java`
    * `src/main/java/com/bu/service/repositoryindexer/featureservice/ProjectService.java`
    * `src/main/java/com/bu/service/repositoryindexer/entity/Artifact.java` & `ManuscriptFile.java`
* **Software Layer:** Application State Machine & Concurrency Layer
* **CWE Mapping:** **CWE-362** (*Concurrent Execution using Shared Resource with Improper Synchronization / Race
  Condition*) & **CWE-841** (*Improper Enforcement of Behavioral Workflow*)
* **Technical Root Cause:**
    * In `ArtifactService` and `ProjectService`, parent-child existence and state checks (verifying `Project` existence,
      checking duplicate artifact constraints, or replacing `ManuscriptFile` attachments) execute as non-atomic
      `findById` -> mutate -> `save` sequences without optimistic locking (whih is absent around `Project`,
      `Artifact`, and `ManuscriptFile` entities). Concurrent `PUT`/`POST`/`DELETE` requests against the same `projectId`
      or `artifactId` can interleave, causing lost updates, orphaned `ManuscriptFileData` rows, or inconsistent catalog
      state transitions.
* **Remediation:** Add sync guards around `Project`, `Artifact`, and `ManuscriptFile` entities and enforce explicit
  state-transition guards within `@Transactional` service boundaries.

### 5. Web Transport & Servlet Container Layer - HTTP/2 Stream Reset Exhaustion & TOCTOU Filesystem Race

* **Source Evidence:**
    * `build.gradle`: `org.springframework.boot` version `3.3.5` resolves
      `org.apache.tomcat.embed:tomcat-embed-core:10.1.31` in `gradle.lockfile`.
    * **Reachable CVEs:** `CVE-2025-48989`, `CVE-2026-55957`, `CVE-2026-24880`, `CVE-2024-56337`.
* **Software Layer:** Network / HTTP Transport & Servlet Container Layer
* **CWE Mapping:** **CWE-444** (*Inconsistent Interpretation of HTTP Requests*) & **CWE-367** (*Time-of-check
  Time-of-use (TOCTOU) Race Condition*)
* **Technical Root Cause:**
    * Embedded Tomcat `10.1.31` handles all ingress HTTP traffic for `projectportal`. It lacks proper throttling on
      HTTP/2 `RST_STREAM` frames and contains an incomplete `DefaultServlet` TOCTOU fix on case-insensitive filesystems.
* **Remediation:** Upgrade `org.springframework.boot` to `3.5.15+` to pull `tomcat-embed-core:10.1.52+`.

## Part 2: False Positives & Non-Exploitable Findings Ledger

65 Semgrep Supply Chain findings** are **False Positives (Unreachable in Context)**:

| Dependency & Version                                              | Representative CVEs                        | Severity            | CWE               | Source-Verified Root Cause for False Positive Disposition                                                                                                             |
|:------------------------------------------------------------------|:-------------------------------------------|:--------------------|:------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `org.springframework:spring-expression` (`6.1.14`)                | `CVE-2025-41249` (and 4 related SpEL CVEs) | `CRITICAL` / `HIGH` | CWE-917           | `SpelExpressionParser` is never instantiated and no user input is passed to `@Value` or SpEL evaluation contexts.                                                     |
| `org.springframework:spring-webmvc` (`6.1.14`)                    | `CVE-2025-41242`, `CVE-2025-22233`         | `CRITICAL` / `HIGH` | CWE-22 / CWE-284  | Standard `@RestController` mappings (`/api/projects`, `/api/academic-units`) are used without custom `RouterFunctions` or static `ResourceHttpRequestHandler` mounts. |
| `org.apache.tomcat.embed:tomcat-embed-core` (`10.1.31`)           | `CVE-2025-24813`, `CVE-2024-50379`         | `CRITICAL`          | CWE-502 / CWE-367 | `application.properties` does not configure Tomcat `FileStore` session persistence or enable partial HTTP `PUT` writes on `DefaultServlet`.                           |
| `org.springframework.boot:spring-boot-actuator` / transitive libs | Various transitive findings                | `MEDIUM` / `HIGH`   | CWE-200           | Vulnerable optional integrations (e.g., AJP connector, WebDAV servlet, JMX exporters) are absent from `application.properties` and `build.gradle`.                    |

## Unified Remediation Plan

1. **Upgrade Platform Dependencies (`build.gradle`):**
   Bump `org.springframework.boot` from `3.3.5` to `3.5.15+` and regenerate locks, eliminating all **26 reachable CVEs**
   and **47 undetermined alerts** across Tomcat, Jackson, Spring Framework, Logback, and PostgreSQL JDBC.
2. **Implement Authentication & Ownership Checks:**
   Add `spring-boot-starter-security` and enforce role/membership authorization in `ProjectService` and
   `ArtifactService`.
3. **Harden Input Bounds & Entity Concurrency:**
   Add `spring.servlet.multipart.max-file-size` limits in `application.properties`, validate MIME types in
   `ArtifactService`, and add `@Version` optimistic locking to `Project`, `Artifact`, and `ManuscriptFile`.

| Layer / severity                            | Finding and root cause                                                                                                                                                                                                                                          | CWE                                                                                                          | Validation and impact                                                                                                                                                                                                                                                            | Recommended mitigation                                                                                                                                                                           |
|---------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| API authorization / High                    | Controllers expose project and artifact reads and mutations without an authentication filter or role/object-authorization check. For example, `ProjectController` methods directly call the services and accept mutations without establishing caller identity. | **CWE-862: Missing Authorization** (primary); CWE-306 applies where authentication is also absent.           | Confirmed by the public controller mappings and absence of Spring Security configuration or method authorization in the snapshot. An unauthenticated caller can invoke operations without project membership checks; impact depends on network exposure and data classification. | Authenticate callers and enforce project/object and role authorization on every read and mutation. Restrict purge to a separately authorized maintenance identity and audit destructive actions. |
| File export / Medium (downstream-dependent) | ZIP entries are named with `manuscript.getId() + "/" + file.getOriginalFilename()` in `ArtifactService.downloadManuscriptsZip`. The client-controlled original filename is not normalized or constrained before becoming an archive path.                       | **CWE-22: Improper Limitation of a Pathname to a Restricted Directory** (archive-entry path traversal risk). | Confirmed unsafe archive metadata. The service creates, but does not extract, the ZIP; exploitation requires a downstream recipient or tool that extracts unsafe entry paths. This is not evidence of server-side file overwrite.                                                | Generate server-side entry names or use a sanitized basename; reject separators and traversal components, normalize each entry, and test extraction safety with supported consumers.             |
| Database configuration / High (conditional) | `application.properties` supplies `projectportal-local` when `DB_PASSWORD` is unset; `docker-compose.yml` repeats the same fallback for the database and application.                                                                                           | **CWE-798: Use of Hard-coded Credentials**                                                                   | Confirmed repository-visible default. Exposure depends on a deployment omitting the override and making the database reachable; the files alone do not prove a deployed system is exposed.                                                                                       | Remove production defaults, require externally provisioned secrets, rotate any reused credential, use a least-privilege database role, and keep the database network-private.                    |

## 2. Automated dependency alerts and disposition

| Alert / group                                                                     | Disposition                                                                                                                                                         | Rationale and action                                                                                                                                                                                                                                                    |
|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Semgrep supply-chain summary: 26 reachable, 47 undetermined, 65 unreachable       | **Version/reachability alerts; per-CVE exploitability remains unconfirmed.** The 47 undetermined and 65 unreachable findings are not automatically false positives. | The archive reports these categories against `master`. Review individual advisories and runtime paths; upgrade affected dependencies or document a justified VEX/non-applicability decision.                                                                            |
| Apache Tomcat `CVE-2024-56337`, reported reachable at `tomcat-embed-core:10.1.31` | **Dependency version match; exploitability conditional, not a false positive.**                                                                                     | The log describes preconditions including a case-insensitive filesystem and default-servlet write configuration. The finding should be addressed by upgrading to a fixed version; do not claim exploitability or non-exploitability solely from the reachability label. |
| Trivy lockfile (45 findings) and built JAR (43 findings) in the supplied archive  | **Scanner-positive package/version matches; individual exploitability not established here.**                                                                       | The JAR and lockfile inventories overlap. Review advisory-specific preconditions and upgrade paths; the reported counts are not 88 unique CVEs. The Ubuntu image layer had 0 findings in this run.                                                                      |
| A2 Trivy Spring MVC alert `CVE-2026-47884` (CRITICAL)                             | **Not applicable to the inspected snapshot's current view configuration; retain as a dependency alert.**                                                            | The recorded advisory review found no `XsltView` or catch-all view mapping in the application. This removes the documented application trigger, not the vulnerable component/version; reassess if view configuration changes and upgrade the dependency.                |
| A2 Trivy Spring MVC alert `CVE-2026-41842` (HIGH)                                 | **Untriaged from the available excerpt.**                                                                                                                           | The evidence appendix does not include enough advisory detail to establish applicability. Do not mark this alert as a false positive without reviewing its vendor conditions.                                                                                           |
| SonarQube on `master`: quality gate failed                                        | **Pipeline result, not a vulnerability disposition.**                                                                                                               | The supplied log confirms analysis ran and the gate failed, but does not show the failing condition or issue list. Review the SonarCloud analysis before assigning issue-level true/false-positive labels. No Sonar result is available for the A2 branch.              |

