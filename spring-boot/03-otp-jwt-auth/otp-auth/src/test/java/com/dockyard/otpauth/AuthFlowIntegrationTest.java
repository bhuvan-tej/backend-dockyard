package com.dockyard.otpauth;

import com.dockyard.otpauth.dto.OtpRequest;
import com.dockyard.otpauth.dto.OtpVerifyRequest;
import com.dockyard.otpauth.dto.RefreshRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthFlowIntegrationTest — the end-to-end journey against the REAL app.
 *
 * This is the equivalent of the QR project's encode→decode round-trip: it drives
 * the whole system (security, JPA, JWT, OTP hashing) exactly as a client would.
 *
 *   request OTP → verify → call protected /me → refresh → /me again
 *
 * plus the important negative paths (no token, wrong code).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("full journey: request → verify → /me → refresh → /me")
    void fullHappyPath() throws Exception {
        // 1. Request an OTP. Dev mode returns the code as devCode.
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setIdentifier("integration@dockyard.dev");

        MvcResult requestResult = mockMvc.perform(post("/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challengeId").exists())
                .andExpect(jsonPath("$.devCode").exists())
                .andExpect(jsonPath("$.ttlSeconds").value(300))
                .andReturn();

        JsonNode challengeJson = objectMapper.readTree(requestResult.getResponse().getContentAsString());
        String challengeId = challengeJson.get("challengeId").asText();
        String code = challengeJson.get("devCode").asText();

        // 2. Verify the OTP → receive tokens.
        OtpVerifyRequest verify = new OtpVerifyRequest();
        verify.setChallengeId(challengeId);
        verify.setCode(code);

        MvcResult verifyResult = mockMvc.perform(post("/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        JsonNode tokens = objectMapper.readTree(verifyResult.getResponse().getContentAsString());
        String accessToken = tokens.get("accessToken").asText();
        String refreshToken = tokens.get("refreshToken").asText();

        // 3. Protected /me WITHOUT a token → 401.
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // 4. Protected /me WITH the access token → 200 and the right subject.
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("integration@dockyard.dev"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());

        // 5. Refresh → new access token, still bound to the same subject.
        RefreshRequest refresh = new RefreshRequest();
        refresh.setRefreshToken(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String newAccessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        assertThat(newAccessToken).isNotBlank();

        // 6. The refreshed access token also works on /me.
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("integration@dockyard.dev"));
    }

    @Test
    @DisplayName("verifying with a wrong code returns 401 with attemptsRemaining")
    void wrongCodeIsRejected() throws Exception {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setIdentifier("wrong-code@dockyard.dev");

        MvcResult requestResult = mockMvc.perform(post("/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String challengeId = objectMapper.readTree(requestResult.getResponse().getContentAsString())
                .get("challengeId").asText();

        OtpVerifyRequest verify = new OtpVerifyRequest();
        verify.setChallengeId(challengeId);
        verify.setCode("000000"); // almost certainly wrong

        mockMvc.perform(post("/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid OTP"))
                .andExpect(jsonPath("$.errors.attemptsRemaining").exists());
    }

    @Test
    @DisplayName("missing identifier is rejected with 400 and a field error")
    void validationError() throws Exception {
        mockMvc.perform(post("/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.identifier").exists());
    }
}

