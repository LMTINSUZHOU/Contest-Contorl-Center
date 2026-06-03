package org.example.contest.web.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.example.contest.security.UserPrincipal;
import org.example.contest.service.ImportExportService;
import org.example.contest.web.dto.ImportDtos;
import org.springframework.core.io.ByteArrayResource;
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
@RequestMapping("/api/admin/import-export")
public class AdminImportExportController {
    private final ImportExportService importExportService;

    public AdminImportExportController(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @GetMapping("/{type}/template")
    public ResponseEntity<ByteArrayResource> template(@PathVariable String type) {
        return excel(type + "-template.xlsx", importExportService.template(type));
    }

    @PostMapping("/{type}/import")
    public ImportDtos.ImportResult importFile(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return importExportService.importFile(type, file, principal.id());
    }

    @GetMapping("/{type}/export")
    public ResponseEntity<ByteArrayResource> export(@PathVariable String type) {
        return excel(type + "-export.xlsx", importExportService.export(type));
    }

    @GetMapping("/{type}/import-errors/{jobId}")
    public ResponseEntity<ByteArrayResource> errors(@PathVariable String type, @PathVariable UUID jobId) {
        return excel(type + "-import-errors.xlsx", importExportService.errorReport(jobId));
    }

    private ResponseEntity<ByteArrayResource> excel(String filename, byte[] bytes) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(new ByteArrayResource(bytes));
    }
}
