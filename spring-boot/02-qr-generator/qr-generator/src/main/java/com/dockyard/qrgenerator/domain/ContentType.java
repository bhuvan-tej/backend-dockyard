package com.dockyard.qrgenerator.domain;

/**
 * ContentType — a best-effort classification of what a QR code encodes.
 *
 * This is what makes the history and analytics genuinely useful: instead of
 * "we generated 500 QR codes", you learn "62% were URLs, 20% Wi-Fi, 18% contacts".
 *
 * The classification is heuristic — it inspects the payload prefix/shape.
 * QR codes have no formal "type" field; scanners recognize these conventions:
 *
 *   URL    → https://example.com
 *   EMAIL  → mailto:me@x.com   OR a bare address
 *   PHONE  → tel:+9112345
 *   SMS    → smsto:+9112345:hi
 *   WIFI   → WIFI:T:WPA;S:ssid;P:pass;;
 *   GEO    → geo:12.97,77.59
 *   VCARD  → BEGIN:VCARD…END:VCARD
 *   TEXT   → anything else (plain text)
 */
public enum ContentType {
    URL,
    EMAIL,
    PHONE,
    SMS,
    WIFI,
    GEO,
    VCARD,
    TEXT;

    /**
     * Classifies a raw QR payload into one of the known content types.
     * Prefix checks are case-insensitive because scanners treat them that way.
     */
    public static ContentType classify(String content) {
        if (content == null || content.isBlank()) {
            return TEXT;
        }
        String value = content.trim();
        String lower = value.toLowerCase();

        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return URL;
        }
        if (lower.startsWith("mailto:")) {
            return EMAIL;
        }
        if (lower.startsWith("tel:")) {
            return PHONE;
        }
        if (lower.startsWith("smsto:") || lower.startsWith("sms:")) {
            return SMS;
        }
        if (lower.startsWith("wifi:")) {
            return WIFI;
        }
        if (lower.startsWith("geo:")) {
            return GEO;
        }
        if (lower.startsWith("begin:vcard")) {
            return VCARD;
        }
        // A bare e-mail address with no scheme is still clearly an e-mail.
        if (value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return EMAIL;
        }
        return TEXT;
    }
}