/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXDialog;
import com.workshiftly.common.model.FileDownload;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.CommonUtility;
import com.workshiftly.common.utility.DotEnvUtility;
import static com.workshiftly.common.utility.FileUtility.FILE_SEPARATOR;
import com.workshiftly.service.http.HttpRequestCaller;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttributeView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.util.Duration;

/**
 *
 * @author jade_m
 */
public class ApplicationDownloaderViewModel  implements ViewModel {
    
    private final SimpleStringProperty statusTxtProp;
    private final SimpleDoubleProperty progressBarValueProp;
    private final SimpleStringProperty progressPrecentageProp;
    private final SimpleBooleanProperty completeBtnVisibility;
    
    private FileDownload fileDownload;
    private JFXDialog parentDialog;
    
    public ApplicationDownloaderViewModel() {
        
        this.statusTxtProp = new SimpleStringProperty("Pending...");
        this.progressBarValueProp = new SimpleDoubleProperty(0.00);
        this.progressPrecentageProp = new SimpleStringProperty("0 MiB / 0 MiB");
        this.completeBtnVisibility = new SimpleBooleanProperty(false);
    }

    public void setFileDownload(FileDownload fileDownload) {
        this.fileDownload = fileDownload;
        
        PublishSubject<Double> publishSubject = this.fileDownload.getProgress();
        Disposable subscribe = publishSubject.subscribe((var progressValue) -> {
            Platform.runLater(() -> {
                this.progressBarValueProp.set(progressValue);
            
                if (progressValue < 0.01) {
                    
                    statusTxtProp.set("checking");
                } else {
                    statusTxtProp.set("downloading");
                    Long totalBytes = fileDownload.getTotalBytes();

                    if (totalBytes != null) {
                        String totalBytesTxt = 
                                CommonUtility.humanReadableByteCountBin(totalBytes);

                        Double currentDownloadedBytes = progressValue * totalBytes;
                        String downloadPercentageFormat = "%s/%s ";
                        String currentProgressTxt = 
                            CommonUtility.humanReadableByteCountBin(currentDownloadedBytes.longValue());
                        currentProgressTxt = String.format(
                                downloadPercentageFormat, 
                                currentProgressTxt, totalBytesTxt
                        );
                        progressPrecentageProp.set(currentProgressTxt);
                        
                        if (progressValue == 1.0) {
                            completeBtnVisibility.set(true);
                        }
                    }
                }
            });
        });
    }

    public SimpleStringProperty getStatusTxtProp() {
        return statusTxtProp;
    }

    public SimpleDoubleProperty getProgressBarValueProp() {
        return progressBarValueProp;
    }

    public SimpleStringProperty getProgressPrecentageProp() {
        return progressPrecentageProp;
    }

    public SimpleBooleanProperty getCompleteBtnVisibility() {
        return completeBtnVisibility;
    }

    public void setParentDialog(JFXDialog parentDialog) {
        this.parentDialog = parentDialog;
    }
    
    public void onClickCompleteBtn(ActionEvent actionEvent) {
        if (parentDialog != null) {
            
       File appDownloadDir = null;
       String systemUserHome = System.getProperty("user.home", null);
        
      
        
        String downloadsDir = systemUserHome + FILE_SEPARATOR +"Downloads";
        String applicationName = DotEnvUtility.ApplicationDataDirectory();
        String appDownloadsParentDir = downloadsDir + FILE_SEPARATOR + applicationName;
        
        appDownloadDir = new File(appDownloadsParentDir);
        
        HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
        String currentVersion = DotEnvUtility.getApplicationVersion();
        Response apiResponse = httpRequestCaller.getClientAppLatestVersion(currentVersion);
        JsonObject responseData = apiResponse.getData().getAsJsonObject();
        String latestAppVer = responseData.get("latestVersion").getAsString();
        
        String appName = DotEnvUtility.ApplicationName();
        String applicationVersion = DotEnvUtility.getApplicationVersion();
        String stageTitle = appName + "-"+latestAppVer+".msi";

         
        
        
        //HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\{3DFE7F32-C8B8-3E51-B477-0EBB16A834FE}
        String completeFilePath = appDownloadsParentDir + FILE_SEPARATOR+stageTitle;
        completeFilePath = completeFilePath.replace(FILE_SEPARATOR, FILE_SEPARATOR+FILE_SEPARATOR);

               
        try {
           
            ProcessBuilder builder = new ProcessBuilder("msiexec", "/i", completeFilePath);
            builder.start();
           // ProcessBuilder builder = new ProcessBuilder("msiexec", "/i", completeFilePath, "/quiet");
           // Process process = builder.start();
         
             System.exit(0);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
            
            
            
            parentDialog.close();
        }
    }
}
