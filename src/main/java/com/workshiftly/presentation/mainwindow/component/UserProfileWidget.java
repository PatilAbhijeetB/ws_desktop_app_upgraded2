/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;


public class UserProfileWidget implements Initializable, FxmlView<UserProfileWidgetModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(UserProfileWidget.class);

    private static final String CENTER_CLOCK_ICON_IMAGE = "/images/dashboard/user_profile_clock_icon.png";

    @FXML
    private ImageView centerClockIconImageView;

    @FXML
    private Label fullNameLabel;

    @FXML
    private ImageView profilePictureImageView;

    @FXML
    private Label companyTimeLabel;

    @FXML
    private Label userTimeLabel;

    @InjectViewModel
    private UserProfileWidgetModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        InputStream centerClockIconImageStream = getClass().getResourceAsStream(CENTER_CLOCK_ICON_IMAGE);
        Image centerClockIconImage = new Image(centerClockIconImageStream);
        centerClockIconImageView.setImage(centerClockIconImage);

        fullNameLabel.textProperty().bind(viewModel.getFullNameLabelTextProperty());
        companyTimeLabel.textProperty().bind(viewModel.getCompanyTimeLabelTextProperty());
        userTimeLabel.textProperty().bind(viewModel.getUserTimeLabelTextProperty());

        profilePictureImageView.imageProperty().bind(viewModel.getProfilePictureImageViewImageProperty());
        profilePictureImageView.setClip(new Circle(32, 32, 32));
    }
}
