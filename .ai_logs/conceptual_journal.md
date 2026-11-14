# MASTER CONCEPTUAL JOURNAL & AI TELEMETRY LOG

* **Student Name:** Arshdeep Dhillon
* **Assigned Domain Module:** Domain 1: Project Artifact & Repository Indexer
* **Core Technology Stack:** Spring Boot

---

## SECTION 1: SYSTEM ENVIRONMENT & TOOL SPECIFICATION

### 1.1 AI Tool & Engine Deployments

| Tool Context                            | Framework / Platform Name & Version | Underlying LLM Brain Model String | Operational Setting / Access Mode |
|:----------------------------------------|:------------------------------------|:----------------------------------|:----------------------------------|
| **Code Generation**/**Document/Design** | Copilot Fusion                      | gpt-6-luna                        | IDE and CLI                       |

## SECTION 2: COGNITIVE REFLECTION - CODE GENERATION & COMPREHENSION

### 2.1 The Architectural Flow

All the requests flow through the controller layer, which is responsible for handling incoming HTTP requests. The
controller methods are mapped to specific endpoints, such as POST, GET, PUT, and DELETE, for both Project and Artifact
resources. The controller methods delegate the business logic to the service layer, which interacts with the repository
layer to perform CRUD operations on the database. The service layer also handles error handling before returning the
response to the controller. The repository layer is responsible for interacting with the database, using JPA
repositories to perform the actual data access operations. The overall architecture follow Controller-Service-Repository
pattern prevalent in Spring applications, with clear separation of concerns between the different layers. All the Spring
managed beans use Singleton scope by default, ensuring that only one instance of each bean is created and shared across
the application. All the endpoints are designed to handle JSON payloads, and the application uses standard HTTP status
codes to indicate the success or failure of the operations. As for repository design, the AI tool generated JPA
repositories, allowing for easy data access and manipulation. The repositories are annotated with @Repository, enabling
Spring to detect and manage them as beans. The repositories also include custom query methods to support specific data
retrieval requirements, such as finding artifacts by project ID or retrieving projects based on certain criteria. The AI
tool also generated entity classes annotated with @Entity to indicate that they are JPA entities. The entity classes
include fields representing the attributes of the entities, along with appropriate annotations for mapping them to
database tables. The entity classes also include relationships between the entities, such as a one-to-many relationship
between Project and Artifact, allowing for easy navigation and retrieval of related data. The AI tool also generated
DTO (Data Transfer Object) classes, which are used to transfer data between the different layers of the application. The
DTO classes include fields representing the attributes of the entities (excluding the deleted attribute), along with
appropriate annotations for mapping them to JSON payloads.

### 2.2 Defensive Edge-Case Evaluation (Line-by-Line Code Review)

*Perform a line-by-line human audit of the generated source code. Identify where the AI tool completely failed to
account for edge cases, unusual boundaries, or unexpected system properties prior to running your pipeline scanners. (
e.g., What happens if an integer balance value scales to a negative integer? How does the routing loop handle an invalid
JSON payload sequence?)*

#### ProjectController

In the ProjectController, the AI tool generated a sanitization check for the `artifactType` parameter, converting it to
an ArtifactType enum if it is not null. If an invalid `artifactType` string is passed then `IllegalArgumentException` is
thrown. But `projects#search()` method can handle null ArtifactType. Since `artifactType` of string is being
accepted,throwing`IllegalArgumentException` it an unusual boundary. It should fall back to null. If `artifactType`
argument was of type `ArtifactType` then throwing `IllegalArgumentException` would be appropriate.

```java

@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projects;

    @GetMapping
    public List<ProjectResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID labId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String artifactType,
            @RequestParam(name = "timeRange.createdFrom", required = false) Long createdFrom,
            @RequestParam(name = "timeRange.createdTo", required = false) Long createdTo
    ) {
        ArtifactType type = artifactType == null ? null : ArtifactType.fromPathToken( artifactType );
        return projects.search( keyword, labId, departmentId, courseId, type, createdFrom, createdTo );
    }
}
```

---

In ProjectService, the AI tool generated a search method that filters projects based on various criteria. However, it
does not handle the case where `createdFrom` is greater than `createdTo`, which could lead to unexpected results.

```java
    private boolean matchesTimeRange( Project project, Long createdFrom, Long createdTo ) {
    return (createdFrom == null || project.getCreatedAt() >= createdFrom)
            && (createdTo == null || project.getCreatedAt() <= createdTo);
}
```

---
In ProjectService, `projects.findAllByDeleted( ACTIVE ).stream()` retrieves all active projects and filters them based
on the provided criteria. However, this approach may lead to performance issues if there are a large number of projects,
as it loads all active projects into memory before filtering. A more efficient approach would be to perform the
filtering directly in the database query.

```java
  public List<ProjectResponse> search(
        String keyword,
        UUID labId,
        UUID departmentId,
        UUID courseId,
        ArtifactType artifactType,
        Long createdFrom,
        Long createdTo
) {
    String normalizedKeyword = keyword == null ? null : keyword.toLowerCase( Locale.ROOT );
    return projects.findAllByDeleted( ACTIVE ).stream()
            .filter( project -> matchesTimeRange( project, createdFrom, createdTo ) )
            .filter( project -> matchesUnit( project, labId, AcademicUnit.Type.LAB ) )
            .filter( project -> matchesUnit( project, departmentId, AcademicUnit.Type.DEPARTMENT ) )
            .filter( project -> matchesUnit( project, courseId, AcademicUnit.Type.COURSE ) )
            .filter( project -> artifactType == null || hasActiveArtifactType( project, artifactType ) )
            .filter( project -> normalizedKeyword == null || matchesKeyword( project, normalizedKeyword ) )
            .map( this::toResponse )
            .toList();
}
```

---

In ProjectService, the AI tool generated a method to check if an artifact matches a given keyword. However, not all
fields should be checked for keyword matches, such as:

* `artifact.typePathToken()`
* `artifact.getTotalCommitCount()`
* `artifact.getRepoUrl()`
* `artifact.getTotalCommitCount()`
* `artifact.getVideoUrl()`
* `artifact.getThumbnailUrl()`
* `artifact.getDurationMillis()`
* `artifact.getManuscriptFile().getSha256()`

```java
 private boolean matchesArtifactKeyword( Artifact artifact, String keyword ) {
    return contains( artifact.typePathToken(), keyword )
            || contains( artifact.getRepoUrl(), keyword )
            || contains( artifact.getTargetBranch(), keyword )
            || contains( artifact.getPrimaryTechStack(), keyword )
            || contains( artifact.getTotalCommitCount(), keyword )
            || contains( artifact.getLatestCommitTimestamp(), keyword )
            || contains( artifact.getTitle(), keyword )
            || contains( artifact.getAbstractText(), keyword )
            || contains( artifact.getYear(), keyword )
            || contains( artifact.getPublisherName(), keyword )
            || contains( artifact.getPublisherVenue(), keyword )
            || contains( artifact.getVideoUrl(), keyword )
            || contains( artifact.getThumbnailUrl(), keyword )
            || contains( artifact.getDurationMillis(), keyword )
            || contains( artifact.getDescription(), keyword )
            || artifact.getAuthors().stream().anyMatch( author -> contains( author, keyword ) )
            || artifact.getKeywords().stream().anyMatch( value -> contains( value, keyword ) )
            || artifact.getManuscriptFile() != null
            && (contains( artifact.getManuscriptFile().getSha256(), keyword )
            || contains( artifact.getManuscriptFile().getOriginalFilename(), keyword )
            || contains( artifact.getManuscriptFile().getMediaType(), keyword ));
}
```

---
In ProjectService, the AI tool generated a method to create a new project. However, it does not validate the input
fields, such as checking for null or empty values for the title and description. This could lead to the creation of
projects with invalid data.

```java

public ProjectResponse create( ProjectRequest request ) {
    long now = System.currentTimeMillis();
    Project project = new Project( request.title(), request.description(), now );
    replaceAssociations( project, request );
    return toResponse( projects.save( project ) );
}


public class Project {

    @Column(nullable = false)
    private String title;
}

```

---
In ProjectController, the AI tool generated methods without wrapping the methods in try-catch blocks to handle
exceptions that may occur during the execution of the service methods. This could lead to unhandled exceptions and
potentially expose sensitive information in the response.

```java
// eg
@PostMapping
public ResponseEntity<ProjectResponse> create( @RequestBody ProjectRequest request ) {
    ProjectResponse response = projects.create( request );
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path( "/{id}" )
            .buildAndExpand( response.id() )
            .toUri();
    return ResponseEntity.created( location ).body( response );
}
```

---
In ProjectService, the AI tool generated an update method that increments the revision version and updates the updatedAt
timestamp if the title or description has changed. However, it does not handle concurrent updates, which could lead to
race conditions and lost updates if multiple users are updating the same project simultaneously.
Additionally, it does not validate the input fields, such as checking for null or empty values for the title and
description. This could lead to the update of projects with invalid data.

```java
    public ProjectResponse update( UUID id, ProjectRequest request ) {
    Project project = projects.findByIdAndDeleted( id, ACTIVE ).orElseThrow();
    boolean scalarFieldsChanged = !Objects.equals( project.getTitle(), request.title() )
            || !Objects.equals( project.getDescription(), request.description() );
    project.setTitle( request.title() );
    project.setDescription( request.description() );
    if ( scalarFieldsChanged ) {
        project.setRevisionVersion( project.getRevisionVersion() + 1 );
        project.setUpdatedAt( Math.max( System.currentTimeMillis(), project.getUpdatedAt() + 1 ) );
    }
    replaceAssociations( project, request );
    return toResponse( projects.save( project ) );
}
```

---
In ArtifactService, when creating a new artifact, the AI tool generated
`apply( Artifact artifact, GitRepositoryRequest request )` method which does not validate
the repository URL, target branch, etc. Because of the way it designed the Artifact entity, it cannot enforce such
validation rules, thus allowing incorrect state.

```java
    public ArtifactResponse createRepository( UUID projectId, GitRepositoryRequest request ) {
    //..
    apply( artifact, request );
    //..
}

private void apply( Artifact artifact, GitRepositoryRequest request ) {
    artifact.setRepoUrl( request.repoUrl() );
    artifact.setTargetBranch( request.targetBranch() );
    artifact.setPrimaryTechStack( request.primaryTechStack() );
    artifact.setTotalCommitCount( request.totalCommitCount() );
    artifact.setLatestCommitTimestamp( request.latestCommitTimestamp() );
}
```

--- 
In ProjectService, there is no synchronization mechanism around `purgeDeleted` and "update" methods which could lead to
race conditions if a project is being updated while the purge operation is running. This could result in inconsistent
state or data loss.

---
In ArtifactService, the AI tool generated a method which simply throws a generic `NoSuchElementException` if the
artifact is not found. This could lead to unhandled exceptions and potentially expose sensitive information in the
response.

```java
    private Project activeProject( UUID projectId ) {
    return projects.findByIdAndDeleted( projectId, ACTIVE ).orElseThrow();
}
```

---
The Artifact entity is very broad because it holds information about all the supported artifact types. This could lead
to a large number of nullable fields and make it difficult to maintain the entity. It would be better to have separate
entities for each artifact type, with a common base class or interface.

```java

@Entity
@Table(name = "artifacts")
@Getter
@Setter
@NoArgsConstructor
public class Artifact {
    //...

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false)
    private ArtifactType artifactType;

    @Column(nullable = false)
    private int deleted;

    @Column(name = "repo_url", columnDefinition = "TEXT")
    private String repoUrl;

    @Column(name = "target_branch")
    private String targetBranch;

    @Column(name = "primary_tech_stack")
    private String primaryTechStack;

    @Column(name = "total_commit_count")
    private Integer totalCommitCount;

    @Column(name = "latest_commit_timestamp")
    private Long latestCommitTimestamp;

    @Column(name = "manuscript_title")
    private String title;

    @Column(name = "manuscript_abstract", columnDefinition = "TEXT")
    private String abstractText;

    @ElementCollection
    @CollectionTable(name = "manuscript_authors", joinColumns = @JoinColumn(name = "artifact_id"))
    @Column(name = "author_name")
    private List<String> authors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "manuscript_keywords", joinColumns = @JoinColumn(name = "artifact_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @Column(name = "publication_year")
    private Integer year;

    @Column(name = "publisher_name")
    private String publisherName;

    @Column(name = "publisher_venue")
    private String publisherVenue;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;
//...
}
```

Its side effect is prevalent in the `toResponse` method of ProjectService, which has a switch statement that checks the
artifact type and returns the appropriate response object. This could lead to a large and complex method that is
difficult to maintain and extend.

```java
private ArtifactResponse toResponse( Artifact artifact ) {
    return switch ( artifact.getArtifactType() ) {
        case REPOSITORIES -> new GitRepositoryResponse(
                artifact.getId(),
                artifact.getProject().getId(),
                artifact.typePathToken(),
                artifact.getRepoUrl(),
                artifact.getTargetBranch(),
                artifact.getPrimaryTechStack(),
                artifact.getTotalCommitCount(),
                artifact.getLatestCommitTimestamp()
        );
        case MANUSCRIPTS -> manuscriptResponse( artifact );
        case VIDEOS -> new VideoResponse(
                artifact.getId(),
                artifact.getProject().getId(),
                artifact.typePathToken(),
                artifact.getVideoUrl(),
                artifact.getThumbnailUrl(),
                artifact.getDurationMillis(),
                artifact.getDescription()
        );
    };
}
```

The same issue exists with  `apply` methods.

```java

private void apply( Artifact artifact, GitRepositoryRequest request ) {
    artifact.setRepoUrl( request.repoUrl() );
    artifact.setTargetBranch( request.targetBranch() );
    artifact.setPrimaryTechStack( request.primaryTechStack() );
    artifact.setTotalCommitCount( request.totalCommitCount() );
    artifact.setLatestCommitTimestamp( request.latestCommitTimestamp() );
}

private void apply( Artifact artifact, ManuscriptRequest request ) {
    artifact.setTitle( request.title() );
    artifact.setAbstractText( request.abstractText() );
    artifact.setAuthors( request.authors() );
    artifact.setKeywords( request.keywords() );
    artifact.setYear( request.year() );
    PublisherRequest publisher = request.publisher();
    artifact.setPublisherName( publisher == null ? null : publisher.name() );
    artifact.setPublisherVenue( publisher == null ? null : publisher.venue() );
}

private void apply( Artifact artifact, VideoRequest request ) {
    artifact.setVideoUrl( request.videoUrl() );
    artifact.setThumbnailUrl( request.thumbnailUrl() );
    artifact.setDurationMillis( request.durationMillis() );
    artifact.setDescription( request.description() );
}
```

---

In ArtifactService, when invalid `artifactType` is passed to `getByType` method, it throws `IllegalArgumentException`,
which could lead to potentially exposing sensitive internal information in the response. It would be better to return a
400 Bad Request response with a clear error message. Same applies to other instances of
`ArtifactType.fromPathToken(..)`.

```java

@GetMapping("/{artifactType}")
public List<ArtifactResponse> getByType(
        @PathVariable UUID projectId,
        @PathVariable String artifactType
) {
    return artifacts.getByType( projectId, ArtifactType.fromPathToken( artifactType ) );
}
```

### 2.3 The AI Agent's "Security Sins"

Let's use `ArtifactController#createManuscript()` as an example. It calls
`artifacts.createManuscript( projectId, request ) )` without validating the request such as `title` should not be empty
and null.

```java

@PostMapping("/manuscripts")
public ResponseEntity<ArtifactResponse> createManuscript(
        @PathVariable UUID projectId,
        @RequestBody ManuscriptRequest request
) {
    return created( artifacts.createManuscript( projectId, request ) );
}
```

---

Another instance of security sin, is in ProjectService where the `update` method increments the revision version and
updates the updatedAt timestamp if the title or description has changed. However, it does not handle concurrent updates,
which could lead to race conditions and lost updates if multiple users are updating the same project simultaneously.

```java
    public ProjectResponse update( UUID id, ProjectRequest request ) {
    Project project = projects.findByIdAndDeleted( id, ACTIVE ).orElseThrow();
    boolean scalarFieldsChanged = !Objects.equals( project.getTitle(), request.title() )
            || !Objects.equals( project.getDescription(), request.description() );
    project.setTitle( request.title() );
    project.setDescription( request.description() );
    if ( scalarFieldsChanged ) {
        project.setRevisionVersion( project.getRevisionVersion() + 1 );
        project.setUpdatedAt( Math.max( System.currentTimeMillis(), project.getUpdatedAt() + 1 ) );
    }
    replaceAssociations( project, request );
    return toResponse( projects.save( project ) );
}
```

## SECTION 3: COGNITIVE REFLECTION - DOCUMENT AUTHORING & THREAT DESIGN

### 3.1 AI Collaboration Matrix for Security Analysis

I had used the `/plan` mode using the cli, where I described that the project implements happy-path without any
security, validation. Analyze each and every API which is exposed and generate abuse cases, then derive attack trees for
those abuse cases.

### 3.2 Evaluation of Automated Conceptual "Blind Spots"

Yes, the AI agent mentioned about `spring-expression` SpEL flaws and `tomcat-embed-core` CGI/AJP/FileStore
bugs being present in `gradle.lockfile` but those vulnerable execution paths are absent from the `src/main`.

## SECTION 4: HUMAN VERIFICATION & AUDIT TRAIL LOG

| Timestamp      | Component Target                                                                  | Testing/Auditing New Methodology                               | Verified Outcome / Discovered Defect                              |
|:---------------|:----------------------------------------------------------------------------------|:---------------------------------------------------------------|:------------------------------------------------------------------|
| *[2026-11-15]* | *`POST http://localhost:8080/api/v1/projects`*                                    | *Executed manual Postman requests passing negative member id.* | *Failed Check: System accepted negative member id.*               |
| *[2026-11-15]* | *`POST http://localhost:8080/api/v1/projects/{project_id}/artifacts/manuscripts`* | *Executed manual Postman requests passing negative year.*      | *Failed Check: System accepted negative year for the manuscript.* |
