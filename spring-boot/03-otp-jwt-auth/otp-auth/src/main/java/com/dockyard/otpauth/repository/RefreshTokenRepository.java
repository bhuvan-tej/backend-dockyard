package com.dockyard.otpauth.repository;

import com.dockyard.otpauth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * RefreshTokenRepository — database access for tracked refresh tokens.
 *
 * Supports rotation (look a token up by its jti, then revoke it) and reuse
 * detection (revoke every token for a subject when a used one is replayed).
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Find a stored refresh token by the jti carried in the JWT. */
    Optional<RefreshToken> findByTokenId(String tokenId);

    /**
     * Revoke every still-active refresh token for a subject. Called when a
     * revoked token is replayed — the safe response to a suspected theft is to
     * invalidate the whole session family and force re-authentication.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.subject = :subject AND r.revoked = false")
    int revokeAllForSubject(@Param("subject") String subject);
}