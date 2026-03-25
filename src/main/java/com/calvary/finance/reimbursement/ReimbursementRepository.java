package com.calvary.finance.reimbursement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    Page<Reimbursement> findAllByStatus(ReimbursementStatus reimbursementStatus, Pageable pageable);
    Page<Reimbursement> findAllByExpenditureDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Reimbursement> findAllByExpenditureDateGreaterThanEqual(LocalDate startDate, Pageable pageable);
    Page<Reimbursement> findAllByExpenditureDateLessThanEqual(LocalDate endDate, Pageable pageable);
    Page<Reimbursement> findAllByStatusAndExpenditureDateBetween(
            ReimbursementStatus reimbursementStatus,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    Page<Reimbursement> findAllByStatusAndExpenditureDateGreaterThanEqual(
            ReimbursementStatus reimbursementStatus,
            LocalDate startDate,
            Pageable pageable
    );
    Page<Reimbursement> findAllByStatusAndExpenditureDateLessThanEqual(
            ReimbursementStatus reimbursementStatus,
            LocalDate endDate,
            Pageable pageable
    );
}
