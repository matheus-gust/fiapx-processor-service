package br.com.fiap.fiapx.processor.infra.messaging;

import br.com.fiap.fiapx.processor.application.VideoProcessorService;
import br.com.fiap.fiapx.processor.application.messages.VideoProcessingMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoProcessingConsumerTest {

    @Mock private VideoProcessorService processorService;
    @InjectMocks private VideoProcessingConsumer consumer;

    @Test
    void consume_shouldDelegateToProcessorService() {
        VideoProcessingMessage message = new VideoProcessingMessage(
                UUID.randomUUID(), "videos/key.mp4", "user@test.com");

        consumer.consume(message);

        verify(processorService).process(message);
    }
}
