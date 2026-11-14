package com.bu.service.repositoryindexer.featureservice;

/*
 * Agent Contribution Weight: [100%] Agent Generated / [0%] Human Refactored
 */

import com.bu.repository.ArtifactRepository;
import com.bu.repository.ProjectRepository;
import com.bu.service.repositoryindexer.api.ArtifactApi.ArtifactResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.FileMetadataResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.GitRepositoryRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.GitRepositoryResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.ManuscriptRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.ManuscriptResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.PublisherRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.PublisherResponse;
import com.bu.service.repositoryindexer.api.ArtifactApi.VideoRequest;
import com.bu.service.repositoryindexer.api.ArtifactApi.VideoResponse;
import com.bu.service.repositoryindexer.entity.Artifact;
import com.bu.service.repositoryindexer.entity.ArtifactType;
import com.bu.service.repositoryindexer.entity.ManuscriptFile;
import com.bu.service.repositoryindexer.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class ArtifactService
{
    private static final int ACTIVE = 0;
    private static final int DELETED = 1;

    private final ProjectRepository projects;
    private final ArtifactRepository artifacts;

    public ArtifactService( ProjectRepository projects, ArtifactRepository artifacts )
    {
        this.projects = projects;
        this.artifacts = artifacts;
    }

    public ArtifactResponse createRepository( UUID projectId, GitRepositoryRequest request )
    {
        Artifact artifact = new Artifact( activeProject( projectId ), ArtifactType.REPOSITORIES );
        apply( artifact, request );
        return toResponse( artifacts.save( artifact ) );
    }

    public ArtifactResponse createManuscript( UUID projectId, ManuscriptRequest request )
    {
        Artifact artifact = new Artifact( activeProject( projectId ), ArtifactType.MANUSCRIPTS );
        apply( artifact, request );
        return toResponse( artifacts.save( artifact ) );
    }

    public ArtifactResponse createVideo( UUID projectId, VideoRequest request )
    {
        Artifact artifact = new Artifact( activeProject( projectId ), ArtifactType.VIDEOS );
        apply( artifact, request );
        return toResponse( artifacts.save( artifact ) );
    }

    @Transactional(readOnly = true)
    public List<ArtifactResponse> getAll( UUID projectId )
    {
        activeProject( projectId );
        return artifacts.findAllByProject_IdAndDeletedOrderById( projectId, ACTIVE ).stream()
                .map( this::toResponse )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArtifactResponse> getByType( UUID projectId, ArtifactType type )
    {
        activeProject( projectId );
        return artifacts.findAllByProject_IdAndArtifactTypeAndDeletedOrderById( projectId, type, ACTIVE ).stream()
                .map( this::toResponse )
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtifactResponse getById( UUID projectId, ArtifactType type, UUID artifactId )
    {
        return toResponse( activeArtifact( projectId, type, artifactId ) );
    }

    public ArtifactResponse updateRepository( UUID projectId, UUID artifactId, GitRepositoryRequest request )
    {
        Artifact artifact = activeArtifact( projectId, ArtifactType.REPOSITORIES, artifactId );
        apply( artifact, request );
        return toResponse( artifacts.save( artifact ) );
    }

    public ArtifactResponse updateManuscript( UUID projectId, UUID artifactId, ManuscriptRequest request )
    {
        Artifact artifact = activeArtifact( projectId, ArtifactType.MANUSCRIPTS, artifactId );
        apply( artifact, request );
        return toResponse( artifacts.save( artifact ) );
    }

    public ArtifactResponse updateVideo( UUID projectId, UUID artifactId, VideoRequest request )
    {
        Artifact artifact = activeArtifact( projectId, ArtifactType.VIDEOS, artifactId );
        apply( artifact, request );
        return toResponse( artifacts.save( artifact ) );
    }

    public void softDelete( UUID projectId, ArtifactType type, UUID artifactId )
    {
        Artifact artifact = activeArtifact( projectId, type, artifactId );
        artifact.setDeleted( DELETED );
        artifacts.save( artifact );
    }

    @Transactional(rollbackFor = IOException.class)
    public FileUploadResult uploadFile( UUID projectId, UUID artifactId, MultipartFile upload ) throws IOException
    {
        Artifact artifact = activeArtifact( projectId, ArtifactType.MANUSCRIPTS, artifactId );
        byte[] content = upload.getBytes();
        String hash = sha256( content );
        ManuscriptFile currentFile = artifact.getManuscriptFile();
        boolean replacing = currentFile != null;
        if ( replacing )
        {
            currentFile.getFileContent().setContent( content );
            currentFile.setSha256( hash );
            currentFile.setOriginalFilename( upload.getOriginalFilename() );
            currentFile.setMediaType( upload.getContentType() );
        }
        else
        {
            artifact.setManuscriptFile( new ManuscriptFile(
                    artifact,
                    content,
                    hash,
                    upload.getOriginalFilename(),
                    upload.getContentType()
            ) );
        }
        artifacts.save( artifact );
        ManuscriptFile savedFile = artifact.getManuscriptFile();
        return new FileUploadResult(
                replacing,
                new FileMetadataResponse( savedFile.getSha256(), savedFile.getOriginalFilename(), savedFile.getMediaType() )
        );
    }

    @Transactional(readOnly = true)
    public FileDownload getFile( UUID projectId, UUID artifactId )
    {
        ManuscriptFile file = activeManuscript( projectId, artifactId ).getManuscriptFile();
        return new FileDownload( file.getFileContent().getContent(), file.getOriginalFilename(), file.getMediaType() );
    }

    public void deleteFile( UUID projectId, UUID artifactId )
    {
        Artifact artifact = activeArtifact( projectId, ArtifactType.MANUSCRIPTS, artifactId );
        artifact.setManuscriptFile( null );
        artifacts.save( artifact );
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public byte[] downloadManuscriptsZip( UUID projectId ) throws IOException
    {
        activeProject( projectId );
        List<Artifact> manuscripts = artifacts.findAllByProject_IdAndArtifactTypeAndDeletedOrderById(
                projectId,
                ArtifactType.MANUSCRIPTS,
                ACTIVE
        );
        try (
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream( bytes )
        )
        {
            for ( Artifact manuscript : manuscripts )
            {
                ManuscriptFile file = manuscript.getManuscriptFile();
                if ( file != null )
                {
                    zip.putNextEntry( new ZipEntry( manuscript.getId() + "/" + file.getOriginalFilename() ) );
                    zip.write( file.getFileContent().getContent() );
                    zip.closeEntry();
                }
            }
            zip.finish();
            return bytes.toByteArray();
        }
    }

    private Project activeProject( UUID projectId )
    {
        return projects.findByIdAndDeleted( projectId, ACTIVE ).orElseThrow();
    }

    private Artifact activeManuscript( UUID projectId, UUID artifactId )
    {
        return activeArtifact( projectId, ArtifactType.MANUSCRIPTS, artifactId );
    }

    private Artifact activeArtifact( UUID projectId, ArtifactType type, UUID artifactId )
    {
        return artifacts.findByIdAndProject_IdAndArtifactTypeAndDeleted(
                artifactId,
                projectId,
                type,
                ACTIVE
        ).orElseThrow();
    }

    private void apply( Artifact artifact, GitRepositoryRequest request )
    {
        artifact.setRepoUrl( request.repoUrl() );
        artifact.setTargetBranch( request.targetBranch() );
        artifact.setPrimaryTechStack( request.primaryTechStack() );
        artifact.setTotalCommitCount( request.totalCommitCount() );
        artifact.setLatestCommitTimestamp( request.latestCommitTimestamp() );
    }

    private void apply( Artifact artifact, ManuscriptRequest request )
    {
        artifact.setTitle( request.title() );
        artifact.setAbstractText( request.abstractText() );
        artifact.setAuthors( request.authors() );
        artifact.setKeywords( request.keywords() );
        artifact.setYear( request.year() );
        PublisherRequest publisher = request.publisher();
        artifact.setPublisherName( publisher == null ? null : publisher.name() );
        artifact.setPublisherVenue( publisher == null ? null : publisher.venue() );
    }

    private void apply( Artifact artifact, VideoRequest request )
    {
        artifact.setVideoUrl( request.videoUrl() );
        artifact.setThumbnailUrl( request.thumbnailUrl() );
        artifact.setDurationMillis( request.durationMillis() );
        artifact.setDescription( request.description() );
    }

    private ArtifactResponse toResponse( Artifact artifact )
    {
        return switch ( artifact.getArtifactType() )
        {
            case REPOSITORIES -> new GitRepositoryResponse(
                    artifact.getId(),
                    artifact.getProject().getId(),
                    artifact.typePathToken(),
                    artifact.getRepoUrl(),
                    artifact.getTargetBranch(),
                    artifact.getPrimaryTechStack(),
                    artifact.getTotalCommitCount(),
                    artifact.getLatestCommitTimestamp()
            );
            case MANUSCRIPTS -> manuscriptResponse( artifact );
            case VIDEOS -> new VideoResponse(
                    artifact.getId(),
                    artifact.getProject().getId(),
                    artifact.typePathToken(),
                    artifact.getVideoUrl(),
                    artifact.getThumbnailUrl(),
                    artifact.getDurationMillis(),
                    artifact.getDescription()
            );
        };
    }

    private ManuscriptResponse manuscriptResponse( Artifact artifact )
    {
        PublisherResponse publisher = artifact.getPublisherName() == null && artifact.getPublisherVenue() == null
                ? null
                : new PublisherResponse( artifact.getPublisherName(), artifact.getPublisherVenue() );
        ManuscriptFile file = artifact.getManuscriptFile();
        FileMetadataResponse fileMetadata = file == null
                ? null
                : new FileMetadataResponse( file.getSha256(), file.getOriginalFilename(), file.getMediaType() );
        return new ManuscriptResponse(
                artifact.getId(),
                artifact.getProject().getId(),
                artifact.typePathToken(),
                artifact.getTitle(),
                artifact.getAbstractText(),
                artifact.getAuthors().stream().toList(),
                artifact.getKeywords().stream().toList(),
                artifact.getYear(),
                publisher,
                fileMetadata
        );
    }

    private String sha256( byte[] content )
    {
        try
        {
            return HexFormat.of().formatHex( MessageDigest.getInstance( "SHA-256" ).digest( content ) );
        } catch ( NoSuchAlgorithmException exception )
        {
            throw new IllegalStateException( exception );
        }
    }

    public record FileUploadResult( boolean replacing, FileMetadataResponse file )
    {
    }

    public static final class FileDownload
    {
        private final byte[] content;
        private final String originalFilename;
        private final String mediaType;

        public FileDownload( byte[] content, String originalFilename, String mediaType )
        {
            this.content = content;
            this.originalFilename = originalFilename;
            this.mediaType = mediaType;
        }

        public byte[] content()
        {
            return content;
        }

        public String originalFilename()
        {
            return originalFilename;
        }

        public String mediaType()
        {
            return mediaType;
        }
    }
}
