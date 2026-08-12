package br.com.fiap.fiapx.processor.infra.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FfmpegVideoProcessorTest {

    private final FfmpegVideoProcessor processor = new FfmpegVideoProcessor();

    @TempDir
    Path tempDir;

    @Test
    void extractFramesToZip_shouldThrowWhenFfmpegFails() {
        File invalidVideo = tempDir.resolve("fake.mp4").toFile();
        try (FileWriter fw = new FileWriter(invalidVideo)) { fw.write("not-a-real-video"); }
        catch (Exception e) { throw new RuntimeException(e); }

        assertThatThrownBy(() -> processor.extractFramesToZip(invalidVideo, "test-id"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void extractFramesToZip_shouldThrowWhenVideoFileIsEmpty() {
        File emptyFile = tempDir.resolve("empty.mp4").toFile();
        try { emptyFile.createNewFile(); } catch (Exception e) { throw new RuntimeException(e); }

        assertThatThrownBy(() -> processor.extractFramesToZip(emptyFile, "test-id"))
                .isInstanceOf(RuntimeException.class);
    }
}
