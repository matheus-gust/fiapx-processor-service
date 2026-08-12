package br.com.fiap.fiapx.processor.video.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.*;

class VideoProcessingServiceTest {

    private final VideoProcessingService service = new VideoProcessingService();

    @TempDir
    Path tempDir;

    @Test
    void process_throwsOnEmptyStream() {
        assertThatThrownBy(() -> service.process(new ByteArrayInputStream(new byte[0]), "empty.mp4"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void process_throwsOnInvalidVideo() {
        byte[] notAVideo = "this is not a video file".getBytes();
        assertThatThrownBy(() -> service.process(new ByteArrayInputStream(notAVideo), "fake.mp4"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void zipFrames_producesValidZip() throws Exception {
        Path frame1 = tempDir.resolve("frame_0001.jpg");
        Path frame2 = tempDir.resolve("frame_0002.jpg");
        Files.write(frame1, "fake-jpeg-1".getBytes());
        Files.write(frame2, "fake-jpeg-2".getBytes());

        Method zipFrames = VideoProcessingService.class.getDeclaredMethod("zipFrames", List.class);
        zipFrames.setAccessible(true);
        byte[] zip = (byte[]) zipFrames.invoke(service, List.of(frame1, frame2));

        assertThat(zip).isNotEmpty();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry1 = zis.getNextEntry();
            assertThat(entry1).isNotNull();
            assertThat(entry1.getName()).isEqualTo("frame_0001.jpg");
            ZipEntry entry2 = zis.getNextEntry();
            assertThat(entry2).isNotNull();
            assertThat(entry2.getName()).isEqualTo("frame_0002.jpg");
        }
    }

    @Test
    void deleteDirectory_cleansUpFiles() throws Exception {
        Path dir = Files.createTempDirectory("fiapx-test-");
        Files.write(dir.resolve("file1.txt"), "content".getBytes());
        Files.createDirectory(dir.resolve("subdir"));
        Files.write(dir.resolve("subdir").resolve("file2.txt"), "content".getBytes());

        Method deleteDir = VideoProcessingService.class.getDeclaredMethod("deleteDirectory", Path.class);
        deleteDir.setAccessible(true);
        deleteDir.invoke(service, dir);

        assertThat(dir).doesNotExist();
    }

    @Test
    void deleteDirectory_handlesNonExistentDir() throws Exception {
        Path nonExistent = tempDir.resolve("does-not-exist");

        Method deleteDir = VideoProcessingService.class.getDeclaredMethod("deleteDirectory", Path.class);
        deleteDir.setAccessible(true);

        assertThatNoException().isThrownBy(() -> deleteDir.invoke(service, nonExistent));
    }
}
