package com.bu.service.repositoryindexer.controller;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.service.repositoryindexer.api.ProjectApi.ProjectRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectResponse;
import com.bu.service.repositoryindexer.api.ProjectApi.PurgeResponse;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import com.bu.service.repositoryindexer.featureservice.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController
{
    private final ProjectService projects;

    public ProjectController( ProjectService projects )
    {
        this.projects = projects;
    }

    @GetMapping
    public List<ProjectResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID labId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String artifactType,
            @RequestParam(name = "timeRange.createdFrom", required = false) Long createdFrom,
            @RequestParam(name = "timeRange.createdTo", required = false) Long createdTo
    )
    {
        ArtifactType type = artifactType == null ? null : ArtifactType.fromPathToken( artifactType );
        return projects.search( keyword, labId, departmentId, courseId, type, createdFrom, createdTo );
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create( @RequestBody ProjectRequest request )
    {
        ProjectResponse response = projects.create( request );
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path( "/{id}" )
                .buildAndExpand( response.id() )
                .toUri();
        return ResponseEntity.created( location ).body( response );
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getById( @PathVariable UUID projectId )
    {
        return projects.getById( projectId );
    }

    @PutMapping("/{projectId}")
    public ProjectResponse update( @PathVariable UUID projectId, @RequestBody ProjectRequest request )
    {
        return projects.update( projectId, request );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete( @PathVariable UUID projectId )
    {
        projects.softDelete( projectId );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purge")
    public ResponseEntity<PurgeResponse> purge()
    {
        return ResponseEntity.ok( new PurgeResponse( projects.purgeDeleted() ) );
    }
}
