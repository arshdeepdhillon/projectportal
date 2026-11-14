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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.bu.service.FunctionalTestSupport.createProject;
import static com.bu.service.FunctionalTestSupport.projectRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
                .andExpect( jsonPath( "$.sha256" ).value( sha256( fileBytes ) ) )
                .andExpect( jsonPath( "$.originalFilename" ).value( "paper.pdf" ) )
                .andExpect( jsonPath( "$.mediaType" ).value( MediaType.APPLICATION_PDF_VALUE ) );

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
                .andExpect( jsonPath( "$.sha256" ).value( sha256( "revised manuscript".getBytes() ) ) )
                .andExpect( jsonPath( "$.originalFilename" ).value( "paper-revised.pdf" ) )
                .andExpect( jsonPath( "$.mediaType" ).value( MediaType.APPLICATION_PDF_VALUE ) );
        mockMvc.perform( get(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        artifactId
                ) )
                .andExpect( status().isOk() )
                .andExpect( header().string( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE ) )
                .andExpect( header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString( "paper-revised.pdf" )
                ) )
                .andExpect( result -> assertThat( result.getResponse().getContentAsByteArray() )
                        .isEqualTo( "revised manuscript".getBytes() ) );

        UUID secondArtifactId = createManuscript( projectId, "Second manuscript" );
        mockMvc.perform( multipart(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        secondArtifactId
                )
                        .file( new MockMultipartFile(
                                "file",
                                "paper.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                "second manuscript".getBytes()
                        ) ) )
                .andExpect( status().isCreated() );

        createManuscript( projectId, "Manuscript without a file" );
        UUID deletedManuscriptId = createManuscript( projectId, "Deleted manuscript" );
        mockMvc.perform( multipart(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        deletedManuscriptId
                )
                        .file( new MockMultipartFile(
                                "file",
                                "deleted.pdf",
                                MediaType.APPLICATION_PDF_VALUE,
                                "deleted manuscript".getBytes()
                        ) ) )
                .andExpect( status().isCreated() );
        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}",
                        projectId,
                        deletedManuscriptId
                ) )
                .andExpect( status().isNoContent() );

        assertThat( zipEntries( projectId ) )
                .isEqualTo( Map.of(
                        artifactId + "/paper-revised.pdf", "revised manuscript",
                        secondArtifactId + "/paper.pdf", "second manuscript"
                ) );
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
        assertThat( zipEntries( projectId ) )
                .isEqualTo( Map.of( secondArtifactId + "/paper.pdf", "second manuscript" ) );
        mockMvc.perform( delete(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        secondArtifactId
                ) )
                .andExpect( status().isNoContent() );
        assertThat( zipEntries( projectId ) ).isEmpty();

        mockMvc.perform( multipart(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts/{artifactId}/file",
                        projectId,
                        artifactId
                )
                        .file( replacement ) )
                .andExpect( status().isCreated() );
        assertThat( zipEntries( projectId ) )
                .isEqualTo( Map.of( artifactId + "/paper-revised.pdf", "revised manuscript" ) );
    }

    private UUID createManuscript( UUID projectId, String title ) throws Exception
    {
        ManuscriptRequest request = new ManuscriptRequest(
                title,
                "ZIP coverage.",
                List.of( "Test Author" ),
                List.of( "zip" ),
                2026,
                null
        );
        byte[] response = mockMvc.perform( post(
                        "/api/v1/projects/{projectId}/artifacts/manuscripts",
                        projectId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( request ) ) )
                .andExpect( status().isCreated() )
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return UUID.fromString( objectMapper.readTree( response ).get( "id" ).asText() );
    }

    private Map<String, String> zipEntries( UUID projectId ) throws Exception
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
            Map<String, String> entries = new HashMap<>();
            ZipEntry entry;
            while ( (entry = zip.getNextEntry()) != null )
            {
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                zip.transferTo( content );
                entries.put( entry.getName(), content.toString( StandardCharsets.UTF_8 ) );
                zip.closeEntry();
            }
            return entries;
        }
    }

    private String sha256( byte[] content ) throws Exception
    {
        return HexFormat.of().formatHex( MessageDigest.getInstance( "SHA-256" ).digest( content ) );
    }
}
