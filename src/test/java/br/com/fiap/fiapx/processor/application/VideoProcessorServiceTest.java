package br.com.fiap.fiapx.processor.application;

import br.com.fiap.fiapx.processor.application.messages.VideoProcessingMessage;
import br.com.fiap.fiapx.processor.infra.messaging.NotificationPublisher;
import br.com.fiap.fiapx.processor.infra.persistence.entity.VideoProcessingJpaEntity;
import br.com.fiap.fiapx.processor.infra.persistence.repository.VideoProcessingJpaRepository;
import br.com.fiap.fiapx.processor.infra.processing.FfmpegVideoProcessor;
import br.com.fiap.fiapx.processor.infra.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoProcessorServiceTest {

    @Mock private VideoProcessingJpaRepository jpaRepository;
    @Mock private MinioStorageService storageService;
    @Mock private FfmpegVideoProcessor ffmpegProcessor;
    @Mock private NotificationPublisher notificationPublisher;

    @InjectMocks
    private VideoProcessorService processorService;

    @TempDir
    Path tempDir;

    private UUID videoId;
    private VideoProcessingMessage message;
    private VideoProcessingJpaEntity entity;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        message = new VideoProcessingMessage(videoId, "videos/user/video.mp4", "user@test.com");
        entity = VideoProcessingJpaEntity.builder()
                .id(videoId).userEmail("user@test.com").s3Key("videos/user/video.mp4").status("PENDING").build();
    }

    @Test
    void process_shouldUpdateStatusToDoneOnSuccess() throws Exception {
        File zipFile = tempDir.resolve("result.zip").toFile();
        zipFile.createNewFile();

        when(jpaRepository.findById(videoId)).thenReturn(Optional.of(entity));
        when(storageService.downloadFile("videos/user/video.mp4"))
                .thenReturn(new ByteArrayInputStream("video-data".getBytes()));
        when(ffmpegProcessor.extractFramesToZip(any(), eq(videoId.toString()))).thenReturn(zipFile);
        when(storageService.uploadZip(any(), eq(videoId.toString()), eq("user@test.com")))
                .thenReturn("zips/user/video.zip");

        processorService.process(message);

        verify(jpaRepository, atLeastOnce()).save(argThat(e -> "DONE".equals(e.getStatus())));
        verify(notificationPublisher, never()).publishError(any(), any(), any());
    }

    @Test
    void process_shouldUpdateStatusToErrorAndNotifyOnFailure() throws Exception {
        when(jpaRepository.findById(videoId)).thenReturn(Optional.of(entity));
        when(storageService.downloadFile(any())).thenThrow(new RuntimeException("MinIO down"));

        processorService.process(message);

        verify(jpaRepository, atLeastOnce()).save(argThat(e -> "ERROR".equals(e.getStatus())));
        verify(notificationPublisher).publishError(eq(videoId), eq("user@test.com"), contains("MinIO down"));
    }

    @Test
    void process_shouldSetProcessingStatusFirst() throws Exception {
        File zipFile = tempDir.resolve("result.zip").toFile();
        zipFile.createNewFile();

        when(jpaRepository.findById(videoId)).thenReturn(Optional.of(entity));
        when(storageService.downloadFile(any())).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(ffmpegProcessor.extractFramesToZip(any(), any())).thenReturn(zipFile);
        when(storageService.uploadZip(any(), any(), any())).thenReturn("zip-key");

        processorService.process(message);

        verify(jpaRepository, atLeast(2)).save(any());
    }
}
