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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RefreshRotationIntegrationTest — proves refresh-token rotation and reuse
 * detection against the real app.
 *
 *   verify → refresh(RT1) → RT1 is now dead → replaying RT1 revokes the family
 *
 * This is the security behaviour that a bare stateless JWT cannot provide.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefreshRotationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    /** Runs request→verify and returns the token JSON node. */
    private JsonNode login(String identifier) throws Exception {
        OtpRequest req = new OtpRequest();
        req.setIdentifier(identifier);

        MvcResult reqResult = mockMvc.perform(post("/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode challenge = objectMapper.readTree(reqResult.getResponse().getContentAsString());

        OtpVerifyRequest verify = new OtpVerifyRequest();
        verify.setChallengeId(challenge.get("challengeId").asText());
        verify.setCode(challenge.get("devCode").asText());

        MvcResult verifyResult = mockMvc.perform(post("/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(verifyResult.getResponse().getContentAsString());
    }

    private MvcResult refresh(String refreshToken) throws Exception {
        RefreshRequest r = new RefreshRequest();
        r.setRefreshToken(refreshToken);
        return mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andReturn();
    }

    @Test
    @DisplayName("a refresh token works once, then is rejected (rotation)")
    void refreshTokenIsSingleUse() throws Exception {
        String rt1 = login("rotate@dockyard.dev").get("refreshToken").asText();

        // First use succeeds and returns a new pair.
        MvcResult first = refresh(rt1);
        org.assertj.core.api.Assertions.assertThat(first.getResponse().getStatus()).isEqualTo(200);

        // Reusing the SAME (now rotated-away) refresh token is rejected.
        MvcResult second = refresh(rt1);
        org.assertj.core.api.Assertions.assertThat(second.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("replaying a used refresh token revokes the whole family")
    void reuseRevokesFamily() throws Exception {
        String rt1 = login("theft@dockyard.dev").get("refreshToken").asText();

        // Legitimate rotation: rt1 → rt2.
        JsonNode rotated = objectMapper.readTree(refresh(rt1).getResponse().getContentAsString());
        String rt2 = rotated.get("refreshToken").asText();

        // Attacker replays the old rt1 → reuse detected → 401 and family revoked.
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rt1 + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid Token"));

        // Because the family was revoked, even the good rt2 no longer works.
        org.assertj.core.api.Assertions.assertThat(refresh(rt2).getResponse().getStatus()).isEqualTo(401);
    }
}

