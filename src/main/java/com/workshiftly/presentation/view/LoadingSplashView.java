/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.view;

import com.workshiftly.presentation.viewmodel.LoadingSplashViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author chamara
 */
public class LoadingSplashView implements Initializable, FxmlView<LoadingSplashViewModel> {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LoadingSplashView.class);

    private static final String IMAGE_NAME = "/images/logoblack.png";
    
    @FXML
    private StackPane loadingSplashViewWrapper;
    
    @FXML
    private ImageView imageView;
    
    @InjectViewModel
    LoadingSplashViewModel viewModel;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            InputStream inputStream = getClass().getResourceAsStream(IMAGE_NAME);
            Image workshiftlyLogo = new Image(inputStream);
            imageView.setImage(workshiftlyLogo);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.ALL, "Loading Splash View Initialization Exception", ex);
        }
        
        int fadeTransitionTimeout = viewModel.fadeTranstiitonTimeout().get();
        
        FadeTransition fadeInTransition = new FadeTransition(
                Duration.seconds(fadeTransitionTimeout), loadingSplashViewWrapper
        );
        fadeInTransition.setFromValue(viewModel.fadeTransitionFromValue().doubleValue());
        fadeInTransition.setToValue(viewModel.fadeTransitionToValue().doubleValue());
        fadeInTransition.setCycleCount(1);
        
        FadeTransition fadeOutTransition = new FadeTransition(
                Duration.seconds(fadeTransitionTimeout), loadingSplashViewWrapper
        );
        fadeOutTransition.setFromValue(viewModel.fadeTransitionToValue().doubleValue());
        fadeOutTransition.setToValue(viewModel.fadeTransitionFromValue().doubleValue());
        fadeOutTransition.setCycleCount(1);
        
        fadeInTransition.play();
        
        fadeInTransition.setOnFinished((ActionEvent event) -> {
            fadeOutTransition.play();
        });
        
        fadeOutTransition.setOnFinished((ActionEvent event) -> {
            fadeInTransition.play();
        });
    }
    
}
