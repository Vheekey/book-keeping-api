package com.calvary.finance.reimbursement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
}
