package br.com.fiap.fiapx.processor.infra.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks NotificationPublisher publisher;

    @Test
    void publishError_shouldSendToNotificationRoute() {
        UUID videoId = UUID.randomUUID();

        publisher.publishError(videoId, "user@test.com", "Erro ffmpeg");

        verify(rabbitTemplate).convertAndSend(
                eq("fiapx.videos"),
                eq("video.notify"),
                any(NotificationPublisher.NotificationMessage.class)
        );
    }
}
