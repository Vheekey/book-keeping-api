package com.calvary.finance.receipt;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class StagedReceipt {
    String path;
    String contentType;
    String originalFilename;
    long originalSizeBytes;
}
