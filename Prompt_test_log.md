---
 AI Contribution Weight: [ 99% ] AI Generated / [1%] Human Refactored
---

### Prompt 1 **Spring Boot Catalog Specification Design**

[https\://gemini.google.com/app/047b51e202033919](https://gemini.google.com/app/047b51e202033919)

*User prompt: Draft a comprehensive technical functional specification (spec.md) for a Spring Boot 3 / Java 17 backend
service. It must include \- REST endpoints \- JSON schemas \- JPA entity models with relationships \- Static mock data
structure adhering to enterprise standards To make the spec.md focused, the service is a backend micro-service designed
to catalog public Git repository profiles, academic manuscript documents, and video presentation metadata. It links
these artifacts directly to academic labs, department programs, and course affiliations. Before implementing the
spec.md, drill down on questions until an understanding is found and the feture/design is clarified. If an answer an be
derived or is found from previous answers or from spec.md, use that to derive your question instead of repeating
yourself.*

Response: To ensure the specification addresses your precise structural and domain requirements, let's clarify a few
architectural and domain choices:

> 1. **Cataloging Scope & Metadata Ingestion**

* Are the repository profiles (Git), manuscripts (PDF/DOI metadata), and video presentation metadata ingested via
  external APIs (e.g., GitHub REST API, Crossref/ORCID, YouTube API) synchronously via integration endpoints, or is this
  service purely a storage/catalog layer managing state passed to it by upstream producers?

> 2. **Artifact-to-Entity Relationships**

* Can a single artifact (e.g., a Git repository or manuscript) be associated with multiple academic labs, departments,
  or courses simultaneously, or is a 1-to-N (one lab/course owns many artifacts) relationship sufficient?

> 3. **Data Access & Querying Patterns**

* Do you require advanced filtering endpoints (e.g., filtering manuscripts by publication date, Git repos by language,
  or artifacts tagged with specific course IDs) alongside basic CRUD operations? Should we include paginated responses
  (page, size, sort) as part of the standard response JSON schemas?

> 4. **Persistence & Mock Strategy**

* Are there specific database preferences for the JPA mappings (e.g., PostgreSQL JSONB columns for flexible metadata vs.
  standard relational mapping for all sub-attributes)?
* For the static mock data structure, should we provide a standalone JSON fixture file / Spring @Profile ("dev")
  database seeder configuration alongside the schema definitions?

---

*User prompt: For \#1 For external data dependencies, they must be managed using local static mock data structures. For
\#2 Yes. Single artifact can be associated with multiple academic labs, departments, or courses. For \#3 Yes, we require
advanced filtering endpoints. And we should include paginated responses as part of the standard response JSON schemas.
For \#4 Lets use standard relations mapping for now to keep it simple. For static mock, yes use a JSON fixture file.*

Response: I will now generate the comprehensive technical functional specification (spec.md) based on your requirements,
complete with Java 17 / Spring Boot 3 JPA entities, standardized REST endpoints, JSON schemas, and enterprise static
mock data fixtures.

```
# Technical Functional Specification: Academic Artifact Catalog Service

## 1. Overview & Scope

The **Academic Artifact Catalog Service** is a Spring Boot 3 (Java 17) microservice responsible for indexing, categorizing, and serving metadata for three distinct types of academic artifacts:
1. **Git Repository Profiles** (code repositories, open-source tools, scripts)
2. **Academic Manuscript Documents** (papers, preprints, technical reports)
3. **Video Presentation Metadata** (lectures, talk recordings, conference presentations)

Each artifact can be dynamically associated across multiple academic organizational contexts:
- **Academic Labs** (e.g., Vision & Learning Lab)
- **Department Programs** (e.g., Department of Computer Science)
- **Course Affiliations** (e.g., CS-701 Deep Learning)

---

## 2. System Architecture & Tech Stack

- **JDK Version:** Java 17 LTS
- **Framework:** Spring Boot 3.2+
- **Persistence:** Spring Data JPA / Hibernate 6 (Relational Database)
- **Data Validation:** Jakarta Bean Validation (`jakarta.validation`)
- **JSON Processing:** Jackson 2.x
- **Data Seed Strategy:** Local JSON fixture files loaded on `@Profile("dev")` or `@Profile("test")` startup.

---

## 3. Entity Data Model (JPA Mappings)

### 3.1 Entity Relationship Diagram (Logical)
- `Artifact` (Abstract Base Class / Single Table Inheritance)
  - `GitRepositoryArtifact`
  - `ManuscriptArtifact`
  - `VideoPresentationArtifact`
- `AcademicLab`
- `DepartmentProgram`
- `CourseAffiliation`

Many-to-Many associations exist between `Artifact` and each of the three academic context entities using explicit join tables.

### 3.2 Core JPA Entity Definitions

#### Base Class: `Artifact.java`
```java
package com.academic.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "artifacts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "artifact_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class Artifact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToMany
    @JoinTable(
        name = "artifact_lab_associations",
        joinColumns = @JoinColumn(name = "artifact_id"),
        inverseJoinColumns = @JoinColumn(name = "lab_id")
    )
    private Set<AcademicLab> labs = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "artifact_department_associations",
        joinColumns = @JoinColumn(name = "artifact_id"),
        inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<DepartmentProgram> departments = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "artifact_course_associations",
        joinColumns = @JoinColumn(name = "artifact_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<CourseAffiliation> courses = new HashSet<>();
}
```

#### Subclass: `GitRepositoryArtifact.java`
```java
package com.academic.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("GIT_REPOSITORY")
@Getter
@Setter
public class GitRepositoryArtifact extends Artifact {

    @Column(name = "repo_url", nullable = false)
    private String repoUrl;

    @Column(name = "primary_language")
    private String primaryLanguage;

    @Column(name = "star_count")
    private Integer starCount = 0;

    @Column(name = "fork_count")
    private Integer forkCount = 0;

    @Column(name = "license_name")
    private String licenseName;

    @Column(name = "default_branch")
    private String defaultBranch = "main";
}
```

#### Subclass: `ManuscriptArtifact.java`
```java
package com.academic.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MANUSCRIPT")
@Getter
@Setter
public class ManuscriptArtifact extends Artifact {

    @Column(name = "doi", unique = true)
    private String doi;

    @Column(name = "publication_venue")
    private String publicationVenue;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "document_pdf_url")
    private String documentPdfUrl;

    @Column(name = "citation_count")
    private Integer citationCount = 0;
}
```

#### Subclass: `VideoPresentationArtifact.java`
```java
package com.academic.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("VIDEO_PRESENTATION")
@Getter
@Setter
public class VideoPresentationArtifact extends Artifact {

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "platform_name")
    private String platformName; // e.g., YouTube, Vimeo, Institutional LMS

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}
```

#### Association Entities

```java
package com.academic.catalog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "academic_labs")
@Getter
@Setter
public class AcademicLab {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;
}

@Entity
@Table(name = "department_programs")
@Getter
@Setter
public class DepartmentProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;
}

@Entity
@Table(name = "course_affiliations")
@Getter
@Setter
public class CourseAffiliation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;

    @Column(name = "course_name", nullable = false)
    private String courseName;
}
```

---

## 4. REST Endpoints Specification

### 4.1 Endpoint Matrix

| Method | Endpoint | Description | Query Parameters / Filters |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/artifacts` | Search & list all artifacts (paginated) | `type`, `labId`, `departmentId`, `courseId`, `search`, `page`, `size`, `sort` |
| `GET` | `/api/v1/artifacts/{id}` | Fetch a single artifact by UUID | N/A |
| `POST` | `/api/v1/artifacts` | Create a new artifact profile | N/A |
| `PUT` | `/api/v1/artifacts/{id}` | Update an existing artifact | N/A |
| `DELETE` | `/api/v1/artifacts/{id}` | Delete an artifact by UUID | N/A |
| `GET` | `/api/v1/labs` | List academic labs | `page`, `size`, `sort` |
| `GET` | `/api/v1/departments` | List department programs | `page`, `size`, `sort` |
| `GET` | `/api/v1/courses` | List course affiliations | `page`, `size`, `sort` |

---

## 5. JSON Schemas

### 5.1 Standard Paginated Response Envelope Schema (`paginated-response.json`)
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "PaginatedResponse",
  "type": "object",
  "required": ["content", "page", "size", "totalElements", "totalPages", "last"],
  "properties": {
    "content": {
      "type": "array",
      "items": { "type": "object" }
    },
    "page": { "type": "integer", "minimum": 0 },
    "size": { "type": "integer", "minimum": 1 },
    "totalElements": { "type": "integer", "minimum": 0 },
    "totalPages": { "type": "integer", "minimum": 0 },
    "last": { "type": "boolean" }
  }
}
```

### 5.2 Artifact Request/Response Payload Schema (`artifact-dto.json`)
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "ArtifactDTO",
  "type": "object",
  "required": ["id", "title", "artifactType", "createdAt", "updatedAt"],
  "properties": {
    "id": { "type": "string", "format": "uuid" },
    "title": { "type": "string", "maxLength": 255 },
    "description": { "type": ["string", "null"] },
    "artifactType": {
      "type": "string",
      "enum": ["GIT_REPOSITORY", "MANUSCRIPT", "VIDEO_PRESENTATION"]
    },
    "createdAt": { "type": "string", "format": "date-time" },
    "updatedAt": { "type": "string", "format": "date-time" },
    "gitDetails": {
      "type": ["object", "null"],
      "properties": {
        "repoUrl": { "type": "string", "format": "uri" },
        "primaryLanguage": { "type": "string" },
        "starCount": { "type": "integer" },
        "forkCount": { "type": "integer" },
        "licenseName": { "type": "string" },
        "defaultBranch": { "type": "string" }
      }
    },
    "manuscriptDetails": {
      "type": ["object", "null"],
      "properties": {
        "doi": { "type": "string" },
        "publicationVenue": { "type": "string" },
        "publicationDate": { "type": "string", "format": "date" },
        "documentPdfUrl": { "type": "string", "format": "uri" },
        "citationCount": { "type": "integer" }
      }
    },
    "videoDetails": {
      "type": ["object", "null"],
      "properties": {
        "videoUrl": { "type": "string", "format": "uri" },
        "platformName": { "type": "string" },
        "durationSeconds": { "type": "integer" },
        "thumbnailUrl": { "type": "string", "format": "uri" }
      }
    },
    "labIds": {
      "type": "array",
      "items": { "type": "string", "format": "uuid" }
    },
    "departmentIds": {
      "type": "array",
      "items": { "type": "string", "format": "uuid" }
    },
    "courseIds": {
      "type": "array",
      "items": { "type": "string", "format": "uuid" }
    }
  }
}
```

---

## 6. Static Enterprise Mock Data Structure

Below is the local JSON seed data fixture structure (`src/main/resources/mocks/artifacts-fixture.json`), utilized during integration testing and developer static mocking mode.

```json
{
  "labs": [
    {
      "id": "11111111-1111-1111-1111-111111111111",
      "name": "Vision & Autonomous Systems Lab",
      "code": "VASL-01"
    },
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "name": "Quantum Computing & Information Group",
      "code": "QCIG-02"
    }
  ],
  "departments": [
    {
      "id": "33333333-3333-3333-3333-333333333333",
      "name": "Department of Computer Science",
      "code": "CS"
    },
    {
      "id": "44444444-4444-4444-4444-444444444444",
      "name": "Department of Electrical Engineering",
      "code": "EE"
    }
  ],
  "courses": [
    {
      "id": "55555555-5555-5555-5555-555555555555",
      "courseCode": "CS-701",
      "courseName": "Advanced Deep Learning Frameworks"
    },
    {
      "id": "66666666-6666-6666-6666-666666666666",
      "courseCode": "EE-502",
      "courseName": "Embedded Robotics and Perception"
    }
  ],
  "artifacts": [
    {
      "id": "77777777-7777-7777-7777-777777777777",
      "title": "TorchPerception: Real-time Object Detection Toolkit",
      "description": "An open-source library for low-latency visual perception models deployed on edge devices.",
      "artifactType": "GIT_REPOSITORY",
      "createdAt": "2026-01-15T10:30:00Z",
      "updatedAt": "2026-02-01T14:20:00Z",
      "gitDetails": {
        "repoUrl": "https://github.com/vasl-lab/torch-perception",
        "primaryLanguage": "Python",
        "starCount": 1420,
        "forkCount": 230,
        "licenseName": "Apache-2.0",
        "defaultBranch": "main"
      },
      "labIds": [
        "11111111-1111-1111-1111-111111111111"
      ],
      "departmentIds": [
        "33333333-3333-3333-3333-333333333333",
        "44444444-4444-4444-4444-444444444444"
      ],
      "courseIds": [
        "55555555-5555-5555-5555-555555555555",
        "66666666-6666-6666-6666-666666666666"
      ]
    },
    {
      "id": "88888888-8888-8888-8888-888888888888",
      "title": "Sub-millisecond Transformer Inference on Embedded GPUs",
      "description": "Peer-reviewed manuscript evaluating specialized quantization strategies for edge computer vision.",
      "artifactType": "MANUSCRIPT",
      "createdAt": "2026-02-10T08:00:00Z",
      "updatedAt": "2026-02-10T08:00:00Z",
      "manuscriptDetails": {
        "doi": "10.1145/3318464.3389700",
        "publicationVenue": "IEEE International Conference on Computer Vision (ICCV)",
        "publicationDate": "2026-02-01",
        "documentPdfUrl": "https://vasl.institution.edu/papers/sub-ms-transformer.pdf",
        "citationCount": 42
      },
      "labIds": [
        "11111111-1111-1111-1111-111111111111"
      ],
      "departmentIds": [
        "33333333-3333-3333-3333-333333333333"
      ],
      "courseIds": [
        "55555555-5555-5555-5555-555555555555"
      ]
    },
    {
      "id": "99999999-9999-9999-9999-999999999999",
      "title": "Keynote: Scalable Quantum Error Correction Algorithms",
      "description": "Video recording of the annual symposium presentation on fault-tolerant quantum computing architectures.",
      "artifactType": "VIDEO_PRESENTATION",
      "createdAt": "2026-02-18T16:45:00Z",
      "updatedAt": "2026-02-18T16:45:00Z",
      "videoDetails": {
        "videoUrl": "https://youtube.com/watch?v=mock_video_id_123",
        "platformName": "YouTube",
        "durationSeconds": 3240,
        "thumbnailUrl": "https://img.youtube.com/vi/mock_video_id_123/hqdefault.jpg"
      },
      "labIds": [
        "22222222-2222-2222-2222-222222222222"
      ],
      "departmentIds": [
        "33333333-3333-3333-3333-333333333333"
      ],
      "courseIds": []
    }
  ]
}
```
```

I have generated the comprehensive functional specification document spec.md for your Spring Boot 3 / Java 17 service.

### **Summary of Included Artifact Components:**

> 1. **JPA Entity Models:** Single-table inheritance strategy for Artifact with explicit subclasses for Git
     Repositories, Manuscripts, and Video Presentations, connected via standard @ManyToMany relational join tables to
     AcademicLab, DepartmentProgram, and CourseAffiliation.
> 2. **REST Endpoint API Matrix:** Comprehensive CRUD and query operations supporting pagination parameters (page, size,
     sort) and dynamic filter predicates (labId, departmentId, courseId, type, search).
> 3. **JSON Schemas:** Schemas for paginated response envelopes and polymorphic artifact payload DTOs.
> 4. **Static Enterprise Mock Fixtures:** Complete JSON mock data fixture demonstrating multi-associations across labs,
     departments, and courses for all three artifact types.

Would you like to move on to drafting the Spring Data JPA Repository query interfaces or the Service layer logic for
dynamic filtering next?

---


