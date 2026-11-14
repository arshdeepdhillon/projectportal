---
AI Contribution Weight: [100%] AI Generated / [0%] Human Refactored
---

# Project Artifact & Repository Indexer — Functional Specification

## 1. Purpose and scope

The Project Artifact & Repository Indexer is Domain 1 of Project Portal. It is the system of record for projects and the
Git repositories, academic manuscripts, and video presentations owned by those projects. Projects are associated with
academic labs, department programs, and courses.

The service provides project lifecycle operations, artifact metadata operations, manuscript file storage, and project
search. Project artifacts inherit their academic context from the parent project.

## 2. Functional model

### 2.1 Project

A project has:

- A service-generated UUID.
- A title and description.
- `createdAt` and `updatedAt` timestamps represented as UTC epoch milliseconds.
- An integer `revisionVersion`, initially `1`.
- One or more member-role entries. Each entry has an opaque string `memberId` and an open string `role`. A member may
  have multiple distinct role entries on the same project.
- One or more lab, department, and course references in total. Each association is owned by the project.
- An internal integer `deleted` flag, initially `0`; the flag is not included in API responses.

Project create and update requests include the full member and academic-reference collections. `PUT` replaces project
scalar fields and those complete collections, and leaves artifacts unchanged.

`revisionVersion` advances only when the project title or description actually changes. An identical update or a change
only to members or academic references does not advance the revision or change `updatedAt`. Soft-deleting a project
changes `updatedAt` but does not advance `revisionVersion`.

Deleting a project sets `deleted` to `1`. Normal project reads and search exclude deleted projects immediately. There is
no project restore operation. The internal purge operation permanently removes deleted projects and their owned data.

### 2.2 Academic reference records

Labs and departments have `id`, `name`, and `code`. Courses have `id`, `courseCode`, and `courseName`. Their identifiers
are UUIDs. The service exposes read-only list endpoints for these records; every list is an unpaginated JSON array.
Production records are provisioned by deployment. Example records are loaded only when `dev` or `test` is explicitly
active.

### 2.3 Artifacts

An artifact has a service-generated UUID, an `artifactType`, a parent `projectId`, and an internal integer `deleted`
flag, initially `0`. The flag is not included in API responses. Artifact responses contain type-specific fields and do
not add shared title, description, or timestamp fields.

Artifact type path tokens are `repositories`, `manuscripts`, and `videos`.

#### Git repository

- `repoUrl`
- `targetBranch`
- `primaryTechStack` (one string)
- `totalCommitCount`
- `latestCommitTimestamp` (UTC epoch milliseconds)

#### Manuscript

- `title`
- `abstract`
- `authors` (list of names)
- `keywords` (list of strings)
- `year`
- Optional `publisher` object with `name` and `venue`
- Optional current file metadata containing the SHA-256 hash, original filename, and media type

A manuscript has at most one current file. File metadata is stored in a separate one-to-one database record, with its
binary content in a separate associated record. Uploading another file replaces the current file and its hash. Deleting
a manuscript file removes both records while retaining the manuscript and its bibliographic metadata.

#### Video presentation

- `videoUrl`
- `thumbnailUrl`
- `durationMillis`
- `description`

Deleting an artifact sets its `deleted` flag to `1`. Normal artifact lists, individual reads, project search, and
manuscript ZIP downloads exclude deleted artifacts. There is no artifact restore operation. Deleted artifacts are
physically removed when their parent project is purged.

## 3. REST API

All timestamps are UTC epoch milliseconds. Successful reads return `200 OK`. Create operations return `201 Created`,
the created representation, and a `Location` header. Updates return `200 OK` with the updated representation. Delete
operations return `204 No Content`.

| Method | Endpoint | Behavior |
|:--|:--|:--|
| `GET` | `/api/v1/projects` | Search and list active projects using query parameters. Returns a JSON array. |
| `POST` | `/api/v1/projects` | Create a project. |
| `GET` | `/api/v1/projects/{project_id}` | Read one active project. |
| `PUT` | `/api/v1/projects/{project_id}` | Replace project scalar fields, members, and academic references. |
| `DELETE` | `/api/v1/projects/{project_id}` | Soft-delete a project. |
| `POST` | `/api/v1/projects/purge` | Internal maintenance operation: permanently remove deleted projects and their owned data. Returns `200 OK` with `{"purgedProjectCount": 0}`. |
| `GET` | `/api/v1/projects/{project_id}/artifacts` | List active artifacts for a project as a JSON array. |
| `GET` | `/api/v1/projects/{project_id}/artifacts/{artifact_type}` | List active artifacts of one type as a JSON array. |
| `POST` | `/api/v1/projects/{project_id}/artifacts/{artifact_type}` | Create an artifact of the path type. |
| `GET` | `/api/v1/projects/{project_id}/artifacts/{artifact_type}/{artifact_id}` | Read one active artifact. |
| `PUT` | `/api/v1/projects/{project_id}/artifacts/{artifact_type}/{artifact_id}` | Replace the artifact’s type-specific fields. |
| `DELETE` | `/api/v1/projects/{project_id}/artifacts/{artifact_type}/{artifact_id}` | Soft-delete an artifact. |
| `POST` | `/api/v1/projects/{project_id}/artifacts/manuscripts/{artifact_id}/file` | Upload one manuscript file as multipart form data in the `file` part. Returns `201 Created` for the first file and `200 OK` when replacing it, with current file metadata. |
| `GET` | `/api/v1/projects/{project_id}/artifacts/manuscripts/{artifact_id}/file` | Download the current manuscript file using its stored media type and original filename. |
| `DELETE` | `/api/v1/projects/{project_id}/artifacts/manuscripts/{artifact_id}/file` | Remove the current file and its metadata; retain the manuscript. |
| `GET` | `/api/v1/projects/{project_id}/artifacts/manuscripts/download` | Download a ZIP of current files belonging to active manuscripts. |
| `GET` | `/api/v1/labs` | List labs as a JSON array. |
| `GET` | `/api/v1/departments` | List departments as a JSON array. |
| `GET` | `/api/v1/courses` | List courses as a JSON array. |

ZIP entries use `<artifact-UUID>/<original filename>` to keep entries distinct when filenames match. Manuscripts without
a current file are omitted.

### 3.1 Project search parameters

`GET /api/v1/projects` accepts these optional query parameters:

| Parameter | Meaning |
|:--|:--|
| `keyword` | Case-insensitive substring search across project text and artifact metadata. |
| `labId` | Match projects associated with this lab UUID. |
| `departmentId` | Match projects associated with this department UUID. |
| `courseId` | Match projects associated with this course UUID. |
| `artifactType` | Match projects containing at least one active artifact of the selected type: `repositories`, `manuscripts`, or `videos`. |
| `timeRange.createdFrom` | Inclusive lower bound on project `createdAt`, as UTC epoch milliseconds. |
| `timeRange.createdTo` | Inclusive upper bound on project `createdAt`, as UTC epoch milliseconds. |

Each time bound is independently optional. When multiple filter categories are supplied, a project must match every
supplied category. Keyword search considers active artifacts only. Project search is unpaginated.

## 4. API representations

### 4.1 Project representation

```json
{
  "id": "77777777-7777-7777-7777-777777777777",
  "title": "Project title",
  "description": "Project description",
  "createdAt": 1794594000000,
  "updatedAt": 1794594000000,
  "revisionVersion": 1,
  "members": [
    { "memberId": "member-001", "role": "student" },
    { "memberId": "member-001", "role": "team lead" }
  ],
  "labIds": ["11111111-1111-1111-1111-111111111111"],
  "departmentIds": ["33333333-3333-3333-3333-333333333333"],
  "courseIds": ["55555555-5555-5555-5555-555555555555"]
}
```

Project representations do not embed artifacts. Artifact representations include `id`, `projectId`, and
`artifactType`, plus their type-specific fields.

### 4.2 Purge response

```json
{
  "purgedProjectCount": 3
}
```

## 5. Persistence and runtime

The service uses Spring Data JPA with a relational database and Flyway-managed schema migrations. Manuscript file
records store the file bytes, SHA-256 hash, original filename, and media type separately from manuscript metadata.

The `dev` profile uses the local file-backed H2 database. The `postgres` profile is used by Docker Compose. No profile
is active by default; `dev` and `test` must be selected explicitly for their example reference data.
