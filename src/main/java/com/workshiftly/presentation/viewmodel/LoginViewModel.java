/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.viewmodel;

import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;

public final class LoginViewModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LoginViewModel.class);

    private final AppValidator appValidator = new AppValidator();
    
    private SimpleStringProperty username = new SimpleStringProperty();
    private SimpleStringProperty password = new SimpleStringProperty();
    private SimpleStringProperty loginErrorMsg = new SimpleStringProperty();
    
    private SimpleBooleanProperty disableLoginBtn = new SimpleBooleanProperty();
    private SimpleBooleanProperty didEditedLoginFormOnce = new SimpleBooleanProperty(false);
    
    public LoginViewModel() {
        
        username.addListener((ObservableValue<? extends String> arg0, String oldValue, String newValue) -> {
            didEditedLoginFormOnce.set(true);
        });
    }
    
    // methods related to username field
    public SimpleStringProperty usernameProperty() {
        return this.username;
    }
    
    public String getUsername() {
        return this.username.get();
    }
    
    public void setUsername(String username) {
        this.username.set(username);
    }
    
    // methods related to password field
    public SimpleStringProperty passwordProperty() {
        return this.password;
    }
    
    public String getPassword() {
        return this.password.get();
    }
    
    public void setPassword(String password) {
        this.password.set(password);
    }
    
    public SimpleBooleanProperty disableLoginBtnProperty() {
        return this.disableLoginBtn;
    }
    
    // methods related to loginErrorText Property
    public SimpleStringProperty loginErrorTextProperty() {
        return this.loginErrorMsg;
    }
    
    public void setLoginErrorText(String error) {
        this.loginErrorMsg.set(error);
    }
    
    public String getLoginErrorText() {
        return this.loginErrorMsg.get();
    }
    
    /**
     * Event handling methods 
     * @param observableValue
     * @param oldValue
     * @param newValue 
     */
    public void onFocusOutUsernameField(
            ObservableValue observableValue, boolean oldValue, boolean newValue
    ) {
        if (!didEditedLoginFormOnce.get()) {
            return;
        }
        
        String usernameValue = usernameProperty().get();
        
        if (usernameValue == null || usernameValue.isEmpty()) {
            setLoginErrorText("Email is required for login. please enter your email address");
            return;
        }
        
        boolean isValidEmailAddress = appValidator.validateEmailPattern(usernameValue);
        if (!isValidEmailAddress) {
            setLoginErrorText("Email address is not valid. please enter valid email address");
            return;
        }
        
        setLoginErrorText("");
    }
    
    public void handleSubmitLoginBtn(ActionEvent actionEvent) {
        
        disableLoginBtn.set(true);
        boolean isValidLoginForm = validateLoginForm();
        
        if (!isValidLoginForm) {
            disableLoginBtn.set(false);
            return;
        }
        
        System.out.println("#### IUIUI BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        //            MainScreenController.activate(StateName.TERMS_OF_SERVICE_SCREEN, true);
        MainScreenController.showLoadingSplashScreen();

        
    }
    
    private boolean validateLoginForm() {
        
        String username = usernameProperty().get();
        String password = passwordProperty().get();
        
        if (isEmpty(username) || isEmpty(password)) {
            setLoginErrorText("Email and password are required for login."
                    + "please enter your email and password");
            return false;
        }
        
        if (username.isBlank() || password.isBlank()) {
            setLoginErrorText("Email and password should not be blank values."
                    + "please enter valid email and password");
            return false;
        }
        
        if (!appValidator.validateEmailPattern(username)) {
            setLoginErrorText("Email address is not valid. please enter valid email address");
            return false;
        }
        return true;
    }
    
    private boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }
}
