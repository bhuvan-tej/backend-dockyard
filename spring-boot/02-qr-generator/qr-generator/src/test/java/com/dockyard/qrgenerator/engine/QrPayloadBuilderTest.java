package com.dockyard.qrgenerator.engine;

import com.dockyard.qrgenerator.dto.VCardQrRequest;
import com.dockyard.qrgenerator.dto.WifiQrRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QrPayloadBuilderTest — verifies the WiFi and vCard grammars, including the
 * escaping that makes or breaks real-world scanning.
 */
class QrPayloadBuilderTest {

    private final QrPayloadBuilder builder = new QrPayloadBuilder();

    @Test
    @DisplayName("WiFi payload follows the WIFI:T:...;S:...;P:...;; grammar")
    void wifi_standardNetwork() {
        WifiQrRequest req = new WifiQrRequest();
        req.setSsid("OfficeNet");
        req.setPassword("secret123");
        req.setEncryption("WPA");

        String payload = builder.buildWifi(req);

        assertThat(payload).isEqualTo("WIFI:T:WPA;S:OfficeNet;P:secret123;;");
    }

    @Test
    @DisplayName("open WiFi network omits the password")
    void wifi_openNetwork_hasNoPassword() {
        WifiQrRequest req = new WifiQrRequest();
        req.setSsid("GuestWiFi");
        req.setEncryption("nopass");

        String payload = builder.buildWifi(req);

        assertThat(payload).isEqualTo("WIFI:T:nopass;S:GuestWiFi;;");
    }

    @Test
    @DisplayName("special characters in the WiFi password are escaped")
    void wifi_escapesSpecialCharacters() {
        WifiQrRequest req = new WifiQrRequest();
        req.setSsid("Net");
        req.setPassword("a;b:c\\d");
        req.setEncryption("WPA");

        String payload = builder.buildWifi(req);

        assertThat(payload).contains("P:a\\;b\\:c\\\\d;");
    }

    @Test
    @DisplayName("vCard includes provided fields and omits empty ones")
    void vcard_buildsExpectedFields() {
        VCardQrRequest req = new VCardQrRequest();
        req.setFullName("Ada Lovelace");
        req.setPhone("+911234567890");
        req.setEmail("ada@analytical.engine");

        String payload = builder.buildVCard(req);

        assertThat(payload)
                .startsWith("BEGIN:VCARD")
                .contains("VERSION:3.0")
                .contains("FN:Ada Lovelace")
                .contains("TEL;TYPE=CELL:+911234567890")
                .contains("EMAIL:ada@analytical.engine")
                .doesNotContain("ORG:")
                .endsWith("END:VCARD");
    }
}

