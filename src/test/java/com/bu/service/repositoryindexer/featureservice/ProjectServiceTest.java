package com.bu.service.repositoryindexer.featureservice;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.AcademicUnitRepository;
import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectResponse;
import com.bu.service.repositoryindexer.entity.AcademicUnit;
import com.bu.service.repositoryindexer.entity.Artifact;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import com.bu.service.repositoryindexer.entity.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest
{
    @Mock
    private ProjectRepository projects;

    @Mock
    private AcademicUnitRepository academicUnits;

    private ProjectService service;

    @BeforeEach
    void setUp()
    {
        service = new ProjectService( projects, academicUnits );
    }

    @Test
    void updateAdvancesRevisionAndTimestampWhenProjectFieldsChange()
    {
        UUID projectId = UUID.randomUUID();
        Project project = new Project( "Before", "Description", 100 );
        project.setId( projectId );
        when( projects.findByIdAndDeleted( projectId, 0 ) ).thenReturn( Optional.of( project ) );
        when( academicUnits.findAllById( List.of() ) ).thenReturn( List.of() );
        when( projects.save( any( Project.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

        ProjectResponse response = service.update( projectId, request( "After", "Description" ) );

        assertThat( response.title() ).isEqualTo( "After" );
        assertThat( response.revisionVersion() ).isEqualTo( 2 );
        assertThat( response.updatedAt() ).isGreaterThan( 100 );
        verify( projects ).save( project );
    }

    @Test
    void updateDoesNotAdvanceRevisionOrTimestampForNoOpProjectFields()
    {
        UUID projectId = UUID.randomUUID();
        Project project = new Project( "Unchanged", "Description", 100 );
        project.setId( projectId );
        when( projects.findByIdAndDeleted( projectId, 0 ) ).thenReturn( Optional.of( project ) );
        when( academicUnits.findAllById( List.of() ) ).thenReturn( List.of() );
        when( projects.save( any( Project.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

        ProjectResponse response = service.update( projectId, request( "Unchanged", "Description" ) );

        assertThat( response.revisionVersion() ).isEqualTo( 1 );
        assertThat( response.updatedAt() ).isEqualTo( 100 );
    }

    @Test
    void searchAppliesInclusiveOptionalTimeBoundsAndAllAcademicFilters()
    {
        UUID labId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Project project = new Project( "Project", "Description", 100 );
        project.setAcademicUnits( List.of(
                new AcademicUnit( labId, "Lab", "LAB", AcademicUnit.Type.LAB ),
                new AcademicUnit( departmentId, "Department", "DEPT", AcademicUnit.Type.DEPARTMENT ),
                new AcademicUnit( courseId, "COURSE-1", "Course", AcademicUnit.Type.COURSE )
        ) );
        Artifact manuscript = new Artifact( project, ArtifactType.MANUSCRIPTS );
        manuscript.setTitle( "Learning at the edge" );
        project.getArtifacts().add( manuscript );
        when( projects.findAllByDeleted( 0 ) ).thenReturn( List.of( project ) );

        List<ProjectResponse> matches = service.search(
                "EDGE",
                labId,
                departmentId,
                courseId,
                ArtifactType.MANUSCRIPTS,
                100L,
                100L
        );

        assertThat( matches ).hasSize( 1 );
        assertThat( matches.get( 0 ).labIds() ).containsExactly( labId );
        assertThat( matches.get( 0 ).departmentIds() ).containsExactly( departmentId );
        assertThat( matches.get( 0 ).courseIds() ).containsExactly( courseId );
        assertThat( service.search( null, UUID.randomUUID(), departmentId, courseId, null, null, null ) )
                .isEmpty();
        assertThat( service.search( null, null, null, null, null, 100L, null ) ).hasSize( 1 );
        assertThat( service.search( null, null, null, null, null, null, 100L ) ).hasSize( 1 );
        assertThat( service.search( null, null, null, null, null, 101L, null ) ).isEmpty();
        assertThat( service.search( null, null, null, null, null, null, 99L ) ).isEmpty();
    }

    private ProjectRequest request( String title, String description )
    {
        return new ProjectRequest(
                title, description, List.of( new MemberRequest( "member-001", "student" ) ),
                List.of(), List.of(), List.of()
        );
    }
}
