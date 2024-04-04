/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.workstatuslog;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.QueryBuilder;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.BreakReason;
import com.workshiftly.common.model.CompanyConfiguration;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.common.utility.rawdata.BaseRawDataHandler;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hashan
 */
public class WorkStatusLogUtility implements BaseRawDataHandler {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(WorkStatusLogUtility.class);
    private static final HttpRequestCaller HTTPREQUEST_CALLER = new HttpRequestCaller();
    
    Gson GSON = new Gson();

    private static WorkStatusLogUtility singleton;

    public static WorkStatusLogUtility getInstance() throws Exception {
        if (singleton == null) {
            singleton = new WorkStatusLogUtility();
        }
        return singleton;
    }

    private WorkStatusLogUtility() throws Exception {
        
        WorkDateTime workDateTimeObj = TimeUtility.initWorkDateTime();
        StateStorage.set(StateName.CURRENT_WORKDATETIME_INSTANCE, WorkDateTime.class, workDateTimeObj);
        
//        WorkStatusLog lastWorkStatusLog = getLastWorkStatusLog();
//        StateStorage.set(StateName.LAST_WORK_STATUS_LOG, WorkStatusLog.class, lastWorkStatusLog);
    }

    private WorkStatusLog getLastWorkStatusLog() throws Exception {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            String userId = userSession.getId();

            Database<WorkStatusLog> database = DatabaseProxy.openConnection(WorkStatusLog.class);
            WorkStatusLog lastWorkStatusLog = database.getQueryBuilder()
                    .orderBy(WorkStatusLog.FIELD_ACTION_TIMESTAMP, false)
                    .where().eq(WorkStatusLog.FIELD_USER_ID, userId)
                    .queryForFirst();

            return lastWorkStatusLog;
        } finally {
            DatabaseProxy.closeConnection(WorkStatusLog.class);
        }
    }

    public Response recordWorkStatusLog(
            WorkStatusLog.WorkStatus workStatus, 
            BreakReason breakReason
    ) throws Exception {
        long currentTimestamp = TimeUtility.getCurrentTimestamp();
        return recordWorkStatusLog(currentTimestamp, workStatus, breakReason);
    }
    
    private Response recordWorkStatusLog(
            long actionTimestamp, 
            WorkStatusLog.WorkStatus workStatus, 
            BreakReason breakReason
    ) throws Exception {
        
        WorkStatusLog workStatusLog = new WorkStatusLog();

        UserSession userSession = StateStorage.getCurrentState(
                StateName.USER_SESSION
        );
        workStatusLog.setUserId(userSession.getId());

        WorkDateTime workDateTime = StateStorage.getCurrentState(
                StateName.CURRENT_WORKDATETIME_INSTANCE
        );
        String userDate = workDateTime.getFormattedDate();
        workStatusLog.setUserDate(userDate);
        
        long date = workDateTime.getDateStartedTimestamp();
        workStatusLog.setDate(date);

        workStatusLog.setActionTimestamp(actionTimestamp);
        workStatusLog.setWorkStatus(workStatus);
        workStatusLog.setIsSynced(true);
 workStatusLog.setDeviceId(userSession.getDeviceId().toString());
        if (workStatus == WorkStatusLog.WorkStatus.BREAK && breakReason != null) {
            String breakReasonId = breakReason.getId();
            String breakReasonTitle = 
                    !breakReason.getTitle().equals(StateName.OTHER_BREAK_REASON_TITLE) ?
                    breakReason.getTitle() :
                    breakReason.getTitle().concat(" - ").concat(breakReason.getOtherReason());
            boolean isBreakAutomaticallyStart = breakReason.isStartedAutomatically();
            long breakAutomaticallyStartDuration = breakReason.getDuration();

            workStatusLog.setBreakReasonId(breakReasonId);
            workStatusLog.setBreakReasonTitle(breakReasonTitle);
            workStatusLog.setIsBreakAutomaticallyStart(isBreakAutomaticallyStart);
            workStatusLog.setBreakAutomaticallyStartDuration(breakAutomaticallyStartDuration); 
            workStatusLog.setDeviceId(userSession.getDeviceId().toString());
        }
        
        List<WorkStatusLog> workStatusLogs = new ArrayList<>();
        workStatusLogs.add(workStatusLog);
        Response apiResponse = HTTPREQUEST_CALLER
                .callPostCreateWorkStatusLogs(userSession.getId(), workStatusLogs);
        
        if (apiResponse.isError()) {
            workStatusLog.setIsSynced(false);
        } else {
            updateWorkStatusObserver();
        }
        
        try {
            Database<WorkStatusLog> database = DatabaseProxy
                    .openConnection(WorkStatusLog.class);
            Response response = database.create(workStatusLog, true);
            
            if (response.isError()) {
                return response;
            }
            
            StateStorage.set(
                    StateName.LAST_WORK_STATUS_LOG, WorkStatusLog.class, workStatusLog
            );
            StateStorage.set(StateName.CURRENT_BREAK_REASON, BreakReason.class, null);
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "failed to save workstatus log into database", 
                    ex
            );
            throw ex;
        } finally {
            DatabaseProxy.closeConnection(WorkStatusLog.class);
        }
    }
    
    @Override
    public boolean handlePeridociDatabaseWriting() {
        return true;
    }
    
    public List<WorkStatusLog> getWorkStatusLogs(WorkDateTime workDateTime, UserSession userSession) throws Exception {
        
        List<WorkStatusLog> workStatusLogs;
        
        try {
            Database<WorkStatusLog> database = DatabaseProxy.openConnection(WorkStatusLog.class);
            
            QueryBuilder<WorkStatusLog, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where().eq(WorkStatusLog.FIELD_USER_ID, userSession.getId())
                    .and().eq(WorkStatusLog.FIELD_DATE, workDateTime.getDateStartedTimestamp());
            
            workStatusLogs = queryBuilder.query();
        } finally {
            DatabaseProxy.closeConnection(WorkStatusLog.class);
        }
        
        return workStatusLogs;
    }

    public List<BreakReason> getBreakReasonList(boolean withOtherReason) {
        
        CompanyConfiguration companyConfiguration = StateStorage.getCurrentState(StateName.COMPANY_CONFIGURATION);
        
        if (companyConfiguration == null) {
            return null;
        }
        
        Gson gson = new Gson();
        
        List<BreakReason> companyBreakReasonList = companyConfiguration.getBreakReasons();
        Type listOfBreakReasonType = new TypeToken<List<BreakReason>>(){}.getType();
        JsonElement serializedElement = gson.toJsonTree(companyBreakReasonList);
        List<BreakReason> deepClonedList = gson.fromJson(serializedElement, listOfBreakReasonType);

        if (withOtherReason) {
            BreakReason breakReason = new BreakReason();
            breakReason.setRowId(null);
            breakReason.setId(null);
            breakReason.setStartedAutomatically(false);
            breakReason.setDuration(0);
            breakReason.setTitle(StateName.OTHER_BREAK_REASON_TITLE);
            deepClonedList.add(breakReason);
        }
        
        return deepClonedList;
    }

    static public void updateWorkStatusObserver() throws Exception {
        PublishSubject<Long> publisher = StateStorage.getCurrentState(StateName.LAST_WORK_STATUS_LOG_SYNCED_TIME);

        if (publisher == null) {
            publisher = PublishSubject.create();
            StateStorage.set(StateName.LAST_WORK_STATUS_LOG_SYNCED_TIME, PublishSubject.class, publisher);
        }

        long currentTimestamp = TimeUtility.getCurrentTimestamp();
        publisher.onNext(currentTimestamp);
    }
}
