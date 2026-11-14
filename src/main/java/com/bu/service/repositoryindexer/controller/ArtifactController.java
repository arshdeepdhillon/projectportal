package com.bu.service.repositoryindexer.controller;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.service.repositoryindexer.api.ArtifactApi.ArtifactResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.FileMetadataResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.GitRepositoryRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.ManuscriptRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.VideoRequest;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import com.bu.service.repositoryindexer.featureservice.ArtifactService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/artifacts")
public class ArtifactController
{
    private final ArtifactService artifacts;
    private final ObjectMapper objectMapper;

    public ArtifactController( ArtifactService artifacts, ObjectMapper objectMapper )
    {
        this.artifacts = artifacts;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<ArtifactResponse> getAll( @PathVariable UUID projectId )
    {
        return artifacts.getAll( projectId );
    }

    @GetMapping("/{artifactType}")
    public List<ArtifactResponse> getByType(
            @PathVariable UUID projectId,
            @PathVariable String artifactType
    )
    {
        return artifacts.getByType( projectId, ArtifactType.fromPathToken( artifactType ) );
    }

    @PostMapping("/repositories")
    public ResponseEntity<ArtifactResponse> createRepository(
            @PathVariable UUID projectId,
            @RequestBody GitRepositoryRequest request
    )
    {
        return created( artifacts.createRepository( projectId, request ) );
    }

    @PostMapping("/manuscripts")
    public ResponseEntity<ArtifactResponse> createManuscript(
            @PathVariable UUID projectId,
            @RequestBody ManuscriptRequest request
    )
    {
        return created( artifacts.createManuscript( projectId, request ) );
    }

    @PostMapping("/videos")
    public ResponseEntity<ArtifactResponse> createVideo(
            @PathVariable UUID projectId,
            @RequestBody VideoRequest request
    )
    {
        return created( artifacts.createVideo( projectId, request ) );
    }

    @GetMapping("/{artifactType}/{artifactId}")
    public ArtifactResponse getById(
            @PathVariable UUID projectId,
            @PathVariable String artifactType,
            @PathVariable UUID artifactId
    )
    {
        return artifacts.getById( projectId, ArtifactType.fromPathToken( artifactType ), artifactId );
    }

    @PutMapping("/{artifactType}/{artifactId}")
    public ArtifactResponse update(
            @PathVariable UUID projectId,
            @PathVariable String artifactType,
            @PathVariable UUID artifactId,
            @RequestBody JsonNode request
    ) throws IOException
    {
        return switch ( ArtifactType.fromPathToken( artifactType ) )
        {
            case REPOSITORIES -> artifacts.updateRepository(
                    projectId,
                    artifactId,
                    objectMapper.treeToValue( request, GitRepositoryRequest.class )
            );
            case MANUSCRIPTS -> artifacts.updateManuscript(
                    projectId,
                    artifactId,
                    objectMapper.treeToValue( request, ManuscriptRequest.class )
            );
            case VIDEOS -> artifacts.updateVideo(
                    projectId,
                    artifactId,
                    objectMapper.treeToValue( request, VideoRequest.class )
            );
        };
    }

    @DeleteMapping("/{artifactType}/{artifactId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID projectId,
            @PathVariable String artifactType,
            @PathVariable UUID artifactId
    )
    {
        artifacts.softDelete( projectId, ArtifactType.fromPathToken( artifactType ), artifactId );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manuscripts/{artifactId}/file")
    public ResponseEntity<FileMetadataResponse> uploadManuscriptFile(
            @PathVariable UUID projectId,
            @PathVariable UUID artifactId,
            @RequestPart("file") MultipartFile file
    ) throws IOException
    {
        ArtifactService.FileUploadResult result = artifacts.uploadFile( projectId, artifactId, file );
        if ( result.replacing() )
        {
            return ResponseEntity.ok( result.file() );
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
        return ResponseEntity.created( location ).body( result.file() );
    }

    @GetMapping("/manuscripts/{artifactId}/file")
    public ResponseEntity<byte[]> downloadManuscriptFile(
            @PathVariable UUID projectId,
            @PathVariable UUID artifactId
    )
    {
        ArtifactService.FileDownload file = artifacts.getFile( projectId, artifactId );
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( file.mediaType() ) )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename( file.originalFilename(), StandardCharsets.UTF_8 )
                                .build()
                                .toString()
                )
                .body( file.content() );
    }

    @DeleteMapping("/manuscripts/{artifactId}/file")
    public ResponseEntity<Void> deleteManuscriptFile(
            @PathVariable UUID projectId,
            @PathVariable UUID artifactId
    )
    {
        artifacts.deleteFile( projectId, artifactId );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/manuscripts/download")
    public ResponseEntity<byte[]> downloadManuscriptsZip( @PathVariable UUID projectId ) throws IOException
    {
        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( "application/zip" ) )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename( "manuscripts.zip" ).build().toString()
                )
                .body( artifacts.downloadManuscriptsZip( projectId ) );
    }

    private ResponseEntity<ArtifactResponse> created( ArtifactResponse response )
    {
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path( "/{id}" )
                .buildAndExpand( response.id() )
                .toUri();
        return ResponseEntity.created( location ).body( response );
    }
}
