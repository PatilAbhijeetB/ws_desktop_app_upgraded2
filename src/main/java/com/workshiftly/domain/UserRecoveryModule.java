/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.domain;

import com.google.gson.JsonObject;
import com.j256.ormlite.stmt.QueryBuilder;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.model.WorkStatusLog.WorkStatus;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chamara
 */
public class UserRecoveryModule {
    
    // recovery work status releated WorkStatusLog instance
    private final WorkStatusLog.WorkStatus[] RECOVERY_WORK_STATUS = {
        WorkStatus.BEGINNING, WorkStatus.START, WorkStatus.BREAK
    };
    // Initialize logger service
    private final InternalLogger LOGGER = LoggerService.getLogger(UserRecoveryModule.class);
    
    // instance memvbers
    private String userId;
    private HttpRequestCaller httpRequestCaller;
    private WorkDateTime workDateTime;
    private List<Long> latestRawdataTimestamps;
    
    // locally stored latest raw data
    private WorkStatusLog lastWorkStatusLog;
    private ActiveWindow lastActiveWindow;
    private Screenshot lastScreenshot;
    
    /*
     * Default constructor access level private due to UserRecoverModule should be
     * initialized and run related to a user
     */
    private UserRecoveryModule() {}
    
    /*
     * Constructor which access level is public and should be provided userId
     * as logged user
     */
    public UserRecoveryModule(String userId) throws Exception {

        this.userId = userId;
        this.httpRequestCaller = new HttpRequestCaller();
        this.workDateTime = WorkDateTime.getInstance();
        this.latestRawdataTimestamps = new ArrayList<>();
        
        this.lastWorkStatusLog = getLastWorkStatusLog();
        this.lastActiveWindow = getLastCapturedActiveWindow();
        this.lastScreenshot = getLastCapturedScreenshot();
        
        long lastScreenshotTimestamp = this.lastScreenshot != null 
                ? this.lastScreenshot.getTimestamp() : 0;
        latestRawdataTimestamps.add(lastScreenshotTimestamp);
        
        long lastActiveWindowTimestamp = this.lastActiveWindow != null 
                ? this.lastActiveWindow.getEndTimestamp() : 0;
        this.latestRawdataTimestamps.add(lastActiveWindowTimestamp);
    }
    
    /**
     * global main exposed function to call and obtain recovery status related to
     * user who is currently logged in.
     * @return Response object
     */
    public Response getRecoveryStatus() {
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("startTimestamp", Long.toString(workDateTime.getDateStartedTimestamp()));
        queryParams.put("endTimestamp", Long.toString(workDateTime.getDateEndedTimestamp()));
        
        Response apiResponse = httpRequestCaller.callUserRecoveryService(this.userId, queryParams);
        if (apiResponse.isError()) {
            return new Response(
                    false, 
                    StatusCode.NETWORK_ERROR, 
                    "Error occurred while calling recovery service"
            );
        }
        
        try {
            JsonObject result = new JsonObject();
            
            JsonObject apiResponseData = apiResponse.getData().getAsJsonObject();
            JsonObject localRecoveryResult = getLocalRecoveryStatus();
            
            boolean shouldRecoverApiStatus = apiResponseData.get("shouldRecover").getAsBoolean();
            boolean shouldRecoverLocalStatus = localRecoveryResult.get("shouldRecover").getAsBoolean();
            boolean isRecoveryNecessarey = shouldRecoverApiStatus || shouldRecoverLocalStatus;
            
            result.addProperty("shouldRecover", isRecoveryNecessarey);
            
            // if it is not necessary to recover, success response will be returned
            if (!isRecoveryNecessarey) {
                result.addProperty("recoveryStatus", "NO_NEED_TO_RECOVERY");
                return new Response(
                        false, 
                        StatusCode.SUCCESS, 
                        "Successfully retireved recovery details", 
                        result
                );
            }
            
            processApiResponseData(apiResponseData);
            long latestRawdataTimestamp = getLatestRawdataTimestamp();
            result.addProperty("latestRawdataTimestamp", latestRawdataTimestamp);
            
            long totalWorkedDuration_api = apiResponseData.has("totalWorkedDuration")
                    ? apiResponseData.get("totalWorkedDuration").getAsLong() : 0;
            long totalWorkedDuration_local = calculateDailyWorkingTime();
            long totalWorkedDuration = Math.max(totalWorkedDuration_api, totalWorkedDuration_local);
            result.addProperty("totalWorkedDuration", totalWorkedDuration);
            
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Successfully retireved recovery details", 
                    result
            );
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Application error occurred while calling recovery services", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Application error occurred while calling recovery services"
            );
        }
    }
    
    /**
     * Retrieve last captured work status log from local database
     * @return
     * @throws Exception 
     */
    private WorkStatusLog getLastWorkStatusLog() throws Exception {
        
        WorkStatusLog lastWorkStatusLog = null;
        try {
            Database<WorkStatusLog> database = DatabaseProxy.openConnection(WorkStatusLog.class);
            QueryBuilder<WorkStatusLog, Long> queryBuilder = database.getQueryBuilder();
            
            queryBuilder.where().eq(WorkStatusLog.FIELD_USER_ID, this.userId);
            queryBuilder.orderBy(WorkStatusLog.FIELD_ACTION_TIMESTAMP, false);
            
            lastWorkStatusLog = queryBuilder.queryForFirst(); 
        } finally {
            DatabaseProxy.closeConnection(WorkStatusLog.class);
        }
        return lastWorkStatusLog;
    }
    
    /**
     * Retrieve last captured screenshot from local database
     * @return
     * @throws Exception 
     */
    private Screenshot getLastCapturedScreenshot() throws Exception {
        
        Screenshot lastScreenshot = null;
        try {
            Database<Screenshot> database = DatabaseProxy.openConnection(Screenshot.class);
            QueryBuilder<Screenshot, Long> queryBuilder = database.getQueryBuilder();
            
            queryBuilder.where()
                    .eq(Screenshot.FIELD_USER_ID, this.userId)
                    .and()
                    .ge(Screenshot.FIELD_TIMESTAMP, this.workDateTime.getDateStartedTimestamp())
                    .and()
                    .lt(Screenshot.FIELD_TIMESTAMP, this.workDateTime.getDateEndedTimestamp());
            queryBuilder.orderBy(Screenshot.FIELD_TIMESTAMP, false);
            
            lastScreenshot = queryBuilder.queryForFirst();
        } finally {
            DatabaseProxy.closeConnection(Screenshot.class);
        }
        return lastScreenshot;
    }
    
    /**
     * Retrieve last captured active window from local database
     * @return
     * @throws Exception 
     */
    private ActiveWindow getLastCapturedActiveWindow() throws Exception {
        
        ActiveWindow lastActiveWindow = null;
        try {
            Database<ActiveWindow> database = DatabaseProxy.openConnection(ActiveWindow.class);
            QueryBuilder<ActiveWindow, Long> queryBuilder = database.getQueryBuilder();
            
            queryBuilder.where()
                    .eq(ActiveWindow.FIELD_USER_ID, this.userId)
                    .and()
                    .ge(ActiveWindow.FIELD_END_TIMESTAMP, this.workDateTime.getDateStartedTimestamp())
                    .and()
                    .lt(ActiveWindow.FIELD_END_TIMESTAMP, this.workDateTime.getDateEndedTimestamp());
            queryBuilder.orderBy(ActiveWindow.FIELD_END_TIMESTAMP, false);
            
            lastActiveWindow = queryBuilder.queryForFirst();
        } finally {
            DatabaseProxy.closeConnection(ActiveWindow.class);
        }
        return lastActiveWindow;
    }
    
    /**
     * Get user recovery status based on local persisted data
     * @return
     * @throws Exception 
     */
    private JsonObject getLocalRecoveryStatus() throws Exception {
        
        JsonObject result = new JsonObject();
        
        long dateStartedTimestamp = this.workDateTime.getDateStartedTimestamp();
        long dateEndedTimestamp = this.workDateTime.getDateEndedTimestamp();
        
        if (this.lastWorkStatusLog == null) {
            result.addProperty("shouldRecover", false);
            return result;
        }
        
        WorkStatus lastWorkStatus = this.lastWorkStatusLog.getWorkStatus();
        boolean isRecoverStatus = isRecoverWorkStatus(lastWorkStatus);
        result.addProperty("shouldRecover", isRecoverStatus);
        
        if (!isRecoverStatus) {
            return result;
        }
        
        // check recovery process will be executed within same day
        long lastActionTimestamp = this.lastWorkStatusLog.getActionTimestamp();
        boolean isRecoverSameDay = lastActionTimestamp >= dateStartedTimestamp 
                && lastActionTimestamp < dateEndedTimestamp;
        
        if (!isRecoverSameDay) {
            result.addProperty("recoveryStatus", "CAN_NOT_RECOVER_DUE_TO_NOT_SAME_DAY");
            return result;
        }
        
        return result;
    }
    
    /**
     * Check whether work status of last work status log should be recoverable
     * status
     * @param workStatus
     * @return 
     */
    private boolean isRecoverWorkStatus(WorkStatus workStatus) {
        
        for (WorkStatus currentWorkStatus : RECOVERY_WORK_STATUS) {
            if (currentWorkStatus.equals(workStatus)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get latest timestamp from raw data retrieved from local and remote databases
     * @return 
     */
    private long getLatestRawdataTimestamp() {
        
        long latestRawdataTimestamp = 0;
        for (Long currentTiemestamp : this.latestRawdataTimestamps) {
            if (currentTiemestamp > latestRawdataTimestamp) {
                latestRawdataTimestamp = currentTiemestamp;
            }
        }
        return latestRawdataTimestamp;
    }
    
    /**
     * Process API response's data where extract necessary
     * @param apiResponseData 
     */
    private void processApiResponseData(JsonObject apiResponseData) {
        
        if (apiResponseData.has("latestTaskStatusLog")) {
            JsonObject workstatusLog = apiResponseData.get("latestTaskStatusLog").getAsJsonObject();
            if (workstatusLog.has("actionTimestamp")) {
                long actionTimestamp = workstatusLog.get("actionTimestamp").getAsLong();
                this.latestRawdataTimestamps.add(actionTimestamp);
            }
        }
        
        if (apiResponseData.has("latestActiveWindow")) {
            JsonObject activeWindow = apiResponseData.get("latestActiveWindow").getAsJsonObject();
            if (activeWindow.has("endTimestamp")) {
                long actionTimestamp = activeWindow.get("endTimestamp").getAsLong();
                this.latestRawdataTimestamps.add(actionTimestamp);
            }    
        }
        
        if (apiResponseData.has("latestScreenshot")) {
            JsonObject screenshot = apiResponseData.get("latestScreenshot").getAsJsonObject();
            if (screenshot.has("timestamp")) {
                long actionTimestamp = screenshot.get("timestamp").getAsLong();
                this.latestRawdataTimestamps.add(actionTimestamp);
            }    
        }
    }
    
    private long calculateDailyWorkingTime() throws Exception {
        
        long dailyWorkingTime = 0;
        try {
            Database<ActiveWindow> database = DatabaseProxy.openConnection(ActiveWindow.class);
            QueryBuilder<ActiveWindow, Long> queryBuilder = database.getQueryBuilder();
            
            queryBuilder.where()
                    .eq(ActiveWindow.FIELD_USER_ID, this.userId)
                    .and()
                    .ge(ActiveWindow.FIELD_STARTED_TIMESTAMP, this.workDateTime.getDateStartedTimestamp())
                    .and()
                    .lt(ActiveWindow.FIELD_END_TIMESTAMP, this.workDateTime.getDateEndedTimestamp());
            List<ActiveWindow> activeWindows = queryBuilder.query();
            
            for (ActiveWindow currentWindow : activeWindows) {
                long focusDuration = currentWindow.getFocusDuration();
                dailyWorkingTime += focusDuration;
            }
        } finally {
            DatabaseProxy.closeConnection(ActiveWindow.class);
        }
        
        return dailyWorkingTime;
    }
}
