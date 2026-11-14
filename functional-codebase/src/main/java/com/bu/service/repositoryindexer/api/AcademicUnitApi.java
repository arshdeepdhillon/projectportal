/*
 * A2 functional baseline: Java 17, Spring Boot 3.3.5, Gradle 9.6.0.
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored.
 * Human verification: Done
 */

package com.bu.service.repositoryindexer.api;

import java.util.UUID;

public final class AcademicUnitApi
{
    private AcademicUnitApi()
    {
    }

    public record UnitResponse( UUID id, String name, String code )
    {
    }

    public record CourseResponse( UUID id, String courseCode, String courseName )
    {
    }
}
