/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.service.UserTermAndConditionService;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.text.Text;

/**
 *
 * @author chamara
 */
public class TermsOfServiceViewModel implements ViewModel {
    
    private final JsonObject latestTNCUserRecord;
    
    public TermsOfServiceViewModel() {
        this.latestTNCUserRecord = StateStorage
                .getCurrentState(StateName.LATEST_TNC_DOCUMENT);
    }

    public JsonObject getLatestTNCUserRecord() {
        return latestTNCUserRecord;
    }
    
    /**
     * Method: handleAcceptance
     * Description: handle term of condition button action and call
     *  service UserTermAndConditionService.POST_TNC_ACCEPTANCE
     * @param action 
     */
    public void handleAcceptance(String action) {
        try {
            JsonObject TNCDocumentMeta = latestTNCUserRecord
                    .getAsJsonObject("documentMeta");
            String version = TNCDocumentMeta.get("version").getAsString();
            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("action", action);
            serviceData.put("version", version);
            
            UserTermAndConditionService userTnCService; 
            userTnCService = new UserTermAndConditionService(
                            UserTermAndConditionService
                                    .RequestType.POST_TNC_ACCEPTANCE, 
                            serviceData
            );
            userTnCService.setOnSucceeded((t) -> {
                Response result = userTnCService.getValue();
                if (result.isError()) {
                    JFXDialogLayout dialogLayout = new JFXDialogLayout();
                    JFXDialog dialog = new JFXDialog(
                            null, 
                            dialogLayout, 
                            JFXDialog.DialogTransition.NONE
                    );
                    
                    dialogLayout.setBody(new Text(result.getMessage()));
                    JFXButton closeBtn = new JFXButton("Close");
                    closeBtn.getStyleClass().add("dialog-modal-primary-btn");
                    closeBtn.setOnAction((var event) -> {
                        dialog.close();
                        try {
                            MainScreenController.activate(
                                    StateName.MAIN_LOGIN_SCREEN, true
                            );
                        } catch (Exception ex) {
                            LoggerService.LogRecord(
                                    TermsOfServiceViewModel.class, 
                                    "Unable to change view", 
                                    InternalLogger.LOGGER_LEVEL.ALL, ex
                            );
                        }
                    });
                    dialogLayout.setActions(closeBtn);
                    MainScreenController.showJFXDialog(dialog);
                    return;
                }
                
                JsonObject responseData = result.getData().getAsJsonObject();
                String latestAction = responseData.get("action").getAsString();
                
                try {
                    if (latestAction.equals("accepted")) {
                        UserSession userSession = StateStorage.getCurrentState(
                                StateName.USER_SESSION
                        );
                        if (userSession != null) {
                            boolean isClientActive = userSession.isIsClientActive();
                            String nextView = isClientActive 
                                    ? StateName.AUTHENTICATED_MAIN_WINDOW
                                    : StateName.CHANGE_PASSWORD_SCREEN;
                            MainScreenController.activate(nextView, false);
                        }
                    } else {
                        JFXDialogLayout dialogLayout = new JFXDialogLayout();
                        JFXDialog dialog = new JFXDialog(
                                null, dialogLayout, JFXDialog.DialogTransition.CENTER
                        );
                        String contentTxt = "You must agree to the Terms and Conditions to continue ";
                                
                        dialogLayout.setBody(new Text(contentTxt));
                        JFXButton closeBtn = new JFXButton("OK");
                        closeBtn.getStyleClass().add("dialog-modal-primary-btn");
                        closeBtn.setOnMouseClicked((var eevent) -> {
                            dialog.close();
                            Platform.runLater(() -> {
                                String nextView = StateName.MAIN_LOGIN_SCREEN;
                                try {
                                    MainScreenController.activate(nextView, false);
                                } catch (Exception ex) {
                                }
                            });
                        });
                        dialogLayout.setActions(closeBtn);
                        MainScreenController.showJFXDialog(dialog);
                    }
                } catch (Exception ex) {
                }
            });
            userTnCService.start();
        } catch (Exception ex) {
            LoggerService.LogRecord(this.getClass(), 
                    ex.getMessage(), 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }   
    }
}
