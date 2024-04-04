/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.application;

import com.google.gson.JsonObject;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.WebBrowserLog;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.domain.AuthenticationModule;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.presentation.mainwindow.AuthenticatedMainWindowViewModel;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApplication extends Application {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(MainApplication.class);
   
    public static final int WINDOW_WIDTH = 1063;
    public static final int WINDOW_HEIGHT = 672;
    
    
   
    
    @Override
    public void init() {
        
        try {
            TimeUtility.initailize();
            StateStorage.set(StateName.TIME_UTILITY_INIT_STATE, Boolean.class, true);
            
            int activeWindowDbWritingThreshold = DotEnvUtility.databaseWritingThreashold(StateName.ACTIVE_WINDOW);
            StateStorage.set(StateName.ACTIVE_WINDOW_WRITING_THRESHOLD, Integer.class, activeWindowDbWritingThreshold);
            StateStorage.set(StateName.ACTIVE_WINDOW_WRITING_SKIPS, Integer.class, 0);

            Long activeWindowMaxFoucsDuration = DotEnvUtility.activeWindowMaxFoucDuration();
            StateStorage.set(StateName.ACTIVE_WINDOW_MAX_FOCUS_DURATION, Long.class, activeWindowMaxFoucsDuration);

            List<ActiveWindow> activeWindowList = new ArrayList<>();
            StateStorage.set(StateName.ACTIVE_WINDOW_LIST, ArrayList.class, (ArrayList) activeWindowList);

            boolean screenshotPersistanceInLocalDisk = DotEnvUtility.persistScreenshotsInLocalDisk();
            StateStorage.set(StateName.PERSIST_SCREEN_SHOTS_LOCAL_DISK, Boolean.class, screenshotPersistanceInLocalDisk);

            StateStorage.set(StateName.SCREENSHOT_LIST, ArrayList.class, new ArrayList<>());
            
            List<WebBrowserLog> urlCaptureList = new ArrayList<>();
            StateStorage.set(StateName.URL_LIST, ArrayList.class, (ArrayList) urlCaptureList);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "MainApplication init", ex);
        }
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            MainScreenController.initialize(stage);
            stage.getIcons().add(new Image("/images/favicon.png"));
            stage.setOnCloseRequest((WindowEvent event) -> {
            String currentMainScreen = StateStorage.getCurrentState(StateName.CURRENT_MAIN_SCREEN);   
             
  
           
                
        

                boolean isAtAuthenticatedMainWindow = currentMainScreen.equals(StateName.AUTHENTICATED_MAIN_WINDOW);
                if (isAtAuthenticatedMainWindow) {
                   
                    event.consume();
                    stage.setIconified(true);
                    
                            
                    WindowEvent hideAppWindowEvent = new WindowEvent(stage, WindowEvent.WINDOW_HIDDEN);
                    stage.fireEvent(hideAppWindowEvent);
                    
                    
                    
                }
            });
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "MainApplicatin start", ex);
        }
    }
    
    public void launchApplication(String... args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        try {
            super.stop();
           
            String userHome = System.getProperty("user.home");

        // Define the application data directory name (adjust as needed)
        String appName = "WorkshiftlyApp";

        // Construct the full path to the application data directory
        String appDataDirectory = userHome + "/Documents/" + appName; // For Windows
        // String appDataDirectory = userHome + "/Library/Application Support/" + appName; // For macOS
        // String appDataDirectory = userHome + "/.config/" + appName; // For Linux

        // Delete the application data directory
        try {
            Path directoryPath = Paths.get(appDataDirectory);
            deleteFileForcefully(directoryPath);
             AuthenticationModule authenticationModule = new AuthenticationModule();
        Response response = authenticationModule.syncUserStatus("offline");
        } catch (IOException e) {
            e.printStackTrace();
        }
            
            System.exit(0);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "MainApplication stop", ex);
        }
    }
    
     private void deleteFileForcefully(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            // Delete all files and subdirectories inside the directory
            Files.walk(path)
                 .sorted((p1, p2) -> -p1.compareTo(p2)) // Delete deeper paths first
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         System.out.println("Error deleting file: " + e.getMessage());
                     }
                 });
        } else {
            // Delete a single file
            Files.delete(path);
        }
    }
         
}
