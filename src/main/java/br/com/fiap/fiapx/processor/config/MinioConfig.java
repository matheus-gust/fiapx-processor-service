package br.com.fiap.fiapx.processor.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String url;
    private String accessKey;
    private String secretKey;

    public static final String VIDEOS_BUCKET = "fiapx-videos";
    public static final String ZIPS_BUCKET = "fiapx-zips";

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        ensureBuckets(client);
        return client;
    }

    private void ensureBuckets(MinioClient client) {
        for (String bucket : new String[]{VIDEOS_BUCKET, ZIPS_BUCKET}) {
            try {
                if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
            } catch (Exception e) {
                log.warn("Could not ensure bucket {}: {}", bucket, e.getMessage());
            }
        }
    }
}
