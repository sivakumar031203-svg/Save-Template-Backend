package com.example.save_template_backend;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "http://localhost:3000")
public class TemplateController {

    private final TemplateService templateService;
    private final PdfService pdfService;

    public TemplateController(TemplateService templateService, PdfService pdfService) {
        this.templateService = templateService;
        this.pdfService = pdfService;
    }

    @PostMapping
    public ResponseEntity<?> saveTemplate(@RequestBody Template template) {
        Template saved = templateService.saveTemplate(template);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody Map<String, Object> payload) {
        try {
            String html = (String) payload.get("html");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            byte[] pdfBytes = pdfService.generatePdf(html, data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("generated.pdf").build());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }
}


