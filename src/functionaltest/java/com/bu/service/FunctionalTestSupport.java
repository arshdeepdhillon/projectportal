package com.bu.service;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.service.repositoryindexer.api.ProjectApi.MemberRequest;
import com.bu.service.repositoryindexer.api.ProjectApi.ProjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class FunctionalTestSupport
{
    static final UUID LAB_ID = UUID.fromString( "11111111-1111-1111-1111-111111111111" );
    static final UUID DEPARTMENT_ID = UUID.fromString( "33333333-3333-3333-3333-333333333333" );
    static final UUID COURSE_ID = UUID.fromString( "55555555-5555-5555-5555-555555555555" );

    private FunctionalTestSupport()
    {
    }

    static ProjectRequest projectRequest( String title, String description, List<MemberRequest> members )
    {
        return new ProjectRequest(
                title,
                description,
                members,
                List.of( LAB_ID ),
                List.of( DEPARTMENT_ID ),
                List.of( COURSE_ID )
        );
    }

    static JsonNode createProject( MockMvc mockMvc, ObjectMapper objectMapper, ProjectRequest request ) throws Exception
    {
        byte[] response = mockMvc.perform( post( "/api/v1/projects" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( objectMapper.writeValueAsBytes( request ) ) )
                .andExpect( status().isCreated() )
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return objectMapper.readTree( response );
    }
}
