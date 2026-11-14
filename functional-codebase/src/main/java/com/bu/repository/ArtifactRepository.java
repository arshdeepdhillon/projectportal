/*
 * A2 functional baseline: Java 17, Spring Boot 3.3.5, Gradle 9.6.0.
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored.
 * Human verification: Done
 */

package com.bu.repository;

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
