/*
 * A2 functional baseline: Java 17, Spring Boot 3.3.5, Gradle 9.6.0.
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored.
 * Human verification: Done
 */

package com.bu.service.repositoryindexer.featureservice;

import com.bu.repository.AcademicUnitRepository;
import com.bu.service.repositoryindexer.api.AcademicUnitApi.CourseResponse;
import com.bu.service.repositoryindexer.api.AcademicUnitApi.UnitResponse;
import com.bu.service.repositoryindexer.entity.AcademicUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AcademicUnitService
{
    private final AcademicUnitRepository academicUnits;

    public AcademicUnitService( AcademicUnitRepository academicUnits )
    {
        this.academicUnits = academicUnits;
    }

    public List<UnitResponse> getLabs()
    {
        return academicUnits.findAllByTypeOrderByNameAsc( AcademicUnit.Type.LAB ).stream()
                .map( unit -> new UnitResponse( unit.getId(), unit.getName(), unit.getCode() ) )
                .toList();
    }

    public List<UnitResponse> getDepartments()
    {
        return academicUnits.findAllByTypeOrderByNameAsc( AcademicUnit.Type.DEPARTMENT ).stream()
                .map( unit -> new UnitResponse( unit.getId(), unit.getName(), unit.getCode() ) )
                .toList();
    }

    public List<CourseResponse> getCourses()
    {
        return academicUnits.findAllByTypeOrderByCourseNameAsc( AcademicUnit.Type.COURSE ).stream()
                .map( unit -> new CourseResponse( unit.getId(), unit.getCourseCode(), unit.getCourseName() ) )
                .toList();
    }
}
