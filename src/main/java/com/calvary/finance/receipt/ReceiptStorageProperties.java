package com.calvary.finance.receipt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.receipts")
@Data
public class ReceiptStorageProperties {
    private String storagePath = "uploads/receipts";
    private long maxUploadBytes = 10 * 1024 * 1024;
    private int maxImageDimension = 1600;
    private float jpegQuality = 0.75f;
}
