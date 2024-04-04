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
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author jade_m
 */
@DatabaseTable(tableName = "company_project")
public class CompanyProject implements DatabaseModel, Comparable<CompanyProject> {
    public static final String DATABASE_NAME = "company_project";
    public static final String GENERAL_PROJECT_TYPE = "general";
    
    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_ID = "id";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_COMPANY_ID = "company_id";
    public static final String FIELD_IS_ACTIVE = "is_active";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_UPDATED_AT = "updated_at";
    
    @DatabaseField(columnName = CompanyProject.FIELD_ROW_ID, generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = CompanyProject.FIELD_ID, unique = true)
    private String id;
    
    @DatabaseField(columnName = CompanyProject.FIELD_CODE)
    private String code;
    
    @DatabaseField(columnName = CompanyProject.FIELD_NAME)
    private String name;
    
    @DatabaseField(columnName = CompanyProject.FIELD_TYPE)
    private String type;
    
    @DatabaseField(columnName = CompanyProject.FIELD_COMPANY_ID)
    private String companyId;
    
    @DatabaseField(columnName = CompanyProject.FIELD_IS_ACTIVE)
    private String isActive;
    
    @DatabaseField(columnName = CompanyProject.FIELD_DESCRIPTION)
    private String description;
    
    @DatabaseField(columnName = CompanyProject.FIELD_CREATED_AT)
    private Integer createdAt;
    
    @DatabaseField(columnName = CompanyProject.FIELD_UPDATED_AT)
    private Integer updatedAt;
    
    private List<ProjectTask> projectTasks = new ArrayList<>();
    private List<MeetingTimeLog> meetingTimeLogs = new ArrayList<>();
            
    @Override
    public String getDatabaseName() {
        return CompanyProject.DATABASE_NAME;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<ProjectTask> getProjectTasks() {
        return projectTasks;
    }

    public void setProjectTasks(List<ProjectTask> projectTasks) {
        this.projectTasks = projectTasks;
    }

    public List<MeetingTimeLog> getMeetingTimeLogs() {
        return meetingTimeLogs;
    }

    public void setMeetingTimeLogs(List<MeetingTimeLog> meetingTimeLogs) {
        this.meetingTimeLogs = meetingTimeLogs;
    }

    @Override
    public int compareTo(CompanyProject element) {
        return Comparator.comparing(CompanyProject::getCode)
                .thenComparing(CompanyProject::getName)
                .thenComparing(CompanyProject::getDescription)
                .thenComparing(CompanyProject::getIsActive)
                .compare(this, element);
    }
}
