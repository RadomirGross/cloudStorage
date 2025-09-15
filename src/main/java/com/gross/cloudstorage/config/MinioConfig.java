package com.gross.cloudstorage.config;

import io.minio.MinioClient;
import io.minio.admin.MinioAdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.url}")
    private String url;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient
                .builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }


    @Bean
    public MinioAdminClient minioAdminClient() {
        return MinioAdminClient
                .builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
