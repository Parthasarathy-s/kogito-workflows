package org.partha.audit;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProcessAuditRepository implements PanacheRepository<ProcessAuditLog> {

    public List<ProcessAuditLog> findByProcessInstanceId(String instanceId) {
        return list("processInstanceId", instanceId);
    }

    public List<ProcessAuditLog> findByUserId(String userId) {
        return list("userId", userId);
    }

    public List<ProcessAuditLog> findByTaskId(String taskId) {
        return list("taskId", taskId);
    }

    public List<ProcessAuditLog> findRecent(int limit) {
        return find("order by timestamp desc").page(0, limit).list();
    }

    public List<ProcessAuditLog> searchAuditLogs(
            String processInstanceId,
            String userId,
            String eventType,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Map<String, Object> params = new HashMap<>();
        StringBuilder query = new StringBuilder();

        if (processInstanceId != null && !processInstanceId.isEmpty()) {
            query.append("processInstanceId = :processInstanceId");
            params.put("processInstanceId", processInstanceId);
        }
        if (userId != null && !userId.isEmpty()) {
            if (query.length() > 0)
                query.append(" and ");
            query.append("userId = :userId");
            params.put("userId", userId);
        }
        if (eventType != null && !eventType.isEmpty()) {
            if (query.length() > 0)
                query.append(" and ");
            query.append("eventType = :eventType");
            params.put("eventType", eventType);
        }
        if (startDate != null) {
            if (query.length() > 0)
                query.append(" and ");
            query.append("timestamp >= :startDate");
            params.put("startDate", startDate);
        }
        if (endDate != null) {
            if (query.length() > 0)
                query.append(" and ");
            query.append("timestamp <= :endDate");
            params.put("endDate", endDate);
        }

        if (query.length() == 0) {
            return listAll();
        }

        return list(query.toString(), params);
    }
}
