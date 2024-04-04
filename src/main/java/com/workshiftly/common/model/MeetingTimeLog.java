/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;

/**
 *
 * @author jade_m
 */
@DatabaseTable(tableName = "meeting_task_log")
public class MeetingTimeLog  implements DatabaseModel {
    
    public static final String DATABASE_NAME = "meeting_time_log";
    
    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_TASK_USER_ID = "task_user_id";
    public static final String FIELD_PROJECT_ID = "project_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_TASK_ID = "task_id";
    public static final String FIELD_IS_SYNCED = "is_synced";
    public static final String FIELD_IS_COMPLETED = "is_completed";
    public static final String FIELD_START_TIMESTAMP = "start_timestamp";
    public static final String FIELD_END_TIMESTAMP = "end_timestamp";
    public static final String FIELD_PARTICIPANTS = "participants";
    public static final String FIELD_SUMMARY = "summary";
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_ROW_ID, generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_TASK_USER_ID)
    private String taskUserId;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_PROJECT_ID)
    private String projectId;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_USER_ID)
    private String userId;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_TASK_ID)
    private String taskId;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_IS_SYNCED)
    private boolean isSynced;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_IS_COMPLETED)
    private boolean isCompleted;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_START_TIMESTAMP)
    private Long startedTimestamp;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_END_TIMESTAMP)
    private Long endTimestamp;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_PARTICIPANTS)
    private String participants;
    
    @DatabaseField(columnName = MeetingTimeLog.FIELD_SUMMARY)
    private String summary;
    
    @Override
    public String getDatabaseName() {
        return MeetingTimeLog.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    @Override
    public String getId() {
        return null;
    }

    public String getTaskUserId() {
        return taskUserId;
    }

    public void setTaskUserId(String taskUserId) {
        this.taskUserId = taskUserId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getStartedTimestamp() {
        return startedTimestamp;
    }

    public void setStartedTimestamp(Long startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isIsSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public boolean isIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    
    
}
