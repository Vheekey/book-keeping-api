package com.calvary.finance.reimbursement;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ReimbursementResponse {
    private String message;
    private List<Reimbursement> reimbursements;
}
