package com.dockyard.otpauth.dto;

import com.dockyard.otpauth.domain.DeliveryChannel;
import com.dockyard.otpauth.domain.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * OtpRequest — asks the server to generate and "send" a one-time password.
 *
 * Only {@code identifier} is required; purpose and channel default sensibly, so
 * the simplest request is just: { "identifier": "user@example.com" }.
 */
@Data
public class OtpRequest {

    /** Email address or phone number the OTP is for. */
    @NotBlank(message = "identifier is required")
    @Size(max = 320, message = "identifier cannot exceed 320 characters")
    private String identifier;

    /** Why the OTP is needed. Defaults to LOGIN. */
    @NotNull(message = "purpose is required")
    private OtpPurpose purpose = OtpPurpose.LOGIN;

    /** How it would be delivered. Defaults to EMAIL. */
    @NotNull(message = "channel is required")
    private DeliveryChannel channel = DeliveryChannel.EMAIL;
}