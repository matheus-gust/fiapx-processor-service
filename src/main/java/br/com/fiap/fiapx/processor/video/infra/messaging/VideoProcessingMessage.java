package br.com.fiap.fiapx.processor.video.infra.messaging;

import java.util.UUID;

public record VideoProcessingMessage(
        UUID videoId,
        UUID userId,
        String s3Key,
        String originalFilename,
        String userEmail
) {}
