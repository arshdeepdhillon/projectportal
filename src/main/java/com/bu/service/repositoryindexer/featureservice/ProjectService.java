package com.bu.service.repositoryindexer.featureservice;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.AcademicUnitRepository;
import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberResponse;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectResponse;
import com.bu.service.repositoryindexer.entity.AcademicUnit;
import com.bu.service.repositoryindexer.entity.Artifact;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import com.bu.service.repositoryindexer.entity.Project;
import com.bu.service.repositoryindexer.entity.ProjectMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class ProjectService
{
    private static final int ACTIVE = 0;
    private static final int DELETED = 1;

    private final ProjectRepository projects;
    private final AcademicUnitRepository academicUnits;

    public ProjectService( ProjectRepository projects, AcademicUnitRepository academicUnits )
    {
        this.projects = projects;
        this.academicUnits = academicUnits;
    }

    public ProjectResponse create( ProjectRequest request )
    {
        long now = System.currentTimeMillis();
        Project project = new Project( request.title(), request.description(), now );
        replaceAssociations( project, request );
        return toResponse( projects.save( project ) );
    }

    public ProjectResponse getById( UUID id )
    {
        return projects.findByIdAndDeleted( id, ACTIVE )
                .map( this::toResponse )
                .orElseThrow();
    }

    public List<ProjectResponse> search(
            String keyword,
            UUID labId,
            UUID departmentId,
            UUID courseId,
            ArtifactType artifactType,
            Long createdFrom,
            Long createdTo
    )
    {
        String normalizedKeyword = keyword == null ? null : keyword.toLowerCase( Locale.ROOT );
        return projects.findAllByDeleted( ACTIVE ).stream()
                .filter( project -> matchesTimeRange( project, createdFrom, createdTo ) )
                .filter( project -> matchesUnit( project, labId, AcademicUnit.Type.LAB ) )
                .filter( project -> matchesUnit( project, departmentId, AcademicUnit.Type.DEPARTMENT ) )
                .filter( project -> matchesUnit( project, courseId, AcademicUnit.Type.COURSE ) )
                .filter( project -> artifactType == null || hasActiveArtifactType( project, artifactType ) )
                .filter( project -> normalizedKeyword == null || matchesKeyword( project, normalizedKeyword ) )
                .map( this::toResponse )
                .toList();
    }

    public ProjectResponse update( UUID id, ProjectRequest request )
    {
        Project project = projects.findByIdAndDeleted( id, ACTIVE ).orElseThrow();
        boolean scalarFieldsChanged = !Objects.equals( project.getTitle(), request.title() )
                || !Objects.equals( project.getDescription(), request.description() );
        project.setTitle( request.title() );
        project.setDescription( request.description() );
        if ( scalarFieldsChanged )
        {
            project.setRevisionVersion( project.getRevisionVersion() + 1 );
            project.setUpdatedAt( Math.max( System.currentTimeMillis(), project.getUpdatedAt() + 1 ) );
        }
        replaceAssociations( project, request );
        return toResponse( projects.save( project ) );
    }

    public void softDelete( UUID id )
    {
        Project project = projects.findByIdAndDeleted( id, ACTIVE ).orElseThrow();
        project.setDeleted( DELETED );
        project.setUpdatedAt( Math.max( System.currentTimeMillis(), project.getUpdatedAt() + 1 ) );
        projects.save( project );
    }

    public int purgeDeleted()
    {
        List<Project> deletedProjects = projects.findAllByDeleted( DELETED );
        projects.deleteAll( deletedProjects );
        return deletedProjects.size();
    }

    private void replaceAssociations( Project project, ProjectRequest request )
    {
        List<ProjectMember> members = request.members().stream()
                .map( member -> toMember( project, member ) )
                .toList();
        project.getMembers().clear();
        project.getMembers().addAll( members );

        List<UUID> ids = new ArrayList<>();
        ids.addAll( request.labIds() );
        ids.addAll( request.departmentIds() );
        ids.addAll( request.courseIds() );
        project.setAcademicUnits( academicUnits.findAllById( ids ) );
    }

    private ProjectMember toMember( Project project, MemberRequest member )
    {
        return new ProjectMember( project, member.memberId(), member.role() );
    }

    private boolean matchesTimeRange( Project project, Long createdFrom, Long createdTo )
    {
        return ( createdFrom == null || project.getCreatedAt() >= createdFrom )
                && ( createdTo == null || project.getCreatedAt() <= createdTo );
    }

    private boolean matchesUnit( Project project, UUID id, AcademicUnit.Type type )
    {
        return id == null || project.getAcademicUnits().stream()
                .anyMatch( unit -> unit.getType() == type && unit.getId().equals( id ) );
    }

    private boolean hasActiveArtifactType( Project project, ArtifactType type )
    {
        return project.getArtifacts().stream()
                .anyMatch( artifact -> artifact.getDeleted() == ACTIVE && artifact.getArtifactType() == type );
    }

    private boolean matchesKeyword( Project project, String keyword )
    {
        if ( contains( project.getTitle(), keyword ) || contains( project.getDescription(), keyword ) )
        {
            return true;
        }

        return project.getArtifacts().stream()
                .filter( artifact -> artifact.getDeleted() == ACTIVE )
                .anyMatch( artifact -> matchesArtifactKeyword( artifact, keyword ) );
    }

    private boolean matchesArtifactKeyword( Artifact artifact, String keyword )
    {
        return contains( artifact.typePathToken(), keyword )
                || contains( artifact.getRepoUrl(), keyword )
                || contains( artifact.getTargetBranch(), keyword )
                || contains( artifact.getPrimaryTechStack(), keyword )
                || contains( artifact.getTotalCommitCount(), keyword )
                || contains( artifact.getLatestCommitTimestamp(), keyword )
                || contains( artifact.getTitle(), keyword )
                || contains( artifact.getAbstractText(), keyword )
                || contains( artifact.getYear(), keyword )
                || contains( artifact.getPublisherName(), keyword )
                || contains( artifact.getPublisherVenue(), keyword )
                || contains( artifact.getVideoUrl(), keyword )
                || contains( artifact.getThumbnailUrl(), keyword )
                || contains( artifact.getDurationMillis(), keyword )
                || contains( artifact.getDescription(), keyword )
                || artifact.getAuthors().stream().anyMatch( author -> contains( author, keyword ) )
                || artifact.getKeywords().stream().anyMatch( value -> contains( value, keyword ) )
                || artifact.getManuscriptFile() != null
                && ( contains( artifact.getManuscriptFile().getSha256(), keyword )
                || contains( artifact.getManuscriptFile().getOriginalFilename(), keyword )
                || contains( artifact.getManuscriptFile().getMediaType(), keyword ) );
    }

    private boolean contains( Object value, String keyword )
    {
        return value != null && value.toString().toLowerCase( Locale.ROOT ).contains( keyword );
    }

    private ProjectResponse toResponse( Project project )
    {
        List<MemberResponse> members = project.getMembers().stream()
                .map( member -> new MemberResponse( member.getMemberId(), member.getRole() ) )
                .toList();
        List<UUID> labIds = project.getAcademicUnits().stream()
                .filter( unit -> unit.getType() == AcademicUnit.Type.LAB )
                .map( AcademicUnit::getId )
                .toList();
        List<UUID> departmentIds = project.getAcademicUnits().stream()
                .filter( unit -> unit.getType() == AcademicUnit.Type.DEPARTMENT )
                .map( AcademicUnit::getId )
                .toList();
        List<UUID> courseIds = project.getAcademicUnits().stream()
                .filter( unit -> unit.getType() == AcademicUnit.Type.COURSE )
                .map( AcademicUnit::getId )
                .toList();
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getRevisionVersion(),
                members,
                labIds,
                departmentIds,
                courseIds
        );
    }
}
