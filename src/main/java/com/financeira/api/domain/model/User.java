package com.financeira.api.domain.model;

import java.time.Instant;

public class User {

    private String uid;
    private String email;
    private String name;
    private Instant createdAt;

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public String getUid()       { return uid; }
    public String getEmail()     { return email; }
    public String getName()      { return name; }
    public Instant getCreatedAt(){ return createdAt; }

    public void setName(String name)  { this.name = name; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
