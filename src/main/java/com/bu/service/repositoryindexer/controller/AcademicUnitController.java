package com.bu.service.repositoryindexer.controller;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.service.repositoryindexer.api.AcademicUnitApi.CourseResponse;
import com.bu.service.repositoryindexer.api.AcademicUnitApi.UnitResponse;
import com.bu.service.repositoryindexer.featureservice.AcademicUnitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AcademicUnitController
{
    private final AcademicUnitService academicUnits;

    public AcademicUnitController( AcademicUnitService academicUnits )
    {
        this.academicUnits = academicUnits;
    }

    @GetMapping("/labs")
    public List<UnitResponse> getLabs()
    {
        return academicUnits.getLabs();
    }

    @GetMapping("/departments")
    public List<UnitResponse> getDepartments()
    {
        return academicUnits.getDepartments();
    }

    @GetMapping("/courses")
    public List<CourseResponse> getCourses()
    {
        return academicUnits.getCourses();
    }
}
