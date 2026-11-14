package com.bu.repository;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

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
