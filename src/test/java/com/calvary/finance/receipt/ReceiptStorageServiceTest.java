package com.calvary.finance.receipt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceiptStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void stageStoresOriginalFileAndProcessCompressesImage() throws Exception {
        ReceiptStorageProperties properties = new ReceiptStorageProperties();
        properties.setStoragePath(tempDir.toString());
        properties.setMaxImageDimension(80);
        ReceiptStorageService service = new ReceiptStorageService(properties);

        MockMultipartFile receipt = new MockMultipartFile(
                "receipt",
                "fuel.png",
                "image/png",
                pngBytes(240, 120)
        );

        StagedReceipt stagedReceipt = service.stage(receipt);
        assertThat(stagedReceipt.getContentType()).isEqualTo("image/png");
        assertThat(stagedReceipt.getOriginalFilename()).isEqualTo("fuel.png");
        assertThat(stagedReceipt.getOriginalSizeBytes()).isPositive();
        assertThat(Files.exists(tempDir.resolve("staged").resolve(stagedReceipt.getPath()))).isTrue();

        StoredReceipt storedReceipt = service.process(stagedReceipt.getPath(), stagedReceipt.getContentType());

        assertThat(storedReceipt.getContentType()).isEqualTo("image/jpeg");
        assertThat(storedReceipt.getStoredSizeBytes()).isPositive();
        assertThat(storedReceipt.getSha256()).hasSize(64);

        Path storedPath = tempDir.resolve("processed").resolve(storedReceipt.getPath());
        assertThat(Files.exists(storedPath)).isTrue();
        BufferedImage storedImage = ImageIO.read(storedPath.toFile());
        assertThat(Math.max(storedImage.getWidth(), storedImage.getHeight())).isEqualTo(80);
    }

    @Test
    void storeRejectsUnsupportedFileTypes() {
        ReceiptStorageProperties properties = new ReceiptStorageProperties();
        properties.setStoragePath(tempDir.toString());
        ReceiptStorageService service = new ReceiptStorageService(properties);
        MockMultipartFile receipt = new MockMultipartFile(
                "receipt",
                "receipt.txt",
                "text/plain",
                "not a receipt image".getBytes()
        );

        assertThatThrownBy(() -> service.stage(receipt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Receipt must be a JPEG, PNG, or PDF file");
    }

    private byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.drawString("Receipt", 20, 40);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
