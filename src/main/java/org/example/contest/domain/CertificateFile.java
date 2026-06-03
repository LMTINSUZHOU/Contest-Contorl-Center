package org.example.contest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 证书元数据表。实际文件保存在本地目录，后续可平滑迁移为 MinIO/S3。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "certificate_files")
public class CertificateFile extends AuditableEntity {
    @Column(nullable = false)
    private UUID awardId;

    @Column(nullable = false, length = 260)
    private String originalName;

    @Column(nullable = false, length = 600)
    private String storagePath;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    private UUID uploadedByUserId;

    @Column(nullable = false)
    private boolean active = true;
}
