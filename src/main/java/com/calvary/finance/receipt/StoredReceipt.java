package com.calvary.finance.receipt;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class StoredReceipt {
    String path;
    String contentType;
    long storedSizeBytes;
    String sha256;
}
