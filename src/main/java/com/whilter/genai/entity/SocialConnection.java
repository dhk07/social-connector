package com.whilter.genai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "social_connections")
public class SocialConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String provider;
    private String providerUserId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiry;
    private String username;
    private boolean connected;
} 