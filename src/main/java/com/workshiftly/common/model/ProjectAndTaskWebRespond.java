/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

/**
 *
 * @author hashan
 */
public class ProjectAndTaskWebRespond {
    private String projectId;
    private String projectName;
    private String projectCode;

    private String taskId;
    private String taskTitle;
    private long taskEstimate;
    private long taskDueDate;
    private long spentTime;
    
    private Long createdAt;
    private Long updatedAt;

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public long getTaskEstimate() {
        return taskEstimate;
    }

    public long getTaskDueDate() {
        return taskDueDate;
    }

    public long getSpentTime() {
        return spentTime;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setTaskEstimate(long taskEstimate) {
        this.taskEstimate = taskEstimate;
    }

    public void setTaskDueDate(long taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public void setSpentTime(long spentTime) {
        this.spentTime = spentTime;
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
