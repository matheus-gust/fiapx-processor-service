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
    void extractFramesToZip_shouldReturnZipWithFramesForValidVideo() throws Exception {
        File video = tempDir.resolve("synthetic.mp4").toFile();
        Process generate = new ProcessBuilder(
                "ffmpeg", "-f", "lavfi", "-i", "color=c=black:s=32x32:d=2:r=1",
                "-y", video.getAbsolutePath())
                .redirectErrorStream(true).start();
        generate.waitFor();
        assertThat(video).exists();

        File zip = processor.extractFramesToZip(video, "success-id");

        assertThat(zip).exists();
        try (ZipFile zipFile = new ZipFile(zip)) {
            assertThat(zipFile.size()).isGreaterThan(0);
        }
    }

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
