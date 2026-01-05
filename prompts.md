🔹 Prompt 1: Generate Base Quarkus + Kogito Project
Generate a minimal Quarkus project with Kogito BPMN support.
Use Maven, Java 21.
Include only required dependencies for Kogito workflows and REST.
Provide pom.xml, project structure, and application.properties

🔹 
generate pom file with jdk 21, kogito bpmn with quarkus
🔹 

mvn -N io.takari:maven:wrapper
🔹 

curl -X GET http://localhost:8080/checker_maker
🔹 
curl -X POST -H 'Content-Type: application/json' -d '{}' http://localhost:8080/checker_maker
🔹 
curl -X GET http://localhost:8080/checker_maker/2f2bce11-345c-45cd-9a32-370505613845/tasks
🔹 
curl -X GET "http://localhost:8080/checker_maker/2f2bce11-345c-45cd-9a32-370505613845/tasks?user=checker"
🔹 
http://localhost:8080/checker_maker/2f2bce11-345c-45cd-9a32-370505613845/Checker/77394c41-449c-4cb7-a7a1-b0e37ce364dc?user=checker
🔹 
curl -X GET "http://localhost:8080/checker_maker/2f2bce11-345c-45cd-9a32-370505613845/tasks?user=maker"


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

3. **Get "Checker" Task** (Act as user 'checker')
   ```bash
   curl -X GET "http://localhost:8080/checker_maker/<ID>/tasks?user=checker"
   ```
   *Note ID of the task returned.*

4. **Complete "Checker" Task**
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{}' "http://localhost:8080/checker_maker/<ID>/Checker/<TASK_ID>?user=checker"
   ```

5. **Get "Maker" Task** (Act as user 'maker')
   ```bash
   curl -X GET "http://localhost:8080/checker_maker/<ID>/tasks?user=maker"
   ```

6. **Complete "Maker" Task**
   ```bash
   curl -X POST -H 'Content-Type: application/json' -d '{}' "http://localhost:8080/checker_maker/<ID>/Maker/<TASK_ID>?user=maker"
   ```
