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
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.io.IOException;
import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;

/**
 *
 * @author chamara
 */
public class ChangePasswordViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ChangePasswordViewModel.class);

    private final AppValidator appValidator = new AppValidator();

    private final SimpleStringProperty oldPasswordErrorTxt = new SimpleStringProperty();
    private final SimpleStringProperty oldPasswordTxtInput = new SimpleStringProperty();
    private final SimpleStringProperty newPasswordTxtInput = new SimpleStringProperty();
    private final SimpleStringProperty newPasswordErrorTxt = new SimpleStringProperty();
    private final SimpleStringProperty retypePasswordTxtInput = new SimpleStringProperty();
    private final SimpleStringProperty retypePasswordErrorTxt = new SimpleStringProperty();
    
    private final SimpleBooleanProperty isNewPasswordTxtInputDirty = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty isRePasswordTxtInputDirty = new SimpleBooleanProperty(true);

    private final SimpleBooleanProperty disableOldPasswordTxtInput = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty disableChangePasswordBtn = new SimpleBooleanProperty(false);
    
    public ChangePasswordViewModel() {
        String oldPassword = StateStorage.getCurrentState(StateName.USER_INITIAL_PASSWORD);
        if (oldPassword != null) {
            oldPasswordTxtInput.set(oldPassword);
        }
    }

    public SimpleStringProperty oldPasswordErrorTxt() {
        return this.oldPasswordErrorTxt;
    }

    public SimpleStringProperty oldPasswordTxtInput() {
        return this.oldPasswordTxtInput;
    }

    public SimpleBooleanProperty disableOldPasswordTxtInput() {
        return this.disableOldPasswordTxtInput;
    }
    
    public SimpleStringProperty newPasswordTxtInput() {
        return this.newPasswordTxtInput;
    }
    
    public SimpleStringProperty newPasswordErrorTxt() {
        return this.newPasswordErrorTxt;
    }
    
    public SimpleStringProperty retypePasswordTxtInput() {
        return this.retypePasswordTxtInput;
    }
    
    public SimpleStringProperty retypePasswordErrorTxt() {
        return this.retypePasswordErrorTxt;
    }
    
    public SimpleBooleanProperty disableChangePasswordBtn() {
        return this.disableChangePasswordBtn;
    }

    public SimpleBooleanProperty isNewPasswordTxtInputDirty() {
        return isNewPasswordTxtInputDirty;
    }

    public SimpleBooleanProperty isRePasswordTxtInputDirty() {
        return isRePasswordTxtInputDirty;
    }
    
    public void onSubmitChangePasswordBtn(ActionEvent event) {
        
        flushPreviousFormErrors();
        disableChangePasswordBtn.set(true);
        
        boolean isValidForm = validateChangePasswordForm();
        if (!isValidForm) {
            disableChangePasswordBtn.set(false);
            return;
        }
        
        Service<Response> changePasswordService;
        changePasswordService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        JsonObject serviceData = new JsonObject();
                        serviceData.addProperty("currentPassword", oldPasswordTxtInput.get());
                        serviceData.addProperty("password", newPasswordTxtInput.get());
                        serviceData.addProperty("confirmPassword", retypePasswordTxtInput.get());
                        
                        AuthenticationModule authenticationModule = new AuthenticationModule();
                        Response response = authenticationModule.handleChangeInitailPassword(serviceData);
                        return response;
                    }
                };
            }
        };
        
        changePasswordService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
            MainScreenController.showLoadingSplashScreen();
        });
        
        changePasswordService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            
            MainScreenController.hideLoadingSplashScreen();
            Response result = changePasswordService.getValue();
            
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            
            Text dialogHeading = new Text();
            dialogLayout.setHeading(dialogHeading);
            
            Text dialogContentTxt = new Text();
            dialogLayout.setBody(dialogContentTxt);
            
            JFXButton dialogBtn = new JFXButton("OK");
            dialogBtn.getStyleClass().add("dialog-modal-primary-btn");
            dialogLayout.setActions(dialogBtn);
            
            JFXDialog dialogModal = new JFXDialog();
            dialogModal.setTransitionType(JFXDialog.DialogTransition.CENTER);
            dialogModal.setContent(dialogLayout);
            
            dialogBtn.setOnAction((var actionEvent) -> {
                dialogModal.close();
            });
            
            StatusCode statusCode = result.getStatusCode();
            
            switch (statusCode) {
                case SUCCESS: {
                    dialogHeading.setText("Success");
                    dialogHeading.getStyleClass().add("dialog-modal-heading");
                    String successBodyTxt = "Successfully reset your password. "
                            + "Please login with your new password";
                    dialogContentTxt.setText(successBodyTxt);
                     dialogContentTxt.getStyleClass().add("dialog-modal-bodytext");
                    dialogBtn.setText("Back to Login");
                    dialogBtn.setOnAction((var arg0) -> {
                        try {
                            MainScreenController.activate(StateName.MAIN_LOGIN_SCREEN, true, true);
                            
                        } catch (MainScreenController.UnknownSceneException | IOException ex) {
                            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Change Password Service UnknownSceneException/IOException", ex);
                        } catch (Exception ex) {
                            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Change Password Service Exception", ex);
                        }
                    });
                    break;
                }
                
                case APPLICATION_ERROR: {
                    dialogHeading.setText("Application Error");
                    dialogContentTxt.setText("Application error is occurred, please try agian");
                    break;
                }
                
                case NETWORK_ERROR: {
                    dialogHeading.setText("Authentication Error");
                    dialogContentTxt.setText(result.getMessage());
                    break;
                }
                
                case VALIDATION_ERROR: {
                    dialogHeading.setText("Validation Error");
                    dialogContentTxt.setText(result.getMessage());
                    break;
                }
                
                default: {
                    dialogHeading.setText("Error");
                    dialogContentTxt.setText(result.getMessage());
                }
            }
            
            MainScreenController.showJFXDialog(dialogModal);
            disableChangePasswordBtn.set(!result.isError());
        });
        
        changePasswordService.start();
    }
    
    private void flushPreviousFormErrors() {
        
        StringProperty[] errorTxtProperties = {
            newPasswordErrorTxt, retypePasswordErrorTxt, oldPasswordErrorTxt
        };
        
        Arrays.stream(errorTxtProperties).forEach((element) -> {
            element.set("");
        });
    }
    
    private boolean validateChangePasswordForm() {
        
        String newPassword = newPasswordTxtInput.getValue();
        if (newPassword.isEmpty() || newPassword.isBlank()) {
            newPasswordErrorTxt.set("New Password should be a non empty value");
            return false;
        }
        
        Response passwordValidation = appValidator.validatePassword(newPassword);
        if (passwordValidation.isError()) {
            newPasswordErrorTxt.set(passwordValidation.getMessage());
            return false;
        }

        String retypePassword = retypePasswordTxtInput.getValue();
        if (retypePassword.isEmpty() || retypePassword.isBlank()) {
            retypePasswordErrorTxt.set("Re-type password should be a non empty value");
            return false;
        }
        
        boolean isEqualPasswordAndRetypeOne = newPassword.equals(retypePassword);
        if (!isEqualPasswordAndRetypeOne) {
            retypePasswordErrorTxt.set("New Password and re-type password should be same");
            return false;
        }
        return true;
    }

    public void onChanageNewPasswordTxtInput(String newValue) {
        if (isNewPasswordTxtInputDirty.get()) {
            validateNewPasswordTxtInput(false);
        }
    }

    public void onChanageRePasswordTxtInput(String newValue) {
        if (isRePasswordTxtInputDirty.get()) {
            validateRePasswordTxtInput(false);
        }
    }

    public void validateNewPasswordTxtInput(boolean isFromSubmitBtn) {

        if (isFromSubmitBtn) {
            isNewPasswordTxtInputDirty.set(true);
        }

        boolean _isNewPasswordTxtInputDirty = isNewPasswordTxtInputDirty.get();
        if (!_isNewPasswordTxtInputDirty) {
            return;
        }

        String errorMessage = "";
        boolean isEmptyValue = newPasswordTxtInput.isEmpty().get();

        if (isEmptyValue) {
            errorMessage += "New password is required field";
        } else {
            Response passwordValidation = appValidator.validatePassword(newPasswordTxtInput.get());
            if (passwordValidation.isError()) {
                errorMessage += passwordValidation.getMessage();
            }
            else if(passwordValidation.getStatusCode()==StatusCode.SUCCESS){
             errorMessage = passwordValidation.getMessage();
            }
        }

       
        newPasswordErrorTxt.set(errorMessage);
    }

    public void validateRePasswordTxtInput(boolean isFromSubmitBtn) {

        if (isFromSubmitBtn) {
            isRePasswordTxtInputDirty.set(true);
        }

        boolean _isRePasswordTxtInputDirty = isRePasswordTxtInputDirty.get();
        if (!_isRePasswordTxtInputDirty) {
            return;
        }

        String errorMessage = "";
        boolean isEmptyValue = retypePasswordTxtInput.isEmpty().get();

        if (isEmptyValue) {
            errorMessage += "Retype New Password is required field";
        } else {
            boolean isValidRetypePassword = retypePasswordTxtInput.get().equals(newPasswordTxtInput.get());
            if (!isValidRetypePassword) {
                errorMessage += "New password mismatch";
            }
        }

        retypePasswordErrorTxt.set(errorMessage);
    }
}
