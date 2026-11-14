package com.bu.service.repositoryindexer.config;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.AcademicUnitRepository;
import com.bu.service.repositoryindexer.entity.AcademicUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile({"dev", "test"})
public class AcademicUnitFixtureLoader implements ApplicationRunner
{
    private final ObjectMapper objectMapper;
    private final AcademicUnitRepository academicUnits;

    public AcademicUnitFixtureLoader( ObjectMapper objectMapper, AcademicUnitRepository academicUnits )
    {
        this.objectMapper = objectMapper;
        this.academicUnits = academicUnits;
    }

    @Override
    @Transactional
    public void run( ApplicationArguments args ) throws Exception
    {
        try ( InputStream input = new ClassPathResource( "fixtures/academic-units.json" ).getInputStream() )
        {
            Fixture fixture = objectMapper.readValue( input, Fixture.class );
            List<AcademicUnit> units = new ArrayList<>();
            fixture.labs().forEach( unit -> units.add(
                    new AcademicUnit( unit.id(), unit.name(), unit.code(), AcademicUnit.Type.LAB )
            ) );
            fixture.departments().forEach( unit -> units.add(
                    new AcademicUnit( unit.id(), unit.name(), unit.code(), AcademicUnit.Type.DEPARTMENT )
            ) );
            fixture.courses().forEach( course -> units.add(
                    new AcademicUnit( course.id(), course.courseCode(), course.courseName() )
            ) );
            academicUnits.saveAll( units );
        }
    }

    public record Fixture(
            List<NamedUnitFixture> labs,
            List<NamedUnitFixture> departments,
            List<CourseFixture> courses
    )
    {
    }

    public record NamedUnitFixture( UUID id, String name, String code )
    {
    }

    public record CourseFixture( UUID id, String courseCode, String courseName )
    {
    }
}
