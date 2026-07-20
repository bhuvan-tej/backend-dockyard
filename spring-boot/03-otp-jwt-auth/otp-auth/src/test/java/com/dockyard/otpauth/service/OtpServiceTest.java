package com.dockyard.otpauth.service;

import com.dockyard.otpauth.config.OtpProperties;
import com.dockyard.otpauth.domain.DeliveryChannel;
import com.dockyard.otpauth.domain.OtpPurpose;
import com.dockyard.otpauth.dto.OtpChallengeResponse;
import com.dockyard.otpauth.dto.OtpRequest;
import com.dockyard.otpauth.dto.OtpVerifyRequest;
import com.dockyard.otpauth.dto.TokenResponse;
import com.dockyard.otpauth.entity.OtpChallenge;
import com.dockyard.otpauth.exception.ChallengeNotFoundException;
import com.dockyard.otpauth.exception.InvalidOtpException;
import com.dockyard.otpauth.exception.OtpExpiredException;
import com.dockyard.otpauth.exception.TooManyAttemptsException;
import com.dockyard.otpauth.repository.OtpChallengeRepository;
import com.dockyard.otpauth.security.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * OtpServiceTest — the business rules of request + verify, with collaborators
 * mocked. Confirms every validity/expiry gate is enforced in the right order.
 */
@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock private OtpChallengeRepository repository;
    @Mock private OtpGenerator otpGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenIssuer tokenIssuer;

    private OtpProperties properties;
    private OtpService service;

    @BeforeEach
    void setUp() {
        properties = new OtpProperties();
        properties.setLength(6);
        properties.setTtlSeconds(300);
        properties.setMaxAttempts(5);
        properties.setResendCooldownSeconds(30);
        properties.setExposeCode(true);
        service = new OtpService(repository, otpGenerator, passwordEncoder, tokenIssuer, properties);
    }

    private OtpRequest requestFor(String id) {
        OtpRequest r = new OtpRequest();
        r.setIdentifier(id);
        r.setPurpose(OtpPurpose.LOGIN);
        r.setChannel(DeliveryChannel.EMAIL);
        return r;
    }

    private OtpChallenge challenge() {
        return OtpChallenge.builder()
                .challengeId("cid-1")
                .identifier("ada@x.io")
                .purpose(OtpPurpose.LOGIN)
                .channel(DeliveryChannel.EMAIL)
                .codeHash("HASH")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .maxAttempts(5)
                .consumed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- requestOtp -------------------------------------------------------

    @Test
    @DisplayName("requestOtp stores a hash and returns the dev code in dev mode")
    void requestOtp_returnsChallenge() {
        when(repository.findTopByIdentifierAndPurposeOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(otpGenerator.generate()).thenReturn("123456");
        when(passwordEncoder.encode("123456")).thenReturn("HASH");
        when(repository.save(any(OtpChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        OtpChallengeResponse response = service.requestOtp(requestFor("ada@x.io"));

        assertThat(response.getChallengeId()).isNotBlank();
        assertThat(response.getDevCode()).isEqualTo("123456");
        assertThat(response.getTtlSeconds()).isEqualTo(300);

        // The stored code must be the HASH, never the plain code.
        verify(repository).save(argThat(c -> c.getCodeHash().equals("HASH")));
    }

    @Test
    @DisplayName("requestOtp hides the code when expose-code is false")
    void requestOtp_hidesCodeInProdMode() {
        properties.setExposeCode(false);
        when(repository.findTopByIdentifierAndPurposeOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(otpGenerator.generate()).thenReturn("123456");
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(repository.save(any(OtpChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

        OtpChallengeResponse response = service.requestOtp(requestFor("ada@x.io"));

        assertThat(response.getDevCode()).isNull();
    }

    @Test
    @DisplayName("requestOtp within the cooldown is rejected with 429")
    void requestOtp_cooldown() {
        OtpChallenge recent = challenge();
        recent.setCreatedAt(LocalDateTime.now().minusSeconds(5)); // < 30s cooldown
        when(repository.findTopByIdentifierAndPurposeOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(recent));

        assertThatThrownBy(() -> service.requestOtp(requestFor("ada@x.io")))
                .isInstanceOf(TooManyAttemptsException.class);
        verify(repository, never()).save(any());
    }

    // --- verify -----------------------------------------------------------

    @Test
    @DisplayName("verify with the correct code consumes the OTP and issues tokens")
    void verify_success() {
        OtpChallenge c = challenge();
        when(repository.findByChallengeId("cid-1")).thenReturn(Optional.of(c));
        when(passwordEncoder.matches("123456", "HASH")).thenReturn(true);
        when(tokenIssuer.issueFor("ada@x.io"))
                .thenReturn(TokenResponse.builder().subject("ada@x.io").accessToken("AT").build());

        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setChallengeId("cid-1");
        req.setCode("123456");

        TokenResponse response = service.verify(req);

        assertThat(response.getAccessToken()).isEqualTo("AT");
        assertThat(c.isConsumed()).isTrue();
        verify(tokenIssuer).issueFor("ada@x.io");
    }

    @Test
    @DisplayName("verify with a wrong code burns an attempt and throws 401")
    void verify_wrongCode() {
        OtpChallenge c = challenge();
        when(repository.findByChallengeId("cid-1")).thenReturn(Optional.of(c));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setChallengeId("cid-1");
        req.setCode("000000");

        assertThatThrownBy(() -> service.verify(req))
                .isInstanceOf(InvalidOtpException.class);
        assertThat(c.getAttempts()).isEqualTo(1);
        verify(tokenIssuer, never()).issueFor(anyString());
    }

    @Test
    @DisplayName("verify of an expired OTP throws 410")
    void verify_expired() {
        OtpChallenge c = challenge();
        c.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(repository.findByChallengeId("cid-1")).thenReturn(Optional.of(c));

        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setChallengeId("cid-1");
        req.setCode("123456");

        assertThatThrownBy(() -> service.verify(req))
                .isInstanceOf(OtpExpiredException.class);
    }

    @Test
    @DisplayName("verify of an already-consumed OTP throws 401")
    void verify_consumed() {
        OtpChallenge c = challenge();
        c.setConsumed(true);
        when(repository.findByChallengeId("cid-1")).thenReturn(Optional.of(c));

        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setChallengeId("cid-1");
        req.setCode("123456");

        assertThatThrownBy(() -> service.verify(req))
                .isInstanceOf(InvalidOtpException.class);
    }

    @Test
    @DisplayName("the final wrong attempt locks the challenge (429)")
    void verify_lockout() {
        OtpChallenge c = challenge();
        c.setAttempts(4); // one away from the limit of 5
        when(repository.findByChallengeId("cid-1")).thenReturn(Optional.of(c));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setChallengeId("cid-1");
        req.setCode("000000");

        assertThatThrownBy(() -> service.verify(req))
                .isInstanceOf(TooManyAttemptsException.class);
        assertThat(c.isConsumed()).isTrue(); // burned
    }

    @Test
    @DisplayName("verify with an unknown challengeId throws 404")
    void verify_unknownChallenge() {
        when(repository.findByChallengeId("nope")).thenReturn(Optional.empty());

        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setChallengeId("nope");
        req.setCode("123456");

        assertThatThrownBy(() -> service.verify(req))
                .isInstanceOf(ChallengeNotFoundException.class);
    }
}

