/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.common.constant.ApiEntityStatus;
import com.workshiftly.persistence.Database.DatabaseModel;
import java.util.List;

/**
 *
 * @author hashan
 */
@DatabaseTable(tableName = "project")
public class Project implements DatabaseModel {
    public static final String DATABASE_NAME = "project";

    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_API_ENTITY_STATUS = "api_entity_status";

    @DatabaseField(columnName = Project.FIELD_ROW_ID, generatedId = true)
    private Long rowId;

    @DatabaseField(columnName = Project.FIELD_ID)
    private String id;

    @DatabaseField(columnName = Project.FIELD_NAME)
    private String name;

    @DatabaseField(columnName = Project.FIELD_CODE)
    private String code;
    
    @DatabaseField(columnName = Project.FIELD_USER_ID)
    private String userId;
    
    @DatabaseField(columnName = Project.FIELD_API_ENTITY_STATUS)
    private ApiEntityStatus apiEntityStatus;
    
    private List<Task> tasks;
    
    @Override
    public String getDatabaseName() {
        return Project.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ApiEntityStatus getApiEntityStatus() {
        return apiEntityStatus;
    }

    public void setApiEntityStatus(ApiEntityStatus apiEntityStatus) {
        this.apiEntityStatus = apiEntityStatus;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
