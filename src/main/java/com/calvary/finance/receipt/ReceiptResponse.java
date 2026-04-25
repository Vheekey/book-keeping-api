package com.calvary.finance.receipt;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ReceiptResponse {
    private String message;
    private List<ReimbursementReceipt> receipts;
}
