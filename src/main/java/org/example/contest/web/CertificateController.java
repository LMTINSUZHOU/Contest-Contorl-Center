package org.example.contest.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.CertificateService;
import org.example.contest.web.dto.ManagementDtos;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CertificateController {
    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/certificates/awards/{awardId}")
    public ManagementDtos.CertificateView upload(
            @PathVariable UUID awardId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return certificateService.upload(awardId, file, principal);
    }

    @PostMapping("/admin/certificates/{certificateId}/replace")
    public ManagementDtos.CertificateView replace(
            @PathVariable UUID certificateId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return certificateService.replace(certificateId, file, principal);
    }

    @GetMapping("/certificates/{certificateId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable UUID certificateId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateService.DownloadFile download = certificateService.download(certificateId, principal);
        String encodedName = URLEncoder.encode(download.certificate().getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.certificate().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(download.resource());
    }
}
