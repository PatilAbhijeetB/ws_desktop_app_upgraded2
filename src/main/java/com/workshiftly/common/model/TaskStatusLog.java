/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;

/**
 *
 * @author hashan
 */
@DatabaseTable(tableName = "task_status_log")
public class TaskStatusLog implements DatabaseModel {
    public static final String DATABASE_NAME = "task_status_log";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_TASK_ID = "task_id";
    public static final String FIELD_USER_DATE = "user_date";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_ACTION_TIMESTAMP = "action_timestamp";
    public static final String FIELD_WORK_STATUS = "task_status";
    public static final String FIELD_IS_SYNCED = "is_synced";
    public static final String FIELD_DEVICE_ID = "device_id";

    public enum TaskStatus {
        TODO,
        START,
        BREAK,
        STOP;
    }

    @DatabaseField(columnName = TaskStatusLog.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = TaskStatusLog.FIELD_USER_ID)
    private String userId;

    @DatabaseField(columnName = TaskStatusLog.FIELD_TASK_ID)
    private String taskId;

    @DatabaseField(columnName = TaskStatusLog.FIELD_USER_DATE)
    private String userDate;

    @DatabaseField(columnName = TaskStatusLog.FIELD_DATE)
    private long date;

    @DatabaseField(columnName = TaskStatusLog.FIELD_ACTION_TIMESTAMP)
    private long actionTimestamp;

    @DatabaseField(columnName = TaskStatusLog.FIELD_WORK_STATUS)
    private TaskStatusLog.TaskStatus taskStatus;

    @DatabaseField(columnName = TaskStatusLog.FIELD_IS_SYNCED)
    private boolean isSynced;
    
    @DatabaseField(columnName = TaskStatusLog.FIELD_DEVICE_ID)
    private String deviceId;

    @Override
    public String getDatabaseName() {
        return TaskStatusLog.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getUserDate() {
        return userDate;
    }

    public long getDate() {
        return date;
    }

    public long getActionTimestamp() {
        return actionTimestamp;
    }

    public TaskStatusLog.TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public boolean isIsSynced() {
        return isSynced;
    }

    public String getDeviceId() {
        return deviceId;
    }
    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setUserDate(String userDate) {
        this.userDate = userDate;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setActionTimestamp(long actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public void setTaskStatus(TaskStatusLog.TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }
    
    public void setDeviceId(String StrDeviceId) {
        this.deviceId = StrDeviceId;
    }
    
    @Override
    public String getId() {
        return null;
    }

}
