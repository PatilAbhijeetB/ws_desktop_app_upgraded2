/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.service;

import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.presentation.mainwindow.project.ProjectViewModel;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author jade_m
 */
public class WorkingTaskUpdater extends Service<Response> {
    
    private ProjectViewModel projectViewModel;
    
    private WorkingTaskUpdater() {}
    
    public WorkingTaskUpdater(ProjectViewModel projectViewModel) {
        this.projectViewModel = projectViewModel;
    }

    @Override
    protected Task<Response> createTask() {
        return new Task<>() {
            @Override
            protected Response call() throws Exception {
                try {
                    Response workingTaskUpdate = projectViewModel
                        .updateCurrentWorkTaskSpentTime();
                    return workingTaskUpdate;
                } catch (Exception ex) {
                    return new Response(
                            true, StatusCode.APPLICATION_ERROR, 
                            "Application Error occurred"
                    );
                }
            }
        };
    }
    
}
