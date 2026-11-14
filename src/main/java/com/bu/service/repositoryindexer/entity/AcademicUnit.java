package com.bu.service.repositoryindexer.entity;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "academic_units")
@Getter
@Setter
@NoArgsConstructor
public class AcademicUnit
{
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    private Type type;
    @Column(name = "name")
    private String name;
    @Column(name = "code")
    private String code;
    @Column(name = "course_code")
    private String courseCode;
    @Column(name = "course_name")
    private String courseName;

    public AcademicUnit( UUID id, String name, String code, Type type )
    {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public AcademicUnit( UUID id, String courseCode, String courseName )
    {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.type = Type.COURSE;
    }

    public enum Type
    {
        LAB,
        DEPARTMENT,
        COURSE
    }
}
