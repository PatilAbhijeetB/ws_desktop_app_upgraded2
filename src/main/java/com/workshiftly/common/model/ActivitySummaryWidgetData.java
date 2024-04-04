/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;
import java.util.HashMap;

/**
 *
 * @author Hashan
 */
@DatabaseTable(tableName = "activity_summary_widget_data")
public class ActivitySummaryWidgetData implements DatabaseModel {
    public static final String DATABASE_NAME = "activity_summary_widget_data";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_WEEK = "week";
    public static final String FIELD_SUMMARY = "summary";

    @DatabaseField(columnName = ActivitySummaryWidgetData.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = ActivitySummaryWidgetData.FIELD_USER_ID)
    private String userId;

    @DatabaseField(columnName = ActivitySummaryWidgetData.FIELD_WEEK)
    private Long week;

    @DatabaseField(columnName = ActivitySummaryWidgetData.FIELD_SUMMARY)
    HashMap<String, Double> summary;

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

    public Long getWeek() {
        return week;
    }

    public HashMap<String, Double> getSummary() {
        return summary;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setWeek(Long week) {
        this.week = week;
    }

    public void setSummary(HashMap<String, Double> summary) {
        this.summary = summary;
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
