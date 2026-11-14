package com.bu.service;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ArtifactApi.GitRepositoryRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.ManuscriptRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.PublisherRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.VideoRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static com.bu.service.FunctionalTestSupport.createProject;
import static com.bu.service.FunctionalTestSupport.projectRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArtifactFunctionalTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void clearProjects()
    {
        projectRepository.deleteAll();
    }

    @Test
    void createsRepositoryArtifact() throws Exception
    {
        UUID projectId = createSampleProject();

        createRepository( projectId );
    }

    @Test
    void readsRepositoryArtifact() throws Exception {
        UUID projectId = createSampleProject();
        UUID artifactId = createRepository( projectId );

        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/repositories/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.repoUrl" ).value( repositoryRequest().repoUrl() ) )
                .andExpect( jsonPath( "$.targetBranch" ).value( "main" ) );
    }

    @Test
    void updatesRepositoryArtifact() throws Exception {
        UUID projectId = createSampleProject();
        UUID artifactId = createRepository( projectId );
        GitRepositoryRequest update = new GitRepositoryRequest(
                repositoryRequest().repoUrl(),
                "release",
                repositoryRequest().primaryTechStack(),
                repositoryRequest().totalCommitCount(),
                repositoryRequest().latestCommitTimestamp()
        );

        mockMvc.perform( put(
                        "/api/v1/projects/{projectId}/artifacts/repositories/{artifactId}",
                        projectId,
                        artifactId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( update ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.targetBranch" ).value( "release" ) );
    }

    @Test
    void deletesRepositoryArtifact() throws Exception {
        UUID projectId = createSampleProject();
        UUID artifactId = createRepository( projectId );

        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/repositories/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/repositories", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
    }

    @Test
    void createsManuscriptArtifact() throws Exception {
        UUID projectId = createSampleProject();
        ManuscriptRequest manuscript = manuscriptRequest();

        createArtifact( projectId, "/manuscripts", manuscript, "title", manuscript.title() );
    }

    @Test
    void readsManuscriptArtifact() throws Exception {
        UUID projectId = createSampleProject();
        ManuscriptRequest manuscript = manuscriptRequest();
        UUID artifactId = createArtifact( projectId, "/manuscripts", manuscript, "title", manuscript.title() );

        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.authors.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.keywords.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.publisher.name" ).value( "Campus Press" ) );
    }

    @Test
    void updatesManuscriptArtifact() throws Exception {
        UUID projectId = createSampleProject();
        ManuscriptRequest manuscript = manuscriptRequest();
        UUID artifactId = createArtifact( projectId, "/manuscripts", manuscript, "title", manuscript.title() );
        ManuscriptRequest update = new ManuscriptRequest(
                manuscript.title(),
                "Updated abstract.",
                manuscript.authors(),
                manuscript.keywords(),
                manuscript.year(),
                manuscript.publisher()
        );

        mockMvc.perform( put(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        artifactId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( update ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.abstract" ).value( "Updated abstract." ) );
    }

    @Test
    void deletesManuscriptArtifact() throws Exception {
        UUID projectId = createSampleProject();
        UUID artifactId = createArtifact(
                projectId,
                "/manuscripts",
                manuscriptRequest(),
                "artifactType",
                "manuscripts"
        );

        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/manuscripts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
    }

    @Test
    void createsVideoArtifact() throws Exception {
        UUID projectId = createSampleProject();
        VideoRequest video = videoRequest();

        createArtifact( projectId, "/videos", video, "durationMillis", 240000 );
    }

    @Test
    void readsVideoArtifact() throws Exception {
        UUID projectId = createSampleProject();
        VideoRequest video = videoRequest();
        UUID artifactId = createArtifact( projectId, "/videos", video, "durationMillis", 240000 );

        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.videoUrl" ).value( video.videoUrl() ) )
                .andExpect( jsonPath( "$.thumbnailUrl" ).value( video.thumbnailUrl() ) )
                .andExpect( jsonPath( "$.durationMillis" ).value( 240000 ) );
    }

    @Test
    void updatesVideoArtifact() throws Exception {
        UUID projectId = createSampleProject();
        VideoRequest video = videoRequest();
        UUID artifactId = createArtifact( projectId, "/videos", video, "durationMillis", 240000 );
        VideoRequest update = new VideoRequest(
                video.videoUrl(),
                video.thumbnailUrl(),
                300000L,
                video.description()
        );

        mockMvc.perform( put(
                        "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                        projectId,
                        artifactId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( update ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.durationMillis" ).value( 300000 ) );
    }

    @Test
    void deletesVideoArtifact() throws Exception {
        UUID projectId = createSampleProject();
        UUID artifactId = createArtifact( projectId, "/videos", videoRequest(), "durationMillis", 240000 );

        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/videos", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
    }

    @Test
    void listsAllArtifactsAndEachArtifactType() throws Exception {
        UUID projectId = createSampleProject();
        createRepository( projectId );
        createArtifact( projectId, "/manuscripts", manuscriptRequest(), "artifactType", "manuscripts" );
        createArtifact( projectId, "/videos", videoRequest(), "durationMillis", 240000 );

        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 3 ) );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/repositories", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/manuscripts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/videos", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
    }

    @Test
    void searchesProjectsByArtifactType() throws Exception {
        UUID projectId = createSampleProject();
        createRepository( projectId );
        createArtifact( projectId, "/manuscripts", manuscriptRequest(), "artifactType", "manuscripts" );
        createArtifact( projectId, "/videos", videoRequest(), "durationMillis", 240000 );

        for ( String artifactType : List.of( "repositories", "manuscripts", "videos" ) )
        {
            mockMvc.perform( get( "/api/v1/projects" ).param( "artifactType", artifactType ) )
                    .andExpect( status().isOk() )
                    .andExpect( jsonPath( "$.length()" ).value( 1 ) );
        }
    }

    @Test
    void searchesProjectsByArtifactKeyword() throws Exception {
        UUID projectId = createSampleProject();
        createArtifact( projectId, "/manuscripts", manuscriptRequest(), "artifactType", "manuscripts" );

        mockMvc.perform( get( "/api/v1/projects" )
                        .param( "keyword", "alignment" )
                        .param( "artifactType", "manuscripts" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
    }

    private UUID createSampleProject() throws Exception {
        return UUID.fromString( createProject(
                mockMvc,
                objectMapper,
                projectRequest(
                        "Artifact test project", "Project description", List.of(
                                new MemberRequest( "member-002", "student" )
                        )
                )
        ).get( "id" ).asText() );
    }

    private UUID createRepository( UUID projectId ) throws Exception {
        GitRepositoryRequest repository = repositoryRequest();
        return createArtifact( projectId, "/repositories", repository, "targetBranch", "main" );
    }

    private GitRepositoryRequest repositoryRequest() {
        return new GitRepositoryRequest(
                "https://github.com/example/assistive-vision",
                "main",
                "Java / Spring Boot",
                24,
                1794594000000L
        );
    }

    private ManuscriptRequest manuscriptRequest() {
        return new ManuscriptRequest(
                "Visual Alignment on Edge Devices",
                "A study of efficient alignment methods.",
                List.of( "A. Researcher", "B. Student" ),
                List.of( "alignment", "edge computing" ),
                2026,
                new PublisherRequest( "Campus Press", "Project Symposium" )
        );
    }

    private VideoRequest videoRequest() {
        return new VideoRequest(
                "https://video.example.edu/presentation",
                "https://video.example.edu/thumbnail.jpg",
                240000L,
                "A demonstration of the project."
        );
    }

    private UUID createArtifact(
            UUID projectId,
            String artifactPath,
            Object request,
            String responseField,
            Object expectedValue
    ) throws Exception
    {
        MvcResult result = mockMvc.perform( post(
                        "/api/v1/projects/{projectId}/artifacts" + artifactPath,
                        projectId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( request ) ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$." + responseField ).value( expectedValue ) )
                .andReturn();
        JsonNode response = objectMapper.readTree( result.getResponse().getContentAsByteArray() );
        UUID artifactId = UUID.fromString( response.get( "id" ).asText() );
        assertThat( response.get( "projectId" ).asText() ).isEqualTo( projectId.toString() );
        return artifactId;
    }
}
