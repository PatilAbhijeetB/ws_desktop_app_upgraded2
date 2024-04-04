/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.service;

import com.workshiftly.common.model.Response;
import com.workshiftly.domain.UserRecoveryModule;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author chamara
 */
public class UserRecoveryService extends Service<Response> {

    private String requestUserId;
    private UserRecoveryModule userRecoveryModule;
    
    // default constructor
    private UserRecoveryService() {}
    
    public UserRecoveryService(String userId) throws Exception {
        this.requestUserId = userId;
        this.userRecoveryModule = new UserRecoveryModule(userId);
    }
    
    @Override
    protected Task<Response> createTask() {
        return new Task<Response>() {
            String userId = requestUserId;
            
            @Override
            protected Response call() throws Exception {
                Response response = userRecoveryModule.getRecoveryStatus();
                return response;
            }
        };
    }
}
