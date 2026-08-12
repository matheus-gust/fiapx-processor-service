package br.com.fiap.fiapx.processor.infra.messaging;

import br.com.fiap.fiapx.config.RabbitConfig;
import br.com.fiap.fiapx.processor.application.VideoProcessorService;
import br.com.fiap.fiapx.processor.application.messages.VideoProcessingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoProcessingConsumer {

    private final VideoProcessorService processorService;

    @RabbitListener(queues = RabbitConfig.PROCESSING_QUEUE, concurrency = "3-10")
    public void consume(VideoProcessingMessage message) {
        log.info("Received processing request for video: {}", message.videoId());
        processorService.process(message);
    }
}
