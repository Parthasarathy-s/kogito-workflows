package org.partha.audit;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Path("/audit/detailed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DetailedAuditResource {

        @Inject
        ProcessAuditRepository auditRepository; ;

        /**
         * Get complete audit trail for a process instance
         * Shows WHO did WHAT, WHEN, and WHY
         */
        @GET
        @Path("/process/{instanceId}")
        public Response getProcessAuditTrail(@PathParam("instanceId") String instanceId) {
                List<ProcessAuditLog> logs = auditRepository.findByProcessInstanceId(instanceId);

                if (logs.isEmpty()) {
                        return Response.status(404)
                                        .entity(Map.of("error", "No audit trail found for process: " + instanceId))
                                        .build();
                }

                List<Map<String, Object>> auditTrail = logs.stream()
                                .map(this::formatAuditLog)
                                .collect(Collectors.toList());

                return Response.ok(Map.of(
                                "processInstanceId", instanceId,
                                "totalEvents", logs.size(),
                                "auditTrail", auditTrail)).build();
        }

        /**
         * Get audit logs by user - see what a specific user did
         */
        @GET
        @Path("/user/{userId}")
        public Response getUserAuditTrail(@PathParam("userId") String userId) {
                List<ProcessAuditLog> logs = auditRepository.findByUserId(userId);

                List<Map<String, Object>> auditTrail = logs.stream()
                                .map(this::formatAuditLog)
                                .collect(Collectors.toList());

                return Response.ok(Map.of(
                                "userId", userId,
                                "totalActions", logs.size(),
                                "auditTrail", auditTrail)).build();
        }

        /**
         * Get audit logs for a specific task
         */
        @GET
        @Path("/task/{taskId}")
        public Response getTaskAuditTrail(@PathParam("taskId") String taskId) {
                List<ProcessAuditLog> logs = auditRepository.findByTaskId(taskId);

                List<Map<String, Object>> auditTrail = logs.stream()
                                .map(this::formatAuditLog)
                                .collect(Collectors.toList());

                return Response.ok(Map.of(
                                "taskId", taskId,
                                "auditTrail", auditTrail)).build();
        }

        /**
         * Get recent audit activity
         */
        @GET
        @Path("/recent")
        public Response getRecentActivity(@QueryParam("limit") @DefaultValue("50") int limit) {
                List<ProcessAuditLog> logs = auditRepository.findRecent(limit);

                List<Map<String, Object>> auditTrail = logs.stream()
                                .map(this::formatAuditLog)
                                .collect(Collectors.toList());

                return Response.ok(Map.of(
                                "limit", limit,
                                "auditTrail", auditTrail)).build();
        }

        /**
         * Search audit logs with filters
         */
        @GET
        @Path("/search")
        public Response searchAuditLogs(
                        @QueryParam("processInstanceId") String processInstanceId,
                        @QueryParam("userId") String userId,
                        @QueryParam("eventType") String eventType,
                        @QueryParam("startDate") String startDateStr,
                        @QueryParam("endDate") String endDateStr) {

                LocalDateTime startDate = startDateStr != null
                                ? LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_DATE_TIME)
                                : null;
                LocalDateTime endDate = endDateStr != null
                                ? LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_DATE_TIME)
                                : null;

                List<ProcessAuditLog> logs = auditRepository.searchAuditLogs(
                                processInstanceId, userId, eventType, startDate, endDate);

                List<Map<String, Object>> auditTrail = logs.stream()
                                .map(this::formatAuditLog)
                                .collect(Collectors.toList());

                return Response.ok(Map.of(
                                "filters", Map.of(
                                                "processInstanceId",
                                                processInstanceId != null ? processInstanceId : "all",
                                                "userId", userId != null ? userId : "all",
                                                "eventType", eventType != null ? eventType : "all"),
                                "totalResults", logs.size(),
                                "auditTrail", auditTrail)).build();
        }

        /**
         * Get audit summary statistics
         */
        @GET
        @Path("/summary")
        public Response getAuditSummary() {
                List<ProcessAuditLog> allLogs = auditRepository.listAll();

                Map<String, Long> eventTypeCounts = allLogs.stream()
                                .collect(Collectors.groupingBy(
                                                ProcessAuditLog::getEventType,
                                                Collectors.counting()));

                Map<String, Long> userActivityCounts = allLogs.stream()
                                .collect(Collectors.groupingBy(
                                                ProcessAuditLog::getUserId,
                                                Collectors.counting()));

                Map<String, Long> actionCounts = allLogs.stream()
                                .filter(log -> log.getAction() != null)
                                .collect(Collectors.groupingBy(
                                                ProcessAuditLog::getAction,
                                                Collectors.counting()));

                return Response.ok(Map.of(
                                "totalEvents", allLogs.size(),
                                "eventTypeCounts", eventTypeCounts,
                                "userActivityCounts", userActivityCounts,
                                "actionCounts", actionCounts)).build();
        }

        /**
         * Get audit trail in a human-readable timeline format
         */
        @GET
        @Path("/timeline/{instanceId}")
        public Response getTimelineView(@PathParam("instanceId") String instanceId) {
                List<ProcessAuditLog> logs = auditRepository.findByProcessInstanceId(instanceId);

                if (logs.isEmpty()) {
                        return Response.status(404)
                                        .entity(Map.of("error", "No timeline found for process: " + instanceId))
                                        .build();
                }

                List<String> timeline = logs.stream()
                                .map(log -> String.format(
                                                "[%s] %s (%s) performed %s on %s%s",
                                                log.getTimestamp().format(
                                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                                log.getUserName() != null ? log.getUserName() : log.getUserId(),
                                                log.getUserId(),
                                                log.getAction(),
                                                log.getNodeName() != null ? log.getNodeName() : "process",
                                                log.getComments() != null ? " - Comment: " + log.getComments() : ""))
                                .collect(Collectors.toList());

                return Response.ok(Map.of(
                                "processInstanceId", instanceId,
                                "timeline", timeline)).build();
        }

        /**
         * Format audit log for API response
         */
        private Map<String, Object> formatAuditLog(ProcessAuditLog log) {
                Map<String, Object> formatted = new HashMap<>();
                formatted.put("id", log.getId());
                formatted.put("processInstanceId", log.getProcessInstanceId());
                formatted.put("processId", log.getProcessId());
                formatted.put("eventType", log.getEventType());
                formatted.put("nodeName", log.getNodeName());
                formatted.put("taskId", log.getTaskId());
                formatted.put("userId", log.getUserId());
                formatted.put("userName", log.getUserName());
                formatted.put("action", log.getAction());
                formatted.put("comments", log.getComments());
                formatted.put("oldValue", log.getOldValue());
                formatted.put("newValue", log.getNewValue());
                formatted.put("timestamp", log.getTimestamp().toString());
                formatted.put("ipAddress", log.getIpAddress());
                formatted.put("userAgent", log.getUserAgent());

                // Human-readable description
                formatted.put("description", buildDescription(log));

                return formatted;
        }

        /**
         * Build human-readable description
         */
        private String buildDescription(ProcessAuditLog log) {
                StringBuilder desc = new StringBuilder();

                String user = log.getUserName() != null ? log.getUserName() : log.getUserId();
                desc.append(user);

                switch (log.getEventType()) {
                        case "PROCESS_STARTED":
                                desc.append(" started the process");
                                break;
                        case "TASK_ASSIGNED":
                                desc.append(" was assigned task '").append(log.getNodeName()).append("'");
                                break;
                        case "TASK_COMPLETED":
                                desc.append(" completed task '").append(log.getNodeName())
                                                .append("' with decision: ").append(log.getAction());
                                break;
                        case "VARIABLE_CHANGED":
                                desc.append(" changed ").append(log.getNodeName())
                                                .append(" from '").append(log.getOldValue())
                                                .append("' to '").append(log.getNewValue()).append("'");
                                break;
                        case "PROCESS_COMPLETED":
                                desc.append(" completed the process");
                                break;
                        case "PROCESS_ABORTED":
                                desc.append(" aborted the process");
                                break;
                        default:
                                desc.append(" performed ").append(log.getEventType());
                }

                if (log.getComments() != null && !log.getComments().isEmpty()) {
                        desc.append(" (").append(log.getComments()).append(")");
                }

                return desc.toString();
        }
}