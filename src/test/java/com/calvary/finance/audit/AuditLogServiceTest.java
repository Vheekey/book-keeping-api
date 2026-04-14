package com.calvary.finance.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void logUsesSystemActorWhenActorIsBlank() {
        AuditLogService service = new AuditLogService(auditLogRepository, new ObjectMapper());

        service.log(
                "REIMBURSEMENT_CREATED",
                "Reimbursement",
                "10",
                "  ",
                Map.of("amount", "150.00")
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog auditLog = captor.getValue();

        assertThat(auditLog.getAction()).isEqualTo("REIMBURSEMENT_CREATED");
        assertThat(auditLog.getEntityType()).isEqualTo("Reimbursement");
        assertThat(auditLog.getEntityId()).isEqualTo("10");
        assertThat(auditLog.getActor()).isEqualTo("system");
        assertThat(auditLog.getDetailsJson().get("amount").asText()).isEqualTo("150.00");
    }

    @Test
    void logOverloadWithoutActorUsesDefaultSystemActor() {
        AuditLogService service = new AuditLogService(auditLogRepository, new ObjectMapper());

        service.log("ROLE_CREATED", "Role", "3", Map.of("name", "admin"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog auditLog = captor.getValue();

        assertThat(auditLog.getActor()).isEqualTo("system");
        assertThat(auditLog.getDetailsJson().get("name").asText()).isEqualTo("admin");
    }

    @Test
    void logStoresNullDetailsAsNullJson() {
        AuditLogService service = new AuditLogService(auditLogRepository, new ObjectMapper());

        service.log("USER_DELETED", "User", "42", "1", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog auditLog = captor.getValue();

        assertThat(auditLog.getActor()).isEqualTo("1");
        assertThat(auditLog.getDetailsJson()).isNull();
    }
}
