package com.example.save_template_backend;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TemplateService {
    private final TemplateRepository repo;

    public TemplateService(TemplateRepository repo) {
        this.repo = repo;
    }

    public Template saveTemplate(Template template) {
        return repo.save(template);
    }

    public List<Template> getAllTemplates() {
        return repo.findAll();
    }
}


