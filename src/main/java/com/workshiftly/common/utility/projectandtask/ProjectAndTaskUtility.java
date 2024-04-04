/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.projectandtask;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.ApiEntityStatus;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.Project;
import com.workshiftly.common.model.ProjectAndTaskWebRespond;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Task;
import com.workshiftly.common.model.TaskStatusLog;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.concurrent.ThreadExecutorService;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author hashan
 */
public class ProjectAndTaskUtility {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(ProjectAndTaskUtility.class);
    
    Gson GSON = new Gson();
    TaskStatusLogUtility taskStatusLogUtility;
    HttpRequestCaller httpRequestCaller;

    private static ProjectAndTaskUtility singleton;

    public static ProjectAndTaskUtility getInstance() {
        if (singleton == null) {
            singleton = new ProjectAndTaskUtility();
        }

        return singleton;
    }

    public ProjectAndTaskUtility() {
        
        taskStatusLogUtility = TaskStatusLogUtility.getInstance();
        httpRequestCaller = new HttpRequestCaller();
    }
    
    public Response recordTaskStatus(ProjectTask task, long actionTime, TaskStatusLog.TaskStatus taskStatus) throws Exception {
        Response response;

        if (taskStatus == TaskStatusLog.TaskStatus.START) {
            response = taskStatusLogUtility.recordTaskStatusLog(task, actionTime, taskStatus);

            if (response.isError()) {
                return response;
            }

            StateStorage.set(StateName.CURRENT_TASK, ProjectTask.class, task);                
        } else {
            TaskStatusLog lastTaskStatusLog = StateStorage.getCurrentState(StateName.LAST_TASK_STATUS_LOG);

            if (lastTaskStatusLog == null || !lastTaskStatusLog.getTaskId().equals(task.getId())) {
                return new Response(true, StatusCode.BAD_REQUEST, "Requested task is not equal with last task status log");
            }

            response = taskStatusLogUtility.recordTaskStatusLog(task, actionTime, taskStatus);

            if (response.isError()) {
                return response;
            }

            long spendTime = actionTime - lastTaskStatusLog.getActionTimestamp();
            long fullSpendTime = task.getSpentTime() + spendTime;
            task.setSpentTime(fullSpendTime);

            if (taskStatus == TaskStatusLog.TaskStatus.STOP) {
                task.setHasCompleted(true);
            }

            response = httpRequestCaller.callUpdateTaskSpendTime(task);
            if (response.isError()) {
                task.setIsDirty(true);
            }

            try {
                Database<ProjectTask> database 
                        = DatabaseProxy.openConnection(ProjectTask.class);
                response = database.create(task);                
            } catch (Exception e) {
                throw e;
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }

            if (response.isError()) {
                return response;
            }

            StateStorage.set(StateName.CURRENT_TASK, ProjectTask.class, null);
        }

        return new Response(false, StatusCode.SUCCESS, "Task status has been saved successfully");
    }

    public List<ProjectTask> getDirtyTaskList () throws SQLException, Exception {
        try {
            Database<ProjectTask> database = DatabaseProxy.openConnection(ProjectTask.class);
            List<ProjectTask> dirtyTaskList = database.getQueryBuilder()
                    .where().eq(Task.FIELD_IS_DIRTY, true)
                    .query();
            return dirtyTaskList;            
        } catch (Exception e) {
            throw e;
        } finally {
            DatabaseProxy.closeConnection(ProjectTask.class);
        }

    }

    public Response updateDirtyTaskList() throws SQLException, Exception {
        List<ProjectTask> dirtyTaskList = getDirtyTaskList();

        if (!dirtyTaskList.isEmpty()) {
            int dirtyTaskCount = dirtyTaskList.size();
            
            try {
                Database<ProjectTask> database = DatabaseProxy.openConnection(ProjectTask.class);

                dirtyTaskList.forEach((task) -> {
                    Response updateResponse = httpRequestCaller.callUpdateTaskSpendTime(task);

                    if (!updateResponse.isError()) {
                        task.setIsDirty(false);
                        database.update(task);
                    }
                });                
            } catch (Exception e) {
                throw e;
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }

            int failedTaskCount = getDirtyTaskList().size();

            if (failedTaskCount > 0) {
                String msg = "Fail to update " + failedTaskCount + " out of " + dirtyTaskCount + " tasks.";
                return new Response(true, StatusCode.SYNC_ERROR, msg);
            } else {
                String msg = "Successfully update " + dirtyTaskCount + " tasks.";
                return new Response(false, StatusCode.SUCCESS, msg);
            }
        }

        return new Response(false, StatusCode.SUCCESS, "There are no task changes to update");
    }

    public Response saveProjectAndTaskRecord () {
        
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        try {
            AppValidator.validateUserSession(userSession);
        } catch (AuthenticationException ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "AuthenticationException occurred while saving Project And Task Record", ex);
            return new Response(true, StatusCode.SESSION_INVALID, "Session invalidated");
        }
            
        Response apiResponse = httpRequestCaller.callGetProjectAndTaskList(userSession.getId(), null);
        if (apiResponse.isError()) {
            return apiResponse;
        }
        
        JsonElement data = apiResponse.getData();
        Type listType = new TypeToken<List<ProjectAndTaskWebRespond>>(){}.getType();
        List<ProjectAndTaskWebRespond> projectAndTaskEntities = GSON.fromJson(data, listType);
        Response response = saveProjectAndTaskByWebResponse(projectAndTaskEntities, userSession);
        return response;
    }
    
    private Response saveFetchedRemoteProjects(Map<String, Project> projects, String userId) {
        try {
            Database<Project> database = DatabaseProxy.openConnection(Project.class);
            QueryBuilder<Project, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where().eq(Project.FIELD_USER_ID, userId);
            List<Project> projectList = queryBuilder.query();
            
            Map<String, Project> existingProjects = new HashMap<>();
            projectList.stream().forEach((Project curProject) -> {
                String projectId = curProject.getId();
                if (!existingProjects.containsKey(projectId)) {
                    existingProjects.put(projectId, curProject);
                }
            });

            if (!existingProjects.isEmpty()) {
                Response response = database.delete(existingProjects.values());
                if (response.isError()) {
                    return response;
                }
            }
            
            List<Project> processedProjects = new ArrayList<>();
            Set<Map.Entry<String, Project>> entrySet = projects.entrySet();

            entrySet.stream().map((currentEntry) -> {
                
                String projectId = currentEntry.getKey();
                Project project = currentEntry.getValue();
                
                boolean isExistingProject = existingProjects.containsKey(projectId);
                Project removedProject = existingProjects.remove(projectId);
                
                ApiEntityStatus apiEntityStatus = isExistingProject ? ApiEntityStatus.UPDATED : ApiEntityStatus.CREATED;
                project.setApiEntityStatus(apiEntityStatus);
                return project;
            }).forEachOrdered((project) -> {
                processedProjects.add(project);
            });

            Collection<Project> deletedProjects = existingProjects.values();
            deletedProjects.forEach((Project project) -> {
                project.setApiEntityStatus(ApiEntityStatus.DELETED);
                processedProjects.add(project);
            });
            
            Response response = database.create(processedProjects);
            return response;
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while saving projects", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Successfully persisted projects into database");   
        } finally {
            DatabaseProxy.closeConnection(Project.class);
        }
    }
    
    private Response saveFetchRemoteTasks(Map<String, Task> tasks, String userId) {
        
        try {
            Database<Task> database = DatabaseProxy.openConnection(Task.class);
            QueryBuilder<Task, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where().eq(Task.FIELD_USER_ID, userId);
            
            Map<String, Task> existingTasks = queryBuilder.query()
                    .stream()
                    .collect(Collectors.toMap(Task::getId, Function.identity()));
            
            if (!existingTasks.isEmpty()) {
                Response response = database.delete(existingTasks.values());
                if (response.isError()) {
                    return response;
                }
            }
            
            List<Task> processedTasks = new ArrayList<>();
            Set<Map.Entry<String, Task>> taskEntrySet = tasks.entrySet();
            
            taskEntrySet.stream().map((currentEntry) -> {
                
                String taskId = currentEntry.getKey();
                Task task = currentEntry.getValue();
                
                boolean isExistingTask = existingTasks.containsKey(taskId);
                existingTasks.remove(taskId);
                
                ApiEntityStatus apiEntityStatus = isExistingTask ? ApiEntityStatus.UPDATED : ApiEntityStatus.CREATED;
                task.setApiEntityStatus(apiEntityStatus);
                
                return task;                
            }).forEachOrdered((task) -> {
                processedTasks.add(task);
            });
            
            Collection<Task> deletedTasks = existingTasks.values();
            deletedTasks.forEach((Task task) -> {
                task.setApiEntityStatus(ApiEntityStatus.DELETED);
                processedTasks.add(task);
            });
            
            Response response = database.create(processedTasks);
            return response;
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while saving tasks", ex);
            return new Response(true, StatusCode.SUCCESS, "Successfully persisted tasks into database");
        } finally {
            DatabaseProxy.closeConnection(Task.class);
        }
    }
    
    public Response refreshProjectAndTaskList () throws SQLException, Exception {
        
        Response response = updateDirtyTaskList();
        if (response.isError()) {
            return response;
        }
        
        response = saveProjectAndTaskRecord();
        if (response.isError()) {
            return response;
        }
        
        return new Response(false, StatusCode.SUCCESS, "Successfully refresh project and task");
    }
    
    public List<Task> getTaskListByProductId (String productId) throws Exception {
        try {
            Database<Task> database = DatabaseProxy.openConnection(Task.class);

            QueryBuilder<Task, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where()
                    .eq(Task.FIELD_PROJECT_ID, productId)
                    .and()
                    .eq(Task.FIELD_HAS_COMPLETED, false);
            return queryBuilder.query();            
        } catch (Exception e) {
            throw e;
        } finally {
            DatabaseProxy.closeConnection(Task.class);
        }
    }
    
    public List<CompanyProject> getProjectsByUserId(String userId) throws Exception {
        
        try {
            List<ProjectTask> userAssignedTasks = new ArrayList<>();
            try {
                Database<ProjectTask> projectTaskDB = DatabaseProxy
                        .openConnection(ProjectTask.class);
                QueryBuilder<ProjectTask, Long> queryBuilder 
                        = projectTaskDB.getQueryBuilder();
                queryBuilder.where()
                        .eq(ProjectTask.FIELD_USER_ID, userId)
                        .and()
                        .eq(ProjectTask.FIELD_HAS_COMPLETED, false);
                userAssignedTasks = queryBuilder.query();
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
            
            Map<String, List<ProjectTask>> taskMap = new HashMap<>();
            userAssignedTasks.forEach((ProjectTask task) -> {
                String projectId = task.getProjectId();
                
                if (!taskMap.containsKey(projectId)) {
                    List<ProjectTask> taskList = new ArrayList<>();
                    taskMap.put(projectId, taskList);
                }
                taskMap.get(projectId).add(task);
            });
            
            Set<String> projectIds = taskMap.keySet();
            
            List<CompanyProject> userAssignedProjects;
            try {
                Database<CompanyProject> projectDB = DatabaseProxy
                        .openConnection(CompanyProject.class);
                QueryBuilder<CompanyProject, Long> queryBuilder 
                        = projectDB.getQueryBuilder();
                Where<CompanyProject, Long> whereCondition = queryBuilder.where();
                whereCondition.eq(CompanyProject.FIELD_TYPE, CompanyProject.GENERAL_PROJECT_TYPE);
                
                if (!projectIds.isEmpty()) {
                    whereCondition.or().in(CompanyProject.FIELD_ID, projectIds);
                }
                
                userAssignedProjects = whereCondition.query();
            } finally {
                DatabaseProxy.closeConnection(CompanyProject.class);
            }
            
            userAssignedProjects.forEach((CompanyProject project) -> {
                List<ProjectTask> projectTasks 
                        = taskMap.getOrDefault(project.getId(), new ArrayList<>());
                project.setProjectTasks(projectTasks);
            });
            return userAssignedProjects;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public ProjectTask getTaskById(String id, String userId) throws Exception {
        
        try {
            Database<ProjectTask> database 
                    = DatabaseProxy.openConnection(ProjectTask.class);
            QueryBuilder<ProjectTask, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where()
                    .eq(ProjectTask.FIELD_ID, id)
                    .and()
                    .eq(ProjectTask.FIELD_USER_ID, userId);
            
            return queryBuilder.queryForFirst();
        } catch (Exception ex) {
            throw ex;
        } finally {
            DatabaseProxy.closeConnection(ProjectTask.class);
        }
    }

    public Response updateTask(ProjectTask task) {
        
        Response response;
        try {
            // mark task is dirty
            task.setIsDirty(false);
            task.setIsSynced(true);
            
            UserSession userSession = 
                    StateStorage.getCurrentState(StateName.USER_SESSION);
            
            AppValidator.validateUserSession(userSession);
            Response apiResponse = httpRequestCaller
                    .putUserTaskAssignment(userSession.getId(), task);
            
            if (apiResponse.isError()) {
                task.setIsDirty(true); task.setIsSynced(false);
            }
            
            Database<ProjectTask> database = DatabaseProxy.openConnection(ProjectTask.class);
            response = database.update(task);
            
            if (response.isError()) {
                return response;
            }
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to update task", ex);
            return new Response(true, StatusCode.DATABASE_EXCEPTION, "Failed to update task");
        } finally {
            DatabaseProxy.closeConnection(ProjectTask.class);
        }
        
//        asyncUpdateTaskAtRemoteDatabase(task);
        return response;
    }
    
    private void asyncUpdateTaskAtRemoteDatabase(ProjectTask updatedTask) {
        
        Runnable runnable = () -> {
            updatedTask.setIsDirty(false); updatedTask.setIsSynced(true);

            Response apiResonse = httpRequestCaller.callUpdateTaskSpendTime(updatedTask);

            if (!apiResonse.isError()) {
                updatedTask.setIsDirty(true); updatedTask.setIsSynced(false);
            }

            try {
                Database<ProjectTask> database = DatabaseProxy.openConnection(ProjectTask.class);
                database.update(updatedTask);
            } catch (Exception ex) {
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to persist dirty task into database", ex);
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
        };
        ThreadExecutorService.executeTask(runnable);
    }
    
    public Response refreshProjectAndTask(UserSession userSession) {
        
        Long lastTaskUpdatedTimestamp  
                = StateStorage.getCurrentState(StateName.LATEST_TASK_UPDATED_TIMESTAMP);
        
        try {
            if (lastTaskUpdatedTimestamp == null) {
                Task localLastUpdatedTask = getLatestUpdatedTaskFromLocalDB(userSession);
                lastTaskUpdatedTimestamp = localLastUpdatedTask != null ? localLastUpdatedTask.getUpdatedAt() : null;

                if (localLastUpdatedTask != null)
                    lastTaskUpdatedTimestamp = localLastUpdatedTask.getUpdatedAt();
            }
            
            Response apiResponse = httpRequestCaller.callGetProjectAndTaskList(userSession.getId(), lastTaskUpdatedTimestamp);
            if (apiResponse.isError()) {
                return apiResponse;
            }
        
            JsonElement data = apiResponse.getData();
            Type listType = new TypeToken<List<ProjectAndTaskWebRespond>>(){}.getType();
            List<ProjectAndTaskWebRespond> projectAndTaskEntities = GSON.fromJson(data, listType);
            
            Response response = saveProjectAndTaskByWebResponse(projectAndTaskEntities, userSession);
            if (response.isError()) {
                return response;
            }
            
            Type projectListType = new TypeToken<List<Project>>(){}.getType();
            Gson gson = new Gson();
            
            List<CompanyProject> userAssignedProjects = getProjectsByUserId(userSession.getId());
            JsonElement projectListJson = gson.toJsonTree(userAssignedProjects, projectListType);
            
            return new Response(false, StatusCode.SUCCESS, "Successfully refreshed project and tasks", projectListJson);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to refresh project and tasks", ex);
            return new Response(false, StatusCode.APPLICATION_ERROR, "Failed to refresh project and tasks");
        }
    }
    
    private Response saveProjectAndTaskByWebResponse(List<ProjectAndTaskWebRespond> webEntities, UserSession userSession) {
        
        Map<String, Project> projectMap = new HashMap<>();
        Map<String, Task> taskMap = new HashMap<>();
        
        for (ProjectAndTaskWebRespond currentEntity : webEntities) {
            
            String projectId = currentEntity.getProjectId();
            boolean hasEntryInProjectMap = projectMap.containsKey(projectId);
            
            if (!hasEntryInProjectMap) {
                Project project = new Project();
                project.setId(projectId);
                project.setName(currentEntity.getProjectName());
                project.setCode(currentEntity.getProjectCode());
                project.setUserId(userSession.getId());
                
                projectMap.put(projectId, project);
            }
            
            Task task = new Task();
            task.setId(currentEntity.getTaskId());
            task.setProjectId(currentEntity.getProjectId());
            task.setTitle(currentEntity.getTaskTitle());
            task.setDueDate(currentEntity.getTaskDueDate());
            task.setEstimation(currentEntity.getTaskEstimate());
            task.setSpentTime(currentEntity.getSpentTime());
            task.setUserId(userSession.getId());
            task.setCreatedAt(currentEntity.getCreatedAt());
            task.setUpdatedAt(currentEntity.getUpdatedAt());
            taskMap.put(task.getId(), task);
        }
        
        Response response = saveFetchedRemoteProjects(projectMap, userSession.getId());
        if (response.isError()) {
            return response;
        }
        
        response = saveFetchRemoteTasks(taskMap, userSession.getId());
        if (response.isError()) {
            return response;
        }
        
        return new Response(false, StatusCode.SUCCESS, "Successfully save project and task records.");
    }
    
    private Task getLatestUpdatedTaskFromLocalDB(UserSession userSession) throws Exception {
        
        try {
            Database<Task> taskDatabase = DatabaseProxy.openConnection(Task.class);
            QueryBuilder<Task, Long> queryBuilder = taskDatabase.getQueryBuilder();
            queryBuilder.where().eq(Task.FIELD_USER_ID, userSession.getId());
            queryBuilder.orderBy(Task.FIELD_UPDATED_AT, false);

            Task latestUpdatedTask = queryBuilder.queryForFirst();
            return latestUpdatedTask;
        } finally {
            DatabaseProxy.closeConnection(Task.class);
        }
    }
    
    public Response getRemoteProjectAndTask(String employeeId) {
        try {
            HttpRequestCaller httpRequsCaller = new HttpRequestCaller();
            Map<String, String> queryOptions = new HashMap<>();
            queryOptions.put("withtasks", "true");
            Response apiResponse; 
            apiResponse = httpRequsCaller
                    .callGetProjectsByEmployeeId(employeeId, queryOptions);
            
            if (apiResponse.isError()) {
                return apiResponse;
            }
            
            JsonElement responseData = apiResponse.getData();
            if (!responseData.isJsonArray()) {
                return new Response(
                        true, StatusCode.VALIDATION_ERROR, "Invalid response data"
                );
            }
            
            Gson gson = new Gson();
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
            
            Database<CompanyProject> projectDB;
            try {
                projectDB = DatabaseProxy.openConnection(CompanyProject.class);
                // TODO: refactor this
                projectDB.flushData();
                Response dbResponse = projectDB.create(assignedProjects);
                
                if (dbResponse.isError()) {
                    return dbResponse;
                }
            } finally {
                DatabaseProxy.closeConnection(CompanyProject.class);
            }
            
            List<ProjectTask> userTasks = new ArrayList<>();
            assignedProjects.forEach((CompanyProject project) -> {
                List<ProjectTask> projectTasks = project.getProjectTasks();
                
                if (projectTasks != null) {
                    userTasks.addAll(projectTasks);
                }
            });
            
            Database<ProjectTask> projectTaskDB;
            try {
                projectTaskDB = DatabaseProxy.openConnection(ProjectTask.class);
                projectTaskDB.flushData();
                Response dbResponse = projectTaskDB.create(userTasks);
                
                if (dbResponse.isError()) {
                    return dbResponse;
                }
            } finally {
                DatabaseProxy.closeConnection(ProjectTask.class);
            }
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Success",
                    gson.toJsonTree(assignedProjects)
            );
        } catch (Exception ex) {
            InternalLogger.LOGGER_LEVEL logLevel = InternalLogger.LOGGER_LEVEL.SEVERE;
            String errorMsg = "Failed to Retrieve Projects";
            LoggerService.LogRecord(this.getClass(), errorMsg, logLevel, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Failed to Retrieve Projects");
        }
    }
}
