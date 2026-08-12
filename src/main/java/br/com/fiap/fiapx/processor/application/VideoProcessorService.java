package br.com.fiap.fiapx.processor.application;

import br.com.fiap.fiapx.processor.application.messages.VideoProcessingMessage;
import br.com.fiap.fiapx.processor.infra.messaging.NotificationPublisher;
import br.com.fiap.fiapx.processor.infra.persistence.entity.VideoProcessingJpaEntity;
import br.com.fiap.fiapx.processor.infra.persistence.repository.VideoProcessingJpaRepository;
import br.com.fiap.fiapx.processor.infra.processing.FfmpegVideoProcessor;
import br.com.fiap.fiapx.processor.infra.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProcessorService {

    private final VideoProcessingJpaRepository jpaRepository;
    private final MinioStorageService storageService;
    private final FfmpegVideoProcessor ffmpegProcessor;
    private final NotificationPublisher notificationPublisher;

    public void process(VideoProcessingMessage message) {
        log.info("Processing video: {}", message.videoId());
        updateStatus(message.videoId().toString(), "PROCESSING", null, null);

        File videoFile = null;
        File zipFile = null;
        try {
            videoFile = downloadToTemp(message.s3Key(), message.videoId().toString());
            zipFile = ffmpegProcessor.extractFramesToZip(videoFile, message.videoId().toString());
            String zipKey = storageService.uploadZip(zipFile, message.videoId().toString(), message.userEmail());
            updateStatus(message.videoId().toString(), "DONE", zipKey, null);
            log.info("Video processed successfully: {}", message.videoId());
        } catch (Exception e) {
            log.error("Error processing video {}: {}", message.videoId(), e.getMessage(), e);
            updateStatus(message.videoId().toString(), "ERROR", null, e.getMessage());
            notificationPublisher.publishError(message.videoId(), message.userEmail(), e.getMessage());
        } finally {
            if (videoFile != null) videoFile.delete();
            if (zipFile != null) zipFile.delete();
        }
    }

    private File downloadToTemp(String s3Key, String videoId) throws IOException {
        File tmp = Files.createTempFile("video_" + videoId, ".mp4").toFile();
        try (InputStream is = storageService.downloadFile(s3Key);
             FileOutputStream fos = new FileOutputStream(tmp)) {
            is.transferTo(fos);
        }
        return tmp;
    }

    private void updateStatus(String videoId, String status, String zipKey, String errorMessage) {
        jpaRepository.findById(java.util.UUID.fromString(videoId)).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setZipS3Key(zipKey);
            entity.setErrorMessage(errorMessage);
            jpaRepository.save(entity);
        });
    }
}
