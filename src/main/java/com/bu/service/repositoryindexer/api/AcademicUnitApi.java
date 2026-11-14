package com.bu.service.repositoryindexer.api;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

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
