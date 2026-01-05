
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
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{}' "http://localhost:8080/checker_maker/<ID>/Maker/<TASK_ID>?user=maker"
   ```

5. **Get "Checker" Task** (Act as user 'checker')
   ```bash
   curl -X GET "http://localhost:8080/checker_maker/<ID>/tasks?user=checker"
   ```

6. **Complete "Checker" Task**
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{}' "http://localhost:8080/checker_maker/<ID>/Checker/<TASK_ID>?user=checker"
   ```
