package com.bu.service;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ArtifactRepository;
import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ArtifactApi.ManuscriptRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.PublisherRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.GitRepositoryRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.VideoRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SampleFunctionalTest
{
    private static final UUID LAB_ID = UUID.fromString( "11111111-1111-1111-1111-111111111111" );
    private static final UUID DEPARTMENT_ID = UUID.fromString( "33333333-3333-3333-3333-333333333333" );
    private static final UUID COURSE_ID = UUID.fromString( "55555555-5555-5555-5555-555555555555" );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Test
    void managesProjectsArtifactsFilesSearchAndPurge() throws Exception
    {
        projectRepository.deleteAll();
        ProjectRequest createProject = projectRequest(
                "Assistive Vision Project",
                "A campus research project",
                List.of( new MemberRequest( "member-001", "student" ) )
        );
        MvcResult projectCreate = mockMvc.perform( post( "/api/v1/projects" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( createProject ) ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.revisionVersion" ).value( 1 ) )
                .andExpect( jsonPath( "$.deleted" ).doesNotExist() )
                .andReturn();
        JsonNode project = objectMapper.readTree( projectCreate.getResponse().getContentAsByteArray() );
        UUID projectId = UUID.fromString( project.get( "id" ).asText() );
        long createdAt = project.get( "createdAt" ).asLong();
        long updatedAt = project.get( "updatedAt" ).asLong();
        mockMvc.perform( get( "/api/v1/projects/{projectId}", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.artifacts" ).doesNotExist() );
        mockMvc.perform( get( "/api/v1/labs" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );
        mockMvc.perform( get( "/api/v1/departments" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );
        mockMvc.perform( get( "/api/v1/courses" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );

        ProjectRequest associationOnlyUpdate = projectRequest(
                "Assistive Vision Project",
                "A campus research project",
                List.of(
                        new MemberRequest( "member-001", "student" ),
                        new MemberRequest( "member-001", "team lead" )
                )
        );
        mockMvc.perform( put( "/api/v1/projects/{projectId}", projectId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( associationOnlyUpdate ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.revisionVersion" ).value( 1 ) )
                .andExpect( jsonPath( "$.members.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.updatedAt" ).value( updatedAt ) );

        ProjectRequest scalarUpdate = projectRequest(
                "Assistive Vision Project, revised",
                "A campus research project",
                associationOnlyUpdate.members()
        );
        mockMvc.perform( put( "/api/v1/projects/{projectId}", projectId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( scalarUpdate ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.revisionVersion" ).value( 2 ) );

        ManuscriptRequest manuscript = new ManuscriptRequest(
                "Visual Alignment on Edge Devices",
                "A study of efficient alignment methods.",
                List.of( "A. Researcher", "B. Student" ),
                List.of( "alignment", "edge computing" ),
                2026,
                new PublisherRequest( "Campus Press", "Project Symposium" )
        );
        MvcResult artifactCreate = mockMvc.perform( post(
                                "/api/v1/projects/{projectId}/artifacts/manuscripts",
                                projectId
                        )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( manuscript ) ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.artifactType" ).value( "manuscripts" ) )
                .andExpect( jsonPath( "$.abstract" ).value( manuscript.abstractText() ) )
                .andReturn();
        JsonNode createdArtifact = objectMapper.readTree( artifactCreate.getResponse().getContentAsByteArray() );
        UUID artifactId = UUID.fromString( createdArtifact.get( "id" ).asText() );

        GitRepositoryRequest repository = new GitRepositoryRequest(
                "https://github.com/example/assistive-vision",
                "main",
                "Java / Spring Boot",
                24,
                1794594000000L
        );
        MvcResult repositoryCreate = mockMvc.perform( post(
                                "/api/v1/projects/{projectId}/artifacts/repositories",
                                projectId
                        )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( repository ) ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.targetBranch" ).value( "main" ) )
                .andReturn();
        UUID repositoryId = UUID.fromString( objectMapper.readTree(
                repositoryCreate.getResponse().getContentAsByteArray()
        ).get( "id" ).asText() );
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

        VideoRequest video = new VideoRequest(
                "https://video.example.edu/presentation",
                "https://video.example.edu/thumbnail.jpg",
                240000L,
                "A demonstration of the project."
        );
        MvcResult videoCreate = mockMvc.perform( post( "/api/v1/projects/{projectId}/artifacts/videos", projectId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( video ) ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.durationMillis" ).value( 240000 ) )
                .andReturn();
        UUID videoId = UUID.fromString( objectMapper.readTree(
                videoCreate.getResponse().getContentAsByteArray()
        ).get( "id" ).asText() );
        mockMvc.perform( put(
                                "/api/v1/projects/{projectId}/artifacts/videos/{artifactId}",
                                projectId,
                                videoId
                        )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( video ) ) )
                .andExpect( status().isOk() );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 3 ) );
        mockMvc.perform( get( "/api/v1/projects/{projectId}/artifacts/manuscripts", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.file" ).value( nullValue() ) );

        byte[] fileBytes = "manuscript contents".getBytes();
        MockMultipartFile upload = new MockMultipartFile(
                "file",
                "paper.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                fileBytes
        );
        mockMvc.perform( multipart(
                                "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                                projectId,
                                artifactId
                        )
                        .file( upload ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.sha256" ).value( sha256( fileBytes ) ) );

        MockMultipartFile replacement = new MockMultipartFile(
                "file",
                "paper-revised.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                fileBytes
        );
        mockMvc.perform( multipart(
                                "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                                projectId,
                                artifactId
                        )
                        .file( replacement ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.originalFilename" ).value( "paper-revised.pdf" ) );

        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( result -> assertThat( result.getResponse().getContentAsByteArray() ).isEqualTo( fileBytes ) );

        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.file" ).value( nullValue() ) );
        mockMvc.perform( multipart(
                                "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                                projectId,
                                artifactId
                        )
                        .file( replacement ) )
                .andExpect( status().isCreated() );

        MvcResult zipResult = mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/download",
                        projectId
                ) )
                .andExpect( status().isOk() )
                .andReturn();
        try ( ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream( zipResult.getResponse().getContentAsByteArray() )
        ) )
        {
            assertThat( zip.getNextEntry().getName() ).isEqualTo( artifactId + "/paper-revised.pdf" );
        }

        mockMvc.perform( get( "/api/v1/projects" )
                        .param( "keyword", "alignment" )
                        .param( "labId", LAB_ID.toString() )
                        .param( "departmentId", DEPARTMENT_ID.toString() )
                        .param( "courseId", COURSE_ID.toString() )
                        .param( "artifactType", "manuscripts" )
                        .param( "timeRange.createdFrom", Long.toString( createdAt ) )
                        .param( "timeRange.createdTo", Long.toString( createdAt ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].id" ).value( projectId.toString() ) );

        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isNoContent() );
        mockMvc.perform( get( "/api/v1/projects" ).param( "keyword", "alignment" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
        assertThat( artifactRepository.findById( artifactId ).orElseThrow().getDeleted() ).isEqualTo( 1 );
        MvcResult emptyZip = mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/download",
                        projectId
                ) )
                .andExpect( status().isOk() )
                .andReturn();
        try ( ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream( emptyZip.getResponse().getContentAsByteArray() )
        ) )
        {
            assertThat( zip.getNextEntry() ).isNull();
        }

        mockMvc.perform( delete( "/api/v1/projects/{projectId}", projectId ) )
                .andExpect( status().isNoContent() );
        assertThat( projectRepository.findById( projectId ).orElseThrow().getUpdatedAt() )
                .isGreaterThan( updatedAt );
        mockMvc.perform( get( "/api/v1/projects" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
        mockMvc.perform( post( "/api/v1/projects/purge" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.purgedProjectCount" ).value( 1 ) );
        assertThat( projectRepository.findById( projectId ) ).isEmpty();
        assertThat( artifactRepository.findById( artifactId ) ).isEmpty();
    }

    private ProjectRequest projectRequest( String title, String description, List<MemberRequest> members )
    {
        return new ProjectRequest(
                title,
                description,
                members,
                List.of( LAB_ID ),
                List.of( DEPARTMENT_ID ),
                List.of( COURSE_ID )
        );
    }

    private String sha256( byte[] content ) throws NoSuchAlgorithmException
    {
        return HexFormat.of().formatHex( MessageDigest.getInstance( "SHA-256" ).digest( content ) );
    }
}
