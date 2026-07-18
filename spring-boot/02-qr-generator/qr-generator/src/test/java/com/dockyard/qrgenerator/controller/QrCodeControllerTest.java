package com.dockyard.qrgenerator.controller;

import com.dockyard.qrgenerator.domain.ContentType;
import com.dockyard.qrgenerator.domain.ErrorCorrection;
import com.dockyard.qrgenerator.dto.QrCodeRequest;
import com.dockyard.qrgenerator.dto.QrDecodeResponse;
import com.dockyard.qrgenerator.entity.QrCodeRecord;
import com.dockyard.qrgenerator.service.GeneratedQr;
import com.dockyard.qrgenerator.service.QrCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QrCodeControllerTest — HTTP-layer tests with the service mocked.
 *
 * Verifies status codes, content types and validation wiring without booting
 * the database or the real ZXing engine.
 */
@WebMvcTest(QrCodeController.class)
class QrCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("POST /qrcodes/image returns 200 with image/png")
    void generateImage_returnsPng() throws Exception {
        QrCodeRecord record = QrCodeRecord.builder()
                .id(1L).content("hello").contentType(ContentType.TEXT)
                .sizePx(300).errorCorrection(ErrorCorrection.M).byteSize(4)
                .createdAt(LocalDateTime.now()).build();
        when(qrCodeService.generate(any()))
                .thenReturn(new GeneratedQr(new byte[]{1, 2, 3, 4}, record));

        QrCodeRequest request = new QrCodeRequest();
        request.setContent("hello");

        mockMvc.perform(post("/qrcodes/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("POST /qrcodes returns JSON with a data URI")
    void generateJson_returnsDataUri() throws Exception {
        QrCodeRecord record = QrCodeRecord.builder()
                .id(7L).content("https://x.io").contentType(ContentType.URL)
                .sizePx(300).errorCorrection(ErrorCorrection.M).byteSize(3)
                .createdAt(LocalDateTime.now()).build();
        when(qrCodeService.generate(any()))
                .thenReturn(new GeneratedQr(new byte[]{9, 8, 7}, record));

        QrCodeRequest request = new QrCodeRequest();
        request.setContent("https://x.io");

        mockMvc.perform(post("/qrcodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.contentType").value("URL"))
                .andExpect(jsonPath("$.dataUri").value(org.hamcrest.Matchers.startsWith("data:image/png;base64,")));
    }

    @Test
    @DisplayName("blank content is rejected with 400 and a field error")
    void generate_blankContent_returns400() throws Exception {
        QrCodeRequest request = new QrCodeRequest();
        request.setContent("   "); // fails @NotBlank

        mockMvc.perform(post("/qrcodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.content").exists());
    }

    @Test
    @DisplayName("POST /qrcodes/decode returns the decoded text")
    void decode_returnsContent() throws Exception {
        when(qrCodeService.decode(any())).thenReturn(
                QrDecodeResponse.builder()
                        .content("https://x.io").contentType(ContentType.URL)
                        .format("QR_CODE").build());

        MockMultipartFile file = new MockMultipartFile(
                "file", "qr.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/qrcodes/decode").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("https://x.io"))
                .andExpect(jsonPath("$.format").value("QR_CODE"));
    }
}

