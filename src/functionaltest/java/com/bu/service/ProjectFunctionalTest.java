package com.bu.service;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ArtifactRepository;
import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ArtifactApi.GitRepositoryRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static com.bu.service.FunctionalTestSupport.COURSE_ID;
import static com.bu.service.FunctionalTestSupport.DEPARTMENT_ID;
import static com.bu.service.FunctionalTestSupport.LAB_ID;
import static com.bu.service.FunctionalTestSupport.createProject;
import static com.bu.service.FunctionalTestSupport.projectRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

    @Autowired
    private ArtifactRepository artifactRepository;

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
        assertThat( project.get( "title" ).asText() ).isEqualTo( "Assistive Vision Project" );
        assertThat( project.get( "description" ).asText() ).isEqualTo( "A campus research project" );
        assertThat( project.get( "revisionVersion" ).asInt() ).isEqualTo( 1 );
        assertThat( project.get( "deleted" ) ).isNull();

        mockMvc.perform( get( "/api/v1/projects/{projectId}", projectId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.artifacts" ).doesNotExist() )
                .andExpect( jsonPath( "$.labIds[0]" ).value( LAB_ID.toString() ) )
                .andExpect( jsonPath( "$.departmentIds[0]" ).value( DEPARTMENT_ID.toString() ) )
                .andExpect( jsonPath( "$.courseIds[0]" ).value( COURSE_ID.toString() ) );
        mockMvc.perform( get( "/api/v1/labs" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$[*].code", containsInAnyOrder( "VASL-01", "QCIG-02" ) ) );
        mockMvc.perform( get( "/api/v1/departments" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$[*].code", containsInAnyOrder( "CS", "EE" ) ) );
        mockMvc.perform( get( "/api/v1/courses" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 2 ) )
                .andExpect( jsonPath( "$[*].courseCode", containsInAnyOrder( "CS-701", "EE-502" ) ) )
                .andExpect( jsonPath(
                        "$[*].courseName", containsInAnyOrder(
                                "Advanced Deep Learning Frameworks",
                                "Embedded Robotics and Perception"
                        )
                ) );

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

        MvcResult scalarUpdate = mockMvc.perform( put( "/api/v1/projects/{projectId}", projectId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( projectRequest(
                                "Assistive Vision Project, revised",
                                "A campus research project",
                                associationOnlyUpdate.members()
                        ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.title" ).value( "Assistive Vision Project, revised" ) )
                .andExpect( jsonPath( "$.description" ).value( "A campus research project" ) )
                .andExpect( jsonPath( "$.createdAt" ).value( createdAt ) )
                .andExpect( jsonPath( "$.revisionVersion" ).value( 2 ) )
                .andReturn();
        long scalarUpdatedAt = objectMapper.readTree(
                scalarUpdate.getResponse().getContentAsByteArray()
        ).get( "updatedAt" ).asLong();
        assertThat( scalarUpdatedAt ).isGreaterThan( updatedAt );

        mockMvc.perform( get( "/api/v1/projects" )
                        .param( "timeRange.createdFrom", Long.toString( createdAt - 1 ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
        mockMvc.perform( get( "/api/v1/projects" )
                        .param( "timeRange.createdTo", Long.toString( createdAt + 1 ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) );
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

        GitRepositoryRequest repository = new GitRepositoryRequest(
                "https://github.com/example/purge-child",
                "main",
                "Java",
                1,
                createdAt
        );
        MvcResult artifactCreate = mockMvc.perform( post(
                        "/api/v1/projects/{projectId}/artifacts/repositories",
                        projectId
                )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( repository ) ) )
                .andExpect( status().isCreated() )
                .andReturn();
        UUID artifactId = UUID.fromString( objectMapper.readTree(
                artifactCreate.getResponse().getContentAsByteArray()
        ).get( "id" ).asText() );

        mockMvc.perform( delete( "/api/v1/projects/{projectId}", projectId ) )
                .andExpect( status().isNoContent() );
        assertThat( projectRepository.findById( projectId ).orElseThrow().getUpdatedAt() )
                .isGreaterThan( scalarUpdatedAt );
        assertThat( projectRepository.findById( projectId ).orElseThrow().getRevisionVersion() ).isEqualTo( 2 );
        mockMvc.perform( get( "/api/v1/projects" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );
        mockMvc.perform( post( "/api/v1/projects/purge" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.purgedProjectCount" ).value( 1 ) );
        assertThat( projectRepository.findById( projectId ) ).isEmpty();
        assertThat( artifactRepository.findById( artifactId ) ).isEmpty();
    }
}
