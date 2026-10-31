package com.vickynishad.tech.m2m.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "api_clients")
public class ApiClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_name", nullable = false, unique = true)
    private String clientName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiKey> apiKeys;

    public ApiClient() {}

    public ApiClient(String clientName, String contactEmail) {
        this.clientName = clientName;
        this.contactEmail = contactEmail;
    }

    public Long getId() { return id; }
    public String getClientName() { return clientName; }
    public String getContactEmail() { return contactEmail; }
}