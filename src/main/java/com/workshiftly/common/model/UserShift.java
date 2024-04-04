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
 * @author dmhashan
 */
@DatabaseTable(tableName = "user_shift")
public class UserShift implements DatabaseModel {
    public static final String DATABASE_NAME = "user_shift";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_INDEX = "index";
    public static final String FIELD_SHIFT_START = "shift_start";
    public static final String FIELD_SHIFT_END = "shift_end";
    public static final String FIELD_WORKING_HOURS = "working_hours";
    
    @DatabaseField(columnName = UserShift.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = UserShift.FIELD_INDEX)
    private String index;

    @DatabaseField(columnName = UserShift.FIELD_SHIFT_START)
    private String shiftStart;

    @DatabaseField(columnName = UserShift.FIELD_SHIFT_END)
    private String shiftEnd;

    @DatabaseField(columnName = UserShift.FIELD_WORKING_HOURS)
    private String workingHours;

    @Override
    public String getDatabaseName() {
        return TaskStatusLog.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getIndex() {
        return index;
    }

    public String getShiftStart() {
        return shiftStart;
    }

    public String getShiftEnd() {
        return shiftEnd;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setShiftStart(String shiftStart) {
        this.shiftStart = shiftStart;
    }

    public void setShiftEnd(String shiftEnd) {
        this.shiftEnd = shiftEnd;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    @Override
    public String getId() {
        return null;
    }
}
