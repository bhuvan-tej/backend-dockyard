package com.dockyard.otpauth.repository;

import com.dockyard.otpauth.domain.OtpPurpose;
import com.dockyard.otpauth.entity.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * OtpChallengeRepository — database access for OTP challenges.
 *
 * Spring Data JPA generates the implementation at runtime. We add:
 *   - lookup by the public challengeId (used on verify)
 *   - the most-recent challenge for an identifier+purpose (used to enforce
 *     the resend cooldown)
 */
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    /** Find a challenge by its public handle. */
    Optional<OtpChallenge> findByChallengeId(String challengeId);

    /** The latest challenge for an identifier+purpose, for resend throttling. */
    Optional<OtpChallenge> findTopByIdentifierAndPurposeOrderByCreatedAtDesc(
            String identifier, OtpPurpose purpose);
}