/*
 * A2 functional baseline: Java 17, Spring Boot 3.3.5, Gradle 9.6.0.
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored.
 * Human verification: Done
 */

package com.bu.repository;

import com.bu.service.repositoryindexer.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>
{
    List<Project> findAllByDeleted( int deleted );

    Optional<Project> findByIdAndDeleted( UUID id, int deleted );
}
