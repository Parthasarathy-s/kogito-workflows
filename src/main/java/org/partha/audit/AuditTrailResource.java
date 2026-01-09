package org.partha.audit;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuditTrailResource {

    @Inject
    DataSource dataSource;

    /**
     * Get audit trail for a specific process instance
     */
    @GET
    @Path("/process/{instanceId}")
    public Response getProcessAudit(@PathParam("instanceId") String instanceId) {
        List<Map<String, Object>> auditTrail = new ArrayList<>();

        String query = """
                SELECT
                    id,
                    processinstanceid,
                    processid,
                    processversion,
                    status,
                    start_date,
                    end_date,
                    parentprocessinstanceid,
                    rootprocessinstanceid,
                    rootprocessid
                FROM process_instances
                WHERE processinstanceid = ?
                ORDER BY start_date DESC
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, instanceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getString("id"));
                    record.put("processInstanceId", rs.getString("processinstanceid"));
                    record.put("processId", rs.getString("processid"));
                    record.put("processVersion", rs.getString("processversion"));
                    record.put("status", rs.getInt("status"));
                    record.put("startDate", rs.getTimestamp("start_date"));
                    record.put("endDate", rs.getTimestamp("end_date"));
                    record.put("parentProcessInstanceId", rs.getString("parentprocessinstanceid"));
                    record.put("rootProcessInstanceId", rs.getString("rootprocessinstanceid"));
                    record.put("rootProcessId", rs.getString("rootprocessid"));
                    auditTrail.add(record);
                }
            }

            if (auditTrail.isEmpty()) {
                return Response.status(404)
                        .entity(Map.of("error", "No audit records found for process instance: " + instanceId))
                        .build();
            }

            return Response.ok(Map.of(
                    "processInstanceId", instanceId,
                    "auditTrail", auditTrail)).build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity(Map.of("error", "Database error: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all process instances with their history
     */
    @GET
    @Path("/processes")
    public Response getAllProcessAudit(
            @QueryParam("limit") @DefaultValue("50") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset) {

        List<Map<String, Object>> processes = new ArrayList<>();

        String query = """
                SELECT
                    id,
                    processinstanceid,
                    processid,
                    processversion,
                    status,
                    start_date,
                    end_date
                FROM process_instances
                ORDER BY start_date DESC
                LIMIT ? OFFSET ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getString("id"));
                    record.put("processInstanceId", rs.getString("processinstanceid"));
                    record.put("processId", rs.getString("processid"));
                    record.put("processVersion", rs.getString("processversion"));
                    record.put("status", getStatusText(rs.getInt("status")));
                    record.put("startDate", rs.getTimestamp("start_date"));
                    record.put("endDate", rs.getTimestamp("end_date"));
                    processes.add(record);
                }
            }

            return Response.ok(Map.of(
                    "total", processes.size(),
                    "limit", limit,
                    "offset", offset,
                    "processes", processes)).build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity(Map.of("error", "Database error: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get task history for a process instance
     */
    @GET
    @Path("/tasks/{instanceId}")
    public Response getTaskAudit(@PathParam("instanceId") String instanceId) {
        List<Map<String, Object>> tasks = new ArrayList<>();

        // Note: Table name may vary - adjust based on your schema
        String query = """
                SELECT
                    taskid,
                    taskname,
                    status,
                    actualeowner,
                    createdby,
                    createdon,
                    activationtime,
                    expirationtime
                FROM jbpmhumantasks
                WHERE processinstanceid = ?
                ORDER BY createdon DESC
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, instanceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("taskId", rs.getString("taskid"));
                    record.put("taskName", rs.getString("taskname"));
                    record.put("status", rs.getString("status"));
                    record.put("actualOwner", rs.getString("actualeowner"));
                    record.put("createdBy", rs.getString("createdby"));
                    record.put("createdOn", rs.getTimestamp("createdon"));
                    record.put("activationTime", rs.getTimestamp("activationtime"));
                    record.put("expirationTime", rs.getTimestamp("expirationtime"));
                    tasks.add(record);
                }
            }

            return Response.ok(Map.of(
                    "processInstanceId", instanceId,
                    "tasks", tasks)).build();

        } catch (SQLException e) {
            // Table might not exist if tasks haven't been used yet
            return Response.ok(Map.of(
                    "processInstanceId", instanceId,
                    "tasks", new ArrayList<>(),
                    "note", "Task table not found or no tasks recorded")).build();
        }
    }

    /**
     * Get database schema info (useful for debugging)
     */
    @GET
    @Path("/schema")
    public Response getSchemaInfo() {
        List<String> tables = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" })) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);
                }
            }

            return Response.ok(Map.of(
                    "database", "H2",
                    "tables", tables,
                    "totalTables", tables.size())).build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity(Map.of("error", "Database error: " + e.getMessage()))
                    .build();
        }
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "PENDING";
            case 1 -> "ACTIVE";
            case 2 -> "COMPLETED";
            case 3 -> "ABORTED";
            case 4 -> "SUSPENDED";
            default -> "UNKNOWN";
        };
    }
}