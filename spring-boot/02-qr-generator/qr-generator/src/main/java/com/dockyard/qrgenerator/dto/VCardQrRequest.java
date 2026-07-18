package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ErrorCorrection;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * VCardQrRequest — another "how can it be used" helper.
 *
 * Encodes a contact as a vCard 3.0 payload. Scanning the code opens the phone's
 * "add contact" screen pre-filled — perfect for business cards, e-mail
 * signatures and conference badges.
 */
@Data
public class VCardQrRequest {

    @NotBlank(message = "fullName is required")
    @Size(max = 100, message = "fullName cannot exceed 100 characters")
    private String fullName;

    @Size(max = 30, message = "phone cannot exceed 30 characters")
    private String phone;

    @Email(message = "email must be a valid address")
    private String email;

    @Size(max = 100, message = "organization cannot exceed 100 characters")
    private String organization;

    @Size(max = 100, message = "title cannot exceed 100 characters")
    private String title;

    @Size(max = 200, message = "website cannot exceed 200 characters")
    private String website;

    // --- shared visual options ---

    @Min(value = 50, message = "Size must be at least 50px")
    @Max(value = 2000, message = "Size cannot exceed 2000px")
    private int size = 300;

    /** Contact cards carry more data, so Q gives a good scan-reliability margin. */
    private ErrorCorrection errorCorrection = ErrorCorrection.Q;
}