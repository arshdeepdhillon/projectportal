package com.bu.service;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ArtifactApi.ManuscriptRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static com.bu.service.FunctionalTestSupport.createProject;
import static com.bu.service.FunctionalTestSupport.projectRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManuscriptFileFunctionalTest
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
    void uploadsReplacesDownloadsAndDeletesManuscriptFiles() throws Exception
    {
        UUID projectId = UUID.fromString( createProject(
                mockMvc,
                objectMapper,
                projectRequest(
                        "Manuscript file project", "Project description", List.of(
                                new MemberRequest( "member-003", "student" )
                        )
                )
        ).get( "id" ).asText() );
        ManuscriptRequest manuscript = new ManuscriptRequest(
                "Manuscript with file",
                "File storage test.",
                List.of( "A. Researcher" ),
                List.of( "storage" ),
                2026,
                null
        );
        byte[] manuscriptResponse = mockMvc.perform( post(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts",
                        projectId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( manuscript ) ) )
                .andExpect( status().isCreated() )
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        JsonNode artifact = objectMapper.readTree( manuscriptResponse );
        UUID artifactId = UUID.fromString( artifact.get( "id" ).asText() );

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
                "revised manuscript".getBytes()
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
                .andExpect( result -> assertThat( result.getResponse().getContentAsByteArray() )
                        .isEqualTo( "revised manuscript".getBytes() ) );

        assertZipEntry( projectId, artifactId + "/paper-revised.pdf" );
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
        assertZipEntry( projectId, artifactId + "/paper-revised.pdf" );
    }

    private void assertZipEntry( UUID projectId, String expectedName ) throws Exception
    {
        byte[] zipBytes = mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/download",
                        projectId
                ) )
                .andExpect( status().isOk() )
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        try ( ZipInputStream zip = new ZipInputStream( new ByteArrayInputStream( zipBytes ) ) )
        {
            assertThat( zip.getNextEntry().getName() ).isEqualTo( expectedName );
        }
    }

    private String sha256( byte[] content ) throws Exception
    {
        return HexFormat.of().formatHex( MessageDigest.getInstance( "SHA-256" ).digest( content ) );
    }
}
