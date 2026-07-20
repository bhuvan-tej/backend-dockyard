package com.dockyard.otpauth.entity;

import com.dockyard.otpauth.domain.DeliveryChannel;
import com.dockyard.otpauth.domain.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * OtpChallenge — one row per OTP we issue.
 *
 * SECURITY: we store only a BCrypt HASH of the code, never the code itself. If
 * the database leaked, the OTPs could not be read back. This mirrors how real
 * password/OTP stores work — and how the QR project stores metadata, not secrets.
 *
 * The row also holds everything needed to enforce validity and expiry:
 *   expiresAt   — the hard validity deadline
 *   attempts    — wrong-guess counter (brute-force protection)
 *   consumed    — single-use flag (an OTP works exactly once)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "otp_challenges",
        indexes = @Index(name = "idx_challenge_id", columnList = "challengeId", unique = true))
public class OtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public, unguessable handle the client uses to verify (a UUID). */
    @Column(nullable = false, unique = true, length = 36)
    private String challengeId;

    /** Who the OTP is for — an email address or phone number. */
    @Column(nullable = false, length = 320)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DeliveryChannel channel;

    /** BCrypt hash of the numeric code — never the code in clear text. */
    @Column(nullable = false, length = 100)
    private String codeHash;

    /** The hard validity deadline. After this instant the OTP is expired. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Number of wrong guesses so far. */
    @Column(nullable = false)
    private int attempts;

    /** Copied from config at creation time so a row is self-describing. */
    @Column(nullable = false)
    private int maxAttempts;

    /** True once the OTP has been used (or burned by hitting the attempt limit). */
    @Column(nullable = false)
    private boolean consumed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** True if the validity window has elapsed relative to {@code now}. */
    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    /** True if no more guesses are allowed. */
    public boolean isLocked() {
        return attempts >= maxAttempts;
    }
}