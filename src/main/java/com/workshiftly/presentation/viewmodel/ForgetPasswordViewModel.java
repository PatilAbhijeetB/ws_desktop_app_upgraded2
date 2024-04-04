/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;

/**
 *
 * @author chamara
 */
public class ForgetPasswordViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ForgetPasswordViewModel.class);

    private final AppValidator appValidator = new AppValidator();
    
    private final SimpleStringProperty emailTxtInputError = new SimpleStringProperty();
    private final SimpleStringProperty emailTxtInput = new SimpleStringProperty();
    
    private final SimpleBooleanProperty isEmailTxtInputFocusOutOnce = new SimpleBooleanProperty();
    private final SimpleBooleanProperty isForgetPwBtnDisable = new SimpleBooleanProperty();
    
    public ForgetPasswordViewModel() {
        emailTxtInput.addListener(this::onChangeEmailTxtInputError);
    }
    
    public SimpleStringProperty emailTxtInputError() {
        return this.emailTxtInputError;
    }
    
    public SimpleStringProperty emailTxtInput() {
        return this.emailTxtInput;
    }
    
    public SimpleBooleanProperty isForgetPwBtnDisable() {
        return this.isForgetPwBtnDisable;
    }
    
    
    public void onChangeEmailTxtInput(
            ObservableValue<? extends String> observable, String oldValue, String newValue
    ) {
        if (!isEmailTxtInputFocusOutOnce.get()) {
            return;
        }
        validateEmailTxtInputValue();
    }
    
    public void onFocusOutEmailTxtInput(
            ObservableValue<? extends Boolean> observable, boolean oldValue, boolean newValue
    ) {
        if (!newValue) {
            isEmailTxtInputFocusOutOnce.set(true);
        }
    }
    
    public void onSubmitForgetPwBtn(ActionEvent actionEvent) {
        
        validateEmailTxtInputValue();
        isEmailTxtInputFocusOutOnce.setValue(true);
        isForgetPwBtnDisable().set(true);
        
        if (!emailTxtInputError.get().isEmpty()) {
            return;
        }
        
        Service<Response> forgetPasswordService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        JsonObject serviceData = new JsonObject();
                        serviceData.addProperty("email", emailTxtInput.get());
                        
                        AuthenticationModule authenticationModule = new AuthenticationModule();
                        Response response = authenticationModule.handleForgetPassword(serviceData);
                        return response;
                    }
                };
            }
        };
        
        forgetPasswordService.setOnRunning((new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                MainScreenController.showLoadingSplashScreen();
            }
        }));
        
        forgetPasswordService.setOnSucceeded((new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                
                MainScreenController.hideLoadingSplashScreen();
                Response result = forgetPasswordService.getValue();
                
                JFXDialogLayout dialogLayout = new JFXDialogLayout();
                Text dialogHeading = new Text();
                Text dialogBodyTxt = new Text();
                
                JFXButton dialogBtn = new JFXButton("OK");
                dialogBtn.getStyleClass().add("dialog-modal-primary-btn");
                dialogLayout.setActions(dialogBtn);
                
                
                dialogLayout.setHeading(dialogHeading);
                dialogLayout.setBody(dialogBodyTxt);
                
                JFXDialog dialogModal = new JFXDialog(null, dialogLayout, DialogTransition.CENTER, false);
                
                dialogBtn.setOnAction((ActionEvent actionEvent) -> {
                    dialogModal.close();
                });
                
                StatusCode statusCode = result.getStatusCode();
                switch (statusCode) {
                    case SUCCESS:
                        dialogHeading.setText("Password Reset Successful");
                        String successMsg = "Password reset successful! Please login with your new password"
                            + "\nWorkShiftly team will send you instructions by email";
                        dialogBodyTxt.setText(successMsg);
                        dialogBodyTxt.getStyleClass().add("dialog-modal-bodytext");
                       
                        dialogHeading.getStyleClass().add("dialog-modal-heading");
                        dialogBtn.setOnAction((var arg0) -> {
                            try {
                                dialogModal.close();
                                MainScreenController.activate(StateName.MAIN_LOGIN_SCREEN, true, true);
                               
                            } catch (IOException | MainScreenController.UnknownSceneException ex) {
                                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "IOException/UnknownSceneException occurred after the reset password request is success", ex);
                            } catch (Exception ex) {
                                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Exception occurred after the reset password request is success", ex);
                            }
                        });
                        dialogBtn.setText("Back to Login");
                        break;
                    case APPLICATION_ERROR:
                        dialogHeading.setText("Application error");
                        dialogBodyTxt.setText("Application error is occurred, try again");
                        break;
                    case NETWORK_ERROR:
                        dialogHeading.setText("Authentication Error");
                        dialogBodyTxt.setText(result.getMessage());
                        break;
                    default:
                        dialogHeading.setText("Error");
                        dialogBodyTxt.setText(result.getMessage());
                        break;
                }
                MainScreenController.showJFXDialog(dialogModal);
                isForgetPwBtnDisable.set(false);
            }
        }));
        
        forgetPasswordService.start();
        
    }
    
    public void onClickBackToLoginLink(ActionEvent actionEvent) {
        try {
            MainScreenController.activate(StateName.MAIN_LOGIN_SCREEN, true, true);
        } catch (IOException ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "On Click Back To Login Link IOException", ex);
        } catch (MainScreenController.UnknownSceneException ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "On Click Back To Login Link UnknownSceneException", ex);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "On Click Back To Login Link Exception", ex);
        }
    }
    
    private void validateEmailTxtInputValue() {
        
        String emailTxtInputValue = emailTxtInput.get();
        
        if (emailTxtInputValue == null || emailTxtInputValue.isEmpty()) {
            emailTxtInputError.set("Email is required to reset your password");
            return;
        }
        
        if (!appValidator.validateEmailPattern(emailTxtInputValue)) {
            emailTxtInputError.set("Invalid email address. please enter valid email address");
            return;
        }
        
        emailTxtInputError.set("");
    }

    private void onChangeEmailTxtInputError(
            ObservableValue<? extends String> obeservable, String oldValue, String newValue
    ) {
        boolean isEmptyErrorTxt = 
                emailTxtInputError.get() == null || emailTxtInputError.get().isEmpty();
        isForgetPwBtnDisable.set(!isEmptyErrorTxt);
    }
    
}
