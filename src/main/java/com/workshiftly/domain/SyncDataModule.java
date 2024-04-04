/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.domain;

import com.google.gson.Gson;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.MeetingTimeLog;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.model.Task;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.persistence.storage.ObjectStorage;
import com.workshiftly.service.concurrent.ThreadExecutorService;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author chamara
 */
public final class SyncDataModule {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(SyncDataModule.class);
    
    private static final SyncDataModule SINGLETON_INSTANCE = new SyncDataModule();
    private static final double ACTIVE_WINDOW_SYNC_BATCH_SIZE = 20;
    private static final double SCREENSHOT_SYNC_BATCH_SIZE = 10;
    private static final double TASK_SYNC_BATCH_SIZE = 30;
    private static final double URL_SYNC_BATCH_SIZE = 10;
    
    private static final long RAW_DATA_PERIODIC_TASK_INTERVAL = DotEnvUtility.RAW_DATA_PERIODIC_TASK_INTERVAL();
    
    private ScheduledFuture<?>  periodicSyncFuture;
    private boolean isSyncing;
    
    private ScheduledFuture<?> getPeriodicFuture() {
        return SINGLETON_INSTANCE.periodicSyncFuture;
    }
    
    private void SetPeriodicFuture(ScheduledFuture<?> future) {
        this.periodicSyncFuture = future;
    }
    
    public SyncDataModule() {
    }
    
    private static SyncDataModule getModule() {
        return SyncDataModule.SINGLETON_INSTANCE;
    }

    public boolean isIsSyncing() {
        return isSyncing;
    }

    public void setIsSyncing(boolean isSyncing) {
        this.isSyncing = isSyncing;
    }
    
    private boolean syncWorkStatusLogs() {
        
        Database<WorkStatusLog> database;
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            String userId = userSession.getId();
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            
            database = DatabaseProxy.openConnection(WorkStatusLog.class);
            QueryBuilder<WorkStatusLog, Long> queryBuilder = database.getQueryBuilder();
            Where<WorkStatusLog, Long> where = queryBuilder.where();
            where.and(
                    where.eq(WorkStatusLog.FIELD_IS_SYNCED, false),
                    where.eq(WorkStatusLog.FIELD_USER_ID, userId)
            );
            long totalNumberOfRecords = queryBuilder.countOf();
            long totalNumberOfBatches = (long) Math.ceil(totalNumberOfRecords / ACTIVE_WINDOW_SYNC_BATCH_SIZE);
            
            for (int currentBatchId = 1; currentBatchId <= totalNumberOfBatches; currentBatchId++) {
                long limit = (long) ACTIVE_WINDOW_SYNC_BATCH_SIZE;
                
                queryBuilder = queryBuilder.limit(limit);
                List<WorkStatusLog> workStatusLogs = queryBuilder.query();
                Response response = httpRequestCaller.callPostSyncWorkStatusLogs(userId, workStatusLogs);
                
                if (response.isError()) 
                    return false;

                List<Long> rowIds = workStatusLogs.stream()
                        .map(WorkStatusLog::getRowId)
                        .collect(Collectors.toList());
                UpdateBuilder<WorkStatusLog, Long> updateBuilder = database.getUpdateBuilder();
                updateBuilder.updateColumnValue(WorkStatusLog.FIELD_IS_SYNCED, true);
                updateBuilder.where().in(WorkStatusLog.FIELD_ROW_ID, rowIds);
                updateBuilder.update();
            }
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync active windows", ex);
            return false;
        } finally {
            DatabaseProxy.closeConnection(WorkStatusLog.class);
        }
        return true;
    }
    
    private boolean syncActivityWindows() {
        
        Gson gson = new Gson();
        Database<ActiveWindow> database;
        try {
            
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            String userId = userSession.getId();
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            
            database = DatabaseProxy.openConnection(ActiveWindow.class);
            QueryBuilder<ActiveWindow, Long> queryBuilder = database.getQueryBuilder();
            Where<ActiveWindow, Long> where = queryBuilder.where();
            where.and(
                    where.or(
                            where.isNull(ActiveWindow.FIELD_IS_SYNCED),
                            where.eq(ActiveWindow.FIELD_IS_SYNCED, false)
                    ),
                    where.eq(ActiveWindow.FIELD_USER_ID, userId)
            );
            
            long totalNumberOfRecords = queryBuilder.countOf();
            long totalNumberOfBatches = (long) Math.ceil(totalNumberOfRecords / ACTIVE_WINDOW_SYNC_BATCH_SIZE);
            
            for (int currentBatchId = 1; currentBatchId <= totalNumberOfBatches; currentBatchId++) {
                long limit = (long) ACTIVE_WINDOW_SYNC_BATCH_SIZE;
                queryBuilder = queryBuilder.limit(limit);

                List<ActiveWindow> currentBatch = queryBuilder.query();
                Response apiResponse = httpRequestCaller.callPostSyncActiveWindows(userId, currentBatch);
                
                if (apiResponse.isError())
                    return false;

                List<Long> rowIds = currentBatch.stream().map(ActiveWindow::getRowId)
                        .collect(Collectors.toList());

                UpdateBuilder<ActiveWindow, Long> updateBuilder = database.getUpdateBuilder();
                updateBuilder.updateColumnValue(ActiveWindow.FIELD_IS_SYNCED, true);
                updateBuilder.where().in(ActiveWindow.FIELD_ROW_ID, rowIds);
                int updatedRecordCount = updateBuilder.update();
            }
            
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync active windows", ex);
            return false;
        } finally {
            DatabaseProxy.closeConnection(ActiveWindow.class);
        }
        return true;
    }
    
    public boolean syncScreenshots() {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);
            
            ObjectStorage objectStorage = new ObjectStorage(userSession);
            objectStorage.cleanCompletedJobs("putObject", Screenshot.class);
            
            Database<Screenshot> database = DatabaseProxy.openConnection(Screenshot.class);
            QueryBuilder<Screenshot, Long> queryBuilder = database.getQueryBuilder();
            Where<Screenshot, Long> where = queryBuilder.where();
            where.and(
                    where.or(
                            where.isNull(ActiveWindow.FIELD_IS_SYNCED),
                            where.eq(ActiveWindow.FIELD_IS_SYNCED, false)
                    ),
                    where.eq(ActiveWindow.FIELD_USER_ID, userSession.getId())
            );
            
            long totalRecords = queryBuilder.countOf();
            long totalBatches = (long) Math.ceil(totalRecords / SCREENSHOT_SYNC_BATCH_SIZE);
            long limit = (long) SCREENSHOT_SYNC_BATCH_SIZE;
            
            for (int batchId = 1; batchId <= totalBatches; batchId++) {
                long offset = (batchId == 1) ? 0 : batchId * limit;
                
                List<Screenshot> currentBatch = queryBuilder
                        .limit(limit).offset(offset)
                        .query();
                
                Long expiration = 60L * 60;
                objectStorage.initScreenshotStorageURLMeta(
                        currentBatch, "putObject", "base64", expiration
                );
            }
            
            ThreadExecutorService.executeTask(() -> {
                try {
                    objectStorage.completePendingJobs("putObject", Screenshot.class);
                } catch (Exception ex) {
                    String message = "Unable to execute screenshot storage uploads";
                    LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
                }
            });
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync screenshots", ex);
            return false;
        } finally {
            DatabaseProxy.closeConnection(Screenshot.class);
        }
        return true;
    }
    
    private boolean syncDirtyTaskList() {
        
        Database<Task> database;
        HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
        
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            
            database = DatabaseProxy.openConnection(Task.class);
            QueryBuilder<Task, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where()
                    .eq(Task.FIELD_USER_ID, userSession.getId()).and()
                    .eq(Task.FIELD_IS_DIRTY, true).and()
                    .eq(Task.FIELD_IS_SYNCED, false);
            
            List<Task> resultSet = queryBuilder.query();
            
//            for (Task currentTask : resultSet) {
//                Response apiResponse = httpRequestCaller.callUpdateTaskSpendTime(currentTask);
//                
//                if (!apiResponse.isError()) {
//                    currentTask.setIsDirty(false);
//                    currentTask.setIsSynced(true);
//                }
//            }
            
            List<Task> updatedTasks = resultSet.stream().filter(Task::isIsSynced)
                    .collect(Collectors.toList());
            List<String> updatedTaskIds = updatedTasks.stream()
                    .map((task) -> task.getId())
                    .collect(Collectors.toList());
            
            UpdateBuilder<Task, Long> updateBuilder = database.getUpdateBuilder();
            updateBuilder.updateColumnValue(Task.FIELD_IS_DIRTY, false);
            updateBuilder.updateColumnValue(Task.FIELD_IS_SYNCED, true);
            updateBuilder.where().in(Task.FIELD_ID, updatedTaskIds);
            updateBuilder.update();
        }  catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync screenshots", ex);
            return false;
        } finally {
            DatabaseProxy.closeConnection(Task.class);
        }
        return true;
    }
    
    public synchronized static void startPeriodicSyncing() throws Exception {
        
        Runnable syncDataRunnable = getSyncDataServiceRunnable();
        long timeInterval = TimeUnit.MINUTES.toSeconds(RAW_DATA_PERIODIC_TASK_INTERVAL);
        
        ScheduledFuture<?> periodicFuture = ThreadExecutorService.executePeriodicTask(
                syncDataRunnable, timeInterval, timeInterval
        );
        SyncDataModule.getModule().SetPeriodicFuture(periodicFuture);
        StateStorage.set(StateName.IS_PERIODIC_SYNC_ACTIVE, Boolean.class, Boolean.TRUE);
    }
    
    public synchronized static void stopPeriodicSycning() throws Exception {
        ScheduledFuture<?> periodicFuture = SyncDataModule.getModule().getPeriodicFuture();
        if (periodicFuture != null) {
            periodicFuture.cancel(false);
        }
        StateStorage.set(StateName.IS_PERIODIC_SYNC_ACTIVE, Boolean.class, Boolean.FALSE);
    }
    
    private static Runnable getSyncDataServiceRunnable() {
        
        Runnable taskRunnable = () -> {
            try {
                UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
                AppValidator.validateUserSession(userSession);
            
                setIsSyncingState(true);
                
                SyncDataModule syncDataModule = getModule();
                syncDataModule.syncScreenshots();
                
                boolean isSuccess = syncDataModule.syncWorkStatusLogs();
                
                if (!isSuccess) {
                    setIsSyncingState(false);
                    return;
                }
                
                isSuccess = syncDataModule.syncActivityWindows();
                if (!isSuccess) {
                    setIsSyncingState(false);
                    return;
                }
                
                isSuccess = syncDataModule.syncDirtyTaskList();
                if (!isSuccess) {
                    setIsSyncingState(false);
                    return;
                }
                
                syncDataModule.syncMeetingTimeLogs(null);
                setIsSyncingState(false);
                
                if (isSuccess) {
                    PublishSubject<Long> currentLastSyncTime = 
                            StateStorage.getCurrentState(StateName.LAST_SYNCED_TIME);

                    if (currentLastSyncTime == null) {
                        currentLastSyncTime = PublishSubject.create();
                        StateStorage.set(
                                StateName.LAST_SYNCED_TIME, 
                                PublishSubject.class, 
                                currentLastSyncTime
                        );
                    }

                    long currentTimestamp = TimeUtility.getCurrentTimestamp();
                    currentLastSyncTime.onNext(currentTimestamp);
                }
                
            } catch (AuthenticationException ex) {
                String exceptionMsg = "failed to sync raw data due to authentication is failed";
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, exceptionMsg, ex);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to set state", ex);
            }
        };
        return taskRunnable;
    }
    
    public static Future<?> invokeSyncDataAtOnce() {
        Runnable syncDataTask = getSyncDataServiceRunnable();
        return ThreadExecutorService.executeTask(syncDataTask);
    }
    
    private static void setIsSyncingState(boolean state) throws Exception {
        getModule().setIsSyncing(state);
        StateStorage.set(StateName.IS_SYNCING, Boolean.class, state);

        if (!state) {
            // handle changes on syncing end
        }
    }
    
    /**
     * Method Name : _getSessionUserCompanyProjects
     * Purpose : Get company projects for logged user
     * 
     * @param companyProjects 
     */
    public void syncMeetingTimeLogs(List<CompanyProject> companyProjects) {
        
        Runnable runnable = getSyncWorkTimeLogsTask(companyProjects);
        ThreadExecutorService.executeTask(runnable);    
    }
    
    /**
     * Method Name : _getSessionUserCompanyProjects
     * Purpose : Get company projects for logged user
     * 
     * @param companyProjects 
     * @return List of company project related to session user 
     */
    private Runnable getSyncWorkTimeLogsTask(List<CompanyProject> companyProjects) {
        
        return () -> {
            final List<CompanyProject> _companyProjects = new ArrayList<>();
            if (companyProjects == null) {
                try {
                    UserSession userSession 
                            = StateStorage.getCurrentState(StateName.USER_SESSION);
                    AppValidator.validateUserSession(userSession);
                    String companyId = userSession.getCompanyId();
                    if (companyId != null) {
                        Database<CompanyProject> database 
                            = DatabaseProxy.openConnection(CompanyProject.class);
                        QueryBuilder<CompanyProject, Long> queryBuilder = database.getQueryBuilder();
                        queryBuilder.where()
                                .eq(CompanyProject.FIELD_COMPANY_ID, companyId);
                         
                        List<CompanyProject> queryResult = queryBuilder.query();
                        _companyProjects.addAll(queryResult);
                    }
                } catch (Exception ex) {
                    return;
                } finally {
                    DatabaseProxy.closeConnection(CompanyProject.class);
                }
            }
            Database<MeetingTimeLog> database;
            final List<CompanyProject> __companyProjects = companyProjects == null 
                    ? _companyProjects : companyProjects;
            Map<String, CompanyProject> companyProjectMap = __companyProjects.stream()
                    .collect(Collectors.toMap(
                            CompanyProject::getId, Function.identity()
                    ));
            try {
                UserSession userSession 
                        = StateStorage.getCurrentState(StateName.USER_SESSION);
                
                database = DatabaseProxy.openConnection(MeetingTimeLog.class);
                QueryBuilder<MeetingTimeLog, Long> queryBuilder = database.getQueryBuilder();
                
                queryBuilder.where()
                        .eq(MeetingTimeLog.FIELD_USER_ID, userSession.getId())
                        .and()
                        .eq(MeetingTimeLog.FIELD_IS_COMPLETED, true)
                        .and()
                        .eq(MeetingTimeLog.FIELD_IS_SYNCED, false);
                List<MeetingTimeLog> meetingTimeLogs = queryBuilder.query();
                
                if (meetingTimeLogs.isEmpty()) {
                    return;
                }
                
                meetingTimeLogs.forEach((var meetingTimeLog) -> {
                    String projectId = meetingTimeLog.getProjectId();
                    CompanyProject project = companyProjectMap.getOrDefault(projectId, null);
                    
                    if (project != null) {
                        List<MeetingTimeLog> _meetingTimeLogs = project.getMeetingTimeLogs();
                        _meetingTimeLogs.add(meetingTimeLog);
                    }
                });
                
                // Collect synced database records
                List<Long> syncedRecordRowIds = new ArrayList<>();
                
                companyProjectMap.forEach((String projectId, CompanyProject project) -> {
                    if (!(project.getMeetingTimeLogs() == null 
                            || project.getMeetingTimeLogs().isEmpty())) {
                        try {
                            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
                            Response apiResponse = httpRequestCaller.PostSyncMeetingTimeLogs(
                                    project, project.getMeetingTimeLogs()
                            );

                            if (!apiResponse.isError()) {
                                List<Long> syncedRowIds = project.getMeetingTimeLogs().stream()
                                        .map(MeetingTimeLog::getRowId)
                                        .collect(Collectors.toList());
                                syncedRecordRowIds.addAll(syncedRowIds);
                            }
                        } catch (Exception ex) {
                            LOGGER.logRecord(
                                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                                    "Unable to sync meeting time logs", 
                                    ex
                            );
                        }
                    }
                });
                
                if (!syncedRecordRowIds.isEmpty()) {
                    UpdateBuilder<MeetingTimeLog, Long> updateBuilder 
                        = database.getUpdateBuilder();
                    updateBuilder.where().in(MeetingTimeLog.FIELD_ROW_ID, syncedRecordRowIds);
                    updateBuilder.updateColumnValue(MeetingTimeLog.FIELD_IS_SYNCED, true);
                    PreparedUpdate<MeetingTimeLog> preparedUpdate = updateBuilder.prepare();
                    database.update(preparedUpdate);
                }
            } catch (Exception ex) {
                LOGGER.logRecord(
                        InternalLogger.LOGGER_LEVEL.SEVERE,
                        "Error occurred while syncing meeting time logs", 
                        ex
                );
            } finally {
                DatabaseProxy.closeConnection(MeetingTimeLog.class);
            }
        };
    }
}
