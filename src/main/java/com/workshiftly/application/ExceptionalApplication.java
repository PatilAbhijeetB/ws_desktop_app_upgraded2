/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.application;

import com.jfoenix.controls.JFXButton;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogBox;
import com.workshiftly.presentation.factory.popdialogheading.PopupDialogHeading;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author jade_m
 */
public class ExceptionalApplication extends Application {
    
    private static final int APP_WINDOW_WIDTH;
    private static final int APP_WINDOW_HEIGHT;
    private static final String GLOBAL_CSS_FILE;
    private static final StackPane ROOT_PANE;
    private static final Scene BASE_SCENE;
    
    static {
        APP_WINDOW_WIDTH = 500;
        APP_WINDOW_HEIGHT = 250;
        
        GLOBAL_CSS_FILE = "/css/workshiftly_theme.css";
        
        ROOT_PANE = new StackPane();
        ROOT_PANE.setPrefSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
        ROOT_PANE.setMaxSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
        ROOT_PANE.setMinSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
        
        BASE_SCENE = new Scene(ROOT_PANE, APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT);
        try {
            String globalCSS = ExceptionalApplication.class
                    .getResource(GLOBAL_CSS_FILE).toExternalForm();
            BASE_SCENE.getStylesheets().add(globalCSS);
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    ExceptionalApplication.class, 
                    "Unable to load CSS", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
    
    private Stage appStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            this.appStage = stage;
            this.appStage.setWidth(APP_WINDOW_WIDTH);
            this.appStage.setHeight(APP_WINDOW_HEIGHT);
            this.appStage.setResizable(false);
            this.appStage.initStyle(StageStyle.UNDECORATED);
            this.appStage.setScene(BASE_SCENE);
            this.appStage.getIcons().add(new Image("/images/favicon.png"));
            this.appStage.show();

            PopupDialogBox popupDialogBox = new PopupDialogBox();
            PopupDialogHeading headingComponent = new PopupDialogHeading(
                    PopupDialogHeading.PopupType.WARNING, "Warning"
            );
            popupDialogBox.setHeadingComponent(headingComponent);
            popupDialogBox.setDescription(
                    PopupDialogBox.MainContentDescriotion.MAIN_DESCRIPTION, 
                    "Another Application instance is already running"
            );
            JFXButton dialogButton = 
                    popupDialogBox.getDialogButton(PopupDialogBox.DialogButton.RIGHT_MOST);
            dialogButton.setText("Close");
            dialogButton.setOnAction((ActionEvent event) -> {
                stop();
            });
            dialogButton.setVisible(true);
            popupDialogBox.getjFXDialog().show(ROOT_PANE);
            
        } catch (NullPointerException ex) {
            LoggerService.LogRecord(
                    ExceptionalApplication.class, 
                    "Exception occurred", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
    
    public void boostrap(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        try {
            super.stop();
            System.exit(0);
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    this.getClass(), 
                    "Error occurred while closing application", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
    
}
