/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.workshiftly.application.MainApplication;
import com.workshiftly.presentation.viewmodel.LoginViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

/**
 *
 * @author chamara
 */
public class LoginView implements Initializable, FxmlView<LoginViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LoginView.class);

    @FXML
    private Pane rootPane;
    
    @FXML
    private Pane mainFormWrapper;

    @FXML
    private Label formTitle;

    @FXML
    private Label loginErrorText;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private Hyperlink forgotPasswordHyperlink;
    
    @InjectViewModel
    private LoginViewModel viewModel;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        try {
            // runtime configurations of the view
            String imageName = "/images/login_background.jpg";
            
            InputStream inputStream = getClass().getResourceAsStream(imageName);
            Image image = new Image(inputStream);
            BackgroundSize backgroundSize = new BackgroundSize(
                    MainApplication.WINDOW_WIDTH, MainApplication.WINDOW_HEIGHT, false, false, true, true
            );
            
            BackgroundImage backgroundImage = new BackgroundImage(
                    image, 
                    BackgroundRepeat.NO_REPEAT, 
                    BackgroundRepeat.NO_REPEAT, 
                    BackgroundPosition.CENTER,
                    backgroundSize
            );
            Background background = new Background(backgroundImage);
            rootPane.setBackground(background);
            
            // view - viewModel bindings
            usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
            passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
            loginBtn.disableProperty().bind(viewModel.disableLoginBtnProperty());
            loginErrorText.textProperty().bind(viewModel.loginErrorTextProperty());
            
            // event and actions
            loginBtn.setOnAction(viewModel::handleSubmitLoginBtn);
            
            // listeners
            usernameField.focusedProperty().addListener(viewModel::onFocusOutUsernameField);
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Login View Model Initialization Exception", ex);
        }
    }
}
