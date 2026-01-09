# Kogito Maker-Checker Workflow - API Reference

## Complete Endpoint Guide with Audit Logging

All endpoints are fully integrated with audit logging. Every action is automatically tracked in the `PROCESS_AUDIT_LOG` table.

### Base URL
```
http://localhost:8080/maker-checker
```

---

## Process Management Endpoints

### 1. Start a New Process Instance
**POST** `/` or `/maker-checker`

Start a new maker-checker approval workflow process.

```bash
curl -X POST http://localhost:8080/maker-checker \
  -H "Content-Type: application/json" \
  -d '{"status": "PENDING", "initiatedBy": "user@example.com"}'
```

**Response (201 Created):**
```json
{
  "processInstanceId": "d1234567-89ab-cdef-0123-456789abcdef",
  "status": "ACTIVE",
  "message": "Process started successfully"
}
```

**Audit Event:** `PROCESS_START_REQUEST`, `PROCESS_STARTED`

---

### 2. List All Process Instances
**GET** `/instances`

Retrieve all active and completed process instances with their statuses.

```bash
curl -X GET http://localhost:8080/maker-checker/instances
```

**Response:**
```json
{
  "total": 3,
  "instances": [
    {
      "id": "d1234567-89ab-cdef-0123-456789abcdef",
      "processId": "maker_checker",
      "status": "ACTIVE",
      "startDate": "2026-01-09T10:30:00",
      "lastUpdate": "2026-01-09T10:35:00"
    },
    {
      "id": "a9876543-21fe-dcba-9876-543210fedcba",
      "processId": "maker_checker",
      "status": "COMPLETED",
      "startDate": "2026-01-09T09:00:00",
      "lastUpdate": "2026-01-09T09:45:00"
    }
  ]
}
```

**Audit Event:** `QUERY` - Records list operation with instance count

---

### 3. Get Specific Process Instance
**GET** `/instances/{instanceId}`

Retrieve details for a specific process instance.

```bash
curl -X GET http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef
```

**Response:**
```json
{
  "id": "d1234567-89ab-cdef-0123-456789abcdef",
  "processId": "maker_checker",
  "status": "ACTIVE",
  "startDate": "2026-01-09T10:30:00",
  "lastUpdate": "2026-01-09T10:35:00",
  "variables": {
    "decision": "pending"
  }
}
```

**Audit Event:** `QUERY` or `QUERY_FAILED` (if instance not found)

---

## Task Management Endpoints

### 4. Get Tasks for Process Instance
**GET** `/instances/{instanceId}/tasks`

List all pending user tasks in a process instance.

```bash
curl -X GET http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef/tasks
```

**Response:**
```json
{
  "total": 1,
  "tasks": [
    {
      "id": "task-uuid-12345",
      "name": "Maker",
      "processInstanceId": "d1234567-89ab-cdef-0123-456789abcdef",
      "state": "Ready",
      "actualOwner": null
    }
  ]
}
```

**Audit Event:** `QUERY` - Records task query with count

---

### 5. Complete a Task (Task Action)
**POST** `/instances/{instanceId}/tasks/{taskId}/complete`

Complete a task with a decision (approve/reject/reset).

**Complete Maker Task - Approve (proceed to Checker):**
```bash
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "approve"}' \
  http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef/tasks/task-uuid-12345/complete
```

**Complete Maker Task - Reject (restart from Init):**
```bash
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "reject"}' \
  http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef/tasks/task-uuid-12345/complete
```

**Complete Checker Task - Approve (end workflow):**
```bash
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "approve"}' \
  http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef/tasks/task-uuid-67890/complete
```

**Complete Checker Task - Reject (send back to Maker):**
```bash
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "reject"}' \
  http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef/tasks/task-uuid-67890/complete
```

**Complete Checker Task - Reset (restart from Init):**
```bash
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "reset"}' \
  http://localhost:8080/maker-checker/instances/d1234567-89ab-cdef-0123-456789abcdef/tasks/task-uuid-67890/complete
```

**Response (200 OK):**
```json
{
  "taskId": "task-uuid-12345",
  "status": "COMPLETED",
  "message": "Task completed successfully"
}
```

**Audit Events:** 
- `POST_REQUEST_RECEIVED` - Request captured by global filter
- `TASK_COMPLETED` - Task completion recorded
- `TASK_COMPLETION_FAILED` (if error occurs)

---

## Audit Log Endpoints

### 6. Get All Audit Logs
**GET** `/audit-logs`

Retrieve all recorded audit log entries from the system. Returns the most recent logs.

```bash
curl -X GET http://localhost:8080/maker-checker/audit-logs
```

**Response:**
```json
{
  "total": 47,
  "limit": 50,
  "offset": 0,
  "logs": [
    {
      "id": 1,
      "processInstanceId": "d1234567-89ab-cdef-0123-456789abcdef",
      "processId": "maker_checker",
      "eventType": "PROCESS_STARTED",
      "action": "startProcess",
      "userId": "maker-checker-resource",
      "userName": null,
      "taskId": null,
      "comments": "Process instance started successfully. ID: d1234567-89ab-cdef-0123-456789abcdef",
      "timestamp": "2026-01-09T10:30:00"
    },
    {
      "id": 2,
      "processInstanceId": "d1234567-89ab-cdef-0123-456789abcdef",
      "processId": "maker_checker",
      "eventType": "QUERY",
      "action": "getTasks",
      "userId": "maker-checker-resource",
      "userName": null,
      "taskId": null,
      "comments": "Retrieved 1 task(s) for instance",
      "timestamp": "2026-01-09T10:31:00"
    },
    {
      "id": 3,
      "processInstanceId": "d1234567-89ab-cdef-0123-456789abcdef",
      "processId": "maker_checker",
      "eventType": "TASK_COMPLETED",
      "action": "completeTask",
      "userId": "maker-user",
      "userName": "Maker",
      "taskId": "task-uuid-12345",
      "comments": "Maker task completed with decision: approve",
      "timestamp": "2026-01-09T10:35:00"
    }
  ]
}
```

---

### 7. Get Audit Logs for Specific Instance
**GET** `/audit-logs/instance/{instanceId}`

Retrieve audit trail for a specific process instance - shows complete history of actions taken.

```bash
curl -X GET http://localhost:8080/maker-checker/audit-logs/instance/d1234567-89ab-cdef-0123-456789abcdef
```

**Response:**
```json
{
  "instanceId": "d1234567-89ab-cdef-0123-456789abcdef",
  "total": 5,
  "logs": [
    {
      "id": 1,
      "eventType": "PROCESS_START_REQUEST",
      "action": "startProcess",
      "timestamp": "2026-01-09T10:30:00",
      "comments": "POST request to start process with data: {}"
    },
    {
      "id": 2,
      "eventType": "PROCESS_STARTED",
      "action": "startProcess",
      "timestamp": "2026-01-09T10:30:01",
      "comments": "Process instance started successfully"
    },
    {
      "id": 3,
      "eventType": "QUERY",
      "action": "getTasks",
      "timestamp": "2026-01-09T10:31:00",
      "comments": "Retrieved 1 task(s) for instance"
    },
    {
      "id": 4,
      "eventType": "TASK_COMPLETED",
      "action": "completeTask",
      "userId": "maker-user",
      "timestamp": "2026-01-09T10:35:00",
      "comments": "Maker task completed with decision: approve"
    },
    {
      "id": 5,
      "eventType": "TASK_COMPLETED",
      "action": "completeTask",
      "userId": "checker-user",
      "timestamp": "2026-01-09T10:45:00",
      "comments": "Checker task completed with decision: approve"
    }
  ]
}
```

---

### 8. Health Check
**GET** `/health`

Simple health check endpoint to verify service is running.

```bash
curl -X GET http://localhost:8080/maker-checker/health
```

**Response:**
```json
{
  "status": "UP",
  "message": "Maker-Checker workflow service is running"
}
```

---

## Workflow Sequence Examples

### Complete Approval Workflow

```bash
# 1. Start a new process
INSTANCE_ID=$(curl -s -X POST -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:8080/maker-checker | jq -r '.processInstanceId')

echo "Started process: $INSTANCE_ID"

# 2. Get Maker task
TASK_ID=$(curl -s -X GET \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks | \
  jq -r '.tasks[0].id')

echo "Got Maker task: $TASK_ID"

# 3. Complete Maker task with approval
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "approve"}' \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks/$TASK_ID/complete

# 4. Get Checker task
TASK_ID=$(curl -s -X GET \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks | \
  jq -r '.tasks[0].id')

echo "Got Checker task: $TASK_ID"

# 5. Complete Checker task with approval
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "approve"}' \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks/$TASK_ID/complete

echo "Workflow completed!"

# 6. View complete audit trail
curl -X GET http://localhost:8080/maker-checker/audit-logs/instance/$INSTANCE_ID | jq .
```

### Rejection and Rework Scenario

```bash
# Start process and get Maker task as above...
INSTANCE_ID="d1234567-89ab-cdef-0123-456789abcdef"
TASK_ID="task-uuid-12345"

# 1. Maker rejects (send back to Init)
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "reject"}' \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks/$TASK_ID/complete

echo "Maker rejected - process reset"

# 2. Get new Maker task (process returned to Maker after reset)
TASK_ID=$(curl -s -X GET \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks | \
  jq -r '.tasks[0].id')

# 3. Maker approves on second attempt
curl -X POST -H 'Content-Type: application/json' \
  -d '{"decision": "approve"}' \
  http://localhost:8080/maker-checker/instances/$INSTANCE_ID/tasks/$TASK_ID/complete

echo "Workflow proceeding to Checker"
```

---

## Audit Event Types

All actions are logged with the following event types:

| Event Type | Description | Triggered By |
|-----------|-----------|-----------|
| `PROCESS_START_REQUEST` | Process start request received | POST / endpoint |
| `PROCESS_STARTED` | Process instance successfully created | startProcess() |
| `PROCESS_START_FAILED` | Process creation failed | startProcess() error |
| `QUERY` | Data query operation | List/Get endpoints |
| `QUERY_FAILED` | Query operation failed | Query error |
| `POST_REQUEST_RECEIVED` | HTTP POST request intercepted | Global AuditRequestFilter |
| `TASK_COMPLETED` | User task completed successfully | completeTask() |
| `TASK_COMPLETION_FAILED` | Task completion failed | completeTask() error |

---

## Database Details

**Database:** H2 In-Memory (Development)
- Connection: `jdbc:h2:mem:kogito`
- Schema: `KOGITO`
- Audit Table: `PROCESS_AUDIT_LOG`

**Tables:**
- `PROCESS_AUDIT_LOG` - All audit trail records
- `PROCESS_INSTANCES` - Active and completed process instances
- `PROCESS_USER_TASKS` - User task definitions and states
- (+ Kogito internal tables for state management)

---

## Quick Test Commands

**Test 1: Full workflow with audit trail**
```bash
# Using the complete approval workflow example above
```

**Test 2: View audit logs**
```bash
curl -X GET http://localhost:8080/maker-checker/audit-logs | jq '.logs | length'
```

**Test 3: Get all instances**
```bash
curl -X GET http://localhost:8080/maker-checker/instances | jq '.instances | length'
```

**Test 4: Health check**
```bash
curl -X GET http://localhost:8080/maker-checker/health
```

---

## Notes

- All timestamps are in ISO 8601 format (UTC)
- Audit logs are persistent in the database and survive application restarts
- The audit system automatically tracks all API calls
- User IDs and context are captured for full traceability
- All responses include proper HTTP status codes (200, 201, 400, 500, etc.)
- Database is currently H2 in-memory (use persistent database for production)
