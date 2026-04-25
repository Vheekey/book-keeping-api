package com.calvary.finance.receipt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReimbursementReceiptRepository extends JpaRepository<ReimbursementReceipt, Long> {
    List<ReimbursementReceipt> findAllByReimbursementIdOrderByIdDesc(Long reimbursementId);

    Optional<ReimbursementReceipt> findByIdAndReimbursementId(Long id, Long reimbursementId);
}
