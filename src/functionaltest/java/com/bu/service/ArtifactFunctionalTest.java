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
    void managesRepositoryManuscriptAndVideoArtifacts() throws Exception
    {
        UUID projectId = UUID.fromString( createProject(
                mockMvc,
                objectMapper,
                projectRequest(
                        "Artifact test project", "Project description", List.of(
                                new MemberRequest( "member-002", "student" )
                        )
                )
        ).get( "id" ).asText() );

        GitRepositoryRequest repository = new GitRepositoryRequest(
                "https://github.com/example/assistive-vision",
                "main",
                "Java / Spring Boot",
                24,
                1794594000000L
        );
        UUID repositoryId = createArtifact(
                projectId,
                "/repositories",
                repository,
                "targetBranch",
                "main"
        );
        mockMvc.perform( put(
                        "/api/v1/projects/{projectId}/artifacts/repositories/{artifactId}",
                        projectId,
                        repositoryId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( new GitRepositoryRequest(
                                repository.repoUrl(),
                                "release",
                                repository.primaryTechStack(),
                                repository.totalCommitCount(),
                                repository.latestCommitTimestamp()
                        ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.targetBranch" ).value( "release" ) );
        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/repositories/{artifactId}",
                        projectId,
                        repositoryId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.repoUrl" ).value( repository.repoUrl() ) )
                .andExpect( jsonPath( "$.targetBranch" ).value( "release" ) )
                .andExpect( jsonPath( "$.primaryTechStack" ).value( repository.primaryTechStack() ) )
                .andExpect( jsonPath( "$.totalCommitCount" ).value( 24 ) )
                .andExpect( jsonPath( "$.latestCommitTimestamp" ).value( repository.latestCommitTimestamp() ) );

        ManuscriptRequest manuscript = new ManuscriptRequest(
                "Visual Alignment on Edge Devices",
                "A study of efficient alignment methods.",
                List.of( "A. Researcher", "B. Student" ),
                List.of( "alignment", "edge computing" ),
                2026,
                new PublisherRequest( "Campus Press", "Project Symposium" )
        );
        UUID manuscriptId = createArtifact(
                projectId,
                "/manuscripts",
                manuscript,
                "artifactType",
                "manuscripts"
        );
        mockMvc.perform( put(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        manuscriptId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( new ManuscriptRequest(
                                manuscript.title(),
                                "Updated abstract.",
                                manuscript.authors(),
                                manuscript.keywords(),
                                manuscript.year(),
                                manuscript.publisher()
                        ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.abstract" ).value( "Updated abstract." ) );
        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        manuscriptId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.title" ).value( manuscript.title() ) )
                .andExpect( jsonPath( "$.file" ).value( org.hamcrest.Matchers.nullValue() ) )
                .andExpect( jsonPath( "$.authors.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.keywords.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.year" ).value( 2026 ) )
                .andExpect( jsonPath( "$.publisher.name" ).value( "Campus Press" ) )
                .andExpect( jsonPath( "$.publisher.venue" ).value( "Project Symposium" ) );

        VideoRequest video = new VideoRequest(
                "https://video.example.edu/presentation",
                "https://video.example.edu/thumbnail.jpg",
                240000L,
                "A demonstration of the project."
        );
        UUID videoId = createArtifact( projectId, "/videos", video, "durationMillis", 240000 );
        mockMvc.perform( put(
                        "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                        projectId,
                        videoId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( new VideoRequest(
                                video.videoUrl(),
                                video.thumbnailUrl(),
                                300000L,
                                video.description()
                        ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.durationMillis" ).value( 300000 ) );
        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                        projectId,
                        videoId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.videoUrl" ).value( video.videoUrl() ) )
                .andExpect( jsonPath( "$.thumbnailUrl" ).value( video.thumbnailUrl() ) )
                .andExpect( jsonPath( "$.durationMillis" ).value( 300000 ) )
                .andExpect( jsonPath( "$.description" ).value( video.description() ) );

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
        for ( String artifactType : List.of( "repositories", "manuscripts", "videos" ) )
        {
            mockMvc.perform( get( "/api/v1/projects" ).param( "artifactType", artifactType ) )
                    .andExpect( status().isOk() )
                    .andExpect( jsonPath( "$.length()" ).value( 1 ) );
        }
        mockMvc.perform( get( "/api/v1/projects" )
                        .param( "keyword", "alignment" )
                        .param( "artifactType", "manuscripts" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );

        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        manuscriptId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );
        mockMvc.perform( get( "/api/v1/projects" ).param( "keyword", "alignment" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
        mockMvc.perform( get( "/api/v1/projects" ).param( "artifactType", "manuscripts" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/repositories/{artifactId}",
                        projectId,
                        repositoryId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                        projectId,
                        videoId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
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
