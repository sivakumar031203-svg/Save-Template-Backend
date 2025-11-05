package com.example.save_template_backend.controller;

import com.example.save_template_backend.service.TransferHmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer-hm")
@CrossOrigin(origins = "http://localhost:3000")
public class TransferHmController {

    @Autowired
    private TransferHmService service;

    @PostMapping("/save-html")
    public ResponseEntity<String> saveHtml(@RequestBody TemplateRequest request) {
        service.saveHtmlToFile(request.getHtml(), request.getFilename());
        return ResponseEntity.ok("HTML saved successfully!");
    }

    @PostMapping("/save-and-pdf")
    public ResponseEntity<byte[]> saveAndPdf(@RequestBody TemplateRequest request) {
        byte[] pdf = service.saveAndGeneratePdf(request.getHtml(), request.getFilename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getFilename() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    public static class TemplateRequest {
        private String html;
        private String filename;

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }
}


