/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;
import java.util.Comparator;

/**
 *
 * @author jade_m
 */
@DatabaseTable(tableName = "project_task")
public class ProjectTask implements DatabaseModel, Comparable<ProjectTask> {
    
    public static final String DATABASE_NAME = "project_task";
    public static final String MEETING_TASK_TYPE = "meeting";
    public static final String OFFLINE_TASK_TYPE = "offline";
    
    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_ID = "id";
    public static final String FIELD_PROJECT_ID = "project_id";
    public static final String FIELD_PROJECT_NAME = "project_name";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_TASK_ID = "task_id";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ESTIMATE = "estimate";
    public static final String FIELD_DUE_DATE = "due_date";
    public static final String FIELD_SPENT_TIME = "spent_time";
    public static final String FIELD_HAS_COMPLETED = "has_completed";
    public static final String FIELD_IS_DIRTY = "is_dirty";
    public static final String FIELD_IS_SYNCED = "is_synced";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_UPDATED_AT = "updated_at";
    
    @DatabaseField(columnName = ProjectTask.FIELD_ROW_ID, generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = ProjectTask.FIELD_ID)
    private String id;
    
    @DatabaseField(columnName = ProjectTask.FIELD_USER_ID)
    private String userId;
    
    @DatabaseField(columnName = ProjectTask.FIELD_PROJECT_ID)
    private String projectId;
    
    @DatabaseField(columnName = ProjectTask.FIELD_PROJECT_NAME)
    private String projectName;
    
    @DatabaseField(columnName = ProjectTask.FIELD_TASK_ID)
    private String taskId;
    
    @DatabaseField(columnName = ProjectTask.FIELD_CODE)
    private String code;
    
    @DatabaseField(columnName = ProjectTask.FIELD_TITLE)
    private String title;
    
    @DatabaseField(columnName = ProjectTask.FIELD_TYPE)
    private String type;
    
    @DatabaseField(columnName = ProjectTask.FIELD_DESCRIPTION)
    private String description;
    
    @DatabaseField(columnName = ProjectTask.FIELD_ESTIMATE)
    private Long estimate;
    
    @DatabaseField(columnName = ProjectTask.FIELD_DUE_DATE)
    private Long dueDate;
    
    @DatabaseField(columnName = ProjectTask.FIELD_SPENT_TIME)
    private Long spentTime;
    
    @DatabaseField(columnName = ProjectTask.FIELD_HAS_COMPLETED)
    private boolean hasCompleted;
    
    @DatabaseField(columnName = ProjectTask.FIELD_IS_DIRTY)
    private boolean isDirty;
    
    @DatabaseField(columnName = ProjectTask.FIELD_IS_SYNCED)
    private boolean isSynced;
    
    @DatabaseField(columnName = ProjectTask.FIELD_CREATED_AT)
    private Integer createdAt;
    
    @DatabaseField(columnName = ProjectTask.FIELD_UPDATED_AT)
    private Integer updatedAt;

    @Override
    public String getDatabaseName() {
        return ProjectTask.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }
    
    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }
    
     public String getProjectName() {
        return projectName;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
      public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEstimate() {
        return estimate;
    }

    public void setEstimate(Long estimate) {
        this.estimate = estimate;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public Long getSpentTime() {
        return spentTime;
    }

    public void setSpentTime(Long spentTime) {
        this.spentTime = spentTime;
    }

    public boolean isHasCompleted() {
        return hasCompleted;
    }

    public void setHasCompleted(boolean hasCompleted) {
        this.hasCompleted = hasCompleted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isIsDirty() {
        return isDirty;
    }

    public void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean isIsSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public Integer getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Integer updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(ProjectTask task) {
        if (this.equals(task)) {
            return 0;
        }
        Comparator<ProjectTask> comparator = Comparator.comparing(ProjectTask::getCode)
                .thenComparing(ProjectTask::getTitle)
                .thenComparing(ProjectTask::getDescription)
                .thenComparing(ProjectTask::isHasCompleted)
                .thenComparingLong((ProjectTask t) -> {
                    try {
                        return t.getEstimate();
                    } catch (NullPointerException ex) {
                        return 0;
                    }
                })
                .thenComparingLong((ProjectTask t) -> {
                    try {
                        return t.getDueDate();
                    } catch (NullPointerException ex) {
                        return 0;
                    }
                });
        
        try {
            int comparisonBit = comparator.compare(this, task);
            return comparisonBit;
        } catch (Exception ex) {
            return 0;
        }
    }
}
