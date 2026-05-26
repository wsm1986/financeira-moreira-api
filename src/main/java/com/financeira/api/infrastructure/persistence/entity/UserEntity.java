package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "uid", nullable = false, unique = true)
    private String uid;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserEntity() {}

    public static UserEntity fromDomain(User user) {
        UserEntity e = new UserEntity();
        e.uid = user.getUid();
        e.email = user.getEmail();
        e.name = user.getName();
        e.createdAt = user.getCreatedAt();
        return e;
    }

    public User toDomain() {
        User u = new User(uid, email, name);
        u.setCreatedAt(createdAt);
        return u;
    }

    public String getUid()        { return uid; }
    public String getEmail()      { return email; }
    public String getName()       { return name; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUid(String uid)            { this.uid = uid; }
    public void setEmail(String email)        { this.email = email; }
    public void setName(String name)          { this.name = name; }
    public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }
}
