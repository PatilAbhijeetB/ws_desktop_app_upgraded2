/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSchedule;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.UserShift;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.CommonUtility;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.domain.SettingsModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.presentation.mainwindow.component.HeaderNavigation;
import com.workshiftly.presentation.service.CheckAppUpdateService;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.css.Style;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author dmhashan
 */
public class SettingsViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(SettingsViewModel.class);

    Gson GSON = new Gson();
    private final AppValidator appValidator = new AppValidator();
    private final SettingsModule settingsModule = new SettingsModule();

    final String defaultProfileImagePath = "/images/dashboard/default_profile_picture.png";

    private final SimpleBooleanProperty loadingProperty = new SimpleBooleanProperty(false);

    private final SimpleStringProperty userNameLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty userEmailLabelTextProperty = new SimpleStringProperty();
    private final SimpleObjectProperty<Image> userProfileImageViewImageProperty = new SimpleObjectProperty<>();

    private final SimpleStringProperty currentPWInputTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty newPWInputTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty retypePWInputTextProperty = new SimpleStringProperty();
    
    
    
    private final SimpleStringProperty newPWShowTextProperty = new SimpleStringProperty();

    private final SimpleStringProperty currentPWErrorLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty newPWErrorLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty retypePWErrorLabelTextProperty = new SimpleStringProperty();

    private final SimpleBooleanProperty noWorkSchedulesPaneVisibleProperty = new SimpleBooleanProperty(false);
    private final SimpleStringProperty scheduleNameLabelTextProperty = new SimpleStringProperty();
    private final ObservableList<UserShift> userScheduleList = FXCollections.observableArrayList();
    
    private final SimpleBooleanProperty appUpdateAlertVisibility;
    private final SimpleStringProperty latestAppVersion;
    //private final SimpleStringProperty appUpdateRemainingLblTxt;
    
    private final Timeline remainingUpdateTimeline;
    private JsonObject appUpdateInfo;
    private Duration appUpdateRemainder;
    
    // injectable openjfx elements
    private TabPane parentTabPane;
    private Tab appUpdatePane;

    public SettingsViewModel() {
        initializeProfileTabProperty();
        initializeScheduleTabProperty();
        
        appUpdateAlertVisibility = new SimpleBooleanProperty(false);
        latestAppVersion = new SimpleStringProperty("version");
       // appUpdateRemainingLblTxt = new SimpleStringProperty("Not Afraid");
        
        Duration keyFrameDuration = Duration.minutes(1.0);
        KeyFrame appUpdateKeyFrame = new KeyFrame(keyFrameDuration, (var actionVEvent) ->{
            if (appUpdateInfo == null || appUpdateRemainder == null) {
                return;
            }
            
            appUpdateRemainder = appUpdateRemainder.subtract(keyFrameDuration);
            buildAppUpdateRemainingTxt(appUpdateRemainder);
        });
        remainingUpdateTimeline = new Timeline(appUpdateKeyFrame);
        remainingUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        checkAppUpdateAvailability().start();
    }

    public String getDefaultProfileImagePath() {
        return defaultProfileImagePath;
    }

    public SimpleStringProperty getUserNameLabelTextProperty() {
        return userNameLabelTextProperty;
    }

    public SimpleStringProperty getUserEmailLabelTextProperty() {
        return userEmailLabelTextProperty;
    }

    public SimpleObjectProperty<Image> getUserProfileImageViewImageProperty() {
        return userProfileImageViewImageProperty;
    }

    public SimpleStringProperty getCurrentPWInputTextProperty() {
        return currentPWInputTextProperty;
    }

    public SimpleStringProperty getNewPWInputTextProperty() {
        return newPWInputTextProperty;
    }
    
    public SimpleStringProperty getNewPWShowTextProperty() {
        return newPWShowTextProperty;
    }

    public SimpleStringProperty getRetypePWInputTextProperty() {
        return retypePWInputTextProperty;
    }

    public SimpleStringProperty getCurrentPWErrorLabelTextProperty() {
        return currentPWErrorLabelTextProperty;
    }

    public SimpleStringProperty getNewPWErrorLabelTextProperty() {
        return newPWErrorLabelTextProperty;
    }

    public SimpleStringProperty getRetypePWErrorLabelTextProperty() {
        return retypePWErrorLabelTextProperty;
    }

    public SimpleStringProperty getScheduleNameLabelTextProperty() {
        return scheduleNameLabelTextProperty;
    }

    public ObservableList<UserShift> getUserScheduleList() {
        return userScheduleList;
    }

    public SimpleBooleanProperty getLoadingProperty() {
        return loadingProperty;
    }

    public SimpleBooleanProperty getNoWorkSchedulesPaneVisibleProperty() {
        return noWorkSchedulesPaneVisibleProperty;
    }
    
    SimpleBooleanProperty getAppUpdateAlertVisibility() {
        return appUpdateAlertVisibility;
    }

    void setParentTabPane(TabPane parentTabPane) {
        this.parentTabPane = parentTabPane;
    }

    void setAppUpdatePane(Tab appUpdatePane) {
        this.appUpdatePane = appUpdatePane;
    }

    SimpleStringProperty getLatestAppVersion() {
        return latestAppVersion;
    }
    
    //SimpleStringProperty getAppUpdateRemainingLblText() {
     //   return appUpdateRemainingLblTxt;
   // }

    public void onClickPasswordUpdateButton() {
        boolean isValidForm = validateChangePasswordForm();
        if (!isValidForm) {
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
                        serviceData.addProperty("currentPassword", currentPWInputTextProperty.get());
                        serviceData.addProperty("password", newPWInputTextProperty.get());
                        serviceData.addProperty("confirmPassword", retypePWInputTextProperty.get());

                        AuthenticationModule authenticationModule = new AuthenticationModule();
                        Response response = authenticationModule.handleChangeInitailPassword(serviceData);
                        return response;
                    }
                };
            }
        };

        changePasswordService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
            loadingProperty.set(true);
        });

        changePasswordService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            loadingProperty.set(false);

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
                    String successBodyTxt = "Your password has been changed successfully.";
                    dialogContentTxt.setText(successBodyTxt);
                    dialogModal.setOnDialogClosed((var arg0) -> {
                        onClickClearPWChangeFormBtn();
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
        });

        changePasswordService.start();
    }
    
    public void onClickClearPWChangeFormBtn() {
        currentPWInputTextProperty.set("");
        newPWInputTextProperty.set("");
        retypePWInputTextProperty.set("");
    }
    
    private void initializeProfileTabProperty() {
        // set user name and email
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);

        try {
            AppValidator.validateUserSession(userSession);
        } catch (AuthenticationException e) {
            JFXDialogLayout dialogLayout = new JFXDialogLayout();

            Text dialogHeading = new Text("Invalid user session");
            dialogLayout.setHeading(dialogHeading);

            Text dialogContentTxt = new Text("User session is invalid. Please try again after re-login.");
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

            MainScreenController.showJFXDialog(dialogModal);

            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "SettingsViewModel initializeProfileTabProperty - AuthenticationException", e);

            return;
        }

        String fullName = userSession.getFirstName() + " " + userSession.getLastName();
        userNameLabelTextProperty.set(fullName);

        String email = userSession.getEmail();
        userEmailLabelTextProperty.set(email);

        // profile picture
        BufferedImage bufferedProfileImage = StateStorage.getCurrentState(StateName.USER_PROFILE_PICTURE);
        Image profileImage;
        if (bufferedProfileImage != null) {
            profileImage = CommonUtility.convertToFxImage(bufferedProfileImage);
        } else {
            InputStream defaultProfileImageStream = getClass().getResourceAsStream(defaultProfileImagePath);
            profileImage = new Image(defaultProfileImageStream);
        }
        userProfileImageViewImageProperty.set(profileImage);
    }
    
    private void initializeScheduleTabProperty() {
        Service<Response> getUserScheduleService;
        getUserScheduleService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        Response response = settingsModule.getUserSchedule();
                        return response;
                    }
                };
            }
        };

        getUserScheduleService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
            loadingProperty.set(true);
        });

        getUserScheduleService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            loadingProperty.set(false);
            noWorkSchedulesPaneVisibleProperty.set(false);

            Response result = getUserScheduleService.getValue();

            if (result.isError()) {
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
                        String successBodyTxt = "Your password has been changed successfully.";
                        dialogContentTxt.setText(successBodyTxt);
                        dialogModal.setOnDialogClosed((var arg0) -> {
                            onClickClearPWChangeFormBtn();
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

                    case NOT_FOUND: {
                        noWorkSchedulesPaneVisibleProperty.set(true);
                        return;
                    }

                    default: {
                        dialogHeading.setText("Error");
                        dialogContentTxt.setText(result.getMessage());
                    }
                }

                MainScreenController.showJFXDialog(dialogModal);
                return;
            }

            JsonElement resultObj = result.getData();
            Type userWorkScheduleType = new TypeToken<UserSchedule>() {}.getType();
            UserSchedule userWorkSchedule = GSON.fromJson(resultObj, userWorkScheduleType);

            String userScheduleName = userWorkSchedule.getName();
            scheduleNameLabelTextProperty.set(userScheduleName);

            ArrayList<UserShift> shifts = userWorkSchedule.getShifts();
            userScheduleList.addAll(shifts);
        });

        getUserScheduleService.start();
    }
    
    private boolean validateChangePasswordForm() {
        flushPreviousFormErrors();
        boolean returnValue = true;

        String currentPassword = currentPWInputTextProperty.getValue();
        if (currentPassword.isEmpty() || currentPassword.isBlank()) {
            currentPWErrorLabelTextProperty.set("Please enter your current password.");
            returnValue = false;
        }

        String newPassword = newPWInputTextProperty.getValue();
        String newPasswordShow = newPWShowTextProperty.getValue();
        if (newPassword.isEmpty() || newPassword.isBlank()) {
            newPWErrorLabelTextProperty.set("Please enter your new password.");
            returnValue = false;
        }

        Response passwordValidation = appValidator.validatePassword(newPassword);
        if (passwordValidation.isError()) {
            newPWErrorLabelTextProperty.set(passwordValidation.getMessage());
            returnValue = false;
        }

        String retypePassword = retypePWInputTextProperty.getValue();
        if (retypePassword.isEmpty() || retypePassword.isBlank()) {
            retypePWErrorLabelTextProperty.set("Please re-enter your new password.");
            returnValue = false;
        }

        boolean isEqualPasswordAndRetypeOne = newPassword.equals(retypePassword);
        if (!isEqualPasswordAndRetypeOne) {
            retypePWErrorLabelTextProperty.set("New Password and re-type password should be same");
            returnValue = false;
        }

        return returnValue;
    }
    
    private void flushPreviousFormErrors() {
        StringProperty[] errorTxtProperties = {
            currentPWErrorLabelTextProperty, newPWErrorLabelTextProperty, retypePWErrorLabelTextProperty
        };

        Arrays.stream(errorTxtProperties).forEach((element) -> {
            element.set("");
        });
    }
    
    private Service<Response> checkAppUpdateAvailability() {
        Service<Response> domainSerivce = new CheckAppUpdateService();
        domainSerivce.setOnSucceeded((var event) -> {
            Response result = domainSerivce.getValue();
            
            if (result.isError()) {
                // error handling
                PopupDialogHeading popupDialogHeading 
                        = new PopupDialogHeading(PopupDialogHeading.PopupType.ERROR);
                popupDialogHeading.setHeadingTxt("Unable to retrieve app updates");
                
                PopupDialogBox popupDialogBox = new PopupDialogBox();
                popupDialogBox.setHeadingComponent(popupDialogHeading);
                popupDialogBox.setDescription(
                        PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                        "Failed to retrieve application version updates from Remote server"
                );
                JFXButton closeBtn 
                        = popupDialogBox.getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
                closeBtn.setText("Try Again");
                closeBtn.setVisible(true);
                closeBtn.setOnAction((var btnEvent) -> {
                    btnEvent.consume();
                    popupDialogBox.close();
                    // restart the domain service to attemp retreiving version info
                    domainSerivce.reset();
                    domainSerivce.start();
                });
                popupDialogBox.load();
                return;
            }
            
            appUpdateInfo = result.getData().getAsJsonObject();
            String latestVersion   = appUpdateInfo.get("latestVersion").getAsString();
            latestAppVersion.set(latestVersion);
                       
            boolean availability = appUpdateInfo.get("availability").getAsBoolean();
            appUpdateAlertVisibility.set(availability);
            HeaderNavigation.setAlertVisibility(StateName.SETTINGS_NAVIGATION_ITEM, availability);
            
            ObservableList<Tab> childTabs = parentTabPane.getTabs();
            boolean isContainedAppUpdateTabPane = childTabs.contains(appUpdatePane);
            if (availability && !isContainedAppUpdateTabPane) {
                childTabs.add(appUpdatePane);
            }
            
            if (availability) {
                appUpdateInfo = result.getData().getAsJsonObject();
                long maxTimestamp = appUpdateInfo.get("allowedLatestTimestamp").getAsLong();
                long currentTimestamp = appUpdateInfo.get("currentTimestamp").getAsLong();
                appUpdateRemainder = Duration.seconds(
                        Math.abs(maxTimestamp - currentTimestamp)
                );
                buildAppUpdateRemainingTxt(appUpdateRemainder);
                remainingUpdateTimeline.play();    
            } 
        });
        return domainSerivce;
    }
    
    void onClickAppUpdateBtn() {
        System.out.println("##### onclick handler for update button btn");
    }
    
    private void buildAppUpdateRemainingTxt(Duration remainDuration) {
        
        Duration tempDuration = Duration.hours(remainDuration.toHours());
        long remainTotalDays = (long) tempDuration.toHours() / 24;
        
        tempDuration = tempDuration.subtract(Duration.hours(remainTotalDays * 24));
        long remainTotalHours = (long) tempDuration.toHours();
        
        tempDuration = tempDuration.subtract(Duration.hours(remainTotalHours));
        long remainTotalMinutes = (long) tempDuration.toMinutes();
        
        ArrayList<String> concatLierals = new ArrayList<>();
        concatLierals.add("You have");
        
        if (remainTotalDays >= 0) {
            concatLierals.add(String.format(
                    "%s day%s", remainTotalDays, (remainTotalDays == 0 ? "" : "s")
            ));
        }
        
        if (remainTotalHours >= 0) {
            concatLierals.add(String.format(
                    "%s hour%s", remainTotalHours, (remainTotalHours == 0 ? "" : "s")
            ));
        }
        
        if (remainTotalMinutes >= 0) {
            concatLierals.add(String.format(
                    "%s minutes%s", remainTotalMinutes, (remainTotalMinutes == 0 ? "" : "s")
            ));
        }
        
       // concatLierals.add("to download the latest version and install.");
       // String concatTxt = String.join(" ", concatLierals);
       // appUpdateRemainingLblTxt.set(concatTxt);
    }
    
    public boolean onChanageNewPasswordTxtInput(String newValue) {
       
       return validateNewPass();
    }
    
    private boolean validateNewPass() {
        flushPreviousFormErrors();
        boolean returnValue = true;

        String newPassword = newPWInputTextProperty.getValue();
        String newPasswordShow = newPWShowTextProperty.getValue();
        if (newPassword.isEmpty() || newPassword.isBlank()) {
            newPWErrorLabelTextProperty.set("Please enter your new password.");
            returnValue = false;
        }

        Response passwordValidation = appValidator.validatePassword(newPassword);
        if (passwordValidation.isError()) {
            newPWErrorLabelTextProperty.set(passwordValidation.getMessage());
            returnValue = false;
        }else
        {
         newPWErrorLabelTextProperty.set(passwordValidation.getMessage());
            returnValue = true;
        }
       
        return returnValue;
    }
    
}
