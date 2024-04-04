/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.mainwindow.project.ProjectViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;

/**
 *
 * @author chamara
 */
public class LogoutNavigationViewModel implements ViewModel {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(LogoutNavigationViewModel.class);
    private SimpleBooleanProperty disableLogoutBtnProperty;
    
    public LogoutNavigationViewModel() {
        this.disableLogoutBtnProperty = new SimpleBooleanProperty(false);
    }
    
    public SimpleBooleanProperty getDisableLogoutBtnProperty() {
        return this.disableLogoutBtnProperty;
    }
    
    public void handleLogout(ActionEvent event) {
        
        SimpleObjectProperty<com.workshiftly.common.model.ProjectTask> currentWorkingTaskProp 
                = ProjectViewModel.getCURRENT_WORKING_TASK();
        com.workshiftly.common.model.ProjectTask currentWorkingTask = currentWorkingTaskProp.get();
        
        if (currentWorkingTask != null) {
            promptToPasueOrStopCurrentWorkingTask();
            return;
        }
         AuthenticationModule authenticationModule = new AuthenticationModule();
          Response response = authenticationModule.syncUserStatus("offline");
          try{
        StateStorage.set(StateName.IsLogout, Boolean.class, true);
          }catch(Exception ex){
          
          }
            
        Service<Response> domainService = getLogoutDomainService();
        domainService.start();
    }
    
    private Service<Response> getLogoutDomainService() {
        
        Service<Response> domainService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        AuthenticationModule authenticationModule = new AuthenticationModule();
                        Response domainResponse = authenticationModule.handleLogout();
                        return domainResponse;
                    }
                };
            }
        };
        domainService.setOnRunning((var stateEvent) -> {
            disableLogoutBtnProperty.set(true);
            MainScreenController.showLoadingSplashScreen();
        });
        domainService.setOnSucceeded((var stateEvent) -> {
            Response domainResult = domainService.getValue();
            boolean isSuccssResult = !domainResult.isError();
            
            MainScreenController.hideLoadingSplashScreen();
            disableLogoutBtnProperty.set(false);
            
            if (isSuccssResult) {
                AuthenticatedMainWindowViewModel.setUSER_WORK_STATUS(WorkStatusLog.WorkStatus.STOP);
            }
            
            if (!isSuccssResult) {
                // show up dialog layout if there is error occurred at domain layer
                JFXDialogLayout erroDialogLayout = new JFXDialogLayout();
                erroDialogLayout.setHeading(new Text("Error"));
                erroDialogLayout.setBody(new Text("Error occurred while logout, please try again"));
                
                JFXButton closeButton = new JFXButton("Close");
                closeButton.getStyleClass().add("dialog-modal-primary-btn");
                erroDialogLayout.setActions(closeButton);
                
                JFXDialog dialogBox = new JFXDialog();
                dialogBox.setTransitionType(JFXDialog.DialogTransition.CENTER);
                dialogBox.setContent(erroDialogLayout);
                dialogBox.setOverlayClose(false);
                closeButton.setOnAction((ActionEvent actionEvent) -> {
                    dialogBox.close();
                });
                MainScreenController.showJFXDialog(dialogBox);
                return;
            }
            
            try {
                MainScreenController.activate(StateName.MAIN_LOGIN_SCREEN, false, true);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Could not able to logout", ex);
            }
            
        });
        return domainService;
    }
    
    private void promptToPasueOrStopCurrentWorkingTask() {
        
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        dialogLayout.setHeading(new Text("Working Task Alert"));
        dialogLayout.setBody(new Text("You are currently working on a task. "
                + "Before logout please pause or stop the working task"));
        
        JFXButton dialogCloseBtn = new JFXButton("OK, I got it");
        dialogCloseBtn.getStyleClass().add("dialog-modal-primary-btn");
        dialogLayout.setActions(dialogCloseBtn);
        
        JFXDialog dialog = new JFXDialog(null, dialogLayout, JFXDialog.DialogTransition.CENTER);
        dialogCloseBtn.setOnAction((ActionEvent event) -> {
            dialog.close();
        });
        MainScreenController.showJFXDialog(dialog);
    }
}
