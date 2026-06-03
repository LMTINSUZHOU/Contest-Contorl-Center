package org.example.contest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.example.contest.common.ApiException;
import org.example.contest.config.AppProperties;
import org.example.contest.domain.CertificateFile;
import org.example.contest.repository.CertificateFileRepository;
import org.example.contest.security.UserPrincipal;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CertificateService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final AppProperties appProperties;
    private final CertificateFileRepository certificateRepository;
    private final AwardService awardService;

    public CertificateService(
            AppProperties appProperties,
            CertificateFileRepository certificateRepository,
            AwardService awardService
    ) {
        this.appProperties = appProperties;
        this.certificateRepository = certificateRepository;
        this.awardService = awardService;
    }

    @Transactional
    public ManagementDtos.CertificateView upload(UUID awardId, MultipartFile file, UserPrincipal principal) {
        awardService.load(awardId);
        if (!awardService.canUserUploadCertificate(principal.id(), principal.user().getRole(), awardId)) {
            throw ApiException.forbidden("无权为该获奖记录上传证书");
        }
        return saveAsActive(awardId, file, principal.id());
    }

    @Transactional
    public ManagementDtos.CertificateView replace(UUID certificateId, MultipartFile file, UserPrincipal principal) {
        CertificateFile existing = certificateRepository.findById(certificateId)
                .orElseThrow(() -> ApiException.notFound("证书不存在"));
        existing.setActive(false);
        return saveAsActive(existing.getAwardId(), file, principal.id());
    }

    public DownloadFile download(UUID certificateId, UserPrincipal principal) {
        CertificateFile certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> ApiException.notFound("证书不存在"));
        if (!awardService.canUserAccessAward(principal.id(), principal.user().getRole(), certificate.getAwardId())) {
            throw ApiException.forbidden("无权下载该证书");
        }
        Path path = Path.of(certificate.getStoragePath());
        if (!Files.exists(path)) {
            throw ApiException.notFound("证书文件不存在");
        }
        return new DownloadFile(certificate, new FileSystemResource(path));
    }

    private ManagementDtos.CertificateView saveAsActive(UUID awardId, MultipartFile file, UUID uploaderId) {
        validateFile(file);
        try {
            Path root = Path.of(appProperties.getStorage().getCertificateDir()).toAbsolutePath().normalize();
            Files.createDirectories(root);
            String extension = extensionOf(file.getOriginalFilename());
            String storedName = awardId + "-" + UUID.randomUUID() + extension;
            Path target = root.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            certificateRepository.findFirstByAwardIdAndActiveTrueOrderByCreatedAtDesc(awardId)
                    .ifPresent(active -> active.setActive(false));

            CertificateFile certificate = new CertificateFile();
            certificate.setAwardId(awardId);
            certificate.setOriginalName(safeOriginalName(file.getOriginalFilename()));
            certificate.setStoragePath(target.toString());
            certificate.setContentType(file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType());
            certificate.setFileSize(file.getSize());
            certificate.setUploadedByUserId(uploaderId);
            certificate.setActive(true);
            CertificateFile saved = certificateRepository.save(certificate);
            return new ManagementDtos.CertificateView(saved.getId(), saved.getAwardId(), saved.getOriginalName(), saved.getContentType(), saved.getFileSize());
        } catch (IOException exception) {
            throw ApiException.badRequest("证书保存失败：" + exception.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("证书文件不能为空");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw ApiException.badRequest("证书仅支持 PDF、JPG、PNG");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw ApiException.badRequest("证书文件类型不支持：" + contentType);
        }
    }

    private String extensionOf(String fileName) {
        String safeName = safeOriginalName(fileName).toLowerCase(Locale.ROOT);
        int index = safeName.lastIndexOf('.');
        return index >= 0 ? safeName.substring(index) : "";
    }

    private String safeOriginalName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "certificate";
        }
        return Path.of(fileName).getFileName().toString();
    }

    public record DownloadFile(CertificateFile certificate, FileSystemResource resource) {
    }
}
