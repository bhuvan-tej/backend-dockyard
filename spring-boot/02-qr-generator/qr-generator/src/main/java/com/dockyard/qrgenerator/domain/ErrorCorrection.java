package com.dockyard.qrgenerator.domain;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * ErrorCorrection — how much of the QR code can be damaged and still scan.
 *
 * QR codes use Reed-Solomon error correction. Higher levels add redundancy,
 * so a torn, dirty or logo-covered code still decodes — at the cost of a
 * denser (harder to scan from far away) image.
 *
 *   L  ~7%  recovery   — clean screens, maximum data capacity
 *   M  ~15% recovery   — the sensible default for most uses
 *   Q  ~25% recovery   — printed media that may get scuffed
 *   H  ~30% recovery   — codes with a logo in the middle, harsh environments
 *
 * We expose our own enum instead of ZXing's so the public API is decoupled
 * from the library — we can swap the QR engine without breaking clients.
 */
public enum ErrorCorrection {

    L(ErrorCorrectionLevel.L),
    M(ErrorCorrectionLevel.M),
    Q(ErrorCorrectionLevel.Q),
    H(ErrorCorrectionLevel.H);

    private final ErrorCorrectionLevel zxingLevel;

    ErrorCorrection(ErrorCorrectionLevel zxingLevel) {
        this.zxingLevel = zxingLevel;
    }

    /** Maps our public enum to the ZXing level used by the encoder. */
    public ErrorCorrectionLevel toZxing() {
        return zxingLevel;
    }
}