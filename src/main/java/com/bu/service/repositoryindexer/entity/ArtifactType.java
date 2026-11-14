package com.bu.service.repositoryindexer.entity;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

public enum ArtifactType
{
    REPOSITORIES( "repositories" ),
    MANUSCRIPTS( "manuscripts" ),
    VIDEOS( "videos" );

    private final String pathToken;

    ArtifactType( String pathToken )
    {
        this.pathToken = pathToken;
    }

    public static ArtifactType fromPathToken( String pathToken )
    {
        for ( ArtifactType type : values() )
        {
            if ( type.pathToken.equals( pathToken ) )
            {
                return type;
            }
        }
        throw new IllegalArgumentException( "Unknown artifact type: " + pathToken );
    }

    public String pathToken()
    {
        return pathToken;
    }
}
