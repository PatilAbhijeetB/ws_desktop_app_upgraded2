/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import static com.workshiftly.common.constant.StatusCode.NETWORK_ERROR;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.presentation.service.CheckAppUpdateService;
import com.workshiftly.presentation.service.UserTermAndConditionService;
import com.workshiftly.presentation.view.AppDownloadAlertView;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author chamara
 */
public class LoginViewModelNew implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LoginViewModelNew.class);

    private final AppValidator appValidator;
    
    private final SimpleBooleanProperty isEmailTxtInputDirty;
    private final SimpleBooleanProperty isPasswordTxtInputDirty;
    
    private final SimpleStringProperty emailTxtInput;
    private final SimpleStringProperty passwordTxtInput;
    private final SimpleStringProperty passwordShowTxtInput;
    private final SimpleStringProperty emailFieldErrorTxt;
    private final SimpleStringProperty passwordErrorTxt;
    
    private final BooleanBinding loginBtnDisability;
    
    public LoginViewModelNew() {
        
        appValidator = new AppValidator();
        
        isEmailTxtInputDirty = new SimpleBooleanProperty(false);
        isPasswordTxtInputDirty = new SimpleBooleanProperty(false);
        
        emailTxtInput = new SimpleStringProperty();
        passwordTxtInput = new SimpleStringProperty();
        passwordShowTxtInput=new SimpleStringProperty();
        emailFieldErrorTxt = new SimpleStringProperty();
        passwordErrorTxt = new SimpleStringProperty();
        
        loginBtnDisability = emailFieldErrorTxt.isNotEmpty()
                .or(passwordErrorTxt.isNotEmpty());
        
        checkApplicationUpdateInfo().start();
    }
    
    public SimpleStringProperty emailTxtInput() {
        return this.emailTxtInput;
    }
    
    public SimpleStringProperty passwordFieldInput() {
        return this.passwordTxtInput;
    }
     public SimpleStringProperty passwordShowFieldInput() {
        return this.passwordShowTxtInput;
    }
    
    public SimpleStringProperty emailErrorTxt() {
        return this.emailFieldErrorTxt;
    }
    
    public SimpleStringProperty passwordErrorTxt() {
        return this.passwordErrorTxt;
    }
    
    public BooleanBinding loginBtnDisability() {
        return this.loginBtnDisability;
    }
    
    public SimpleBooleanProperty isEmailTxtInputDirty() {
        return this.isEmailTxtInputDirty;
    }
    
    public SimpleBooleanProperty isPasswordlTxtInputDirty() {
        return this.isPasswordTxtInputDirty;
    }
    
    public void onChanageEmailTxtInput(String newValue) {
        if (isEmailTxtInputDirty.get()) {
            validateEmailTxtInput(false);
        }
    }
    
    public void onChangePasswordTxtInput() {
        if (isPasswordTxtInputDirty.get()) {
            validatePasswordTxtInput(false);
        }
    }
    
    public void validateEmailTxtInput(boolean isFromLoginBtn) {
        
        if (isFromLoginBtn) {
            isEmailTxtInputDirty.set(true);
        }
        
        boolean _isEmailTxtInputDirty = isEmailTxtInputDirty.get();
        if (!_isEmailTxtInputDirty) {
            return;
        }
        
        String errorMessage = "";
        boolean isEmptyValue = emailTxtInput.isEmpty().get();
        
        if (isEmptyValue) {
            errorMessage += "The email address field is required";
        } else {
            boolean isValidEmail = appValidator.validateEmailPattern(emailTxtInput.get());
            if (!isValidEmail) {
                errorMessage += "Email address should be valid";
            }
        }
        emailFieldErrorTxt.set(errorMessage);
    }
    
    public void validatePasswordTxtInput(boolean isFromLoginBtn) {
        
        if (isFromLoginBtn) {
            isPasswordTxtInputDirty.set(true);
        }
        
        boolean _isPasswordTxtInputDirty = isPasswordTxtInputDirty.get();
        if (!_isPasswordTxtInputDirty) {
            return;
        }
        
        String errorMessage = "";
        boolean isPasswordEmpty = passwordTxtInput.isEmpty().get();
        boolean isPasswordDsipEmpty = passwordShowTxtInput.isEmpty().get();
        
        if (isPasswordEmpty==true && isPasswordDsipEmpty==true ) {
            errorMessage += "The password field is required";
        }else{
        errorMessage="";
        }
        
        passwordErrorTxt.set(errorMessage);   
    }
    
    public void onClickForgetPasswordHyperLink() {
        try {
            passwordErrorTxt.setValue("");
            MainScreenController.activate(StateName.FORGET_PASSWORD_SCREEN, true);
        } catch (IOException ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    "onClickForgetPasswordHyperLink IOException", 
                    ex
            );
        } catch (MainScreenController.UnknownSceneException ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    "onClickForgetPasswordHyperLink UnknownSceneException", 
                    ex
            );
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    "onClickForgetPasswordHyperLink Exception", 
                    ex
            );
        }
    }
    
    public void onSubmitLoginBtn() {
        
        validateEmailTxtInput(true);
        validatePasswordTxtInput(true);
        
        if (loginBtnDisability.get()) {
            return;
        }
        
        Service<Response> loginService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        
                      //  AuthenticationModule authenticationModule = new AuthenticationModule();
                      //  JsonObject loginDetails = new JsonObject();
                     //   loginDetails.addProperty("username", emailTxtInput.get());
                     //   loginDetails.addProperty("password", passwordTxtInput.get());
                        
                     //   Response response = authenticationModule.handleUserLogin(loginDetails);
                     //   return response;
                        
                        
                        String macAddress=getMacAddress();
                        
                         String osName=System.getProperty("os.name");
                         String applicationVersion = DotEnvUtility.getApplicationVersion();
                         String strMachineName=getMachineName();
                         String strMachineUserName=System.getProperty("user.name");
                         String strIPAddress=getIPAddress();
                         
                        AuthenticationModule authenticationModule = new AuthenticationModule();
                        JsonObject loginDetails = new JsonObject();
                        loginDetails.addProperty("username", emailTxtInput.get());
                        loginDetails.addProperty("password", passwordTxtInput.get());
                        loginDetails.addProperty("macAddress", macAddress);
                        loginDetails.addProperty("ipAddress", strIPAddress);
                        loginDetails.addProperty("machineName", strMachineName);
                        loginDetails.addProperty("machineUserName", strMachineUserName);
                        loginDetails.addProperty("platform", "");
                        loginDetails.addProperty("version", applicationVersion);
                        loginDetails.addProperty("osName", osName);
                        loginDetails.addProperty("osVersionMajor", "");
                        loginDetails.addProperty("osVersionMinor", "");
                        loginDetails.addProperty("isForceLogoutDevices", "true");
                        
                        Response response = authenticationModule.handleUserLogin(loginDetails);
                        return response;
                        
                        
                        
                        
                        
                        
                    }
                };
            }
        };
        
        loginService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
            MainScreenController.showLoadingSplashScreen();
        });
        
        loginService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            MainScreenController.hideLoadingSplashScreen();
            Response result = loginService.getValue();
            
            if (result.isError()) {
                StatusCode errorStatus = result.getStatusCode();
                JFXDialogLayout dialogLayout = new JFXDialogLayout();

                switch (errorStatus) {
                    case NETWORK_ERROR: 
                        
                        dialogLayout.setHeading(new Text("Login Failed"));
                        break;
                    default: {
                        dialogLayout.setHeading(new Text("Error"));
                    }
                }
                
                dialogLayout.setBody(new Text(result.getMessage()));
                JFXButton closeButton = new JFXButton("Close");
                closeButton.getStyleClass().add("dialog-modal-loginfailed-btn");
                MainScreenController.showJFXDialogPane(dialogLayout, closeButton);
                return;
            }
            
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            if (!userSession.isIsClientActive()) {
                try {
                    StateStorage.set(
                        StateName.USER_INITIAL_PASSWORD,
                            String.class, passwordTxtInput.get()
                    );
                } catch (Exception ex) {
                    loginService.cancel();
                    PopupDialogHeading headingComponent = new PopupDialogHeading(
                            PopupDialogHeading.PopupType.ERROR, "Unexpected Error");
                    PopupDialogBox popupDialogBox = new PopupDialogBox(headingComponent);
                    popupDialogBox.setDescription(
                            PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                            "Unexpected error occured while loging, Please retry login to app"
                    );
                    JFXButton dialogButton = popupDialogBox.getDialogButton(
                            PopupDialogBox.DialogButton.RIGHT_MOST
                    );
                    dialogButton.setVisible(true); dialogButton.setDisable(false);
                    dialogButton.setOnAction((var actionEvent) -> {
                        popupDialogBox.close();
                    });
                    popupDialogBox.load();
                    return;
                }
            }

            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("userId", userSession.getId());

            UserTermAndConditionService userTnCService = new UserTermAndConditionService(
                    UserTermAndConditionService.RequestType.GET_LAST_TNC_STATUS, 
                    serviceData
            );
            userTnCService.setOnSucceeded((WorkerStateEvent event) -> {
                Response response = userTnCService.getValue();

                if (response.isError()) {
                    JFXDialogLayout dialogLayout = new JFXDialogLayout();
                    JFXDialog dialog = new JFXDialog(
                            null, dialogLayout, JFXDialog.DialogTransition.NONE
                    );
                    String contentTxt = "Error occurred while checking Term and "
                            + "Conditions, Try again";
                    dialogLayout.setBody(new Text(contentTxt));
                    JFXButton closeBtn = new JFXButton("Close");
                    closeBtn.getStyleClass().add("dialog-modal-primary-btn");
                    closeBtn.setOnAction((var actionEvent) -> {
                        dialog.close();
                    });
                    dialogLayout.setActions(closeBtn);
                    MainScreenController.showJFXDialog(dialog);
                    return;
                }

                try {
                    passwordTxtInput.set("");
                    JsonElement element = response.getData();
                    JsonObject jsonObj = element.isJsonObject() 
                            ? element.getAsJsonObject()
                            : new JsonObject();
                    StateStorage.set(
                            StateName.LATEST_TNC_DOCUMENT, JsonObject.class, jsonObj
                    );
                    String currentStatus = jsonObj.has("status")
                            ? jsonObj.get("status").getAsString() : null;

                    if (currentStatus != null && currentStatus.equals("accepted")) {  
                        passwordErrorTxt.setValue("");
                        String nextScreenName = userSession.isIsClientActive()
                                ? StateName.AUTHENTICATED_MAIN_WINDOW 
                                : StateName.CHANGE_PASSWORD_SCREEN;
                        MainScreenController.activate(nextScreenName, false);

                    } else {
                        MainScreenController.activate(
                                StateName.TERMS_OF_SERVICE_SCREEN, false
                        );
                    }
                } catch (Exception ex) {
                    LoggerService.LogRecord(
                            LoginViewModelNew.class,
                            ex.getMessage(), 
                            InternalLogger.LOGGER_LEVEL.ALL, 
                            ex
                    );
                }
            });
            userTnCService.start();
        });
        loginService.start();
    }
    
 private String getMachineName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }   
 private String getIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
    
    public  String getMacAddress() throws UnknownHostException,
            SocketException
    {
        InetAddress ipAddress = InetAddress.getLocalHost();
        NetworkInterface networkInterface = NetworkInterface
                .getByInetAddress(ipAddress);
        byte[] macAddressBytes = networkInterface.getHardwareAddress();
        StringBuilder macAddressBuilder = new StringBuilder();

        for (int macAddressByteIndex = 0; macAddressByteIndex < macAddressBytes.length; macAddressByteIndex++)
        {
            String macAddressHexByte = String.format("%02X",
                    macAddressBytes[macAddressByteIndex]);
            macAddressBuilder.append(macAddressHexByte);

            if (macAddressByteIndex != macAddressBytes.length - 1)
            {
                macAddressBuilder.append(":");
            }
        }

        return macAddressBuilder.toString();
    }
    
    
    private Service<Response> checkApplicationUpdateInfo() {
        CheckAppUpdateService appUpdateService = new CheckAppUpdateService();
        appUpdateService.setOnSucceeded((var event) -> {
            Response response = appUpdateService.getValue();
            
            if (response.isError()) {
                PopupDialogHeading dialogHeading 
                        = new PopupDialogHeading(PopupDialogHeading.PopupType.ERROR);
                dialogHeading.setHeadingTxt("Failed to check app updates");
                
                PopupDialogBox dialogBox = new PopupDialogBox();
                dialogBox.setHeadingComponent(dialogHeading);
                dialogBox.setDescription(
                        PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                        "Unexpected error occurred while version checking"
                );
                dialogBox.setDescription(
                        PopupDialogBox.MainContentDescriotion.SUB_DESCRIPTION, 
                        "Retry to get version information"
                );
                JFXButton closeBtn 
                        = dialogBox.getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
                closeBtn.setText("Retry");
                closeBtn.setVisible(true);
                closeBtn.setOnAction((var btnEvent) -> {
                    dialogBox.close();
                    appUpdateService.reset();
                    appUpdateService.start();
                });
                dialogBox.load();
                return;
            }
            
            JsonObject responseData = response.getData().getAsJsonObject();
            boolean isUpdateAvailable = responseData.get("availability").getAsBoolean();
            
            if (isUpdateAvailable) {
                
                ViewTuple<AppDownloadAlertView, AppDownloadAlertViewModel> appDownloadViewTuple;
                appDownloadViewTuple = FluentViewLoader.fxmlView(AppDownloadAlertView.class).load();
                
                AppDownloadAlertViewModel appDownloadAlertViewModel 
                        = appDownloadViewTuple.getViewModel();
                appDownloadAlertViewModel.setAppUpdateInfo(responseData);
                
                Parent appDownloadAlertView = appDownloadViewTuple.getView();
                JFXDialog downloadAlertDialog = new JFXDialog(
                        null, (Region) appDownloadAlertView, JFXDialog.DialogTransition.NONE
                );
                appDownloadAlertViewModel.setParentDialogComponent(downloadAlertDialog);
                MainScreenController.showJFXDialog(downloadAlertDialog);  
            }
        });
        return appUpdateService;
    }
    
             
  public void onSkipLogin(String userId,String authToken,String DeviceId) {
        
        Service<Response> loginService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        
                      //  AuthenticationModule authenticationModule = new AuthenticationModule();
                      //  JsonObject loginDetails = new JsonObject();
                     //   loginDetails.addProperty("username", emailTxtInput.get());
                     //   loginDetails.addProperty("password", passwordTxtInput.get());
                        
                     //   Response response = authenticationModule.handleUserLogin(loginDetails);
                     //   return response;
                        
                        
                       String AuthentcationToken="Bearer "+authToken;
                        String macAddress=getMacAddress();
                        
                         String osName=System.getProperty("os.name");
                         String applicationVersion = DotEnvUtility.getApplicationVersion();
                         String strMachineName=getMachineName();
                         String strMachineUserName=System.getProperty("user.name");
                         String strIPAddress=getIPAddress();
                         
                        AuthenticationModule authenticationModule = new AuthenticationModule();
                        JsonObject loginDetails = new JsonObject();
                        loginDetails.addProperty("authToken", AuthentcationToken);
                         loginDetails.addProperty("deviceId", DeviceId);
                        loginDetails.addProperty("macAddress", macAddress);
                        loginDetails.addProperty("ipAddress", strIPAddress);
                        loginDetails.addProperty("machineName", strMachineName);
                        loginDetails.addProperty("machineUserName", strMachineUserName);
                        loginDetails.addProperty("platform", "");
                        loginDetails.addProperty("version", applicationVersion);
                        loginDetails.addProperty("osName", osName);
                        loginDetails.addProperty("osVersionMajor", "");
                        loginDetails.addProperty("clientAppVersion", applicationVersion);
                        loginDetails.addProperty("osVersionMinor", "");
                        loginDetails.addProperty("userId", userId);
                        
                        Response response = authenticationModule.handleSkipUserLogin(loginDetails);
                        return response;
                        
                        
                        
                        
                        
                        
                    }
                };
            }
        };
        
        loginService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
            MainScreenController.showLoadingSplashScreen();
        });
        
        loginService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            MainScreenController.hideLoadingSplashScreen();
            Response result = loginService.getValue();
            
            if (result.isError()) {
                StatusCode errorStatus = result.getStatusCode();
                JFXDialogLayout dialogLayout = new JFXDialogLayout();

                switch (errorStatus) {
                    case NETWORK_ERROR: 
                        
                        dialogLayout.setHeading(new Text("Login Failed"));
                        break;
                    default: {
                        dialogLayout.setHeading(new Text("Error"));
                    }
                }
                
                dialogLayout.setBody(new Text(result.getMessage()));
                JFXButton closeButton = new JFXButton("Close");
                closeButton.getStyleClass().add("dialog-modal-loginfailed-btn");
                MainScreenController.showJFXDialogPane(dialogLayout, closeButton);
                return;
            }
            
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            if (!userSession.isIsClientActive()) {
                try {
                    StateStorage.set(
                        StateName.USER_INITIAL_PASSWORD,
                            String.class, passwordTxtInput.get()
                    );
                } catch (Exception ex) {
                    loginService.cancel();
                    PopupDialogHeading headingComponent = new PopupDialogHeading(
                            PopupDialogHeading.PopupType.ERROR, "Unexpected Error");
                    PopupDialogBox popupDialogBox = new PopupDialogBox(headingComponent);
                    popupDialogBox.setDescription(
                            PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                            "Unexpected error occured while loging, Please retry login to app"
                    );
                    JFXButton dialogButton = popupDialogBox.getDialogButton(
                            PopupDialogBox.DialogButton.RIGHT_MOST
                    );
                    dialogButton.setVisible(true); dialogButton.setDisable(false);
                    dialogButton.setOnAction((var actionEvent) -> {
                        popupDialogBox.close();
                    });
                    popupDialogBox.load();
                    return;
                }
            }

            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("userId", userSession.getId());

            UserTermAndConditionService userTnCService = new UserTermAndConditionService(
                    UserTermAndConditionService.RequestType.GET_LAST_TNC_STATUS, 
                    serviceData
            );
            userTnCService.setOnSucceeded((WorkerStateEvent event) -> {
                Response response = userTnCService.getValue();

                if (response.isError()) {
                    JFXDialogLayout dialogLayout = new JFXDialogLayout();
                    JFXDialog dialog = new JFXDialog(
                            null, dialogLayout, JFXDialog.DialogTransition.NONE
                    );
                    String contentTxt = "Error occurred while checking Term and "
                            + "Conditions, Try again";
                    dialogLayout.setBody(new Text(contentTxt));
                    JFXButton closeBtn = new JFXButton("Close");
                    closeBtn.getStyleClass().add("dialog-modal-primary-btn");
                    closeBtn.setOnAction((var actionEvent) -> {
                        dialog.close();
                    });
                    dialogLayout.setActions(closeBtn);
                    MainScreenController.showJFXDialog(dialog);
                    return;
                }

                try {
                    passwordTxtInput.set("");
                    JsonElement element = response.getData();
                    JsonObject jsonObj = element.isJsonObject() 
                            ? element.getAsJsonObject()
                            : new JsonObject();
                    StateStorage.set(
                            StateName.LATEST_TNC_DOCUMENT, JsonObject.class, jsonObj
                    );
                    String currentStatus = jsonObj.has("status")
                            ? jsonObj.get("status").getAsString() : null;

                    if (currentStatus != null && currentStatus.equals("accepted")) {  
                        passwordErrorTxt.setValue("");
                        String nextScreenName = userSession.isIsClientActive()
                                ? StateName.AUTHENTICATED_MAIN_WINDOW 
                                : StateName.CHANGE_PASSWORD_SCREEN;
                        MainScreenController.activate(nextScreenName, false);

                    } else {
                        MainScreenController.activate(
                                StateName.TERMS_OF_SERVICE_SCREEN, false
                        );
                    }
                } catch (Exception ex) {
                    LoggerService.LogRecord(
                            LoginViewModelNew.class,
                            ex.getMessage(), 
                            InternalLogger.LOGGER_LEVEL.ALL, 
                            ex
                    );
                }
            });
            userTnCService.start();
        });
        loginService.start();
    }
   
            
           

             
        
       
    
   
    
}
