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

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void uploadCreatesManuscriptFileMetadataAndBytes() throws Exception
    {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();
        Artifact manuscript = stubManuscript( projectId, artifactId, null );

        FileUploadResult created = service.uploadFile(
                projectId,
                artifactId,
                new MockMultipartFile(
                        "file",
                        "paper.pdf",
                        "application/pdf",
                        "hello world".getBytes( StandardCharsets.UTF_8 )
                )
        );

        assertThat( created.replacing() ).isFalse();
        assertThat( created.file().sha256() )
                .isEqualTo( "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9" );
        assertThat( manuscript.getManuscriptFile().getFileContent().getContent() )
                .isEqualTo( "hello world".getBytes( StandardCharsets.UTF_8 ) );
        verify( artifacts ).save( manuscript );
    }

    @Test
    void uploadReplacesManuscriptFileContentAndMetadata() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID artifactId = UUID.randomUUID();
        Project project = new Project( "Project", "Description", 100 );
        project.setId( projectId );
        Artifact manuscript = new Artifact( project, ArtifactType.MANUSCRIPTS );
        manuscript.setId( artifactId );
        manuscript.setManuscriptFile( new ManuscriptFile(
                manuscript,
                "original".getBytes( StandardCharsets.UTF_8 ),
                "original-hash",
                "original.pdf",
                "application/pdf"
        ) );
        stubActiveManuscript( projectId, artifactId, manuscript );
        byte[] replacement = "revised".getBytes( StandardCharsets.UTF_8 );

        FileUploadResult result = service.uploadFile(
                projectId,
                artifactId,
                new MockMultipartFile( "file", "revised.pdf", "application/pdf", replacement )
        );

        assertThat( result.replacing() ).isTrue();
        assertThat( result.file().originalFilename() ).isEqualTo( "revised.pdf" );
        assertThat( result.file().sha256() )
                .isEqualTo( "1e4de31f94535968f4e4ca8fd577a4598e984bc5c89dae095924e944c121c7a3" );
        assertThat( manuscript.getManuscriptFile().getFileContent().getContent() ).isEqualTo( replacement );
        verify( artifacts ).save( manuscript );
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

    private Artifact stubManuscript( UUID projectId, UUID artifactId, ManuscriptFile file ) {
        Project project = new Project( "Project", "Description", 100 );
        project.setId( projectId );
        Artifact manuscript = new Artifact( project, ArtifactType.MANUSCRIPTS );
        manuscript.setId( artifactId );
        manuscript.setManuscriptFile( file );
        stubActiveManuscript( projectId, artifactId, manuscript );
        return manuscript;
    }

    private void stubActiveManuscript( UUID projectId, UUID artifactId, Artifact manuscript ) {
        when( artifacts.findByIdAndProject_IdAndArtifactTypeAndDeleted(
                artifactId,
                projectId,
                ArtifactType.MANUSCRIPTS,
                0
        ) ).thenReturn( Optional.of( manuscript ) );
        when( artifacts.save( any( Artifact.class ) ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    }
}
