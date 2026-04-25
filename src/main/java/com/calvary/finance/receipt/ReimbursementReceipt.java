package com.calvary.finance.receipt;

import com.calvary.finance.reimbursement.Reimbursement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reimbursement_receipts")
@NoArgsConstructor
@Data
public class ReimbursementReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reimbursement_id", nullable = false)
    @JsonIgnore
    private Reimbursement reimbursement;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReceiptProcessingStatus status = ReceiptProcessingStatus.PENDING;

    private String originalFilename;
    private String sourceContentType;
    private Long originalSizeBytes;
    private String stagedPath;
    private String storedPath;
    private String storedContentType;
    private Long storedSizeBytes;
    private String sha256;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
