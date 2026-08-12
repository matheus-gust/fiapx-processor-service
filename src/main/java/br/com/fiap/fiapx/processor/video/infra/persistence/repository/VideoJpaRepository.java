package br.com.fiap.fiapx.processor.video.infra.persistence.repository;

import br.com.fiap.fiapx.processor.video.infra.persistence.entity.VideoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, UUID> {

    @Modifying
    @Transactional
    @Query("UPDATE VideoJpaEntity v SET v.status = :status, v.zipS3Key = :zipKey, v.errorMessage = :error, v.updatedAt = CURRENT_TIMESTAMP WHERE v.id = :id")
    void updateStatus(@Param("id") UUID id,
                      @Param("status") br.com.fiap.fiapx.processor.video.domain.valueobjects.VideoStatus status,
                      @Param("zipKey") String zipS3Key,
                      @Param("error") String errorMessage);
}
