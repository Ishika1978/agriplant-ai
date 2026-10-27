package com.agriplant.ai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_lang", nullable = false)
    private Language preferredLang = Language.en;

    @Column(length = 80)
    private String state;

    @Enumerated(EnumType.STRING)
    private Landholding landholding = Landholding.small;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Language { en, hi }
    public enum Landholding { small, medium, large }

    // --- getters / setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Language getPreferredLang() { return preferredLang; }
    public void setPreferredLang(Language preferredLang) { this.preferredLang = preferredLang; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Landholding getLandholding() { return landholding; }
    public void setLandholding(Landholding landholding) { this.landholding = landholding; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
