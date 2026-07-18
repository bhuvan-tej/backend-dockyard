package com.dockyard.qrgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QrGeneratorApplication — entry point for the QR Generator service.
 *
 * WHAT THIS APP DOES:
 *   Generates QR codes from any text (URLs, Wi-Fi, contact cards, payments…)
 *   Returns them as a raw PNG image or a Base64 data URI for embedding
 *   Decodes an uploaded QR image back into its original text
 *   Keeps a searchable history of everything generated
 *   Exposes lightweight analytics over that history
 */
@SpringBootApplication
public class QrGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrGeneratorApplication.class, args);
    }

}