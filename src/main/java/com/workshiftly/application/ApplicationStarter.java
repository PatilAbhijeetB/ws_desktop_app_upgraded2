/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.application;

import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.presentation.factory.MainScreenController;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import java.io.InputStream;
import javafx.scene.image.Image;

/**
 *
 * @author chamara
 */
public class ApplicationStarter {
    
    private static final String APPLICATION_ID;
    private static final String BUILD_ENVIRONMENT;
    private static final String APP_LOCK_ID;
    
    static {
        APPLICATION_ID = "com.tcc.workshiftly.saas.desktop.client";
        BUILD_ENVIRONMENT = DotEnvUtility.getBuildEnvironment();
        APP_LOCK_ID = APPLICATION_ID + "." + BUILD_ENVIRONMENT;
    }
    private static final String COMPANY_LOGO_PATH1 = "/images/Frame12.ico";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            // check whether application is already running in another process
            JUnique.acquireLock(APP_LOCK_ID);
            
            MainApplication mainApp = new MainApplication();
            
            mainApp.launchApplication(args);

        } catch (AlreadyLockedException ex) {
            ExceptionalApplication exceptionalApp = new ExceptionalApplication();
            exceptionalApp.boostrap(args);
        } catch (Exception ex) {
            LoggerService.LogRecord(
                    ApplicationStarter.class, 
                    "Exception occurred while boostraping application", 
                    InternalLogger.LOGGER_LEVEL.ALL, 
                    ex
            );
        }
    }
}
