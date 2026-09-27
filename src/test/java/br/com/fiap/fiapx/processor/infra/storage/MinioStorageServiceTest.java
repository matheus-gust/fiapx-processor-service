package br.com.fiap.fiapx.processor.infra.storage;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    private static final String BUCKET = "fiapx-videos";

    @Mock MinioClient minioClient;

    @TempDir Path tempDir;

    MinioStorageService service;

    @BeforeEach
    void setUp() {
        service = new MinioStorageService(minioClient);
        ReflectionTestUtils.setField(service, "bucket", BUCKET);
    }

    @Test
    void downloadFile_shouldDelegateToMinioClient() throws Exception {
        when(minioClient.getObject(any())).thenReturn(null);

        assertThat(service.downloadFile("videos/key.mp4")).isNull();
    }

    @Test
    void downloadFile_shouldWrapExceptionOnFailure() throws Exception {
        when(minioClient.getObject(any())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> service.downloadFile("videos/key.mp4"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao baixar");
    }

    @Test
    void uploadZip_shouldUploadAndReturnKey() throws Exception {
        File zip = tempDir.resolve("out.zip").toFile();
        try (FileWriter fw = new FileWriter(zip)) { fw.write("fake-zip-content"); }

        String key = service.uploadZip(zip, "video-123", "user@test.com");

        assertThat(key).isEqualTo("zips/user@test.com/video-123.zip");
    }

    @Test
    void uploadZip_shouldWrapExceptionOnFailure() throws Exception {
        File zip = tempDir.resolve("out.zip").toFile();
        try (FileWriter fw = new FileWriter(zip)) { fw.write("fake-zip-content"); }
        when(minioClient.putObject(any())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> service.uploadZip(zip, "video-123", "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao fazer upload do zip");
    }
}
