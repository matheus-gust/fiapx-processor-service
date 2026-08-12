package br.com.fiap.fiapx.processor.infra.persistence.repository;

import br.com.fiap.fiapx.processor.infra.persistence.entity.VideoProcessingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoProcessingJpaRepository extends JpaRepository<VideoProcessingJpaEntity, UUID> {}
