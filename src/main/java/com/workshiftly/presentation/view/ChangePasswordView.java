/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.workshiftly.presentation.viewmodel.ChangePasswordViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author chamara
 */
public class ChangePasswordView implements Initializable, FxmlView<ChangePasswordViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ChangePasswordView.class);

    private static final String CHANGE_PASSWORD_SCREEN_IMAGE = "/images/logowork.png";
    private final String COMPANY_LOGO_IMAGE = "/images/splash_screen_image.png";
    private final String VALID_PASSWORD_TOOLTIP_IMAGE = "/images/tooltip.png";
    
    @FXML
    private Pane windowRootPane;

   

  

    @FXML
    private Pane authScrnLeftImgWrapper;

    @FXML
    private ImageView authScrnLeftImage;

    @FXML
    private Pane formContentWrapper;

 

    @FXML
    private Pane outerFormWrapper;

    @FXML
    private Pane subFormWrapper;

    @FXML
    private Label oldPasswordErrorLbl;

    @FXML
    private PasswordField oldPasswordTxtInput;

    @FXML
    private Label oldPasswordLbl;

    @FXML
    private Button changePwBtn;

    @FXML
    private Pane newPasswordInputWrapper;

    @FXML
    private Label newPasswordErrorTxt;

    @FXML
    private PasswordField newPasswordTxtInput;

    @FXML
    private Label newPasswordErrorLbl;

    @FXML
    private Pane rePasswordInputWrapper;

    @FXML
    private Label rePasswordErrorTxt;

    @FXML
    private PasswordField rePasswordTxtInput;
    
    @FXML
    private ImageView validPasswordTooltipImg;

    @FXML
    private Label validPasswordTooltipLbl;

    @InjectViewModel
    private ChangePasswordViewModel viewModel;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        // company logo
       
        
        // change password left image
        InputStream screenLeftImageStream = getClass().getResourceAsStream(CHANGE_PASSWORD_SCREEN_IMAGE);
        Image screenLeftImage = new Image(screenLeftImageStream);
        authScrnLeftImage.setImage(screenLeftImage);
        
        // password validation tooltip image
        InputStream validPasswordTooltipImageStream = getClass().getResourceAsStream(VALID_PASSWORD_TOOLTIP_IMAGE);
        Image validPasswordTooltipImage = new Image(validPasswordTooltipImageStream);
        validPasswordTooltipImg.setImage(validPasswordTooltipImage);
        Tooltip passwordTooltip = new Tooltip(
                "Contains at least 8 characters\n"
                + "Contains at least one lowercase character\n"
                + "Contains at least one uppercase character\n"
                + "Contains at least one special character\n"
                + "Contains at least one digit");
        
        Tooltip.install(validPasswordTooltipImg, passwordTooltip);
        //validPasswordTooltipLbl.setTooltip(passwordTooltip);

        // password text inputs placeholders
        newPasswordTxtInput.setPromptText("New password");
        rePasswordTxtInput.setPromptText("Re-type new password");
        
        // viewModel bingings
        oldPasswordErrorLbl.textProperty().bind(viewModel.oldPasswordErrorTxt());
        oldPasswordTxtInput.textProperty().bindBidirectional(viewModel.oldPasswordTxtInput());
        oldPasswordTxtInput.disableProperty().bind(viewModel.disableOldPasswordTxtInput());
        
        viewModel.newPasswordTxtInput().bind(newPasswordTxtInput.textProperty());
        newPasswordTxtInput.textProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.onChanageNewPasswordTxtInput(newValue);
            StringProperty textProperty = newPasswordErrorTxt.textProperty();
            String strErrorMessage = textProperty.get();
           if(strErrorMessage=="Given password is a valid password"){
           newPasswordErrorTxt.setStyle("-fx-text-fill: #47B058;");
           }
           else{
           newPasswordErrorTxt.setStyle("-fx-text-fill: #f51f1f;");
           }
        });
        newPasswordTxtInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                viewModel.isNewPasswordTxtInputDirty().set(true);
                 StringProperty textProperty = newPasswordErrorTxt.textProperty();
            String strErrorMessage = textProperty.get();
           if(strErrorMessage=="Given password is a valid password"){
           newPasswordErrorTxt.setStyle("-fx-text-fill: #47B058;");
           }
            }
        });

        viewModel.retypePasswordTxtInput().bind(rePasswordTxtInput.textProperty());
        rePasswordTxtInput.textProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.onChanageRePasswordTxtInput(newValue);
            
        });
        rePasswordTxtInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                viewModel.isRePasswordTxtInputDirty().set(true);
            }
        });

        newPasswordErrorTxt.textProperty().bind(viewModel.newPasswordErrorTxt());
        rePasswordErrorTxt.textProperty().bind(viewModel.retypePasswordErrorTxt());
        
        changePwBtn.disableProperty().bind(viewModel.disableChangePasswordBtn());
        
        // components actions
        changePwBtn.setOnAction(viewModel::onSubmitChangePasswordBtn);
        
        
    }
    
}
