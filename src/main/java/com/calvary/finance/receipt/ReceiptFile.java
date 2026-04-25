package com.calvary.finance.receipt;

import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.Resource;

@Builder
@Value
public class ReceiptFile {
    Resource resource;
    String filename;
    String contentType;
}
