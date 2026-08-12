package br.com.fiap.fiapx.processor.video.infra.messaging;

import br.com.fiap.fiapx.processor.config.MinioConfig;
import br.com.fiap.fiapx.processor.config.RabbitConfig;
import br.com.fiap.fiapx.processor.video.application.VideoProcessingService;
import br.com.fiap.fiapx.processor.video.domain.valueobjects.VideoStatus;
import br.com.fiap.fiapx.processor.video.infra.persistence.repository.VideoJpaRepository;
import br.com.fiap.fiapx.processor.video.infra.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoProcessingConsumer {

    private final VideoJpaRepository videoJpaRepository;
    private final MinioStorageService storageService;
    private final VideoProcessingService processingService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.VIDEO_PROCESSING_QUEUE)
    public void consume(VideoProcessingMessage message) {
        log.info("Received video.processing for videoId={}", message.videoId());
        videoJpaRepository.updateStatus(message.videoId(), VideoStatus.PROCESSING, null, null);
        try {
            InputStream videoStream = storageService.downloadFile(MinioConfig.VIDEOS_BUCKET, message.s3Key());
            byte[] zipBytes = processingService.process(videoStream, message.originalFilename());

            String zipKey = "zips/" + message.userId() + "/" + message.videoId() + ".zip";
            storageService.uploadBytes(MinioConfig.ZIPS_BUCKET, zipKey, zipBytes, "application/zip");

            videoJpaRepository.updateStatus(message.videoId(), VideoStatus.DONE, zipKey, null);
            log.info("Video processed successfully videoId={}", message.videoId());
        } catch (Exception e) {
            log.error("Error processing videoId={}: {}", message.videoId(), e.getMessage(), e);
            videoJpaRepository.updateStatus(message.videoId(), VideoStatus.ERROR, null, e.getMessage());
            VideoNotificationMessage notification = new VideoNotificationMessage(
                    message.videoId(), message.userId(), message.userEmail(), e.getMessage());
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.VIDEO_NOTIFICATION_QUEUE, notification);
        }
    }
}
