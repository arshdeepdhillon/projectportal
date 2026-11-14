package com.bu.service.repositoryindexer.entity;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "manuscript_files")
@Getter
@Setter
@NoArgsConstructor
public class ManuscriptFile
{
    @Id
    @Column(name = "artifact_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "artifact_id", nullable = false)
    private Artifact artifact;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @OneToOne(mappedBy = "fileMetadata", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ManuscriptFileData fileContent;

    public ManuscriptFile( Artifact artifact, byte[] content, String sha256, String originalFilename, String mediaType )
    {
        this.id = artifact.getId();
        this.artifact = artifact;
        this.sha256 = sha256;
        this.originalFilename = originalFilename;
        this.mediaType = mediaType;
        this.fileContent = new ManuscriptFileData( this, content );
    }
}
