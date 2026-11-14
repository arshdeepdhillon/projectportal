package com.bu.service.repositoryindexer.entity;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "artifacts")
@Getter
@Setter
@NoArgsConstructor
public class Artifact
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false)
    private ArtifactType artifactType;

    @Column(nullable = false)
    private int deleted;

    @Column(name = "repo_url", columnDefinition = "TEXT")
    private String repoUrl;

    @Column(name = "target_branch")
    private String targetBranch;

    @Column(name = "primary_tech_stack")
    private String primaryTechStack;

    @Column(name = "total_commit_count")
    private Integer totalCommitCount;

    @Column(name = "latest_commit_timestamp")
    private Long latestCommitTimestamp;

    @Column(name = "manuscript_title")
    private String title;

    @Column(name = "manuscript_abstract", columnDefinition = "TEXT")
    private String abstractText;

    @ElementCollection
    @CollectionTable(name = "manuscript_authors", joinColumns = @JoinColumn(name = "artifact_id"))
    @Column(name = "author_name")
    private List<String> authors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "manuscript_keywords", joinColumns = @JoinColumn(name = "artifact_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @Column(name = "publication_year")
    private Integer year;

    @Column(name = "publisher_name")
    private String publisherName;

    @Column(name = "publisher_venue")
    private String publisherVenue;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "duration_millis")
    private Long durationMillis;

    @Column(name = "video_description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ManuscriptFile manuscriptFile;

    public Artifact( Project project, ArtifactType artifactType )
    {
        this.project = project;
        this.artifactType = artifactType;
        this.deleted = 0;
    }

    public String typePathToken()
    {
        return artifactType.pathToken();
    }
}
