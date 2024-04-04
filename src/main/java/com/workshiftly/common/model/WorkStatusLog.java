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
@DatabaseTable(tableName = "work_status_log")
public class WorkStatusLog implements DatabaseModel {

    public static final String DATABASE_NAME = "work_status_log";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_USER_DATE = "user_date";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_ACTION_TIMESTAMP = "action_timestamp";
    public static final String FIELD_WORK_STATUS = "work_status";
    public static final String FIELD_BREAK_REASON_ID = "break_reason_id";
    public static final String FIELD_BREAK_REASON_TITLE = "break_reason_Title";
    public static final String FIELD_IS_BREAK_AUTOMATICALLY_START = "is_break_automatically_start";
    public static final String FIELD_BREAK_AUTOMATICALLY_START_DURATION = "break_automatically_start_duration";
    public static final String FIELD_IS_SYNCED = "is_synced";
    public static final String FIELD_DEVICE_ID = "deviceId";
    
    public enum WorkStatus {
        BEGINNING,
        START,
        BREAK,
        IN_MEETING,
        OFFLINE_TASK,
        STOP;
    }

    @DatabaseField(columnName = WorkStatusLog.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = WorkStatusLog.FIELD_USER_ID, index = true)
    private String userId;

    @DatabaseField(columnName = WorkStatusLog.FIELD_USER_DATE)
    private String userDate;

    @DatabaseField(columnName = WorkStatusLog.FIELD_DATE)
    private long date;

    @DatabaseField(columnName = WorkStatusLog.FIELD_ACTION_TIMESTAMP, index = true)
    private long actionTimestamp;

    @DatabaseField(columnName = WorkStatusLog.FIELD_WORK_STATUS)
    private WorkStatus workStatus;

    @DatabaseField(columnName = WorkStatusLog.FIELD_BREAK_REASON_ID)
    private String breakReasonId;

    @DatabaseField(columnName = WorkStatusLog.FIELD_BREAK_REASON_TITLE)
    private String breakReasonTitle;

    @DatabaseField(columnName = WorkStatusLog.FIELD_IS_BREAK_AUTOMATICALLY_START)
    private boolean isBreakAutomaticallyStart;

    @DatabaseField(columnName = WorkStatusLog.FIELD_BREAK_AUTOMATICALLY_START_DURATION)
    private long breakAutomaticallyStartDuration;

    @DatabaseField(columnName = WorkStatusLog.FIELD_IS_SYNCED, index = true)
    private boolean isSynced;
    
    @DatabaseField(columnName = WorkStatusLog.FIELD_DEVICE_ID)
    private String deviceId;

    @Override
    public String getDatabaseName() {
        return WorkStatusLog.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getUserId() {
        return userId;
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

    public WorkStatus getWorkStatus() {
        return workStatus;
    }

    public String getBreakReasonId() {
        return breakReasonId;
    }

    public String getBreakReasonTitle() {
        return breakReasonTitle;
    }

    public boolean isBreakAutomaticallyStart() {
        return isBreakAutomaticallyStart;
    }

    public long getBreakAutomaticallyStartDuration() {
        return breakAutomaticallyStartDuration;
    }

    public boolean isSynced() {
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

    public void setUserDate(String userDate) {
        this.userDate = userDate;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setActionTimestamp(long actionTimestamp) {
        this.actionTimestamp = actionTimestamp;
    }

    public void setWorkStatus(WorkStatus workStatus) {
        this.workStatus = workStatus;
    }

    public void setBreakReasonId(String breakReasonId) {
        this.breakReasonId = breakReasonId;
    }

    public void setBreakReasonTitle(String breakReasonTitle) {
        this.breakReasonTitle = breakReasonTitle;
    }

    public void setIsBreakAutomaticallyStart(boolean isBreakAutomaticallyStart) {
        this.isBreakAutomaticallyStart = isBreakAutomaticallyStart;
    }

    public void setBreakAutomaticallyStartDuration(long breakAutomaticallyStartDuration) {
        this.breakAutomaticallyStartDuration = breakAutomaticallyStartDuration;
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
