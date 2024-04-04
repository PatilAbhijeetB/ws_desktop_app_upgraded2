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
@DatabaseTable(tableName = "break_reason")
public class BreakReason implements DatabaseModel {

    public static final String DATABASE_NAME = "break_reason";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_ID = "id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_OTHER_REASON = "other_reason";
    public static final String FIELD_IS_AUTOMATICALLY_START = "is_automatically_start";
    public static final String FIELD_DURATION = "duration";

    @DatabaseField(columnName = BreakReason.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = BreakReason.FIELD_ID)
    private String id;

    @DatabaseField(columnName = BreakReason.FIELD_TITLE)
    private String title;

    @DatabaseField(columnName = BreakReason.FIELD_OTHER_REASON)
    private String otherReason;

    @DatabaseField(columnName = BreakReason.FIELD_IS_AUTOMATICALLY_START)
    private boolean isStartedAutomatically;

    @DatabaseField(columnName = BreakReason.FIELD_DURATION)
    private long duration;

    @Override
    public String getDatabaseName() {
        return ActiveWindow.DATABASE_NAME;
    }

    public Long getRowId() {
        return rowId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOtherReason() {
        return otherReason;
    }

    public boolean isStartedAutomatically() {
        return isStartedAutomatically;
    }

    public long getDuration() {
        return duration;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOtherReason(String otherReason) {
        this.otherReason = otherReason;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setStartedAutomatically(boolean isAutomaticallyStart) {
        this.isStartedAutomatically = isAutomaticallyStart;
    }
}
