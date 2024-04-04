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
 * @author Hashan
 */
@DatabaseTable(tableName = "attendance_widget_data")
public class AttendanceWidgetData implements DatabaseModel {
    public static final String DATABASE_NAME = "activity_windows";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_START_SHIFT = "start_shift";
    public static final String FIELD_END_SHIFT = "end_shift";
    public static final String FIELD_START_WORKING = "start_working";
    public static final String FIELD_END_WORKING = "end_working";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_HOLIDAY = "is_holiday";

    public enum Status {
        HOLIDAY,
        ABSENT,
        INCOMPLETE,
        COMPLETE;
    }

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_USER_ID)
    private String userId;

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_DATE)
    private Long date;

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_START_SHIFT)
    private Long startShift;
    
    @DatabaseField(columnName = AttendanceWidgetData.FIELD_END_SHIFT)
    private Long endShift;
    
    @DatabaseField(columnName = AttendanceWidgetData.FIELD_START_WORKING)
    private Long startWorking;

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_END_WORKING)
    private Long endWorking;

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_STATUS)
    private Status status;

    @DatabaseField(columnName = AttendanceWidgetData.FIELD_IS_HOLIDAY)
    private Boolean isHoliday;

    @Override
    public String getDatabaseName() {
        return ActiveWindow.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getUserId() {
        return userId;
    }

    public Long getDate() {
        return date;
    }

    public Long getStartShift() {
        return startShift;
    }

    public Long getEndShift() {
        return endShift;
    }

    public Long getStartWorking() {
        return startWorking;
    }

    public Long getEndWorking() {
        return endWorking;
    }

    public Status getStatus() {
        return status;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public void setStartShift(Long startShift) {
        this.startShift = startShift;
    }

    public void setEndShift(Long endShift) {
        this.endShift = endShift;
    }

    public void setStartWorking(Long startWorking) {
        this.startWorking = startWorking;
    }

    public void setEndWorking(Long endWorking) {
        this.endWorking = endWorking;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }
    
    @Override
    public String getId() {
        return null;
    }
}
