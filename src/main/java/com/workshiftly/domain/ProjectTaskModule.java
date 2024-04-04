/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.domain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.QueryBuilder;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.MeetingTimeLog;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.TaskStatusLog;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.projectandtask.ProjectAndTaskUtility;
import com.workshiftly.common.utility.projectandtask.TaskStatusLogUtility;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.concurrent.ThreadExecutorService;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author chamara
 */
public class ProjectTaskModule {
    
    private static final InternalLogger LOGGER 
            = LoggerService.getLogger(ProjectTaskModule.class);
    
    private final ProjectAndTaskUtility projectTaskUtility;
    private final TaskStatusLogUtility taskStatusLogUtility; 
    
    public ProjectTaskModule() {
        projectTaskUtility = ProjectAndTaskUtility.getInstance();
        taskStatusLogUtility = TaskStatusLogUtility.getInstance();
    }
    
    public Response getUserAssignedProjects() {
        
        try {
            Gson gson = new Gson();
            Boolean didFetchProjectTasks = StateStorage
                    .getCurrentState(StateName.IS_SUCCESS_GET_REMOTE_PROJECT_TASK);
            
            UserSession userSession = StateStorage
                    .getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);
            
            if (didFetchProjectTasks == null || !didFetchProjectTasks) {
                String userId = userSession.getId();
                Future<Response> executionFuture = getAsyncRemoteProjectTasks(userId);
                Response response = executionFuture.get();
                
                if (response.isError()) {    
                    return response;
                }    
            }
            
            List<CompanyProject> projectsByUserId = projectTaskUtility
                    .getProjectsByUserId(userSession.getId());
            JsonElement resultJsonElement = gson.toJsonTree(projectsByUserId);
            
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Successfully retrieved projects", 
                    resultJsonElement
            );
        } catch (Exception ex) {
            StatusCode statusCode = (ex instanceof AuthenticationException)
                    ? StatusCode.SESSION_INVALID 
                    : StatusCode.APPLICATION_ERROR;
            
            String errorMsg = (ex instanceof AuthenticationException)
                    ? "You login session is invalidated, please login again"
                    : "Failed to fetch project and tasks";
            
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    errorMsg,
                    ex
            );
            return new Response(false, statusCode, errorMsg);
        }
    }
    
    public Future<Response> getAsyncRemoteProjectTasks(String userId) {
        
        Callable<Response> callableExecution = () -> {
            try {
                StateStorage.set(StateName.IS_SUCCESS_GET_REMOTE_PROJECT_TASK, Boolean.class, false);
            } catch (Exception ex) {
                LOGGER.logRecord(
                        InternalLogger.LOGGER_LEVEL.SEVERE, 
                        "Failed to persist state at getAsyncRemoteProjectTasks", 
                        ex
                );
            }
            
            Response response = projectTaskUtility.getRemoteProjectAndTask(userId);
            boolean isTaskCompleted = !response.isError();
            
            try {
                StateStorage.set(
                        StateName.IS_SUCCESS_GET_REMOTE_PROJECT_TASK, 
                        Boolean.class, 
                        isTaskCompleted
                );
            } catch (Exception ex) {
                LOGGER.logRecord(
                        InternalLogger.LOGGER_LEVEL.SEVERE, 
                        "Failed to persist state at getAsyncRemoteProjectTasks", 
                        ex
                );
            }
            return response;
        };
        
        Future<Response> future = ThreadExecutorService.submit(callableExecution);
        return future;
    }
    
    /***
     * Handle task start and pause
     * @param task
     * @param  currentTaskStatus 
     * @return 
     */
    public Response recordStartPauseTaskStatus(
            ProjectTask task, TaskStatusLog.TaskStatus currentTaskStatus
    ) {
        
        try {
            Gson gson = new Gson();
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);
            
            ProjectTask taskAtDatabase = projectTaskUtility
                    .getTaskById(task.getId(), userSession.getId());
            
            if (taskAtDatabase == null) {
                Response response = new Response(
                        true, 
                        StatusCode.TASK_NOT_FOUND, 
                        "Task can't be found"
                );
                return response;
            }
            
            if (taskAtDatabase.isHasCompleted()) {
                String errorMsg = "Task is already completed. "
                        + "Therefore you are not allowed to start or pause task anymore";
                return new Response(
                        true, StatusCode.TASK_ALREADY_COMPLETED, errorMsg
                );
            }
            
            TaskStatusLog recentTaskStatusLog =  taskStatusLogUtility
                    .getRecentTaskStatusLog(task.getId(), userSession.getId());
            
            if (recentTaskStatusLog != null 
                    && recentTaskStatusLog.getTaskStatus() == TaskStatusLog.TaskStatus.STOP) {
                return new Response(
                        true, 
                        StatusCode.TASK_ALREADY_STOPPED, 
                        "Task was already stopped. So you can't start or pause task."
                );
            }
            
            // update meeting task spend time
            if (task.getType().equals(ProjectTask.MEETING_TASK_TYPE) && task.isIsDirty()) {
                taskAtDatabase.setSpentTime(task.getSpentTime());
                taskAtDatabase.setIsDirty(true);
            }
            
            TaskStatusLog.TaskStatus nextTaskStatus = TaskStatusLog.TaskStatus.START;
            switch (currentTaskStatus) {
                case TODO:
                case BREAK:
                    nextTaskStatus = TaskStatusLog.TaskStatus.START;
                    break;
                case START:
                    nextTaskStatus = TaskStatusLog.TaskStatus.BREAK;
                    taskAtDatabase.setIsDirty(true);
                    break;
            }
            
            if (taskAtDatabase.isIsDirty()) {
                Response response = projectTaskUtility.updateTask(task);
                if (response.isError()) {
                    return response;
                }
            }
            
            Response response = taskStatusLogUtility
                    .recordTaskStatusLog(task, nextTaskStatus);
            
            if (response.isError()) {
                return response;
            }
            
            TaskStatusLog lastRecordedTaskStatusLog = gson
                    .fromJson(response.getData(), TaskStatusLog.class);
            try {
                StateStorage.set(
                        StateName.LAST_TASK_STATUS_LOG, 
                        TaskStatusLog.class, lastRecordedTaskStatusLog
                );
            } catch (Exception ex) {
                LOGGER.logRecord(
                        InternalLogger.LOGGER_LEVEL.SEVERE, 
                        "failed to persist last task status", 
                        ex
                );
            }
            
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "failed to record task stauts log", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "failed to record task stauts log"
            );
        }
    }

    public Response recordStopTaskStatus(ProjectTask task) {
        
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            
            if (userSession == null || userSession.getId() == null) {
                return new Response(
                        true, 
                        StatusCode.SESSION_INVALID, 
                        "Your session is invalidated. please re login to application"
                );
            }
            
            ProjectTask taskAtDatabase = projectTaskUtility
                    .getTaskById(task.getId(), userSession.getId());
            
            if (taskAtDatabase == null) {
                return new Response(true, StatusCode.TASK_NOT_FOUND, "Task could not be found");
            }
            
            if (taskAtDatabase.isHasCompleted()) {
                String errorMsg = "Task is already completed. Therefore you are "
                        + "not allowed to start or pause task anymore";
                return new Response(true, StatusCode.TASK_ALREADY_COMPLETED, errorMsg);
            }
                        
            taskAtDatabase.setSpentTime(task.getSpentTime());
            taskAtDatabase.setHasCompleted(true);
            taskAtDatabase.setIsDirty(true);
            taskAtDatabase.setIsSynced(false);
           
            Response response = projectTaskUtility.updateTask(taskAtDatabase);
            if (response.isError()) {
                return response;
            }
            
            response = taskStatusLogUtility
                    .recordTaskStatusLog(task, TaskStatusLog.TaskStatus.STOP);
            return response;
            
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "failed to record task stauts log", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "failed to record task stauts log"
            );
        }
    }
    
    /**
     * Method Name : refreshProjectAndTask
     * Purpose : Refresh project and task from by latest updated record   
     *  
     * @return Response 
     */
    public Response refreshProjectAndTask() {
        
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        try {
            AppValidator.validateUserSession(userSession);
        } catch (AuthenticationException ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "User session is invalided", ex);
            return new Response(false, StatusCode.SESSION_INVALID, "User session is invalided");
        }
        
        try {
            Map<String, CompanyProject> projectMapper = _getSessionUserCompanyProjects()
                    .stream()
                    .collect(Collectors.toMap(CompanyProject::getId, Function.identity()));
            
            List<CompanyProject> remoteProjects = _getRemoteUpdatedProjects(
                    userSession, projectMapper.values()
            );
            
            try {
                Database<CompanyProject> database = 
                        DatabaseProxy.openConnection(CompanyProject.class);
                Database<ProjectTask> taskDatabase = 
                        DatabaseProxy.openConnection(ProjectTask.class);
                
                for (CompanyProject curProject : remoteProjects) {
                    CompanyProject existingProject = projectMapper.get(curProject.getId());

                    if (existingProject == null) {
                        Response projectCreate = database.create(curProject);
                        
                        if (projectCreate.isError()) {
                            return projectCreate;
                        }
                        
                        List<ProjectTask> projectTasks = curProject.getProjectTasks();
                        
                        if (projectTasks != null && !projectTasks.isEmpty()) {
                            Response taskCreate = taskDatabase.create(projectTasks);
                        
                            if (taskCreate.isError()) {
                                return taskCreate;
                            }
                        }
                        continue;
                    }
                        
                    curProject.setRowId(existingProject.getRowId());
                    boolean isChanaged = existingProject.compareTo(curProject) != 0;

                    if (isChanaged) {
                        Response projectUpdate = database.update(curProject);

                        if (projectUpdate.isError()) {
                            return projectUpdate;
                        }
                    }

                    Map<String, ProjectTask> projectTaskMap = existingProject.getProjectTasks()
                            .stream()
                            .collect(Collectors.toMap(ProjectTask::getId, Function.identity()));

                    for (ProjectTask curTask : curProject.getProjectTasks()) {
                        ProjectTask existingTask = projectTaskMap.get(curTask.getId());

                        if (existingTask == null) {
                            Response taskCreate = taskDatabase.create(curTask);

                            if (taskCreate.isError()) {
                                return taskCreate;
                            }
                            continue;
                        }
                        
                        curTask.setRowId(existingTask.getRowId());
                        boolean isTaskChanged = curTask.compareTo(existingTask) != 0;
                        
                        if (isTaskChanged) {
                            Response taskUpdate = taskDatabase.update(curTask);
                            
                            if (taskUpdate.isError()) {
                                return taskUpdate;
                            }
                        }
                    }
                }
            } finally {
                DatabaseProxy.closeConnection(CompanyProject.class);
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
        
            Gson gson = new Gson();
            return new Response(false, StatusCode.SUCCESS, "Success", gson.toJsonTree(remoteProjects));
            
        } catch (Exception ex) {
         String errorMsg = "Unable to refresh project and task";
         LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
         return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    private List<CompanyProject> _getRemoteUpdatedProjects(
            UserSession userSession, 
            Collection<CompanyProject> currentProjects
    )   throws Exception {
        
        List<ProjectTask> taskList = new ArrayList<>();
        Integer latestUpdatedTimestamp = 0;

        for (CompanyProject curProject : currentProjects) {
            Integer updatedTimestamp = curProject.getUpdatedAt();

            if (updatedTimestamp > latestUpdatedTimestamp) {
                latestUpdatedTimestamp = updatedTimestamp;
            }
            taskList.addAll(curProject.getProjectTasks());
        }

        Map<String, String> queryOptions = new HashMap<>();
        queryOptions.put("withtasks", "true");
        queryOptions.put("project-timestamp", String.valueOf(latestUpdatedTimestamp));

        latestUpdatedTimestamp = 0;
        for (ProjectTask curTask : taskList) {
            Integer updatedTimestamp = curTask.getUpdatedAt();

            if (updatedTimestamp > latestUpdatedTimestamp) {
                latestUpdatedTimestamp = updatedTimestamp;
            }
        }

        queryOptions.put("task-timestamp", String.valueOf(latestUpdatedTimestamp));

        HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
        Response apiResponse = httpRequestCaller
                .callGetProjectsByEmployeeId(userSession.getId(), queryOptions);
        
        if (apiResponse.isError()) {
            throw new Exception(apiResponse.getMessage());
        }
        
        Gson gson = new Gson();
        JsonElement responseData = apiResponse.getData();
        List<CompanyProject> assignedProjects = new ArrayList<>();
        Iterator<JsonElement> iterator = responseData.getAsJsonArray().iterator();

        while (iterator.hasNext()) {
            JsonElement currentElement = iterator.next();
            CompanyProject curProject 
                    = gson.fromJson(currentElement, CompanyProject.class);

            if (currentElement.isJsonObject()) {
                JsonObject projectJsonObj = currentElement.getAsJsonObject();
                JsonElement tasksElement = projectJsonObj.get("tasks");
                Type projectTasksType = new TypeToken<List<ProjectTask>>(){}.getType();
                List<ProjectTask> tasks = gson.fromJson(tasksElement, projectTasksType);
                curProject.setProjectTasks(tasks);
            }
            assignedProjects.add(curProject);
        }
        
        return assignedProjects;
    }
    
    /**
     * Method Name : createEmployeeMeetingTask
     * Purpose : Create employee meeting task for a company project   
     * 
     * @param project 
     * @return Response 
     */
    public Response createEmployeeMeetingTask(CompanyProject project) {
        try {
            UserSession userSession = StateStorage
                    .getCurrentState(StateName.USER_SESSION);
            
            if (userSession == null) {
                return new Response(
                        true, 
                        StatusCode.SESSION_INVALID, 
                        "Your session is invlaidated, please login again"
                );
            }
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            Response apiResponse = httpRequestCaller
                    .PostCreateMeetingTask(project, userSession);
            
            if (apiResponse.isError()) {
                return apiResponse;
            }
            
            Gson gson = new Gson();
            ProjectTask createdTask = gson.fromJson(
                    apiResponse.getData(), ProjectTask.class
            );
            
            Response dbResponse;
            try {
                Database<ProjectTask> database = 
                        DatabaseProxy.openConnection(ProjectTask.class);
                dbResponse = database.create(createdTask, true);
                
                if (dbResponse.isError()) {
                    return dbResponse;
                }
                
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Successfully created meeting task",
                    dbResponse.getData()
            );
        } catch (Exception ex) {
            String errorMsg = "Could not created project meeting task";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    public Response createEmployeeMeetingTask(String projectId) {
        try {
            CompanyProject companyProject;
            try {
                Database<CompanyProject> database = DatabaseProxy
                    .openConnection(CompanyProject.class);
                QueryBuilder<CompanyProject, Long> queryBuilder = database.getQueryBuilder();
                List<CompanyProject> companyProjects = queryBuilder.where()
                        .eq(CompanyProject.FIELD_ID, projectId).query();

                companyProject = !companyProjects.isEmpty() 
                        ? companyProjects.get(0) : null;
            } finally {
                DatabaseProxy.closeConnection(CompanyProject.class);
            }
            
            Response response = createEmployeeMeetingTask(companyProject);
            return response;
            
        } catch (Exception ex) {
            String errorMsg = "Could not created project meeting task";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name : createOfflineManualTask
     * Purpose : create new offline task
     * 
     * @param  projectTask 
     * @return Response 
     */
    public Response createOfflineManualTask(ProjectTask projectTask) {
        
        Gson gson = new Gson();
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            Response apiResponse = httpRequestCaller
                    .postCreateOfflineTask(projectTask, userSession);
            
            if (apiResponse.isError()) {
                return apiResponse;
            }
            ProjectTask _ProjectTask = gson.fromJson(apiResponse.getData(), ProjectTask.class);
            
            Response dbResponse;
            try {
                Database<ProjectTask> database = 
                        DatabaseProxy.openConnection(ProjectTask.class);
                dbResponse = database.create(_ProjectTask, true);
                
                if (dbResponse.isError()) {
                    return dbResponse;
                }
                
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Successfully created meeting task",
                    dbResponse.getData()
            );
        } catch (Exception ex) {
            return new Response(true, StatusCode.APPLICATION_ERROR, "Application error occurred");
        }
    }
    
    public Response updateOfflineManualTask(ProjectTask projectTask) {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);
            
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            Response response = httpRequestCaller.putOfflineTask(projectTask, userSession);
            return response;
        } catch (AuthenticationException ex) {
            String errorMsg = "Session invalidated";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.SESSION_INVALID, errorMsg);
        } catch (Exception ex) {
            String errorMsg = "Application error occurred";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name : startProjectMeetingTask
     * Purpose : Begin New Meeting Task and Handle domain logics 
     * 
     * @param  project 
     * @param meetingTask
     * @param participants
     * @param meetingSummary
     * @return Response 
     */
    public Response startProjectMeetingTask(
            CompanyProject project,
            ProjectTask meetingTask, 
            List<String> participants, 
            String meetingSummary
    ) {
        try {
            Response validationResponse = validateMeetingTask(meetingTask);
            if (validationResponse.isError()) {
                return validationResponse;
            }
            
            RawDataModule rawdataModule = new RawDataModule();
            Response capturedWorkStatusLog = rawdataModule.captureWorkStatusLog(
                    WorkStatusLog.WorkStatus.IN_MEETING, 
                    null,
                    true
            );
            
            if (capturedWorkStatusLog.isError()) {
                return capturedWorkStatusLog;
            }
            
            Response taskStatusLogResponse = recordStartPauseTaskStatus(
                    meetingTask, TaskStatusLog.TaskStatus.START
            );
            
            Gson gson = new Gson();
            
            if (taskStatusLogResponse.isError()) {
                return taskStatusLogResponse;
            }
            
            String participants_ = gson.toJson(participants);
            
            Response meetingTimeLogResponse = rawdataModule.createMeetingTimeLog(
                    project, meetingTask, participants_, meetingSummary
            );
            
            JsonObject responseData = new JsonObject();
            responseData.add("WorkStatusLog", capturedWorkStatusLog.getData());
            responseData.add("TaskStatusLog", taskStatusLogResponse.getData());
            responseData.add("MeetingTimeLog", meetingTimeLogResponse.getData());
            
            return new Response(false, StatusCode.SUCCESS, "Success", responseData);
        } catch(Exception ex) {
            String errorMsg = "Application Error Occurred, Try Again";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name : endProjectMeetingTask
     * Purpose : End started meeting task and handle domain logic
     * 
     * @param projectTask 
     * @param meetingTimeLog
     * @return Response 
     */
    public Response endProjectMeetingTask(
            ProjectTask projectTask, 
            MeetingTimeLog meetingTimeLog
    ) {
        try {            
            if (meetingTimeLog == null || projectTask == null) {
                return new Response(
                        true, 
                        StatusCode.BAD_REQUEST, 
                        "Meeting time log should not be null"
                );
            }
            
            Response taskStatusLogResponse = recordStartPauseTaskStatus(
                    projectTask, TaskStatusLog.TaskStatus.BREAK
            );
            
            if (taskStatusLogResponse.isError()) {
                return taskStatusLogResponse;
            }
            
            RawDataModule rawDataModule = new RawDataModule();
            Response workStatusLogResponse = rawDataModule.captureWorkStatusLog(
                    WorkStatusLog.WorkStatus.START, null, true
            );
            
            if (workStatusLogResponse.isError()) {
                return workStatusLogResponse;
            }
                    
            Database<MeetingTimeLog> database 
                    = DatabaseProxy.openConnection(MeetingTimeLog.class);
            
            Long currentTimestamp = TimeUtility.getCurrentTimestamp();
            meetingTimeLog.setEndTimestamp(currentTimestamp);
            meetingTimeLog.setIsCompleted(true);
            meetingTimeLog.setIsSynced(false);
            database.update(meetingTimeLog);
            
            Gson gson = new Gson();
            
            JsonObject responseData = new JsonObject();
            responseData.add("WorkStatusLog", workStatusLogResponse.getData());
            responseData.add("TaskStatusLog", taskStatusLogResponse.getData());
            responseData.add("MeetingTimeLog", gson.toJsonTree(meetingTimeLog));
            
            return new Response(false, StatusCode.SUCCESS, "Success", responseData);
            
        } catch (Exception ex) {
            String errorMsg = "Application error ocurred";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        } finally {
            DatabaseProxy.closeConnection(MeetingTimeLog.class);
            SyncDataModule syncDataModule = new SyncDataModule();
            List<CompanyProject> companyProjects = _getSessionUserCompanyProjects();
            syncDataModule.syncMeetingTimeLogs(companyProjects);
        }
    }
    
    /**
     * Method Name : _getSessionUserCompanyProjects
     * Purpose : Get company projects for logged user
     
     * @return List of company project related to session user 
     */
    private List<CompanyProject> _getSessionUserCompanyProjects() {
        
        Database<CompanyProject> database = null;
        List<CompanyProject> companyProjects = new ArrayList<>();
        try {
            UserSession userSession 
                    = StateStorage.getCurrentState(StateName.USER_SESSION);
            database = DatabaseProxy.openConnection(CompanyProject.class);
            QueryBuilder<CompanyProject, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where()
                    .eq(CompanyProject.FIELD_COMPANY_ID, userSession.getCompanyId());
            companyProjects = queryBuilder.query();
            
            if (companyProjects.isEmpty()) {
                return companyProjects;
            }
            
            Map<String, CompanyProject> projectIdMapper = companyProjects
                    .stream()
                    .collect(Collectors.toMap(
                            CompanyProject::getId,
                            Function.identity()
                    ));
            try {
                Database<ProjectTask> projectTaskDB 
                        = DatabaseProxy.openConnection(ProjectTask.class);
                QueryBuilder<ProjectTask, Long> taskQueryBuilder 
                        = projectTaskDB.getQueryBuilder();
                taskQueryBuilder.where()
                        .eq(ProjectTask.FIELD_USER_ID, userSession.getId())
                        .and()
                        .in(ProjectTask.FIELD_PROJECT_ID, projectIdMapper.keySet());
                List<ProjectTask> tasks = taskQueryBuilder.query();

                tasks.forEach((var curTask) -> {
                    String projectId = curTask.getProjectId();
                    CompanyProject project = projectIdMapper.get(projectId);

                    List<ProjectTask> projectTaskList = project.getProjectTasks();
                    if (projectTaskList == null) {
                        projectTaskList = new ArrayList<>();
                        project.setProjectTasks(projectTaskList);
                    }
                    projectTaskList.add(curTask);
                });
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
            
        } catch (Exception ex) {
            String errorMsg = "Unable to retrieve company projects";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        } finally {
            DatabaseProxy.closeConnection(CompanyProject.class);
        }
        
        return companyProjects;
    }
    
    /**
     * Method Name : validateMeetingTask
     * Purpose : Validate Meeting task
     * 
     * @param projectTask
     * @param participants
     * @return Response 
     */
    private Response validateMeetingTask(ProjectTask task) {
        
        if (task == null) {
            return new Response(
                    true, 
                    StatusCode.VALIDATION_ERROR, 
                    "Application identified task is null value, please try again"
            );
        }
        
        String taskType = task.getType();
        if (!taskType.equals(ProjectTask.MEETING_TASK_TYPE)) {
            return new Response(
                    true, StatusCode.VALIDATION_ERROR, "Task is not valid type"
            );
        }
        return new Response(false, StatusCode.SUCCESS, "Success");
    }
    
    /**
     * Method Name : updateTaskSpendTime
     * Purpose : Update Task spend time
     * 
     * @param projectTask
     * @return Response 
     */
    public Response updateTaskSpendTime(ProjectTask projectTask) {
        try {
            projectTask.setIsDirty(true); projectTask.setIsSynced(false);
            
            UserSession userSession = 
                    StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);
            
            HttpRequestCaller _httpRequestCaller = new HttpRequestCaller();
            Response apiResponse = _httpRequestCaller
                    .putUserTaskAssignment(userSession.getId(), projectTask);
            
            if (!apiResponse.isError()) {
                projectTask.setIsDirty(false); projectTask.setIsSynced(true);
            }
            
            
            try {
                Database<ProjectTask> database 
                    = DatabaseProxy.openConnection(ProjectTask.class);
                Response response = database.update(projectTask);
                
                if (response.isError()) {
                    return response;
                }
                
                Gson gson = new Gson();
                return new Response(
                        false, 
                        StatusCode.SUCCESS, 
                        "Task spend time is successfully updated",
                        gson.toJsonTree(projectTask)
                );
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
        } catch (Exception ex) {
            String errorMsg = "Error occurred, Please retry again";
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Unable to update task spent time", 
                    ex
            );
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
}
