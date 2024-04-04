/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.concurrent.TimeUnit;



/**
 *
 * @author chamara
 */
public final class DotEnvUtility {
    private static final InternalLogger LOGGER = LoggerService.getLogger(DotEnvUtility.class);

    private static final Dotenv dotenvFile = Dotenv.load();
    
    public static String getAPI() {
        String buildEnvironment = dotenvFile.get("BUID_ENVIRONMENT");
        String envKeyName = buildEnvironment + "_API";
        return dotenvFile.get(envKeyName);
    }
    
    public static String getBuildEnvironment() {
        String buildEnv = dotenvFile.get("BUID_ENVIRONMENT", "DEVELOPMENT");
        return buildEnv;
    }
    
    public static String getApplicationVersion() {
        return dotenvFile.get("APP_VERSION");
    }
    
    public static int databaseWritingThreashold(String dataType) {
        
        final String suffix = "_WRITING_THERESHOLD";
        String dotEnvEntry = dataType + suffix;
        String stringValue = dotenvFile.get(dotEnvEntry);
        return Integer.parseInt(stringValue);
    }
    
    public static long activeWindowMaxFoucDuration() {
        
        final String dotEnvEntry = "ACTIVE_WINDOW_MAX_FOCUS_DURATION";
        String stringValue = dotenvFile.get(dotEnvEntry);
        int durationInMinutes = Integer.parseInt(stringValue);
        return TimeUnit.MINUTES.toSeconds(durationInMinutes);
    }
    
    public static String ApplicationDataDirectory() {
        final String dotEnvEntry = "APPLICATION_DATA_DIRECTORY";
        return dotenvFile.get(dotEnvEntry, "workshiftly");
    }
    
    public static String ApplicationName() {
        final String dotEnvEntry = "APPLICATION_NAME";
        return dotenvFile.get(dotEnvEntry, "workshiftly-desktop-client");
       // return dotenvFile.get(dotEnvEntry,"WorkShiftly | Helping the World Track Time | v");
    }
    
    public static boolean persistScreenshotsInLocalDisk() {
        final String dotEnvEntry = "PERSIST_SCREEN_SHOTS_LOCAL_DISK";
        String dotEnvValue = dotenvFile.get(dotEnvEntry, "false");
        return Boolean.parseBoolean(dotEnvValue);
    }
    
    public static String termsOfServiceWebURL() {
        final String dotEnvEntry = "TERMS_OF_CONDITION_HTML_URL";
        String dotEnvValue = dotenvFile.get(dotEnvEntry);
        return dotEnvValue;
    }
    
    public static boolean isAnimatedMainScreenChanges() {
        final String dotEnvEntry = "IS_ANIMATE_MAIN_SCREEN_CHANAGES";
        String dotEnvValue = dotenvFile.get(dotEnvEntry, "false");
        boolean parsedValue = Boolean.parseBoolean(dotEnvValue);
        System.out.println("parsedValue ----> " + parsedValue);
        return parsedValue;
    }
    
    public static String getSentryDSN() {
        final String dotEnvEntry = "SENTRY_DSN";
        String dotEnvValue = dotenvFile.get(dotEnvEntry);
        return dotEnvValue;
    }
    
    public static long RAW_DATA_PERIODIC_TASK_INTERVAL() {
        final String dotEnvEntry = "RAW_DATA_PERIODIC_TASK_INTERVAL";
        String entryValue = dotenvFile.get(dotEnvEntry, "60");
        return Long.parseLong(entryValue);
    }
    
    public static boolean SQLITE_ENCRYPTION_MODE() {
        final String dotEnvEntry = "SQLITE_ENCRYPTION_MODE";
        String entryValue = dotenvFile.get(dotEnvEntry, "true");
        boolean parseValue = Boolean.parseBoolean(entryValue);
        return parseValue;
    }
}
