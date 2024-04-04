/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.service;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.FileDownload;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.FileDownloader;
import com.workshiftly.common.utility.FileUtility;
import static com.workshiftly.common.utility.FileUtility.FILE_SEPARATOR;
import com.workshiftly.common.utility.OSUtility;
import com.workshiftly.domain.SettingsModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.presentation.view.ApplicationDownloaderView;
import com.workshiftly.presentation.viewmodel.ApplicationDownloaderViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import java.io.File;
import java.io.IOException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 *
 * @author jade_m
 */
public class AppInstallerDownloadService  extends Service<Response> {
    
    // class static final variables;
    private static final AppInstallerDownloadService singleton; 
    
    // singleton object properties;
    private JsonObject appUpdateInfo;
    private FileDownload fileDownload;
    private final SimpleBooleanProperty isRunning;    
    private String installerName;
    private JFXDialog parentDialogComponent;
    
    // static initializer block;
    static {
        singleton = new AppInstallerDownloadService();
    }
    
    // default constructor access private acrroding singleton pattern;
    private AppInstallerDownloadService() {
        isRunning = new SimpleBooleanProperty(false);
    }
    
    // getter methods
    public JsonObject getAppUpdateInfo() {
        return appUpdateInfo;
    }

    public String getInstallerName() {
        return installerName;
    }

    public FileDownload getFileDownload() {
        return fileDownload;
    }
    
    public boolean getIsRunning() {
        return isRunning.get();
    }

    private JFXDialog getParentDialogComponent() {
        return parentDialogComponent;
    }
    
    // setter methods
    public void setFileDownload(FileDownload fileDownload) {
        this.fileDownload = fileDownload;
    }

    private void setIsRunning(boolean isRunning) {
        this.isRunning.set(isRunning);
    }

    public void setInstallerName(String installerName) {
        this.installerName = installerName;
    }

    public void setParentDialogComponent(JFXDialog parentDialogComponent) {
        this.parentDialogComponent = parentDialogComponent;
    }
    
    public static void setParentDialog(JFXDialog parentDialog) {
        singleton.setParentDialogComponent(parentDialog);
    }
    
    
    private void setAppUpdateInfo(JsonObject appUpdateInfo) {
        
        singleton.appUpdateInfo = appUpdateInfo;
        OSUtility systemOS = OSUtility.getSystemOS();
        
        if (systemOS.equals(OSUtility.OTHER)) {
            return;
            
        }
        
        JsonObject downloadURLs = appUpdateInfo.get("downloadURLs").getAsJsonObject();
        String downloadURL = downloadURLs.get(systemOS.toString()).getAsString();

        String[] particles = downloadURL.split("/");
        String fileName =  particles[particles.length - 1];
        singleton.setInstallerName(fileName);
        
        if (!FileDownloader.contains(fileName)) {
            fileDownload = new FileDownload(fileName, downloadURL);
        }
    }
    
    @Override
    protected Task<Response> createTask() {
        return new Task() {
            @Override
            protected Response call() throws Exception {
                
                if (fileDownload == null) {
                    return new Response(true, StatusCode.NULL_REFERENCE_ERROR, "File download is null");
                }
                
                SettingsModule settingsModule = new SettingsModule();
                Response fileDownloadResponse = settingsModule.downloadLatestApplicationInstaller(fileDownload);
                return fileDownloadResponse;
            }
        };
    }
    
    public static final void download(JsonObject appUpdateInfo) {
        if (singleton.getIsRunning()) {
            return;
        }
        
        singleton.setAppUpdateInfo(appUpdateInfo);
        
        singleton.setOnRunning((WorkerStateEvent workerEvent) -> {
            
            if (singleton.getParentDialogComponent() != null) {
                singleton.getParentDialogComponent().close();
            }
            
            if (singleton.getFileDownload() == null) {
                return;
            }
            
            FileDownload fileDownloadObj = singleton.getFileDownload();
                
            try {
                ViewTuple<ApplicationDownloaderView, ApplicationDownloaderViewModel> downloaderViewTuple;
                downloaderViewTuple = FluentViewLoader.fxmlView(ApplicationDownloaderView.class).load();

                Parent downloaderView = downloaderViewTuple.getView();
                ApplicationDownloaderViewModel downloaderViewModel = downloaderViewTuple.getViewModel();
                downloaderViewModel.setFileDownload(fileDownloadObj);

                JFXDialog downloadInfoDialog = new JFXDialog(null, (Region) downloaderView, JFXDialog.DialogTransition.CENTER);
                downloaderViewModel.setParentDialog(downloadInfoDialog);
                MainScreenController.showJFXDialog(downloadInfoDialog);
            } catch (Exception ex) {
            }
        });
        
        singleton.setOnSucceeded((var workerEvent) -> {
            
            singleton.setIsRunning(false);
            Response response = singleton.getValue();
            
            if (response.isError()) {
                singleton.popupErrorDialog(response);
                return;
            }
        });
        
        singleton.start();
    }
    
    // error response handler: Error Popup
    private void popupErrorDialog(Response response) {
        
        PopupDialogHeading errorDialogHeading = new PopupDialogHeading(
                PopupDialogHeading.PopupType.ERROR,
                "Application Installer Download"
        );
        PopupDialogBox errorDialogBox = new PopupDialogBox(errorDialogHeading);
        errorDialogBox.setDescription(
                PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                "Unexpected error occurred while downloading"
        );
        JFXButton closeBtn = errorDialogBox.getDialogButton(
                PopupDialogBox.DialogButton.RIGHT_MOST
        );
     
        
        closeBtn.setVisible(true); closeBtn.setDisable(false);
        closeBtn.setOnAction((var event) -> {
       
         
        errorDialogBox.close();
        });
        errorDialogBox.load();
        
    }
    
    public static final boolean stop() {
        return AppInstallerDownloadService.singleton.cancel();
    }
}
