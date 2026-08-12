package br.com.fiap.fiapx.processor.infra.messaging;

import br.com.fiap.fiapx.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishError(UUID videoId, String userEmail, String errorMessage) {
        NotificationMessage message = new NotificationMessage(videoId, userEmail, errorMessage);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_NOTIFICATION, message);
        log.info("Published error notification for video {}", videoId);
    }

    public record NotificationMessage(UUID videoId, String userEmail, String errorMessage) {}
}
