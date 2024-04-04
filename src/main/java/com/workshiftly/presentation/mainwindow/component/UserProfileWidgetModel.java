/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.component;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.CommonUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.joda.time.DateTimeZone;


public class UserProfileWidgetModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(UserProfileWidgetModel.class);

    final String defaultProfileImagePath = "/images/dashboard/default_profile_picture.png";

    private final SimpleStringProperty fullNameLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty companyTimeLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty userTimeLabelTextProperty = new SimpleStringProperty();
    private final SimpleObjectProperty<Image> profilePictureImageViewImageProperty = new SimpleObjectProperty<>();

    public UserProfileWidgetModel() {
        // full name
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        if (userSession != null) {
            String fullName = new StringBuilder()
                    .append(userSession.getFirstName())
                    .append(" ")
                    .append(userSession.getLastName())
                    .toString();
            fullNameLabelTextProperty.set(fullName);
        }

        // profile picture
        BufferedImage bufferedProfileImage = StateStorage.getCurrentState(StateName.USER_PROFILE_PICTURE);
        Image profileImage;
        if (bufferedProfileImage != null) {
            profileImage = CommonUtility.convertToFxImage(bufferedProfileImage);
        } else {
            InputStream defaultProfileImageStream = getClass().getResourceAsStream(defaultProfileImagePath);
            profileImage = new Image(defaultProfileImageStream);
        }
        profilePictureImageViewImageProperty.set(profileImage);

        // user and company clock
        KeyFrame clockKeyFrame;
        clockKeyFrame = new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            long timestatmp = TimeUtility.getCurrentTimestamp();

            DateTimeZone companyDateTimeZone = StateStorage.getCurrentState(StateName.COMPANY_TIMEZONE);
            String time = TimeUtility.getHumanReadbleTimeOnly(timestatmp, companyDateTimeZone);
            companyTimeLabelTextProperty.set(time);

            String pcTime = TimeUtility.getHumanReadbleTimeOnly(timestatmp, null);
            userTimeLabelTextProperty.set(pcTime);
        });

        Timeline clockTimeline = new Timeline(clockKeyFrame);
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    public SimpleStringProperty getFullNameLabelTextProperty() {
        return fullNameLabelTextProperty;
    }

    public SimpleStringProperty getCompanyTimeLabelTextProperty() {
        return companyTimeLabelTextProperty;
    }

    public SimpleStringProperty getUserTimeLabelTextProperty() {
        return userTimeLabelTextProperty;
    }

    public SimpleObjectProperty<Image> getProfilePictureImageViewImageProperty() {
        return profilePictureImageViewImageProperty;
    }
}
