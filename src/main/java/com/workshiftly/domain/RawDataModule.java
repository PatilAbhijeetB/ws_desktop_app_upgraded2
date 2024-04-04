/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.domain;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.BreakReason;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.common.utility.idletime.IdleTimeListener;
import com.workshiftly.common.utility.rawdata.activewindow.ActiveWindowUtility;
import com.workshiftly.common.utility.rawdata.screenshot.ScreenshotUtility;
import com.workshiftly.common.utility.rawdata.workstatuslog.WorkStatusLogUtility;
import com.workshiftly.service.concurrent.ThreadExecutorService;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import org.jnativehook.NativeHookException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.workshiftly.common.model.CompanyConfiguration;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.MeetingTimeLog;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.model.WebBrowserLog;
import com.workshiftly.common.utility.rawdata.urlcapture.UrlCaptureUility;
import com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers.Browser;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author chamara
 */
public final class RawDataModule {
    
    private static final InternalLogger LOGGER;
    private static final Map<String, ScheduledFuture<?>> CANCELABLE_THREAD_TASKS;
    private static final List<ActiveWindow> CAPTURED_ACTIVE_WINDOWS;
    
    private static ActiveWindow LAST_CAPTURED_ACTIVE_WINDOW;
    private static IdleTimeListener IDLE_TIMER_LISTENER;
    
    static {
        LOGGER = LoggerService.getLogger(RawDataModule.class);
        CAPTURED_ACTIVE_WINDOWS = Collections.synchronizedList(new ArrayList<>());
        CANCELABLE_THREAD_TASKS = new HashMap<>();
    }

    public static void setLAST_CAPTURED_ACTIVE_WINDOW(ActiveWindow LAST_CAPTURED_ACTIVE_WINDOW) {
        RawDataModule.LAST_CAPTURED_ACTIVE_WINDOW = LAST_CAPTURED_ACTIVE_WINDOW;
    }

    public static List<ActiveWindow> getCAPTURED_ACTIVE_WINDOWS() {
        return CAPTURED_ACTIVE_WINDOWS;
    }

    public static ActiveWindow getLAST_CAPTURED_ACTIVE_WINDOW() {
        return LAST_CAPTURED_ACTIVE_WINDOW;
    }
    
   public ScheduledFuture<?> captureActiveWindow() {
        Runnable runnalbe = () -> {
            
            ActiveWindowUtility activeWindowUtility = null;
            try {
                UserSession userSession  = StateStorage.getCurrentState(StateName.USER_SESSION);
                
                activeWindowUtility = ActiveWindowUtility.getInstance();
                ActiveWindow currentActiveWindow = activeWindowUtility.getCurrentActiveWindow();
                
                String currentAppName = currentActiveWindow.getAppName();
                boolean isWebBrowser = Browser.isWebBrowser(currentAppName);
                
              
                
                if (isWebBrowser) {
                    try {
                        Browser browser = Browser.getBrowser(currentAppName);
                        WebBrowserLog browserLog = UrlCaptureUility.getCurrentURLs(browser, currentActiveWindow);
                        currentActiveWindow.setTitle(browserLog.getDomain());
                    } catch (Exception ex) {
                        // note that do not include Lgger due to it always emit exception except Windows
                    }
                }
                
                boolean isValidActiveWindow = ActiveWindowUtility.validateActiveWindow(
                        currentActiveWindow
                );
                
                if (currentActiveWindow.isIsPartial() || !isValidActiveWindow) {
                    return;
                }
                
                long currentTimestamp = TimeUtility.getCurrentTimestamp();
               
                currentActiveWindow.setStartedTimestamp(currentTimestamp);
                currentActiveWindow.setUserId(userSession.getId());
                currentActiveWindow.setCompanyId(userSession.getCompanyId());
                currentActiveWindow.setdeviceId(userSession.getDeviceId());
                ActiveWindow lastActiveWindow = LAST_CAPTURED_ACTIVE_WINDOW;
                
                if (lastActiveWindow == null) {
                    LAST_CAPTURED_ACTIVE_WINDOW = currentActiveWindow;
                    return;
                }
                
                boolean didChangedWindow = lastActiveWindow.compareTo(currentActiveWindow) != 0;
                
                if (!didChangedWindow) {
                    // handle database writing
                    activeWindowUtility.handlePeridociDatabaseWriting();
                    return;
                }
                
                // handle active window chanages
                lastActiveWindow.setEndTimestamp(currentTimestamp);
                
                long foucsDuration = lastActiveWindow.getEndTimestamp() 
                        - lastActiveWindow.getStartedTimestamp();
                
                boolean isDuplicateRecord = ActiveWindow.isDuplicateRecord(
                        lastActiveWindow, currentActiveWindow
                );
                boolean isValidFocusDuration = foucsDuration > 0;

                if (!isDuplicateRecord && isValidFocusDuration) {
                    lastActiveWindow.setFocusDuration(foucsDuration);
                    CAPTURED_ACTIVE_WINDOWS.add(lastActiveWindow);
                    LAST_CAPTURED_ACTIVE_WINDOW = currentActiveWindow;
                }
                
                // handle database writing
                activeWindowUtility.handlePeridociDatabaseWriting();

            } catch (ActiveWindowUtility.PlatformNotSupportedException ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to capture active windows", ex);
            } catch (ActiveWindowUtility.ActiveWindowUtilityException ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to capture active windows", ex);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to capture active windows", ex);
            }
        };
        
        ScheduledFuture<?> scheduledFuture = ThreadExecutorService.executePeriodicTask(runnalbe, 10, 10);
        return scheduledFuture;
    }

    private ScheduledFuture<?> captureScreenshots() throws Exception {

        try {
            ScreenshotUtility screenshotUtility = ScreenshotUtility.getInstance();
            return screenshotUtility.capture();
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE,
                    "Failed to capture screenshots",
                    ex
            );
            throw ex;
        }
    }

    /**
     *  ******* Refactor whole flow after release for test purposes
     *
     * @param workStatus
     * @param breakReason
     * @return
     */
    public Response captureWorkStatusLog(WorkStatusLog.WorkStatus workStatus, BreakReason breakReason) {
        return captureWorkStatusLog(workStatus, breakReason, false);
        
    }
    
    public Response  captureWorkStatusLog(
            WorkStatusLog.WorkStatus workStatus, 
            BreakReason breakReason, 
            boolean isUserAction
    ) {
        try {
            WorkStatusLogUtility workStatusLogUtility = WorkStatusLogUtility.getInstance();
            Response response = workStatusLogUtility.recordWorkStatusLog(workStatus, breakReason);

            WorkStatusLog lastestLastWorkStatusLog = StateStorage.getCurrentState(StateName.LAST_WORK_STATUS_LOG);
            WorkStatusLog.WorkStatus currentWorkStatus = lastestLastWorkStatusLog.getWorkStatus();

            switch (currentWorkStatus) {
                case START:
                    startCapuringServices();
                    break;
                case OFFLINE_TASK:
                case BREAK:
                case STOP:
                    cancelCapturingServices(isUserAction);
                    break;
                case IN_MEETING:
                    break;
            }
            return response;

        } catch (NativeHookException ex) {

            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "error occurred due to NativeHookException", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Application error was occurred");

        } catch (Exception ex) {

            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to record workstatus log", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Application error was occurred");
        }
    }

    private void startCapuringServices() throws NativeHookException, Exception {

        try {
            resetIdleTimer();

            //start active window capturing
            ScheduledFuture<?> activeWindowScheduledFuture = captureActiveWindow();
            CANCELABLE_THREAD_TASKS.put(StateName.ACTIVE_WINDOW, activeWindowScheduledFuture);

            // retrieve company configurations
            CompanyConfiguration companyConfig
                    = StateStorage.getCurrentState(StateName.COMPANY_CONFIGURATION);

            // screenshot capturing only started enable config at company config
            boolean isActiveCaptureScreenshots = companyConfig != null
                    ? companyConfig.isIsScreenCapturing()
                    : false;

            if (isActiveCaptureScreenshots) {
                ScheduledFuture<?> screenshotScheduledFuture = captureScreenshots();
                CANCELABLE_THREAD_TASKS.put(StateName.SCREENSHOT, screenshotScheduledFuture);
            }

            Boolean isPeriodicSyncActive
                    = StateStorage.getCurrentState(StateName.IS_PERIODIC_SYNC_ACTIVE);

            if (!(isPeriodicSyncActive != null && isPeriodicSyncActive)) {
                try {
                    SyncDataModule.startPeriodicSyncing();
                } catch (Exception ex) {
                    LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "unable to set IS_PERIODIC_SYNC_ACTIVE true", ex);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public final void cancelCapturingServices(boolean isUserAction) throws NativeHookException {
        // keep commented below line, it causes application crashes
        // idleTimeListener.stop();

        Set<Map.Entry<String, ScheduledFuture<?>>> capturingServices
                = CANCELABLE_THREAD_TASKS.entrySet();
        // cancel raw data capturing services
        capturingServices.forEach((var currentEntry) -> {
            try {
                ScheduledFuture<?> currentFuture = currentEntry.getValue();

                if (!currentFuture.isCancelled()) {
                    boolean didCancelExecution = currentFuture.cancel(false);
                    
                    if (!didCancelExecution) {
                        while (!didCancelExecution) {
                            didCancelExecution = currentFuture.cancel(false);
                        }
                    }
                }
                resetIdleTimer();
            } catch (Exception ex) {
                String errorMsg = "Failed to cancel capturing service " + currentEntry.getKey();
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            }
        });

        // forcefully commit activeWindows
        try {
            ActiveWindowUtility activeWindowUtility = ActiveWindowUtility.getInstance();
            activeWindowUtility.forceCommit(isUserAction);
        } catch (Exception ex) {
            String errorMsg = "Failed to force commit active windows";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }

        // forcefully commit screenshots
        try {
            ScreenshotUtility screenshotUtility = ScreenshotUtility.getInstance();
            screenshotUtility.forceCommit();
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE,
                    "Failed to persist screenshot after canceling capturing workers",
                    ex
            );
        }
    }

    public final Map<String, ScheduledFuture<?>> getCancelableRawThreadTasks() {
        return RawDataModule.CANCELABLE_THREAD_TASKS;
    }

    public final Long getUserIdleTime() throws Exception {
        
        Long userIdleTime = IDLE_TIMER_LISTENER.call();
        return userIdleTime;
    }

    public final void resetIdleTimer() throws NativeHookException {
        IDLE_TIMER_LISTENER = new IdleTimeListener();
        IDLE_TIMER_LISTENER.start();
    }

    public Response getTodayWorkingStatusLogs() {

        try {
            Gson gson = new Gson();
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);

            WorkStatusLogUtility workStatusLogUtility = WorkStatusLogUtility.getInstance();
            WorkDateTime workDateTime = StateStorage
                    .getCurrentState(StateName.CURRENT_WORKDATETIME_INSTANCE);
            List<WorkStatusLog> workStatusLogs = workStatusLogUtility
                    .getWorkStatusLogs(workDateTime, userSession);

            Type workStatusLogListType = new TypeToken<List<WorkStatusLog>>() {
            }.getType();
            JsonElement parsedWorkStatusLogs = gson
                    .toJsonTree(workStatusLogs, workStatusLogListType);

            return new Response(
                    false,
                    StatusCode.SUCCESS,
                    "Successfully retrieved work status logs",
                    parsedWorkStatusLogs
            );
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE,
                    "failed to get today work status logs",
                    ex
            );
            return new Response(
                    true,
                    StatusCode.SUCCESS,
                    "failed to get today work status logs"
            );
        }
    }

    public Response getBreakingReasons(boolean isContainedOtherReason) {

        try {
            WorkStatusLogUtility workStatusLogUtility = WorkStatusLogUtility.getInstance();
            List<BreakReason> breakReasons = workStatusLogUtility.getBreakReasonList(isContainedOtherReason);

            Type listOfBreakReasonType = new TypeToken<List<BreakReason>>() {
            }.getType();
            Gson gson = new Gson();
            JsonElement serializedData = gson.toJsonTree(breakReasons, listOfBreakReasonType);

            return new Response(false, StatusCode.SUCCESS, "successfully retireved data", serializedData);
        } catch (Exception ex) {
            String errorMessage = "failed to get break reasons from local database";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMessage, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "failed to get break reasons from local database");
        }

    }

    /*---------------------------------------------------------------------
         |  Method deleteSyncedRowData
         |
         |  Purpose: Delete synced raw data collected by application itself
         |      where raw data should be synced with remote server and keeping
         |      raw data relavant to today working date and logged user
         |
         |  Pre-condition: deleting data should be synced with remote and keep
         |      raw data relevant to today and user should be authenticated at
         |      at that moment
         |
         |  Post-condition: deleted raw data should be synced with remote and
         |      relevant to authenticated user's id and keep data for current
         |      working date
         |  
         |
         |  Parameters: none
         |
         |  Returns: Response object indicating execution success or not
         *-------------------------------------------------------------------*/
    public Response deleteSyncedRowData() {

        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            if (userSession == null) {
                throw new Exception("userSession should not be null");
            }

            JsonObject configurations = new JsonObject();
            configurations.addProperty("keepTodayData", true);
            configurations.addProperty("userId", userSession.getId());
            configurations.addProperty("syncedStatus", true);

            _deleteWorkStatusLogs(configurations);
            _deleteActiveWindows(configurations);
            _deleteScreenshots(configurations);

            return new Response(false, StatusCode.SUCCESS, "Successfully deleted previous synced data");
        } catch (Exception ex) {
            String errorMessage = "Unable to delete previous synced data from database";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMessage, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMessage);
        }
    }

    /*---------------------------------------------------------------------
         |  Method _deleteWorkStatusLogs
         |
         |  Purpose: Delete WorkStatusLogs according to configuration JsonObject.
         |
         |  Pre-condition: configuation object should not be null
         |
         |  Post-condition: Table rows should be removed after functioin execution
         |      releated to the query which is build upon configurations
         |
         |  Parameters:
	 |	configurations - contain parameters releted to deleting scenatio
         |          (refer functional implementation, add conditions on the fly)
         |
         |  Returns: void but exception will be thrown if there is any exception
         |       occured|
         *-------------------------------------------------------------------*/
    private void _deleteWorkStatusLogs(JsonObject configurations) throws Exception {

        try {
            Database<WorkStatusLog> database = DatabaseProxy.openConnection(WorkStatusLog.class);
            QueryBuilder<WorkStatusLog, Long> queryBuilder = database.getQueryBuilder();

            JsonElement userId = configurations.get("userId");
            boolean syncedStatus = configurations.has("syncedStatus")
                    ? configurations.get("syncedStatus").getAsBoolean() : true;

            if (userId.isJsonNull()) {
                throw new Exception("configuration object should contain userId");
            }

            Where<WorkStatusLog, Long> filterCondition = queryBuilder.where()
                    .eq(WorkStatusLog.FIELD_USER_ID, userId.getAsString())
                    .and()
                    .eq(WorkStatusLog.FIELD_IS_SYNCED, syncedStatus);

            boolean keepTodayData = configurations.has("keepTodayData")
                    ? configurations.get("keepTodayData").getAsBoolean() : true;

            if (keepTodayData) {
                WorkDateTime todayWorkingDateTime = WorkDateTime.getInstance();
                long todayStartTimestamp = todayWorkingDateTime.getDateStartedTimestamp();
                filterCondition
                        .and()
                        .le(WorkStatusLog.FIELD_ACTION_TIMESTAMP, todayStartTimestamp);
            }

            List<WorkStatusLog> records = filterCondition.query();

            if (!records.isEmpty()) {
                database.delete(records);
            }
        } finally {
            DatabaseProxy.closeConnection(WorkStatusLog.class);
        }
    }

    /*---------------------------------------------------------------------
         |  Method _deleteActiveWindows
         |
         |  Purpose: Delete ActiveWindows according to configuration JsonObject.
         |
         |  Pre-condition: configuation object should not be null
         |
         |  Post-condition: Table rows should be removed after functioin execution
         |      releated to the query which is build upon configurations
         |
         |  Parameters:
	 |	configurations - contain parameters releted to deleting scenatio
         |          (refer functional implementation, add conditions on the fly)
         |
         |  Returns: void but exception will be thrown if there is any exception
         |       occured|
         *-------------------------------------------------------------------*/
    private void _deleteActiveWindows(JsonObject configurations) throws Exception {

        try {
            Database<ActiveWindow> database = DatabaseProxy.openConnection(ActiveWindow.class);
            QueryBuilder<ActiveWindow, Long> queryBuilder = database.getQueryBuilder();

            JsonElement userId = configurations.get("userId");
            boolean syncedStatus = configurations.has("syncedStatus")
                    ? configurations.get("syncedStatus").getAsBoolean() : true;

            if (userId.isJsonNull()) {
                throw new Exception("configuration object should contain userId");
            }

            Where<ActiveWindow, Long> filterCondition = queryBuilder.where()
                    .eq(ActiveWindow.FIELD_USER_ID, userId.getAsString())
                    .and()
                    .eq(ActiveWindow.FIELD_IS_SYNCED, syncedStatus);

            boolean keepTodayData = configurations.has("keepTodayData")
                    ? configurations.get("keepTodayData").getAsBoolean() : true;

            if (keepTodayData) {
                WorkDateTime todayWorkingDateTime = WorkDateTime.getInstance();
                long todayStartTimestamp = todayWorkingDateTime.getDateStartedTimestamp();
                filterCondition
                        .and()
                        .lt(ActiveWindow.FIELD_END_TIMESTAMP, todayStartTimestamp);
            }

            List<ActiveWindow> records = filterCondition.query();

            if (!records.isEmpty()) {
                database.delete(records);
            }
        } finally {
            DatabaseProxy.closeConnection(ActiveWindow.class);
        }
    }

    /*---------------------------------------------------------------------
         |  Method _deleteActiveWindows
         |
         |  Purpose: Delete ActiveWindows according to configuration JsonObject.
         |
         |  Pre-condition: configuation object should not be null
         |
         |  Post-condition: Table rows should be removed after functioin execution
         |      releated to the query which is build upon configurations
         |
         |  Parameters:
	 |	configurations - contain parameters releted to deleting scenatio
         |          (refer functional implementation, add conditions on the fly)
         |
         |  Returns: void but exception will be thrown if there is any exception
         |       occured|
         *-------------------------------------------------------------------*/
    private void _deleteScreenshots(JsonObject configurations) throws Exception {

        try {
            Database<Screenshot> database = DatabaseProxy.openConnection(Screenshot.class);
            QueryBuilder<Screenshot, Long> queryBuilder = database.getQueryBuilder();

            JsonElement userId = configurations.get("userId");
            boolean syncedStatus = configurations.has("syncedStatus")
                    ? configurations.get("syncedStatus").getAsBoolean() : true;

            if (userId.isJsonNull()) {
                throw new Exception("configuration object should contain userId");
            }

            Where<Screenshot, Long> filterCondition = queryBuilder.where()
                    .eq(Screenshot.FIELD_USER_ID, userId.getAsString())
                    .and()
                    .eq(Screenshot.FIELD_IS_SYNCED, syncedStatus);

            boolean keepTodayData = configurations.has("keepTodayData")
                    ? configurations.get("keepTodayData").getAsBoolean() : true;

            if (keepTodayData) {
                WorkDateTime todayWorkingDateTime = WorkDateTime.getInstance();
                long todayStartTimestamp = todayWorkingDateTime.getDateStartedTimestamp();
                filterCondition.and().lt(Screenshot.FIELD_TIMESTAMP, todayStartTimestamp);
            }

            List<Screenshot> records = queryBuilder.query();

            if (!records.isEmpty()) {
                database.delete(records);
            }
        } finally {
            DatabaseProxy.closeConnection(Screenshot.class);
        }
    }

    /**
     * Method Name : createMeetingTimeLog Purpose : Create MeetingTimeLog
     * instance at database
     *
     * @param project
     * @param projectTask
     * @param participants
     * @param meetingSummary
     * @return Response
     */
    public Response createMeetingTimeLog(
            CompanyProject project,
            ProjectTask projectTask,
            String participants,
            String meetingSummary
    ) {
        try {
            MeetingTimeLog meetingTimeLog = new MeetingTimeLog();
            meetingTimeLog.setTaskUserId(projectTask.getId());
            meetingTimeLog.setTaskId(projectTask.getTaskId());
            meetingTimeLog.setProjectId(project.getId());
            meetingTimeLog.setUserId(projectTask.getUserId());
            meetingTimeLog.setIsSynced(false);

            Long currentTimestamp = TimeUtility.getCurrentTimestamp();
            meetingTimeLog.setStartedTimestamp(currentTimestamp);
            meetingTimeLog.setEndTimestamp(null);

            meetingTimeLog.setParticipants(participants);
            meetingTimeLog.setSummary(meetingSummary);

            Database<MeetingTimeLog> database;
            Response response = null;
            try {
                database = DatabaseProxy.openConnection(MeetingTimeLog.class);
                response = database.create(meetingTimeLog, true);
            } finally {
                DatabaseProxy.closeConnection(MeetingTimeLog.class);
            }
            return response;
        } catch (Exception ex) {
            String errorMsg = "Unable to start meeting";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
}
