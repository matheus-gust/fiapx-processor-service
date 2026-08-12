package br.com.fiap.fiapx.processor.video.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.*;

class VideoProcessingServiceTest {

    private final VideoProcessingService service = new VideoProcessingService();

    @Test
    void process_throwsOnEmptyStream() {
        assertThatThrownBy(() -> service.process(new ByteArrayInputStream(new byte[0]), "empty.mp4"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void process_producesZipWhenFfmpegAvailable() throws Exception {
        // This test requires ffmpeg installed and a valid video file
        // Skipped in CI without ffmpeg
        Path sample = Path.of("src/test/resources/sample.mp4");
        if (!Files.exists(sample)) {
            return;
        }
        byte[] zip = service.process(Files.newInputStream(sample), "sample.mp4");
        assertThat(zip).isNotEmpty();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            assertThat(zis.getNextEntry()).isNotNull();
        }
    }
}
