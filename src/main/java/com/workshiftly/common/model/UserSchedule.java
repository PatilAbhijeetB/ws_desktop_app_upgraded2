/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;
import java.util.ArrayList;

/**
 *
 * @author dmhashan
 */
@DatabaseTable(tableName = "user_schedule")
public class UserSchedule implements DatabaseModel {
    public static final String DATABASE_NAME = "user_schedule";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_SCHEDULE_START_AT = "schedule_start_at";
    public static final String FIELD_SCHEDULE_END_AT = "schedule_end_at";
    public static final String FIELD_SHIFTS = "shifts";
    
    @DatabaseField(columnName = UserSchedule.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = UserSchedule.FIELD_NAME)
    private String name;

    @DatabaseField(columnName = UserSchedule.FIELD_TYPE)
    private String type;

    @DatabaseField(columnName = UserSchedule.FIELD_SCHEDULE_START_AT)
    private String scheduleStartAt;

    @DatabaseField(columnName = UserSchedule.FIELD_SCHEDULE_END_AT)
    private String scheduleEndAt;

    @DatabaseField(columnName = UserSchedule.FIELD_SHIFTS)
    private ArrayList<UserShift> shifts;
    
    @Override
    public String getDatabaseName() {
        return TaskStatusLog.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getScheduleStartAt() {
        return scheduleStartAt;
    }

    public String getScheduleEndAt() {
        return scheduleEndAt;
    }

    public ArrayList<UserShift> getShifts() {
        return shifts;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setScheduleStartAt(String scheduleStartAt) {
        this.scheduleStartAt = scheduleStartAt;
    }

    public void setScheduleEndAt(String scheduleEndAt) {
        this.scheduleEndAt = scheduleEndAt;
    }

    public void setShifts(ArrayList<UserShift> shifts) {
        this.shifts = shifts;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getId() {
        return null;
    }
}
