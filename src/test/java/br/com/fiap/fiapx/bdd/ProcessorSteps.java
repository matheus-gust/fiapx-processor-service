package br.com.fiap.fiapx.bdd;

import br.com.fiap.fiapx.processor.application.VideoProcessorService;
import br.com.fiap.fiapx.processor.application.messages.VideoProcessingMessage;
import br.com.fiap.fiapx.processor.infra.messaging.NotificationPublisher;
import br.com.fiap.fiapx.processor.infra.persistence.entity.VideoProcessingJpaEntity;
import br.com.fiap.fiapx.processor.infra.persistence.repository.VideoProcessingJpaRepository;
import br.com.fiap.fiapx.processor.infra.processing.FfmpegVideoProcessor;
import br.com.fiap.fiapx.processor.infra.storage.MinioStorageService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ProcessorSteps {

    private final VideoProcessingJpaRepository jpaRepository = Mockito.mock(VideoProcessingJpaRepository.class);
    private final MinioStorageService storageService = Mockito.mock(MinioStorageService.class);
    private final FfmpegVideoProcessor ffmpegProcessor = Mockito.mock(FfmpegVideoProcessor.class);
    private final NotificationPublisher notificationPublisher = Mockito.mock(NotificationPublisher.class);
    private final VideoProcessorService service = new VideoProcessorService(
            jpaRepository, storageService, ffmpegProcessor, notificationPublisher);

    private VideoProcessingMessage message;
    private UUID videoId;
    private VideoProcessingJpaEntity entity;
    private final ArgumentCaptor<VideoProcessingJpaEntity> entityCaptor =
            ArgumentCaptor.forClass(VideoProcessingJpaEntity.class);

    @Given("que existe uma mensagem de processamento para o video com email {string}")
    public void mensagemProcessamento(String email) throws Exception {
        videoId = UUID.randomUUID();
        message = new VideoProcessingMessage(videoId, "videos/key.mp4", email);
        entity = VideoProcessingJpaEntity.builder().id(videoId).userEmail(email)
                .s3Key("videos/key.mp4").status("PENDING").build();
        File zip = Files.createTempFile("test", ".zip").toFile();
        when(jpaRepository.findById(videoId)).thenReturn(Optional.of(entity));
        when(storageService.downloadFile(any())).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(ffmpegProcessor.extractFramesToZip(any(), any())).thenReturn(zip);
        when(storageService.uploadZip(any(), any(), any())).thenReturn("zip-key");
    }

    @Given("que existe uma mensagem de processamento com video invalido")
    public void mensagemVideoInvalido() {
        videoId = UUID.randomUUID();
        message = new VideoProcessingMessage(videoId, "videos/bad.mp4", "user@test.com");
        entity = VideoProcessingJpaEntity.builder().id(videoId).userEmail("user@test.com")
                .s3Key("videos/bad.mp4").status("PENDING").build();
        when(jpaRepository.findById(videoId)).thenReturn(Optional.of(entity));
        when(storageService.downloadFile(any())).thenThrow(new RuntimeException("Storage error"));
    }

    @When("o processador consome a mensagem")
    public void processaVideo() {
        service.process(message);
    }

    @When("o processador tenta processar e falha")
    public void processaEFalha() {
        service.process(message);
    }

    @Then("o status do video e atualizado para {string}")
    public void verificaStatus(String status) {
        verify(jpaRepository, atLeastOnce()).save(entityCaptor.capture());
        boolean found = entityCaptor.getAllValues().stream().anyMatch(e -> status.equals(e.getStatus()));
        assertThat(found).isTrue();
    }

    @Then("nenhuma notificacao de erro e enviada")
    public void nenhumaNotificacao() {
        verify(notificationPublisher, never()).publishError(any(), any(), any());
    }

    @Then("uma notificacao de erro e publicada para {string}")
    public void notificacaoEnviada(String email) {
        verify(notificationPublisher).publishError(eq(videoId), eq(email), any());
    }
}
