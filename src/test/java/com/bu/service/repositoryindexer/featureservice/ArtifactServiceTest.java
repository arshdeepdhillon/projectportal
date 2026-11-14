package com.bu.service.repositoryindexer.featureservice;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ArtifactRepository;
import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.entity.Artifact;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import com.bu.service.repositoryindexer.entity.ManuscriptFile;
import com.bu.service.repositoryindexer.entity.Project;
import com.bu.service.repositoryindexer.featureservice.ArtifactService.FileUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtifactServiceTest
{
    @Mock
    private ProjectRepository projects;

    @Mock
    private ArtifactRepository artifacts;

    private ArtifactService service;

    @BeforeEach
    void setUp()
    {
        service = new ArtifactService( projects, artifacts );
    }

    @Test
    void uploadCreatesAndReplacesManuscriptFileMetadataAndBytes() throws Exception
    {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();
        Project project = new Project( "Project", "Description", 100 );
        project.setId( projectId );
        Artifact manuscript = new Artifact( project, ArtifactType.MANUSCRIPTS );
        manuscript.setId( artifactId );
        when( artifacts.findByIdAndProject_IdAndArtifactTypeAndDeleted(
                artifactId,
                projectId,
                ArtifactType.MANUSCRIPTS,
                0
        ) ).thenReturn( Optional.of( manuscript ) );
        when( artifacts.save( any( Artifact.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

        FileUploadResult created = service.uploadFile(
                projectId,
                artifactId,
                new MockMultipartFile( "file", "paper.pdf", "application/pdf", "hello world".getBytes() )
        );

        assertThat( created.replacing() ).isFalse();
        assertThat( created.file().sha256() )
                .isEqualTo( "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9" );
        assertThat( manuscript.getManuscriptFile().getFileContent().getContent() ).isEqualTo( "hello world".getBytes() );

        FileUploadResult replaced = service.uploadFile(
                projectId,
                artifactId,
                new MockMultipartFile( "file", "revised.pdf", "application/pdf", "revised".getBytes() )
        );

        assertThat( replaced.replacing() ).isTrue();
        assertThat( replaced.file().originalFilename() ).isEqualTo( "revised.pdf" );
        assertThat( manuscript.getManuscriptFile().getFileContent().getContent() ).isEqualTo( "revised".getBytes() );
        verify( artifacts, times( 2 ) ).save( manuscript );
    }

    @Test
    void deletingManuscriptFileRetainsArtifactAndRemovesItsFile()
    {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();
        Project project = new Project( "Project", "Description", 100 );
        project.setId( projectId );
        Artifact manuscript = new Artifact( project, ArtifactType.MANUSCRIPTS );
        manuscript.setId( artifactId );
        manuscript.setManuscriptFile( new ManuscriptFile(
                manuscript,
                "content".getBytes(),
                "hash",
                "paper.pdf",
                "application/pdf"
        ) );
        when( artifacts.findByIdAndProject_IdAndArtifactTypeAndDeleted(
                artifactId,
                projectId,
                ArtifactType.MANUSCRIPTS,
                0
        ) ).thenReturn( Optional.of( manuscript ) );
        when( artifacts.save( any( Artifact.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );

        service.deleteFile( projectId, artifactId );

        assertThat( manuscript.getManuscriptFile() ).isNull();
        assertThat( manuscript.getArtifactType() ).isEqualTo( ArtifactType.MANUSCRIPTS );
        verify( artifacts ).save( manuscript );
    }
}
