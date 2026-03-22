package com.calvary.finance.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private static final String DEFAULT_ACTOR = "system";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void log(String action, String entityType, String entityId, Object details) {
        log(action, entityType, entityId, DEFAULT_ACTOR, details);
    }

    public void log(String action, String entityType, String entityId, String actor, Object details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setActor(actor == null || actor.isBlank() ? DEFAULT_ACTOR : actor);
        auditLog.setDetailsJson(toJson(details));

        auditLogRepository.save(auditLog);
    }

    private JsonNode toJson(Object details) {
        if (details == null) {
            return null;
        }

        return objectMapper.valueToTree(details);
    }
}
