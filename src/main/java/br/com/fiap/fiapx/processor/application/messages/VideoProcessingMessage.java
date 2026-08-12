package br.com.fiap.fiapx.processor.application.messages;

import java.util.UUID;

public record VideoProcessingMessage(UUID videoId, String s3Key, String userEmail) {}
