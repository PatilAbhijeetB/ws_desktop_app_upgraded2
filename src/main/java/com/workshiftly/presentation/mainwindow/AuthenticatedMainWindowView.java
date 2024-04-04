/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.Company;
import com.workshiftly.presentation.mainwindow.component.ActivityLogWidget;
import com.workshiftly.presentation.mainwindow.component.ActivityLogWidgetViewModel;
import com.workshiftly.presentation.mainwindow.component.HeaderNavigation;
import com.workshiftly.presentation.mainwindow.component.UserProfileWidget;
import com.workshiftly.presentation.mainwindow.component.UserProfileWidgetModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author chamara
 */
public class AuthenticatedMainWindowView implements Initializable, FxmlView<AuthenticatedMainWindowViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(AuthenticatedMainWindowView.class);

    private static final String COMPANY_LOGO_IMAGE = "/images/logowork.png";
    @FXML
    private Pane rootWrapper;

    @FXML
    private Pane navbarHeaderWrapper;
    
    @FXML
    private Pane mainwindowLogoWrapper;
    
   
    
   
    
    @FXML
    private StackPane navigationContentWrapper;
    
    @FXML
    private Pane breadcumbWrapper;
    
    @FXML
    private ImageView companyLogo;
    
    @FXML
    private Pane logoutTabWrapper;
   
     @FXML
    private Pane projectWraper;
     
    @FXML
    private HBox navMenuItemWrapper;
    
    @FXML
    private Pane workedTimeWrapper;
    
   
    
     @FXML
    private Label fullNameLabel;
     
    @FXML
    private Label WorkedTime;
    
    @FXML
    private Label workedTimeCounterLbl;
    
    @FXML
    private Button timerButton;
    
    @FXML
    private ImageView timeBtnImage;
    
   
    
    @InjectViewModel
    private AuthenticatedMainWindowViewModel viewModel;
    
   
    

    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            StateStorage.set(StateName.AUTHENTICATED_MAIN_WINDOW_VIEWMODEL,
                    AuthenticatedMainWindowViewModel.class,
                    viewModel
            );
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Unable to set state", ex);
        }
       
        
        Company company = StateStorage.getCurrentState(StateName.USER_COMPANY_INSTANCE);
        if (company != null) {
            String timeZone = company.getTimezone();
          //  syncStatusLbl.setText(timeZone);
        }

        // company logo section
        InputStream companyLogoImageStream = getClass().getResourceAsStream(COMPANY_LOGO_IMAGE);
        Image companyLogoImage = new Image(companyLogoImageStream);
        companyLogo.setImage(companyLogoImage);
        //downloadFile();
        try {
            HeaderNavigation.initialize(navigationContentWrapper, navMenuItemWrapper);
        } catch (IOException ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Header navigation initialization error", ex);
        }
        
       Font font = Font.font("Aptos", FontWeight.LIGHT, 12.0);
       Font font2 = Font.font("Aptos", FontWeight.EXTRA_LIGHT, 22.0);
       WorkedTime.setFont(font);
       workedTimeCounterLbl.setFont(font2);
       
       workedTimeCounterLbl.setLineSpacing(28);
        //navigationIdentifierLbl.textProperty().bind(viewModel.mainNavigationIdentifierProperty());
        timeBtnImage.imageProperty().bind(viewModel.timeButtonImageProperty());
        workedTimeCounterLbl.textProperty().bind(viewModel.workedTimeTxtProperty());
        timerButton.disableProperty().bind(viewModel.timerButtonDisability());
      //  syncStatusLbl.textProperty().bind(viewModel.getSyncStatusTxtProperty());
         fullNameLabel.textProperty().bind(viewModel.getFullNameLabelTextProperty());
         
        timerButton.setOnAction((ActionEvent event) -> {
            viewModel.onClickMainTimerButton();
        });
        
       
    }
    
}
