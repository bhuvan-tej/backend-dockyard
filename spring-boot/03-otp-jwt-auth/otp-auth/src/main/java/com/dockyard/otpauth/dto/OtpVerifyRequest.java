package com.dockyard.otpauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * OtpVerifyRequest — presents a code against a challenge to obtain tokens.
 */
@Data
public class OtpVerifyRequest {

    @NotBlank(message = "challengeId is required")
    private String challengeId;

    /** The numeric code the user received. Digits only. */
    @NotBlank(message = "code is required")
    @Pattern(regexp = "^\\d{4,10}$", message = "code must be 4–10 digits")
    private String code;
}