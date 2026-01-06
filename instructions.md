
## Testing Workflow: Checker Maker

1. **Start a specific Instance**
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{}' http://localhost:8080/checker_maker
   ```
   *Response:* `{"id":"<ID>","status":"PENDING"}`

2. **Check Status**
   ```bash
   curl -X GET http://localhost:8080/checker_maker/<ID>
   ```

3. **Get "Maker" Task** (Act as user 'maker')
   ```bash
   curl -X GET "http://localhost:8080/checker_maker/<ID>/tasks?user=maker"
   ```
   *Note ID of the task returned.*

4. **Complete "Maker" Task**
   You can complete the Maker task with one of the following decisions: `approve` or `reject`.

   **Option A: Approve** (Proceed to Checker)
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{"decision": "approve"}' "http://localhost:8080/checker_maker/<ID>/Maker/<TASK_ID>?user=maker"
   ```

   **Option B: Reject** (Restart from Init)
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{"decision": "reject"}' "http://localhost:8080/checker_maker/<ID>/Maker/<TASK_ID>?user=maker"
   ```

5. **Get "Checker" Task** (Act as user 'checker')
   ```bash
   curl -X GET "http://localhost:8080/checker_maker/<ID>/tasks?user=checker"
   ```

6. **Complete "Checker" Task**
   You can complete the task with one of the following decisions: `approve`, `reject`, or `reset`.

   **Option A: Approve** (Ends the workflow)
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{"decision": "approve"}' "http://localhost:8080/checker_maker/<ID>/Checker/<TASK_ID>?user=checker"
   ```

   **Option B: Reject** (Sends back to Maker)
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{"decision": "reject"}' "http://localhost:8080/checker_maker/<ID>/Checker/<TASK_ID>?user=checker"
   ```
   *The process will continually loop back to the Maker task until approved.*

   **Option C: Reset** (Restarts from Init)
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{"decision": "reset"}' "http://localhost:8080/checker_maker/<ID>/Checker/<TASK_ID>?user=checker"
   ```
   *The process will loop back to the initialization step and then to the Maker task.*
