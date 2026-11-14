package com.bu.service.repositoryindexer.entity;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

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
@Table(name = "manuscript_file_contents")
@Getter
@Setter
@NoArgsConstructor
public class ManuscriptFileData
{
    @Id
    @Column(name = "artifact_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "artifact_id", nullable = false)
    private ManuscriptFile fileMetadata;

    @Column(name = "file_content", nullable = false, columnDefinition = "BYTEA")
    private byte[] content;

    public ManuscriptFileData( ManuscriptFile fileMetadata, byte[] content )
    {
        this.id = fileMetadata.getId();
        this.fileMetadata = fileMetadata;
        this.content = content;
    }
}
