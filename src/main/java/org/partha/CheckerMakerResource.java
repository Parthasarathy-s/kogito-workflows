package org.partha;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

import org.kie.kogito.process.WorkItem;
import org.partha.audit.ProcessAuditLog;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Path("/checker-maker")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class CheckerMakerResource {

    @Inject
    Process<? extends org.kie.kogito.Model> checkerMakerProcess;

    @Inject
    EntityManager entityManager;

    @GET
    @Path("/instances")
    public Response getAllInstances() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get all process instances
        checkerMakerProcess.instances().stream().forEach(instance -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", instance.id());
            info.put("status", instance.status());
            info.put("variables", instance.variables());
            result.add(info);
        });

        // Audit: List all instances
        logAudit(null, "getAllInstances", "QUERY", "Listed all checker-maker process instances. Total: " + result.size());

        return Response.ok(result).build();
    }

    @GET
    @Path("/instances/{id}")
    public Response getInstance(@PathParam("id") String id) {
        Optional<?> instance = checkerMakerProcess.instances().findById(id);

        if (instance.isEmpty()) {
            // Audit: Instance not found
            logAudit(id, "getInstance", "QUERY_FAILED", "Process instance not found");
            return Response.status(404).entity("Process instance not found").build();
        }

        ProcessInstance<?> pi = (ProcessInstance<?>) instance.get();
        Map<String, Object> info = new HashMap<>();
        info.put("id", pi.id());
        info.put("status", pi.status());
        info.put("variables", pi.variables());

        // Audit: Retrieved instance details
        logAudit(id, "getInstance", "QUERY", "Retrieved process instance details. Status: " + pi.status());

        return Response.ok(info).build();
    }

    @GET
    @Path("/instances/{id}/tasks")
    public Response getTasks(@PathParam("id") String id) {
        Optional<?> instance = checkerMakerProcess.instances().findById(id);

        if (instance.isEmpty()) {
            // Audit: Instance not found
            logAudit(id, "getTasks", "QUERY_FAILED", "Process instance not found");
            return Response.status(404).entity("Process instance not found").build();
        }

        ProcessInstance<?> pi = (ProcessInstance<?>) instance.get();
        List<WorkItem> workItems = pi.workItems();

        List<Map<String, Object>> tasks = workItems.stream().map(wi -> {
            Map<String, Object> task = new HashMap<>();
            task.put("id", wi.getId());
            task.put("name", wi.getName());
            task.put("state", wi.getPhase());
            task.put("parameters", wi.getParameters());
            System.out.println("Found task - ID: " + wi.getId() + " | Name: " + wi.getName() + " | Phase: " + wi.getPhase());
            return task;
        }).collect(Collectors.toList());

        // Audit: Retrieved tasks
        logAudit(id, "getTasks", "QUERY", "Retrieved tasks for process instance. Total tasks: " + tasks.size());

        return Response.ok(tasks).build();
    }

    @POST
    @Path("/instances/{id}/tasks/{taskId}/complete")
    public Response completeTask(
            @PathParam("id") String id,
            @PathParam("taskId") String taskId,
            Map<String, Object> data) {

        // Audit: Log incoming request body
        logAudit(id, "completeTask", "POST_REQUEST_RECEIVED", 
                "POST request to complete task: " + taskId + " | Request body: " + (data != null ? data.toString() : "empty"));

        Optional<?> instance = checkerMakerProcess.instances().findById(id);
        System.out.println("Completing task. Instance ID: " + id + " | Task ID: " + taskId);
        
        if (instance.isEmpty()) {
            // Audit: Instance not found
            logAudit(id, "completeTask", "TASK_COMPLETION_FAILED", "Process instance not found");
            return Response.status(404).entity("Process instance not found").build();
        }

        ProcessInstance<?> pi = (ProcessInstance<?>) instance.get();
        pi.completeWorkItem(taskId, data);

        // Audit: Task completed
        logAudit(id, "completeTask", "TASK_COMPLETED", 
                "Completed task: " + taskId + " with data: " + (data != null ? data.toString() : "none"));

        return Response.ok().entity("Task completed successfully").build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok()
                .entity(Map.of(
                        "status", "UP",
                        "processId", checkerMakerProcess.id()))
                .build();
    }

    @GET
    @Path("/audit-logs")
    public Response getAuditLogs() {
        List<ProcessAuditLog> logs = entityManager
                .createQuery("SELECT p FROM ProcessAuditLog p ORDER BY p.timestamp DESC", ProcessAuditLog.class)
                .setMaxResults(50)
                .getResultList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", logs.size());
        result.put("logs", logs);
        return Response.ok(result).build();
    }

    @GET
    @Path("/audit-logs/instance/{instanceId}")
    public Response getAuditLogsForInstance(@PathParam("instanceId") String instanceId) {
        List<ProcessAuditLog> logs = entityManager
                .createQuery("SELECT p FROM ProcessAuditLog p WHERE p.processInstanceId = :instanceId ORDER BY p.timestamp DESC", ProcessAuditLog.class)
                .setParameter("instanceId", instanceId)
                .getResultList();

        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", instanceId);
        result.put("total", logs.size());
        result.put("logs", logs);
        return Response.ok(result).build();
    }

@POST
@Path("")
@Transactional // CRITICAL: Ensures data is saved to Oracle
public Response startProcess(Map<String, Object> data) {
    try {
        System.out.println("Starting new checker-maker process with data: " + (data != null ? data : "empty"));
        // 1. Log the intent (joins the transaction)
        logAudit(null, "startProcess", "PROCESS_START_REQUEST", 
                "Attempting to start process with data: " + (data != null ? data : "empty"));

        // 2. FIX THE COMPILATION ERROR:
        // Create an empty model instance of the correct type
        org.kie.kogito.Model model = checkerMakerProcess.createModel();
        
        // Populate the model from the incoming Map
        if (data != null) {
            model.fromMap(data);
        }

        // 3. Create and start the instance using the Model
        var instance = checkerMakerProcess.createInstance(model);
        instance.start();
        
        String instanceId = instance.id();

        // 4. Log the success (joins the transaction)
        logAudit(instanceId, "startProcess", "PROCESS_STARTED", 
                "Process instance started successfully. ID: " + instanceId);

        return Response.status(201)
                .entity(Map.of(
                        "processInstanceId", instanceId,
                        "status", instance.status(),
                        "message", "Process started successfully"))
                .build();

    } catch (Exception e) {
        // If an error occurs, the transaction will roll back (including the first logAudit)
        // unless you use logAudit with REQUIRES_NEW
        System.err.println("Failed to start process: " + e.getMessage());
        return Response.status(500)
                .entity(Map.of(
                        "error", "Failed to start process",
                        "message", e.getMessage()))
                .build();
    }
}

    

    /**
     * Helper method to log audit entries using ProcessAuditLog entity
     */
    private void logAudit(String instanceId, String action, String eventType, String details) {
        try {
            ProcessAuditLog log = new ProcessAuditLog();
            log.setProcessInstanceId(instanceId != null ? instanceId : "SYSTEM");
            log.setProcessId("checker-maker");
            log.setEventType(eventType);
            log.setAction(action);
            log.setUserId("checker-maker-resource");
            log.setComments(details);
            log.setTimestamp(LocalDateTime.now());
            
            // Persist the entity to the database
            entityManager.persist(log);
            entityManager.flush(); // Force immediate write to database
            
            System.out.println("Audit logged: processInstanceId=" + log.getProcessInstanceId() + 
                    ", eventType=" + log.getEventType() + 
                    ", action=" + log.getAction() + 
                    ", timestamp=" + log.getTimestamp());
        } catch (Exception e) {
            System.err.println("Failed to log audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
}