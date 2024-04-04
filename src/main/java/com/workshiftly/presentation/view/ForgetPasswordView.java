/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.workshiftly.presentation.viewmodel.ForgetPasswordViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;

public class ForgetPasswordView implements Initializable, FxmlView<ForgetPasswordViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ForgetPasswordView.class);

    private final String FORGET_PASSWORD_SCREEN_IMAGE = "/images/logo 1.png";
    private final String COMPANY_LOGO_IMAGE = "/images/splash_screen_image.png";

    @FXML
    private Pane windowRootPane;

   // @FXML
   // private Pane rootWindowSvgPathWrapper;

  //  @FXML
   // private SVGPath footerSVGWave;

    @FXML
    private Pane authScrnLeftImgWrapper;

    @FXML
    private ImageView authScrnLeftImage;

    @FXML
    private Pane formContentWrapper;

    //@FXML
   // private ImageView authScrnAppLogo;

    @FXML
    private Pane outerFormWrapper;

    @FXML
    private Pane subFormWrapper;

    @FXML
    private Label emailErrorTxt;

    @FXML
    private TextField emailTextInput;

    @FXML
    private Label emailTextInputLbl;

    @FXML
    private Button forgetPasswordBtn;

    @FXML
    private Hyperlink backTologinLink;
    
    @InjectViewModel
    ForgetPasswordViewModel viewModel;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
        // screen left image initialization
        InputStream inputStream = getClass().getResourceAsStream(FORGET_PASSWORD_SCREEN_IMAGE);
        Image forgetPwImage = new Image(inputStream);
        authScrnLeftImage.setImage(forgetPwImage);
        
     
        // company logo image initialization
       // inputStream = getClass().getResourceAsStream(COMPANY_LOGO_IMAGE);
       // Image companyLogoImage = new Image(inputStream);
      //  authScrnAppLogo.setImage(companyLogoImage);
        
        
        // viewModel property bindings
        emailErrorTxt.textProperty().bind(viewModel.emailTxtInputError());
        emailTextInput.textProperty().bindBidirectional(viewModel.emailTxtInput());
        forgetPasswordBtn.disableProperty().bind(viewModel.isForgetPwBtnDisable());
        
        // actins
        forgetPasswordBtn.setOnAction(viewModel::onSubmitForgetPwBtn);
        backTologinLink.setOnAction(viewModel::onClickBackToLoginLink);
        
        // property listeners and event listeners
        emailTextInput.textProperty().addListener(viewModel::onChangeEmailTxtInput);
        emailTextInput.focusedProperty().addListener(viewModel::onFocusOutEmailTxtInput);
    }
}
