/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.presentation.service;

import com.google.gson.Gson;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.domain.ProjectTaskModule;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author jade_m
 */
public class TaskCreateService extends Service<Response> {
    
    private final ProjectTask projectTask;
    private final boolean isNewTask;
    
    private TaskCreateService() {
        this.projectTask = null;
        this.isNewTask = false;
    }
    
    public TaskCreateService(ProjectTask projectTask, boolean isNewTask) {
        this.projectTask = projectTask;
        this.isNewTask = isNewTask;
    }

    @Override
    protected Task<Response> createTask() {
        return new Task<Response>() {
            @Override
            protected Response call() throws Exception {
                
                String taskType = projectTask.getType();
                ProjectTaskModule projectTaskModule = new ProjectTaskModule();
                Response response = null;
                
                switch (taskType) {
                    case ProjectTask.MEETING_TASK_TYPE:
                        response = projectTaskModule
                                .createEmployeeMeetingTask(projectTask.getProjectId());
                        break;
                    case ProjectTask.OFFLINE_TASK_TYPE:
                        response = isNewTask 
                                ? projectTaskModule.createOfflineManualTask(projectTask)
                                : projectTaskModule.updateOfflineManualTask(projectTask);
                        break;
                }
                return response;
            }
        };
    }
    
}
