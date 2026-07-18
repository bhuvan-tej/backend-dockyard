package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ErrorCorrection;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * WifiQrRequest — a "how can it be used" helper.
 *
 * Instead of forcing the client to know the exact WiFi QR grammar
 * ({@code WIFI:T:WPA;S:<ssid>;P:<pass>;H:<hidden>;;}), they send friendly
 * fields and we build the payload. Scanning the resulting code joins the
 * network with no typing — the killer use case for cafés, offices and events.
 */
@Data
public class WifiQrRequest {

    @NotBlank(message = "ssid (network name) is required")
    @Size(max = 100, message = "ssid cannot exceed 100 characters")
    private String ssid;

    /** Optional for open networks; required for WPA/WEP. */
    @Size(max = 100, message = "password cannot exceed 100 characters")
    private String password;

    /** WPA (covers WPA/WPA2/WPA3), WEP, or nopass for an open network. */
    @Pattern(regexp = "^(WPA|WEP|nopass)$", message = "encryption must be WPA, WEP or nopass")
    private String encryption = "WPA";

    /** True if the SSID is not broadcast. */
    private boolean hidden = false;

    // --- shared visual options (same defaults as QrCodeRequest) ---

    @Min(value = 50, message = "Size must be at least 50px")
    @Max(value = 2000, message = "Size cannot exceed 2000px")
    private int size = 300;

    private ErrorCorrection errorCorrection = ErrorCorrection.M;
}