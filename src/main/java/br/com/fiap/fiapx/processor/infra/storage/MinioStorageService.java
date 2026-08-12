package br.com.fiap.fiapx.processor.infra.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public InputStream downloadFile(String key) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao baixar: " + e.getMessage(), e);
        }
    }

    public String uploadZip(File zipFile, String videoId, String userEmail) {
        String key = "zips/" + userEmail + "/" + videoId + ".zip";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(key)
                    .stream(new FileInputStream(zipFile), zipFile.length(), -1)
                    .contentType("application/zip")
                    .build());
            log.info("Zip uploaded: {}", key);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao fazer upload do zip: " + e.getMessage(), e);
        }
    }
}
