package org.partha.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to store detailed audit trail with user, timestamp, and comments
 */
@Entity
@Table(name = "process_audit_log")
public class ProcessAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_instance_id", nullable = false)
    private String processInstanceId;

    @Column(name = "process_id")
    private String processId;

    @Column(name = "event_type", nullable = false)
    private String eventType; // STARTED, TASK_ASSIGNED, TASK_COMPLETED, VARIABLE_CHANGED, etc.

    @Column(name = "node_name")
    private String nodeName; // Task name or node name

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private String userId; // Who performed the action

    @Column(name = "user_name")
    private String userName; // Display name

    @Column(name = "action")
    private String action; // APPROVE, REJECT, RESET, START, COMPLETE, etc.

    @Column(name = "comments", length = 2000)
    private String comments; // User comments/reason for action

    @Column(name = "old_value", length = 1000)
    private String oldValue; // Previous value (for variable changes)

    @Column(name = "new_value", length = 1000)
    private String newValue; // New value (for variable changes)

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress; // User's IP address

    @Column(name = "user_agent")
    private String userAgent; // Browser/client info

    // Constructors
    public ProcessAuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ProcessAuditLog(String processInstanceId, String eventType, String userId) {
        this();
        this.processInstanceId = processInstanceId;
        this.eventType = eventType;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "ProcessAuditLog{" +
                "id=" + id +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}