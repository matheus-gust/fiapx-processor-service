package br.com.fiap.fiapx.processor.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoProcessingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "zip_s3_key")
    private String zipS3Key;

    @Column(nullable = false)
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
