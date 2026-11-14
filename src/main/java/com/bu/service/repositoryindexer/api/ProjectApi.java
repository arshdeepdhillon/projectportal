package com.bu.service.repositoryindexer.api;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import java.util.List;
import java.util.UUID;

public final class ProjectApi
{
    private ProjectApi()
    {
    }

    public record MemberRequest( String memberId, String role )
    {
    }

    public record MemberResponse( String memberId, String role )
    {
    }

    public record ProjectRequest(
            String title,
            String description,
            List<MemberRequest> members,
            List<UUID> labIds,
            List<UUID> departmentIds,
            List<UUID> courseIds
    )
    {
    }

    public record ProjectResponse(
            UUID id,
            String title,
            String description,
            long createdAt,
            long updatedAt,
            int revisionVersion,
            List<MemberResponse> members,
            List<UUID> labIds,
            List<UUID> departmentIds,
            List<UUID> courseIds
    )
    {
    }

    public record PurgeResponse( int purgedProjectCount )
    {
    }
}
