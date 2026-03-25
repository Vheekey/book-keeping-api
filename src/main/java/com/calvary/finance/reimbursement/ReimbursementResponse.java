package com.calvary.finance.reimbursement;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
@NoArgsConstructor
public class ReimbursementResponse {
    private String message;
    private List<Reimbursement> reimbursements;
}
