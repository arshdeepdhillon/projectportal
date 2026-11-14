package com.bu.repository;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.service.repositoryindexer.entity.AcademicUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AcademicUnitRepository extends JpaRepository<AcademicUnit, UUID>
{
    List<AcademicUnit> findAllByTypeOrderByNameAsc( AcademicUnit.Type type );

    List<AcademicUnit> findAllByTypeOrderByCourseNameAsc( AcademicUnit.Type type );
}
