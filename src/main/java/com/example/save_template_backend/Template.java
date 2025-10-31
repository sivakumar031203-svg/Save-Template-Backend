package com.example.save_template_backend;

import jakarta.persistence.*;

@Entity
@Table(name = "templates_align")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    private String content;

    public Template() {}

    public Template(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContent() { return content; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContent(String content) { this.content = content; }
}


