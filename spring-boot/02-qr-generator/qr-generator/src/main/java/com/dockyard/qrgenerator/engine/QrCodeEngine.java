package com.dockyard.qrgenerator.engine;

import com.dockyard.qrgenerator.domain.ErrorCorrection;
import com.dockyard.qrgenerator.exception.QrDecodingException;
import com.dockyard.qrgenerator.exception.QrGenerationException;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * QrCodeEngine — a thin, isolated wrapper around the ZXing library.
 *
 * WHY ISOLATE ZXING HERE?
 *   Every ZXing type (BitMatrix, QRCodeWriter, EncodeHintType…) lives ONLY in
 *   this class. The service and controllers never import ZXing. That means:
 *     - business logic is testable without caring about the QR library
 *     - swapping ZXing for another engine touches exactly one file
 *     - low-level checked exceptions are translated to our domain exceptions here
 *
 * The engine is stateless and thread-safe, so it is a singleton Spring bean.
 */
@Slf4j
@Component
public class QrCodeEngine {

    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Encodes {@code content} into a PNG image.
     *
     * @param content     the text to encode (already validated, non-blank)
     * @param size        image width/height in pixels
     * @param margin      quiet-zone width in modules
     * @param onColorHex  dark-module colour as #RRGGBB
     * @param offColorHex background colour as #RRGGBB
     * @param level       error-correction level
     * @return the PNG image bytes
     * @throws QrGenerationException if the content cannot be encoded as a QR code
     */
    public byte[] generatePng(String content, int size, int margin,
                              String onColorHex, String offColorHex,
                              ErrorCorrection level) {

        // Encoding hints tune the output: correction level, quiet zone, charset.
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, level.toZxing());
        hints.put(EncodeHintType.MARGIN, margin);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            // 1. Encode the text into a matrix of black/white modules.
            BitMatrix matrix = new QRCodeWriter()
                    .encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            // 2. Colour the matrix. ZXing expects ARGB ints — force full opacity.
            MatrixToImageConfig config = new MatrixToImageConfig(
                    toArgb(onColorHex), toArgb(offColorHex));

            // 3. Render the matrix to PNG bytes in memory.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, IMAGE_FORMAT, out, config);

            byte[] png = out.toByteArray();
            log.debug("Generated {}x{} QR ({} bytes) for {}-char payload",
                    size, size, png.length, content.length());
            return png;

        } catch (WriterException | IOException ex) {
            // WriterException usually means the payload is too big for one QR code.
            throw new QrGenerationException(
                    "Unable to encode content as a QR code — it may be too large", ex);
        }
    }

    /**
     * Decodes a QR code out of an uploaded image back into its text.
     *
     * @param imageBytes raw bytes of a PNG/JPG/etc. containing a QR code
     * @return the decoded text
     * @throws QrDecodingException if the bytes are not an image or hold no QR code
     */
    public String decode(byte[] imageBytes) {
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException ex) {
            throw new QrDecodingException("Uploaded file could not be read as an image", ex);
        }
        if (image == null) {
            throw new QrDecodingException("Uploaded file is not a supported image format");
        }

        // Convert the image into the light/dark bitmap ZXing reads from.
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        // TRY_HARDER trades a little speed for far better tolerance of blur/skew.
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        try {
            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException ex) {
            throw new QrDecodingException("No QR code was found in the uploaded image", ex);
        }
    }

    /**
     * Converts a #RRGGBB hex string to an opaque ARGB int (0xFFRRGGBB).
     * Input is guaranteed valid by request validation, but we keep it defensive.
     */
    private int toArgb(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return 0xFF000000 | rgb; // force alpha = 255 (fully opaque)
    }
}