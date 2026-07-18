package com.dockyard.qrgenerator.engine;

import com.dockyard.qrgenerator.dto.VCardQrRequest;
import com.dockyard.qrgenerator.dto.WifiQrRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * QrPayloadBuilder — turns friendly request objects into the exact string
 * formats that phone cameras recognise.
 *
 * These grammars are de-facto standards (originally from ZXing's "Barcode
 * Contents" spec) honoured by iOS Camera, Google Lens and most scanner apps.
 * Getting the escaping right is the whole game — a stray unescaped ';' in a
 * WiFi password silently breaks the join.
 */
@Component
public class QrPayloadBuilder {

    /**
     * Builds a WiFi network-join payload:
     * {@code WIFI:T:<WPA|WEP|nopass>;S:<ssid>;P:<password>;H:<true|false>;;}
     */
    public String buildWifi(WifiQrRequest req) {
        String encryption = req.getEncryption() == null ? "WPA" : req.getEncryption();
        boolean open = "nopass".equalsIgnoreCase(encryption);

        StringBuilder sb = new StringBuilder("WIFI:");
        sb.append("T:").append(open ? "nopass" : encryption).append(';');
        sb.append("S:").append(escapeWifi(req.getSsid())).append(';');
        // Password is omitted entirely for open networks.
        if (!open && StringUtils.hasText(req.getPassword())) {
            sb.append("P:").append(escapeWifi(req.getPassword())).append(';');
        }
        if (req.isHidden()) {
            sb.append("H:true;");
        }
        sb.append(';'); // grammar terminates with an extra semicolon
        return sb.toString();
    }

    /**
     * Builds a vCard 3.0 contact payload. Only non-empty fields are included,
     * so a name-and-phone-only card stays compact.
     */
    public String buildVCard(VCardQrRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD\n");
        sb.append("VERSION:3.0\n");
        sb.append("N:").append(escapeVcard(req.getFullName())).append('\n');
        sb.append("FN:").append(escapeVcard(req.getFullName())).append('\n');
        if (StringUtils.hasText(req.getOrganization())) {
            sb.append("ORG:").append(escapeVcard(req.getOrganization())).append('\n');
        }
        if (StringUtils.hasText(req.getTitle())) {
            sb.append("TITLE:").append(escapeVcard(req.getTitle())).append('\n');
        }
        if (StringUtils.hasText(req.getPhone())) {
            sb.append("TEL;TYPE=CELL:").append(req.getPhone().trim()).append('\n');
        }
        if (StringUtils.hasText(req.getEmail())) {
            sb.append("EMAIL:").append(req.getEmail().trim()).append('\n');
        }
        if (StringUtils.hasText(req.getWebsite())) {
            sb.append("URL:").append(req.getWebsite().trim()).append('\n');
        }
        sb.append("END:VCARD");
        return sb.toString();
    }

    /** In WiFi payloads, \ ; , : and " are special and must be backslash-escaped. */
    private String escapeWifi(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("([\\\\;,:\"])", "\\\\$1");
    }

    /** In vCard fields, \ ; , and newlines are special. */
    private String escapeVcard(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }
}