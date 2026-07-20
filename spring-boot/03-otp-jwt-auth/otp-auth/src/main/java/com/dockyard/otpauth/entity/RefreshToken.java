package com.dockyard.otpauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * RefreshToken — server-side state for one issued refresh token.
 *
 * Access tokens stay fully stateless (verified by signature alone). Refresh
 * tokens, however, are tracked here by their JWT id ({@code jti}) so they can be
 * ROTATED and REVOKED — the two things a stateless JWT cannot do on its own.
 *
 * ROTATION: each time a refresh token is used, it is marked {@code revoked} and a
 * brand-new one is issued. REUSE DETECTION: if a token that is already revoked is
 * presented again, that signals theft, and every token for the subject is revoked.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens",
        indexes = @Index(name = "idx_refresh_token_id", columnList = "tokenId", unique = true))
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The JWT id ({@code jti}) carried inside the refresh token. */
    @Column(nullable = false, unique = true, length = 36)
    private String tokenId;

    /** Who the token belongs to (the identifier / JWT subject). */
    @Column(nullable = false, length = 320)
    private String subject;

    /** Mirrors the JWT expiry, so expired rows can be pruned later. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** True once the token has been rotated away or revoked. */
    @Column(nullable = false)
    private boolean revoked;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}