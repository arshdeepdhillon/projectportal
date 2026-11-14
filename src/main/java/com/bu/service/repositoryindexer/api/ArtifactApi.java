package com.bu.service.repositoryindexer.api;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public final class ArtifactApi
{
    private ArtifactApi()
    {
    }

    public record GitRepositoryRequest(
            String repoUrl,
            String targetBranch,
            String primaryTechStack,
            Integer totalCommitCount,
            Long latestCommitTimestamp
    )
    {
    }

    public record PublisherRequest( String name, String venue )
    {
    }

    public record PublisherResponse( String name, String venue )
    {
    }

    public record ManuscriptRequest(
            String title,
            @JsonProperty("abstract") String abstractText,
            List<String> authors,
            List<String> keywords,
            Integer year,
            PublisherRequest publisher
    )
    {
    }

    public record VideoRequest(
            String videoUrl,
            String thumbnailUrl,
            Long durationMillis,
            String description
    )
    {
    }

    public record FileMetadataResponse(
            String sha256,
            String originalFilename,
            String mediaType
    )
    {
    }

    public interface ArtifactResponse
    {
        UUID id();

        UUID projectId();

        String artifactType();
    }

    public record GitRepositoryResponse(
            UUID id,
            UUID projectId,
            String artifactType,
            String repoUrl,
            String targetBranch,
            String primaryTechStack,
            Integer totalCommitCount,
            Long latestCommitTimestamp
    ) implements ArtifactResponse
    {
    }

    public record ManuscriptResponse(
            UUID id,
            UUID projectId,
            String artifactType,
            String title,
            @JsonProperty("abstract") String abstractText,
            List<String> authors,
            List<String> keywords,
            Integer year,
            PublisherResponse publisher,
            FileMetadataResponse file
    ) implements ArtifactResponse
    {
    }

    public record VideoResponse(
            UUID id,
            UUID projectId,
            String artifactType,
            String videoUrl,
            String thumbnailUrl,
            Long durationMillis,
            String description
    ) implements ArtifactResponse
    {
    }
}
