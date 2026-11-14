package com.bu.service;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.bu.service.FunctionalTestSupport.COURSE_ID;
import static com.bu.service.FunctionalTestSupport.DEPARTMENT_ID;
import static com.bu.service.FunctionalTestSupport.LAB_ID;
import static com.bu.service.FunctionalTestSupport.createProject;
import static com.bu.service.FunctionalTestSupport.projectRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectFunctionalTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void clearProjects()
    {
        projectRepository.deleteAll();
    }

    @Test
    void managesProjectLifecycleAcademicUnitsAndSearch() throws Exception
    {
        JsonNode project = createProject(
                mockMvc,
                objectMapper,
                projectRequest(
                        "Assistive Vision Project",
                        "A campus research project",
                        List.of( new MemberRequest( "member-001", "student" ) )
                )
        );
        UUID projectId = UUID.fromString( project.get( "id" ).asText() );
        long createdAt = project.get( "createdAt" ).asLong();
        long updatedAt = project.get( "updatedAt" ).asLong();
        assertThat( project.get( "revisionVersion" ).asInt() ).isEqualTo( 1 );
        assertThat( project.get( "deleted" ) ).isNull();

        mockMvc.perform( get( "/api/v1/projects/{projectId}", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.artifacts" ).doesNotExist() );
        mockMvc.perform( get( "/api/v1/labs" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );
        mockMvc.perform( get( "/api/v1/departments" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );
        mockMvc.perform( get( "/api/v1/courses" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) );

        ProjectRequest associationOnlyUpdate = projectRequest(
                "Assistive Vision Project",
                "A campus research project",
                List.of(
                        new MemberRequest( "member-001", "student" ),
                        new MemberRequest( "member-001", "team lead" )
                )
        );
        mockMvc.perform( put( "/api/v1/projects/{projectId}", projectId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( associationOnlyUpdate ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.revisionVersion" ).value( 1 ) )
                .andExpect( jsonPath( "$.members.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$.updatedAt" ).value( updatedAt ) );

        mockMvc.perform( put( "/api/v1/projects/{projectId}", projectId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( projectRequest(
                                "Assistive Vision Project, revised",
                                "A campus research project",
                                associationOnlyUpdate.members()
                        ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.revisionVersion" ).value( 2 ) );

        mockMvc.perform( get( "/api/v1/projects" )
                        .param( "keyword", "revised" )
                        .param( "labId", LAB_ID.toString() )
                        .param( "departmentId", DEPARTMENT_ID.toString() )
                        .param( "courseId", COURSE_ID.toString() )
                        .param( "timeRange.createdFrom", Long.toString( createdAt ) )
                        .param( "timeRange.createdTo", Long.toString( createdAt ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].id" ).value( projectId.toString() ) );

        mockMvc.perform( delete( "/api/v1/projects/{projectId}", projectId ) )
                .andExpect( status().isNoContent() );
        assertThat( projectRepository.findById( projectId ).orElseThrow().getUpdatedAt() )
                .isGreaterThan( updatedAt );
        mockMvc.perform( get( "/api/v1/projects" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
        mockMvc.perform( post( "/api/v1/projects/purge" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.purgedProjectCount" ).value( 1 ) );
        assertThat( projectRepository.findById( projectId ) ).isEmpty();
    }
}
