package com.bu.repository;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.service.repositoryindexer.entity.Artifact;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtifactRepository extends JpaRepository<Artifact, UUID>
{
    List<Artifact> findAllByProject_IdAndDeletedOrderById( UUID projectId, int deleted );

    List<Artifact> findAllByProject_IdAndArtifactTypeAndDeletedOrderById(
            UUID projectId,
            ArtifactType artifactType,
            int deleted
    );

    Optional<Artifact> findByIdAndProject_IdAndArtifactTypeAndDeleted(
            UUID id,
            UUID projectId,
            ArtifactType artifactType,
            int deleted
    );

    Optional<Artifact> findByIdAndProject_IdAndDeleted( UUID id, UUID projectId, int deleted );
}
