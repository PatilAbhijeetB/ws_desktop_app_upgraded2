/*
 * To change this license header, choose License Headers in Task Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.common.constant.ApiEntityStatus;
import com.workshiftly.persistence.Database.DatabaseModel;

/**
 *
 * @author hashan
 */
@DatabaseTable(tableName = "task")
public class Task implements DatabaseModel {
    public static final String DATABASE_NAME = "task";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_ID = "id";
    public static final String FIELD_PROJECT_ID = "project_id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_DUE_DATE = "due_date";
    public static final String FIELD_ESTIMATION = "estimation";
    public static final String FIELD_SPENT_TIME = "spent_time";
    public static final String FIELD_IS_DIRTY = "is_dirty";
    public static final String FIELD_HAS_COMPLETED = "has_completed";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_API_ENTITY_STATUS = "api_entity_status";
    public static final String FIELD_IS_SYNCED = "is_sync";
    public static final String FIELD_UPDATED_AT = "updated_at";
    public static final String FIELD_CREATED_AT = "created_at";
    
    @DatabaseField(columnName = Task.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = Task.FIELD_ID)
    private String id;

    @DatabaseField(columnName = Task.FIELD_PROJECT_ID)
    private String projectId;

    @DatabaseField(columnName = Task.FIELD_TITLE)
    private String title;

    @DatabaseField(columnName = Task.FIELD_DUE_DATE)
    private long dueDate;

    @DatabaseField(columnName = Task.FIELD_ESTIMATION)
    private long estimation;

    @DatabaseField(columnName = Task.FIELD_SPENT_TIME)
    private long spentTime;

    @DatabaseField(columnName = Task.FIELD_IS_DIRTY)
    private boolean isDirty;

    @DatabaseField(columnName = Task.FIELD_HAS_COMPLETED)
    private boolean hasCompleted;
    
    @DatabaseField(columnName = Task.FIELD_USER_ID)
    private String userId;
    
    @DatabaseField(columnName = Task.FIELD_API_ENTITY_STATUS)
    private ApiEntityStatus ApiEntityStatus;
    
    @DatabaseField(columnName = Task.FIELD_IS_SYNCED)
    private boolean isSynced;
    
    @DatabaseField(columnName = Task.FIELD_CREATED_AT)
    private Long createdAt;
    
    @DatabaseField(columnName = Task.FIELD_UPDATED_AT)
    private Long updatedAt;
    
    
    @Override
    public String getDatabaseName() {
        return Task.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public long getDueDate() {
        return dueDate;
    }

    public long getEstimation() {
        return estimation;
    }

    public long getSpentTime() {
        return spentTime;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean hasCompleted() {
        return hasCompleted;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setId(String id) {
        this.id = id;
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
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public void setEstimation(long estimation) {
        this.estimation = estimation;
    }

    public void setSpentTime(long spentTime) {
        this.spentTime = spentTime;
    }

    public void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public void setHasCompleted(boolean hasCompleted) {
        this.hasCompleted = hasCompleted;
    }

    public ApiEntityStatus getApiEntityStatus() {
        return ApiEntityStatus;
    }

    public void setApiEntityStatus(ApiEntityStatus ApiEntityStatus) {
        this.ApiEntityStatus = ApiEntityStatus;
    }

    public boolean isIsSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
