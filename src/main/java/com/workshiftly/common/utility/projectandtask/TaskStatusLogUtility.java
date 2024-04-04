/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.projectandtask;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.j256.ormlite.stmt.QueryBuilder;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.TaskStatusLog;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.common.utility.rawdata.BaseRawDataHandler;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hashan
 */
public class TaskStatusLogUtility implements BaseRawDataHandler {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(TaskStatusLogUtility.class);
    
    private static TaskStatusLogUtility singleton;
    private Gson GSON = new Gson();

    public static TaskStatusLogUtility getInstance() {
        
        if (singleton == null) {
            singleton = new TaskStatusLogUtility();
        }
        return singleton;
    }

    private TaskStatusLogUtility() {
//        TaskStatusLog lastTaskStatusLog = getLastTaskStatusLog();
//        StateStorage.set(StateName.LAST_TASK_STATUS_LOG, TaskStatusLog.class, lastTaskStatusLog);
    }

    private TaskStatusLog getLastTaskStatusLog() throws Exception {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            String userId = userSession.getId();

            Database<TaskStatusLog> database = DatabaseProxy.openConnection(TaskStatusLog.class);
            TaskStatusLog lastTaskStatusLog = database.getQueryBuilder()
                    .orderBy(TaskStatusLog.FIELD_ACTION_TIMESTAMP, false)
                    .where().eq(TaskStatusLog.FIELD_USER_ID, userId)
                    .queryForFirst();

            return lastTaskStatusLog;
        } finally {
            DatabaseProxy.closeConnection(TaskStatusLog.class);
        }
    }

    public Response recordTaskStatusLog(
            ProjectTask task, 
            TaskStatusLog.TaskStatus taskStatus
    ) throws Exception {
        long currentTimestamp = TimeUtility.getCurrentTimestamp();
        return recordTaskStatusLog(task, currentTimestamp, taskStatus);
    }
    
    public Response recordTaskStatusLog(
            ProjectTask task, 
            long actionTimestamp, 
            TaskStatusLog.TaskStatus taskStatus
    ) {
        
        TaskStatusLog taskStatusLog = new TaskStatusLog();

        UserSession userSession = StateStorage.getCurrentState(
                StateName.USER_SESSION
        );
        String userId = userSession.getId();
        taskStatusLog.setUserId(userId);

        taskStatusLog.setTaskId(task.getId());
        
        String deviceId = userSession.getDeviceId();
        taskStatusLog.setDeviceId(deviceId);
        
        WorkDateTime workDateTime = StateStorage.getCurrentState(
                StateName.CURRENT_WORKDATETIME_INSTANCE
        );
        String userDate = workDateTime.getFormattedDate();
        long date = workDateTime.getDateStartedTimestamp();
        taskStatusLog.setUserDate(userDate);
        taskStatusLog.setDate(date);

        taskStatusLog.setActionTimestamp(actionTimestamp);
        taskStatusLog.setTaskStatus(taskStatus);
        taskStatusLog.setIsSynced(false);
       
        
        try {
            Database<TaskStatusLog> database = DatabaseProxy
                    .openConnection(TaskStatusLog.class);
            Response response = database.create(taskStatusLog);
            
            if (response.isError()) {
                return response;
            }
            
            JsonElement data = GSON.toJsonTree(taskStatusLog);
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Successfully saved task status log record.", 
                    data
            );
            
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "failed to create task status log", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.DATABASE_EXCEPTION, 
                    "Error occurred while writing into database"
            );
        } finally {
            DatabaseProxy.closeConnection(TaskStatusLog.class);
        }
    }

    @Override
    public boolean handlePeridociDatabaseWriting() {
        try {
            List<TaskStatusLog> taskStatusLogs = StateStorage.getCurrentState(StateName.TASK_STATUS_LOG_LIST);

            if (taskStatusLogs.isEmpty()) {
                return true;
            }

            Database<TaskStatusLog> database = DatabaseProxy.openConnection(TaskStatusLog.class);
            Response response = database.create(taskStatusLogs);

            if (!response.isError()) {
                StateStorage.set(StateName.TASK_STATUS_LOG_LIST, ArrayList.class, new ArrayList<TaskStatusLog>());
            }
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while writing taskstatuslog peridocly", ex);
            return false;
        } finally {
            DatabaseProxy.closeConnection(TaskStatusLog.class);
        }
        return true;
    }

    public TaskStatusLog getRecentTaskStatusLog(String taskId, String userId) throws Exception {
        
        try {
            Database<TaskStatusLog> database = DatabaseProxy.openConnection(TaskStatusLog.class);
            QueryBuilder<TaskStatusLog, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where().eq(TaskStatusLog.FIELD_TASK_ID, taskId).and().eq(TaskStatusLog.FIELD_USER_ID, userId);
            queryBuilder.orderBy(TaskStatusLog.FIELD_ACTION_TIMESTAMP, false);
            return queryBuilder.queryForFirst();
        } catch (Exception ex) {
            throw ex;
        } finally {
            DatabaseProxy.closeConnection(TaskStatusLog.class);
        }
    }
    
}
