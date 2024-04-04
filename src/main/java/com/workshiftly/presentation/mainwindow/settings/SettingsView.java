/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.settings;

import com.jfoenix.controls.JFXButton;
import com.workshiftly.common.model.UserShift;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 *
 * @author dmhashan
 */
public class SettingsView implements Initializable, FxmlView<SettingsViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(SettingsView.class);

    private static final String SCHEDULE_NAME_ICON_IMAGE = "/images/dashboard/user_profile_clock_icon.png";
    private static final String PASSWORD_TOOLTIP_ICON_IMAGE = "/images/tooltip.png";
    private static final String LOADING_SPINNER_IMAGE_PATH = "/images/loading_spinner.gif";
    private static final String EYE_LOGO_PATH = "/images/eye.png";
    private static final String EYEOPEN_LOGO_PATH = "/images/eyeOpen.png";
    
    @FXML
    private TabPane parentTabPane;

    @FXML
    private ImageView userProfileImageView;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private PasswordField currentPWTextInput;

    @FXML
    private Label currentPWErrorLabel;

    @FXML
    private Label newPasswordLabel;

    @FXML
    private PasswordField newPWTextInput;

    @FXML
    private Label newPWErrorLabel;

    @FXML
    private ImageView passwordTooltipImg;

    @FXML
    private PasswordField retypePWTextInput;

    @FXML
    private Label retypePWErrorLabel;

    @FXML
    private Button pwUpdateBtn;

    @FXML
    private Button clearPWChangeFormBtn;

    @FXML
    private ImageView scheduleNameIconImg;

    @FXML
    private Label scheduleNameLabel;

    @FXML
    private TableView<UserShift> scheduleDetailsTableView;

    @FXML
    private Pane loadingSpinner;

    @FXML
    private ImageView loadingSpinnerImg;

    @FXML
    private Pane noWorkSchedulesPane;

    @FXML
    private Tab appUpdateTab;

    @FXML
    private Label latestAppVersionLbl;

    @FXML
    private Label currentAppVersionLbl;

    @FXML
    private ImageView downloadImgView;

    // @FXML
    // private JFXButton appUpdateBtn;
    @FXML 
    private ImageView eyeLogo;
    
    @FXML
    private Button eyeButton;
   
    @FXML
    private TextField newpasswordshow;
    
     @FXML 
    private ImageView repeyeLogo;
    
    @FXML
    private Button repeyeButton;
   
    @FXML
    private TextField reppasswordshow;
    
    @FXML
    private ImageView appUpdateAlert;

    @InjectViewModel
    private SettingsViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeProfileTab();
        initializeScheduleTab();
        initializeAppUpdateTab();
    }

    private void initializeProfileTab() {
        try {
            try {
                InputStream inputStream = getClass().getResourceAsStream(LOADING_SPINNER_IMAGE_PATH);
                Image loadingSpinnerImage = new Image(inputStream);
                loadingSpinnerImg.setImage(loadingSpinnerImage);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Initialize Profile Tab Exception", ex);
            }

             InputStream eyeLogoStream = getClass().getResourceAsStream(EYE_LOGO_PATH);
        Image eLogo = new Image(eyeLogoStream);
        //eyeLogo = new ImageView(eLogo);
        
        InputStream eyeOpenLogoStream = getClass().getResourceAsStream(EYEOPEN_LOGO_PATH);
        Image eyeOpenLogo = new Image(eyeOpenLogoStream);
        
            newpasswordshow.setVisible(false);
            reppasswordshow.setVisible(false);
            
            InputStream passwordTooltipImageStream = getClass().getResourceAsStream(PASSWORD_TOOLTIP_ICON_IMAGE);
            Image passwordTooltipImage = new Image(passwordTooltipImageStream);
           // passwordTooltipImg.setImage(passwordTooltipImage);
           
            Tooltip passwordTooltip = new Tooltip(
                    "Password should contain\n"
                            + "  - Upper- and lower-case letters\n"
                            + "  - Numbers and special characters (@, %, &, #)\n"
                            + "  - 8-32 characters \n"
                            + "  - At least 1 letter, 1 number or special character");
            
            Tooltip.install(passwordTooltipImg,passwordTooltip);
           // newPasswordLabel.setTooltip(passwordTooltip);
            Paint strokePaint = Color.RED;
            userProfileImageView.imageProperty().bind(viewModel.getUserProfileImageViewImageProperty());
            //userProfileImageView.setClip(new Circle(21, 21, 21));
            Circle circle = new Circle(25, 25, 25);
            // circle.setStrokeWidth(1); // Assuming the framework uses a Color class for color representation
            // circle.setStroke(Color.RED);
            userProfileImageView.setClip(circle);

            userNameLabel.textProperty().bind(viewModel.getUserNameLabelTextProperty());
            userEmailLabel.textProperty().bind(viewModel.getUserEmailLabelTextProperty());

            viewModel.getCurrentPWInputTextProperty().bindBidirectional(currentPWTextInput.textProperty());
            viewModel.getNewPWInputTextProperty().bindBidirectional(newPWTextInput.textProperty());
            viewModel.getRetypePWInputTextProperty().bindBidirectional(retypePWTextInput.textProperty());
            
            newpasswordshow.textProperty().bindBidirectional(viewModel.getNewPWInputTextProperty());
            reppasswordshow.textProperty().bindBidirectional(viewModel.getRetypePWInputTextProperty());
            
            currentPWErrorLabel.textProperty().bind(viewModel.getCurrentPWErrorLabelTextProperty());
            newPWErrorLabel.textProperty().bind(viewModel.getNewPWErrorLabelTextProperty());
            retypePWErrorLabel.textProperty().bind(viewModel.getRetypePWErrorLabelTextProperty());
           
            loadingSpinner.visibleProperty().bind(viewModel.getLoadingProperty());

             newpasswordshow.textProperty().addListener((observable, oldValue, newValue) -> {
                if( viewModel.onChanageNewPasswordTxtInput(newValue)){
                newPWErrorLabel.setStyle(" -fx-text-fill: #3f3f3f;");
                }
              });
              newPWTextInput.textProperty().addListener((observable, oldValue, newValue) -> {
           if( viewModel.onChanageNewPasswordTxtInput(newValue)){
                newPWErrorLabel.setStyle(" -fx-text-fill: #47B058;");
                }
              });
       
            
            pwUpdateBtn.setOnAction((ActionEvent event) -> {
                viewModel.onClickPasswordUpdateButton();
            });

            clearPWChangeFormBtn.setOnAction((ActionEvent event) -> {
                viewModel.onClickClearPWChangeFormBtn();
            });
            
             eyeButton.setOnAction(e -> {
            if (newpasswordshow.isVisible()) {
                newPWTextInput.setText(newpasswordshow.getText());
                newPWTextInput.setVisible(true);
                newpasswordshow.setVisible(false);
                eyeLogo.setImage(eyeOpenLogo);
                newPWErrorLabel.textProperty().bind(viewModel.getNewPWErrorLabelTextProperty());
                
            } else {
                newpasswordshow.setText(newPWTextInput.getText());
                newpasswordshow.setVisible(true);
                newPWTextInput.setVisible(false);
                eyeLogo.setImage(eLogo);
                newPWErrorLabel.textProperty().bind(viewModel.getNewPWErrorLabelTextProperty());
            }
        });
             repeyeButton.setOnAction(e -> {
            if (reppasswordshow.isVisible()) {
                retypePWTextInput.setText(reppasswordshow.getText());
                retypePWTextInput.setVisible(true);
                reppasswordshow.setVisible(false);
                repeyeLogo.setImage(eyeOpenLogo);
                retypePWErrorLabel.textProperty().bind(viewModel.getRetypePWErrorLabelTextProperty());
                
            } else {
                reppasswordshow.setText(retypePWTextInput.getText());
                reppasswordshow.setVisible(true);
                retypePWTextInput.setVisible(false);
                repeyeLogo.setImage(eLogo);
                retypePWErrorLabel.textProperty().bind(viewModel.getRetypePWErrorLabelTextProperty());
            }
            });
        } catch (Exception ex) {
            InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
            LOGGER.logRecord(logLevel, "Initialize Profile Tab Exception", ex);
        }
    }

    private void initializeScheduleTab() {
        try {
            InputStream scheduleNameIconImageStream = getClass().getResourceAsStream(SCHEDULE_NAME_ICON_IMAGE);
            Image scheduleNameIconImage = new Image(scheduleNameIconImageStream);
            scheduleNameIconImg.setImage(scheduleNameIconImage);

            scheduleNameLabel.textProperty().bind(viewModel.getScheduleNameLabelTextProperty());

            TableColumn<UserShift, String> shiftStartColumn = new TableColumn<>("Shift Start");
            shiftStartColumn.setCellValueFactory(new PropertyValueFactory<>("shiftStart"));
            scheduleDetailsTableView.getColumns().add(shiftStartColumn);

            TableColumn<UserShift, String> shiftEndColumn = new TableColumn<>("Shift End");
            shiftEndColumn.setCellValueFactory(new PropertyValueFactory<>("shiftEnd"));
            scheduleDetailsTableView.getColumns().add(shiftEndColumn);

            TableColumn<UserShift, String> workingHoursColumn = new TableColumn<>("Working Hours");
            workingHoursColumn.setCellValueFactory(new PropertyValueFactory<>("workingHours"));
            scheduleDetailsTableView.getColumns().add(workingHoursColumn);

            scheduleDetailsTableView.setItems(viewModel.getUserScheduleList());
            noWorkSchedulesPane.visibleProperty().bind(viewModel.getNoWorkSchedulesPaneVisibleProperty());
        } catch (Exception ex) {
            InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
            LOGGER.logRecord(logLevel, "Initialize Schedule Tab Exception", ex);
        }
    }

    private void initializeAppUpdateTab() {
        try {
            // set up UI elements for control
            viewModel.setParentTabPane(parentTabPane);
            viewModel.setAppUpdatePane(appUpdateTab);

            ObservableList<Tab> settingViewTabs = parentTabPane.getTabs();
            boolean isContainedAppUpdateTab = settingViewTabs.contains(appUpdateTab);

            if (isContainedAppUpdateTab) {
                settingViewTabs.remove(appUpdateTab);
            }

            String exclaimationIconPath = "/images/icons/settings/exclamation-mark-in-a-circle.png";
            InputStream exclaimationIconStream = this.getClass().getResourceAsStream(exclaimationIconPath);
            Image exclaimationIconImg = new Image(exclaimationIconStream);
            appUpdateAlert.setImage(exclaimationIconImg);
            appUpdateAlert.visibleProperty().bind(viewModel.getAppUpdateAlertVisibility());

            String currentAppVersion = DotEnvUtility.getApplicationVersion();
            currentAppVersionLbl.setText(currentAppVersion);
            latestAppVersionLbl.textProperty().bind(viewModel.getLatestAppVersion());
            String downloadIconPath = "/images/icons/settings/download.png";
            InputStream downloadImg = getClass().getResourceAsStream(downloadIconPath);
            downloadImgView.setImage(new Image(downloadImg));

            // appUpdateBtn.setOnAction((var event ) -> {
            // viewModel.onClickAppUpdateBtn();
            // });

          //  updateRemainingTimestamp.textProperty().bind(
            //        viewModel.getAppUpdateRemainingLblText());
        } catch (Exception ex) {
            InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
            LOGGER.logRecord(logLevel, "Initialize App Update Tab Exception", ex);
        }
    }
}
