/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.service;

import com.workshiftly.common.model.Response;
import com.workshiftly.domain.SettingsModule;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author jade_m
 */
public class CheckAppUpdateService extends Service<Response> {
    
    private static Response lastResponse;

    @Override
    protected Task<Response> createTask() {
        return new Task<Response>() {
            @Override
            protected Response call() throws Exception {
                SettingsModule domainModule = new SettingsModule();
                lastResponse = domainModule.checkAppUpdateAvailability();
                return lastResponse;
            }  
        };
    }

    public static Response getLastResponse() {
        return lastResponse;
    }
    
}
