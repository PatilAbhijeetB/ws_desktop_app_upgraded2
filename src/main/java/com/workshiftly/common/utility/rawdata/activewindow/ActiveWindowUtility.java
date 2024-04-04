/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.activewindow;

import com.google.gson.Gson;
import com.sun.jna.Platform;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.rawdata.BaseRawDataHandler;
import com.workshiftly.domain.RawDataModule;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author chamara
 */

public final class ActiveWindowUtility  implements BaseRawDataHandler {
    
    private static final InternalLogger LOGGER;
    private static final Long ACTIVE_WINDOW_MAX_FOCUS_DURATION;
    private static final Integer ACTIVE_WINDOW_MAX_WRITING_INTERVAL;
    
    private static Integer ACTIVE_WINDOW_WRITING_SKIPS;
    
    static {
        LOGGER = LoggerService.getLogger(ActiveWindowUtility.class);
        
        ACTIVE_WINDOW_MAX_FOCUS_DURATION = DotEnvUtility.activeWindowMaxFoucDuration();
        ACTIVE_WINDOW_MAX_WRITING_INTERVAL = 30;
        ACTIVE_WINDOW_WRITING_SKIPS = 0;
    }

    private static ActiveWindowUtility singleton;
    
    public static class Comparator {
        
        private boolean isEqual = false;
        private String field;
        
        private Comparator() {}

        public boolean IsEqual() {
            return isEqual;
        }

        public String getField() {
            return field;
        }
        
        
        public static Comparator compare(ActiveWindow object1, ActiveWindow object2) {
            
            Comparator comparator = new Comparator();
            
            if (object1 == null || object2 == null) {
                return comparator;
            }
            
            if (!object1.getProcessId().equals(object2.getProcessId())) {
                comparator.field = ActiveWindow.FIELD_PROCESS_ID;
                return comparator;
            }
            
            if (!object1.getAppName().equals(object2.getAppName())) {
                comparator.field = ActiveWindow.FIELD_APP_NAME;
                return comparator;
            }
            
            if (!object1.getTitle().equals(object2.getTitle())) {
                comparator.field = ActiveWindow.FIELD_TITLE;
                return comparator;
            }
            
            comparator.isEqual = true;
            return comparator;
        }
    }
    
    
    private ActiveWindowUtility() {
        
    }
    
    public static class PlatformNotSupportedException extends Exception {
        private PlatformNotSupportedException(String message) {
            super(message);
        }
    }
    
    public static class ActiveWindowUtilityException extends Exception {
        ActiveWindowUtilityException(String message) {
            super(message);
        }
    }
    
    public static ActiveWindowUtility getInstance() {
        
        if (singleton == null) {
            singleton = new ActiveWindowUtility();
        }
        
        return singleton;
    }
    
    public synchronized ActiveWindow getCurrentActiveWindow() 
            throws PlatformNotSupportedException, ActiveWindowUtilityException {
        
        ActivityWindowBaseHandler baseHandler = null;
        
        if (Platform.isLinux()) {
            if (Platform.isX11()) {
                baseHandler = LinuxX11Handler.getInstance();
            } else {
                throw new PlatformNotSupportedException("Platform is not supported to retrieve active window");
            }        
        } else if (Platform.isWindows()) {
            baseHandler = WindowsHandler.getInstance();
        } else if (Platform.isMac()) {
            baseHandler = OsXActiveWindowHandler.getInstance();
        } 
        else {
            throw new PlatformNotSupportedException("Platform is not supported to retrieve active window");
        }
        
        ActiveWindow currentActivityWindow = baseHandler.getCurrentActivityWindow();
        return currentActivityWindow;
    }
    
    @Override
    public boolean handlePeridociDatabaseWriting() {
        try {
            List<ActiveWindow> activeWindows = RawDataModule.getCAPTURED_ACTIVE_WINDOWS();
            ActiveWindow currentActiveWindow = RawDataModule.getLAST_CAPTURED_ACTIVE_WINDOW();
            
            Long startedTimestamp = currentActiveWindow.getStartedTimestamp();
            Long currentTimestamp = TimeUtility.getCurrentTimestamp();
            
            Long currentWindowFocusDuration = Math.abs(currentTimestamp - startedTimestamp);
            Long maxFoucsDuration = ACTIVE_WINDOW_MAX_FOCUS_DURATION;
             UserSession userSession  = StateStorage.getCurrentState(StateName.USER_SESSION);
            if (currentWindowFocusDuration >= maxFoucsDuration) {
                currentActiveWindow.setEndTimestamp(currentTimestamp);
                currentActiveWindow.setFocusDuration(currentWindowFocusDuration);
                currentActiveWindow.setdeviceId(userSession.getDeviceId().toString());
                activeWindows.add(currentActiveWindow);
                RawDataModule.setLAST_CAPTURED_ACTIVE_WINDOW(null);
            }

            if (ACTIVE_WINDOW_WRITING_SKIPS < ACTIVE_WINDOW_MAX_WRITING_INTERVAL) {
                ACTIVE_WINDOW_WRITING_SKIPS++;
                return true;
            }
            
            if (activeWindows.isEmpty()) {
                return true;
            }
            
            try {
                Database<ActiveWindow> database 
                        = DatabaseProxy.openConnection(ActiveWindow.class);
                Response create = database.create(activeWindows);

                if (!create.isError()) {
                    activeWindows.clear();
                    ACTIVE_WINDOW_WRITING_SKIPS = 0;
                }

            } finally {
                DatabaseProxy.closeConnection(ActiveWindow.class);
            }
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to handle Peridoci Database Writing", ex);
            return false;
        }
        
        return true;
    }

    public static boolean validateActiveWindow (ActiveWindow activeWindow) {
        
        return activeWindow != null 
                && !AppValidator.isNullOrEmptyOrBlank(activeWindow.getAppName())
                && !AppValidator.isNullOrEmptyOrBlank(activeWindow.getTitle());
    }
    
    public void forceCommit(boolean isUserAction) throws Exception {
        List<ActiveWindow> collectedActiveWindows = RawDataModule.getCAPTURED_ACTIVE_WINDOWS();
        ActiveWindow latestActiveWindow = RawDataModule.getLAST_CAPTURED_ACTIVE_WINDOW();
        UserSession userSession  = StateStorage.getCurrentState(StateName.USER_SESSION);
        
        
        if (isUserAction) {
            RawDataModule.setLAST_CAPTURED_ACTIVE_WINDOW(null);
        } else if (latestActiveWindow != null) {
           // latestActiveWindow.setdeviceId(userSession.getDeviceId().toString());
            Long endTimestamp = latestActiveWindow.getEndTimestamp();
            
            if (endTimestamp == null || endTimestamp <= 0) {
                endTimestamp = TimeUtility.getCurrentTimestamp();
                
                long focusDuration = endTimestamp - latestActiveWindow.getStartedTimestamp();
             
                latestActiveWindow.setEndTimestamp(endTimestamp);
                latestActiveWindow.setFocusDuration(focusDuration);
            }
            
            boolean isValidRecord = latestActiveWindow.isValidRecord();
            
            if (isValidRecord) {
                collectedActiveWindows = collectedActiveWindows.stream()
                        .filter(((ActiveWindow curObj) -> {            
                            boolean duplicateRecord = ActiveWindow.isDuplicateRecord(
                                    curObj, latestActiveWindow
                            );
                            return !duplicateRecord;}))
                        .collect(Collectors.toList());
                
                collectedActiveWindows.add(latestActiveWindow);
            }
            
        }
        
        if (!collectedActiveWindows.isEmpty()) {
                try {
                    Database<ActiveWindow> db = DatabaseProxy.openConnection(ActiveWindow.class);
                    Response response = db.create(collectedActiveWindows);

                    if (!response.isError()) {
                        RawDataModule.setLAST_CAPTURED_ACTIVE_WINDOW(null);
                        RawDataModule.getCAPTURED_ACTIVE_WINDOWS().clear();
                        ACTIVE_WINDOW_WRITING_SKIPS = 0;
                    }
                } finally {
                    DatabaseProxy.closeConnection(ActiveWindow.class);
                }
            }
    }
}
