/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXDialog;
import com.workshiftly.presentation.service.AppInstallerDownloadService;
import de.saxsys.mvvmfx.ViewModel;

/**
 *
 * @author jade_m
 */
public class AppDownloadAlertViewModel implements ViewModel {
    
    private JsonObject appUpdateInfo;
    private JFXDialog parentDialogComponent;
    
    public AppDownloadAlertViewModel() {
        
    }

    public void setAppUpdateInfo(JsonObject appUpdateInfo) {
        this.appUpdateInfo = appUpdateInfo;
    }

    public void setParentDialogComponent(JFXDialog parentDialogComponent) {
        this.parentDialogComponent = parentDialogComponent;
    }
    
    public void onClickDownloadNowBtn() {
        AppInstallerDownloadService.setParentDialog(parentDialogComponent);
        AppInstallerDownloadService.download(appUpdateInfo);
    }
    
    public void onClickRemindMeLater() {
        parentDialogComponent.close();
    }
    
}
