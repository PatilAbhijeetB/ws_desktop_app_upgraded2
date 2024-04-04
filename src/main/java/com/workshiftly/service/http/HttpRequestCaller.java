/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.service.http;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.HttpMethod;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.CompanyProject;
import com.workshiftly.common.model.HttpRequest;
import com.workshiftly.common.model.MeetingTimeLog;
import com.workshiftly.common.model.ProjectTask;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.WebBrowserLog;
import com.workshiftly.common.model.WorkStatusLog;
import com.workshiftly.presentation.viewmodel.LoginViewModelNew;
import com.workshiftly.service.http.adapter.HttpAdapter;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.InternalLogger.LOGGER_LEVEL;
import com.workshiftly.service.logger.LoggerService;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/***
 * 
 * 
 */
public final class HttpRequestCaller {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(HttpRequestCaller.class);
    private static final Gson GSON = new Gson(); 
    private static final LOGGER_LEVEL LOG_LEVEL = LOGGER_LEVEL.SEVERE; 
    
    public Response callUserLogin(JsonObject loginDetails) {
        try {
            //String[] pathSegements = {"client-login"};
            String[] pathSegements = {"client-login-mac"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("email", loginDetails.get("username").getAsString());
            requestBody.addProperty("password", loginDetails.get("password").getAsString());
              requestBody.addProperty("macAddress", loginDetails.get("macAddress").getAsString());
              requestBody.addProperty("ipAddress", loginDetails.get("ipAddress").getAsString());
              requestBody.addProperty("machineName", loginDetails.get("machineName").getAsString());
             requestBody.addProperty("machineUserName", loginDetails.get("machineUserName").getAsString());
             requestBody.addProperty("platform", loginDetails.get("platform").getAsString());
              requestBody.addProperty("version", loginDetails.get("version").getAsString());
              requestBody.addProperty("osName", loginDetails.get("osName").getAsString());
              requestBody.addProperty("osVersionMajor", loginDetails.get("osVersionMajor").getAsString());
             requestBody.addProperty("osVersionMinor", loginDetails.get("osVersionMinor").getAsString());
              requestBody.addProperty("isForceLogoutDevices", loginDetails.get("isForceLogoutDevices").getAsString());
             
              
              
              
              
              
              
              
            httpRequest.setBody(requestBody);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
            
        } catch (Exception ex) {
            System.out.println("#### httpAdapter Exceptions -----> " + ex.getMessage());
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Error occured while connecting api - callUserLogin", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occured while connecting api"
            );
        }
    }
    
    /**
     * Method Name: callUserChanagePassword
     * 
     * Description: Make http call to change user password.
     * Error statusCode will be are followings
     * APPLICATION_ERROR - indicate application related user session errors and exceptions
     * NETWORK_ERROR - http adapters exceptions and api request failures
     * 
     * @param inputData
     * @return 
     */
    public Response callUserChanagePassword(JsonObject inputData) {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            if (userSession == null) {
                return new Response(true, StatusCode.APPLICATION_ERROR, "User should be logged first to change password");
            }
            
            String userId = userSession.getId();
            if (userId == null || userId.isBlank()) {
                return new Response(true, StatusCode.APPLICATION_ERROR, "User session does not contain user id");
            }
            
            JsonObject requestData = new JsonObject();
            requestData.addProperty("currentPassword", inputData.get("currentPassword").getAsString());
            requestData.addProperty("newPassword", inputData.get("password").getAsString());
            
            String[] pathSegments = {"users", userId, "change-client-password"};
            
            HttpRequest httpRequest = new HttpRequest(HttpMethod.PATCH, pathSegments);
            httpRequest.setBody(requestData);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occured while connecting api - callUserChanagePassword", ex);
            return new Response(true, StatusCode.NETWORK_ERROR, "Error occurred while connecting api");
        }
    }
    
    /**
     * Method Name: callgetUTCtimestamp
     * 
     * Description: Retrieve UTC timestamp from remote API
     * Error statusCode will be are followings
     * APPLICATION_ERROR - indicate application related user session errors and exceptions
     * NETWORK_ERROR - http adapters exceptions and api request failures
     * 
     * @param inputData
     * @return 
     */
    public Response callgetUTCtimestamp() {
        try {
            String[] pathSegments = {"server-current-timestamp"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegments);
            Response apiResponse = HttpAdapter.request(httpRequest);

            return apiResponse;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while getting utc timestamp", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while getting utc timestamp");
        }
    }
    
    /**
     * Method Name: callForgetPasswordRequest
     * 
     * Description: Make HTTP request to change password by forget password scenario
     * Error statusCode will be are followings
     * 
     * @param inputData
     * @return 
     */
    public Response callForgetPasswordRequest(JsonObject inputData) {
        try {
            String[] pathSegments = { "reset-client-password" };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegments);
            httpRequest.setBody(inputData);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while calling http request - callForgetPasswordRequest", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while calling http request");
        }
    }
    
    /**
     * Method Name: callGetProjectAndTaskList
     *
     * Description: Make HTTP request to get project and task list
     * scenario Error statusCode will be are followings
     *
     * @param inputData
     * @return
     */
    public Response callGetProjectAndTaskList(String userId, Long latestUpdatedTaskTimestamp) {
        
        try {
            String[] pathSegments = {"users", userId, "projects-with-tasks"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegments);
            
            if (latestUpdatedTaskTimestamp != null) {
                httpRequest.addQueryParameter("lastUpdatedTimestamp", latestUpdatedTaskTimestamp.toString());
            }
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while calling http request - callGetProjectAndTaskList", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while calling http request");
        }
    }
    
    /**
     * Method Name: callGetProjectsByEmployeeId
     *
     * Description: Make HTTP request to get project and task list
     * scenario Error statusCode will be are followings
     *
     * @param employeeId 
     * @param queryOptions 
     * @return
     */
    public Response callGetProjectsByEmployeeId(String employeeId, Map<String, String> queryOptions) {
        try {
            String[] pathSegements = {"users", employeeId, "projects"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            
            if (queryOptions != null) {
                Set<Map.Entry<String, String>> entrySet = queryOptions.entrySet();
                entrySet.forEach((Map.Entry<String, String> curEntry) -> {
                    httpRequest.addQueryParameter(curEntry.getKey(), curEntry.getValue());
                });
            }
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            LOGGER.logRecord(LOG_LEVEL, "Error occurred while retrieving projects", ex);
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occurred while retrieving projects"
            );
        }
    }
        
    /**
     * Method Name: callGetCompany 
     * Description: Make HTTP request to get company instance from API
     * 
     * @param inputData
     * @return Response
     */
    public Response callGetCompany(String comapnyId) {
        
        try {
            String[] pathSegements = { "companies", comapnyId };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String message = "Error occurred while retrieving company from api";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }
    
    /**
     * Method Name: callGetProjectAndTaskList
     *
     * Description: Make HTTP request to get project and task list scenario
     * Error statusCode will be are followings
     *
     * @param inputData
     * @return
     */
    public Response callUpdateTaskSpendTime(ProjectTask task) {
        try {
            String taskId = task.getId();
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("spentTime", task.getSpentTime());
            requestBody.addProperty("hasCompleted", task.isHasCompleted());

            String[] pathSegments = {"tasks", taskId, "update-task-spent-time"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.PUT, pathSegments);
            httpRequest.setBody(requestBody);

            Response response = HttpAdapter.request(httpRequest);
            return response;
            
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while calling http request - callUpdateTaskSpendTime", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while calling http request");
        }
    }

    /**
     * Method Name: callGetCompanyConfigurations
     * Description: Make HTTP request to get company configurations object
     * 
     * @param companyId
     * @param configurationId 
     * @return Response
     */
    public Response callGetCompanyConfigurations(String companyId, String configurationId) {
        try {
            String[] pathSegements = { "companies", companyId, "configurations", configurationId };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String message = "Error occurred while retrieving company configurations";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }
    
    /**
     * Method Name: callPostSyncActiveWindows
     * Description: Make post request to send ActiveWindows to API
     * 
     * @param inputData
     * @return Response
     */
    public Response callPostSyncActiveWindows(String userId, List<ActiveWindow> activeWindows) {
        
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ActiveWindow>>(){}.getType();
            
            String[] pathSegments = { "users", userId, "active-windows" };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegments);
            httpRequest.setBody(gson.toJsonTree(activeWindows, listType));
            
            Response response = HttpAdapter.request(httpRequest);
            System.out.println("### apiResponse ----> " + gson.toJson(response));
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync active windows", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "failed to sync active windows");
        }
    }
    
    /**
     * Method Name: callPostSyncActiveWindows
     * Description: Make post request to send Screenshots to API
     * 
     * @param session 
     * @param metaObjects 
     * @return Response
     */
    public Response callGetScreenshotUploadPresignedURLs(UserSession session, JsonArray metaObjects) {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("companyId", session.getCompanyId());
            requestData.add("metaObjects", metaObjects);
            
            String[] pathSegments = { "users", session.getId(), "screenshots" };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegments);
            httpRequest.setBody(requestData);
            
            Response response = HttpAdapter.request(httpRequest);
            return response;

        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync screenshots", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "failed to sync screenshots");
        }
    }
    
    /**
     * Method Name: callPostSyncScreenshots
     * Description: Synced storage upload completed screenshots
     * 
     * @param usersession  
     * @param screenshots  
     * @return Response
     */
    public Response callPostSyncScreenshots(UserSession usersession, List<Screenshot> screenshots) {
        try {
            screenshots.forEach((var obj) -> {
                obj.setData(null);
            });
            String[] pathSegements = { "users", usersession.getId(), "screenshots" };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.PUT, pathSegements);
            
            // set requestbody
            Gson gson = new Gson();
            JsonElement requestData = gson.toJsonTree(screenshots);
            httpRequest.setBody(requestData);
            
            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            String errorMsg = "Error occurred while syncing screenshots";
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name: callPostSyncWebBrowserLog
     * Description: Make post request to send Captured URLs to API
     * 
     * @param inputData
     * @return Response
     */
    public Response callPostSyncWebBrowserLog(String userId, List<WebBrowserLog> webBrowserLogs) {
        
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<WebBrowserLog>>(){}.getType();
            
            String[] pathSegments = { "users", userId, "web-browser-logs" };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegments);
            httpRequest.setBody(gson.toJsonTree(webBrowserLogs, listType));
            
            Response response = HttpAdapter.request(httpRequest);
            System.out.println("### apiResponse web-browser-logs ----> " + userId + "  " + gson.toJson(response));
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync urls", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "failed to sync urls");
        }
    }
    
    /**
     * Method Name: callPostSyncWorkStatusLogs
     * Description: Make post request to send workStatusLogs to API
     * 
     * @param inputData
     * @return Response
     */
    public Response callPostSyncWorkStatusLogs(String userId, List<WorkStatusLog> workStatusLogs) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<WorkStatusLog>>(){}.getType();
            
            String[] pathSegements = {"users", userId, "task-status-log"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            httpRequest.setBody(gson.toJsonTree(workStatusLogs, listType));
            
            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to sync screenshots", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Failed to sync workStatusLog");
        }
    }
    
    /**
     * Method Name: callGetUserWorkSchedules Description: Make HTTP request
     * to get user work schedules object
     *
     * @param inputData
     * @return Response
     */
    public Response callGetUserWorkSchedules(String userId) {
        try {
            String[] pathSegements = {"users", userId, "work-schedules"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String message = "Error occurred while retrieving user work schedules";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }

    /**
     * Method Name: callGetUserAttendanceData Description: Make HTTP request
     * to get user attendance data object
     *
     * @param companyId
     * @param userId
     * @param from
     * @param to
     * @param today
     * @return Response
     */
    public Response callGetUserAttendanceData(String companyId, String userId, String from, String to, String today) {
        try {
            String[] pathSegements = {"reports", "attendance"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);

            httpRequest.addQueryParameter("companyId", companyId);
            httpRequest.addQueryParameter("from", from);
            httpRequest.addQueryParameter("to", to);
            httpRequest.addQueryParameter("filterType", "users");
            httpRequest.addQueryParameter("filterIds", userId);
            httpRequest.addQueryParameter("range", "day");
            httpRequest.addQueryParameter("today", today);

            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String message = "Error occurred while retrieving user attendance data.";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }

    /**
     * Method Name: callGetUserAttendanceData Description: Make HTTP request to
     * get user attendance data object
     *
     * @param companyId
     * @param userId
     * @param from
     * @param to
     * @return Response
     */
    public Response callGetUserActivitySummaryData(String companyId, String userId, String from, String to) {
        try {
            String[] pathSegements = {"reports", "application-activity"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            //HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            httpRequest.addQueryParameter("companyId", companyId);
            httpRequest.addQueryParameter("from", from);
            httpRequest.addQueryParameter("to", to);
            httpRequest.addQueryParameter("filterType", "users");
            httpRequest.addQueryParameter("filterIds", userId);
            httpRequest.addQueryParameter("page", "0");
            httpRequest.addQueryParameter("limit", "5");

            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String message = "Error occurred while retrieving user attendance data.";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }

    /**
     * Method Name: callPostSyncActiveWindows Description: Make post request
     * to send Screenshots to API
     *
     * @param userId
     * @return Response
     */
    public Response callPostUserAttendance(String userId) {
        try {
            String[] pathSegments = {"users", userId, "attendance"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegments);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("userId", userId);
            httpRequest.setBody(requestBody);

            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "failed to mark user attendance", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "failed to mark user attendance");
        }
    }

    
    /**
     * Method Name: callPostCreateWorkStatusLogs Description: Make post request
     * to send workStatusLogs to API and data stored in web db without queue
     * process
     *
     * @param userId
     * @param workStatusLogs
     * @return Response
     */
    public Response callPostCreateWorkStatusLogs(String userId, List<WorkStatusLog> workStatusLogs) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<WorkStatusLog>>() {
            }.getType();

            String[] pathSegements = {"users", userId, "work-status-log"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            httpRequest.setBody(gson.toJsonTree(workStatusLogs, listType));
            
            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to store workStatusLog in web", ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Failed to store workStatusLog in web");
        }
    }
    
    public Response callUserRecoveryService(String userId, Map<String, String> queryParams) {
        try {
            String[] pathSegements = {"users", userId, "client-app-recovery-status"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            queryParams.forEach(httpRequest::addQueryParameter);
            
            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Failed to call user recovery service API", 
                    ex
            );
            return new Response(true, StatusCode.APPLICATION_ERROR, "Failed to call user recovery service API");
        }
    }
    
    /**
     * Method Name: getLatestUserTOCAcceptance 
     * Description: Query user related term of condition acceptance from API
     *
     * @param userId
     * @return Response
     */
    public Response getLatestUserTOCAcceptance(final String userId) {
        try {
            String[] pathSegements = {"users", userId, "term-and-condition-acceptance"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Failed to fetch TOC acceptance", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Failed to fetch TOC acceptance"
            );
        }
    }
    
    /**
     * Method Name: PostUserTOCAcceptance 
     * Description: Post user related term of condition acceptance to API
     *
     * @param userId
     * @param action 
     * @param version 
     * @return Response
     */
    public Response PostUserTOCAcceptance(
            final String userId, final String action, final String version
    ) {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("action", action);
            requestData.addProperty("version", version);
            
            String[] pathSegements = {"users", userId, "term-and-condition-acceptance"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            httpRequest.setBody(requestData);
            Response response = HttpAdapter.request(httpRequest);
            return response;  
        } catch (Exception ex) {
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Failed to proceed TOC acceptance", 
                    ex
            );
            return new Response(
                    true,
                    StatusCode.APPLICATION_ERROR,
                    "Failed to proceed TOC acceptance"
            );
        }
    }
    
    /**
     * Method Name: PostCreateMeetingTask 
     * Description: Post Request to API to create meeting task for a project
     *
     * @param project 
     * @param session 
     * @return Response
     */
    public Response PostCreateMeetingTask(CompanyProject project, UserSession session) {
        
        try {
            String[] pathSegments = {
                "projects", project.getId(), 
                "employees", session.getId(), 
                "meeting-tasks"
            };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegments);
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("companyId", session.getCompanyId());
            httpRequest.setBody(requestBody);
            
            return HttpAdapter.request(httpRequest);
        } catch (Exception ex) {
            String errorMsg = "Unable to create meeting task for project, "
                    + "try again";
            LOGGER.logRecord(LOG_LEVEL, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
        
    }
    
    /**
     * Method Name: PostCreateMeetingTask 
     * Description: Post Request to API to create meeting task for a project
     *
     * @param projectTask 
     * @param userSession 
     * @return Response
     */
    public Response postCreateOfflineTask(ProjectTask projectTask, UserSession userSession) {
        try {
            String[] pathSegements = {
                "projects", projectTask.getProjectId(),
                "employees", userSession.getId(),
                "offline-tasks"
            };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            
            Gson gson = new Gson();
            httpRequest.setBody(gson.toJsonTree(projectTask));
            
            return HttpAdapter.request(httpRequest);
        } catch (Exception ex) {
            String errorMsg = "Network Error occurred, please try again";
            LOGGER.logRecord(LOG_LEVEL, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name: putOfflineTask 
     * Description: PUT Request to API to update an offline task
     *
     * @param projectTask 
     * @param userSession 
     * @return Response
     */
    public Response putOfflineTask(ProjectTask projectTask, UserSession userSession) {
        try {
            String[] pathSegments = {
                "projects", projectTask.getProjectId(),
                "employees", userSession.getId(),
                "offline-tasks"
            };
            
            HttpRequest httpRequest = new HttpRequest(HttpMethod.PUT, pathSegments);
            Gson gson = new Gson();
            httpRequest.setBody(gson.toJsonTree(projectTask));
            
            return HttpAdapter.request(httpRequest);
        } catch (Exception ex) {
            String errorMsg = "Network Error Occurred, Please Try Again";
            LOGGER.logRecord(LOG_LEVEL, errorMsg, ex);
            return new Response(true, StatusCode.SUCCESS, errorMsg);
        }
    }
    
    /**
     * Method Name: PostUserTOCAcceptance 
     * Description: Post user related term of condition acceptance to API
     *
     * @param project 
     * @param meetingTimeLogs 
     * @return Response
     */
    public Response PostSyncMeetingTimeLogs(
            CompanyProject project, List<MeetingTimeLog> meetingTimeLogs
    ) {
        try {
            Gson gson = new Gson();
            UserSession userSession 
                    = StateStorage.getCurrentState(StateName.USER_SESSION);
            
            if (userSession == null) {
                String errorMsg = "Session is invalidated, please login again";
                throw new NullPointerException(errorMsg);
            }
            
            String[] pathSegements = {
              "projects", project.getId(),
              "employees", userSession.getId(),
              "meeting-time-logs"
            };
            
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            httpRequest.addHeader("Content-Type", "application/json");
            
            JsonArray requestData = new JsonArray();
            meetingTimeLogs.forEach((MeetingTimeLog meetingTimeLog) -> {
                JsonElement serializedJson = gson.toJsonTree(meetingTimeLog);
                requestData.add(serializedJson);
            });
            httpRequest.setBody(requestData);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String errorMsg = "Unable to sync meeting time logs";
            LOGGER.logRecord(LOG_LEVEL, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name: putUserTaskAssignment 
     * Description: Update task user assignment data
     *
     * @param userId 
     * @param projectTask  
     * @return Response
     */
    public Response putUserTaskAssignment(String userId, ProjectTask projectTask) {
        try {
            String[] pathSegments = new String[]{
                "users", userId, "task-assignments", projectTask.getId()
            };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.PUT, pathSegments);
            httpRequest.addHeader("Content-Type", "application/json");
            
            Gson gson = new Gson();
            JsonObject requestData  = gson.toJsonTree(projectTask).getAsJsonObject();
            httpRequest.setBody(requestData);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            
            if (apiResponse.isError()) {
                return apiResponse;
            }
            
            return new Response(
                    false, 
                    StatusCode.SUCCESS, 
                    "Successfully updated user task assignment",
                    apiResponse.getData()
            );
        } catch (Exception ex) {
            String errorMsg = "Unable to update user task assignment";
            LOGGER.logRecord(LOG_LEVEL, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
        
    }
    
    /**
     * Method Name: getClientAppLatestVersion 
     * Description: Get Latest Application Version
     *
     * @param currentVersion
     * @return Response
     */
    public Response getClientAppLatestVersion(String currentVersion) {
        try {
            String[] pathSegments = new String[]{
                "client-app-latest-version"
            };
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegments);
            httpRequest.addQueryParameter("current-version", currentVersion);
            Response response = HttpAdapter.request(httpRequest);
            return response;
        } catch (Exception ex) {
            String errorMsg = "Unable to retrieve app version details";
            LOGGER.logRecord(LOG_LEVEL, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    /**
     * Method Name: callGetUserProductivityData Description: Make HTTP request to
     * get user attendance data object
     *
     * @param companyId
     * @param summeryType
     * @param filterType
     * @param userId
     * @param from
     * @param to
     * @return Response
     */
    public Response callGetUserProductivityWidgetSummery(String companyId, String filterType, String userId, String from, String to) {
        try {
            String[] pathSegements = {"reports", "application-productivity-client-widget"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);

            httpRequest.addQueryParameter("companyId", companyId);
            httpRequest.addQueryParameter("from", from);
            httpRequest.addQueryParameter("to", to);
            httpRequest.addQueryParameter("filterType", filterType);
            httpRequest.addQueryParameter("filterIds", userId);

            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
        } catch (Exception ex) {
            String message = "Error occurred while retrieving user Productivity data.";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, message, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }

 
  public Response insertBandwidthDetails(String userId,JsonObject bandwidthdetails) {
        try {
            //String[] pathSegements = {"client-login"};
            String[] pathSegements = {"users",userId,"specification-monitor","network"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            JsonObject networkDetails = bandwidthdetails.getAsJsonObject();
      
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("jitter", networkDetails.get("Jitter").getAsString());
            requestBody.addProperty("ispName", "1");
            requestBody.addProperty("ping", networkDetails.get("ping").getAsString());
            requestBody.addProperty("downloadSpeed", networkDetails.get("downloadSpeed").getAsString());
            requestBody.addProperty("uploadSpeed", networkDetails.get("uploadSpeed").getAsString());
            requestBody.addProperty("deviceId", "1");
            httpRequest.setBody(requestBody);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
            
        } catch (Exception ex) {
            System.out.println("#### httpAdapter Exceptions -----> " + ex.getMessage());
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Error occured while connecting api - callUserLogin", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occured while connecting api"
            );
        }
    }
 
  
   public Response insertUserStatus(String userId,String uStatus) {
        try {
            //String[] pathSegements = {"client-login"};
            String[] pathSegements = {"users",userId,"online-status"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("status", uStatus);
            requestBody.addProperty("userId", userId);
            httpRequest.setBody(requestBody);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
            
        } catch (Exception ex) {
            System.out.println("#### httpAdapter Exceptions -----> " + ex.getMessage());
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Error occured while connecting api - User Status", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occured while connecting api"
            );
        }
    }
 
    public Response getProjectNameByID(String projectId,String companyId) {
        try {
            //String[] pathSegements = {"client-login"};
            String[] pathSegements = {"companies/"+companyId+"/projects/"+projectId};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, pathSegements);
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
            
        } catch (Exception ex) {
            System.out.println("#### httpAdapter Exceptions -----> " + ex.getMessage());
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Error occured while connecting api - User Status", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occured while connecting api"
            );
        }
    }
 
    
    public Response insertLocationDetails(String userId,String deviceId,JsonObject LocatioDetails){
    try{
          String[] pathSegements = {"users",userId,"specification-monitor","location"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            JsonObject geoloc = LocatioDetails.getAsJsonObject();
            
             JsonObject requestBody = new JsonObject();
             requestBody.addProperty("dataCreatedTime", geoloc.get("dataCreatedTime").getAsLong());
             requestBody.addProperty("deviceId", deviceId);
             requestBody.addProperty("latitude", geoloc.get("latitude").getAsString());
             requestBody.addProperty("longitude", geoloc.get("longitude").getAsString());
             requestBody.addProperty("city", geoloc.get("city").getAsString());
             requestBody.addProperty("country", geoloc.get("country").getAsString());
             requestBody.addProperty("state", geoloc.get("state").getAsString());
             requestBody.addProperty("timezone", geoloc.get("timezone").getAsString());
             httpRequest.setBody(requestBody);
            
          Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
    }catch (Exception es){
    System.out.println("#### httpAdapter Exceptions -----> " + es.getMessage());
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Error occured while connecting api - callUserLogin", 
                    es
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occured while connecting api"
            );
    }
    }
    
     public Response callUserSkipLogin(JsonObject loginDetails) {
        try {
            //String[] pathSegements = {"client-login"};
            String[] pathSegements = {"validate-auth"};
            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, pathSegements);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("macAddress", loginDetails.get("macAddress").getAsString());
            requestBody.addProperty("ipAddress", loginDetails.get("ipAddress").getAsString());
              requestBody.addProperty("machineName", loginDetails.get("machineName").getAsString());
              requestBody.addProperty("machineUserName", loginDetails.get("machineUserName").getAsString());
              requestBody.addProperty("platform", loginDetails.get("platform").getAsString());
             requestBody.addProperty("version", loginDetails.get("version").getAsString());
              requestBody.addProperty("osName", loginDetails.get("osName").getAsString());
              requestBody.addProperty("osVersionMajor", loginDetails.get("osVersionMajor").getAsString());
              requestBody.addProperty("clientAppVersion", loginDetails.get("clientAppVersion").getAsString());
             requestBody.addProperty("osVersionMinor", loginDetails.get("osVersionMinor").getAsString());
              requestBody.addProperty("userId", loginDetails.get("userId").getAsString());
                requestBody.addProperty("deviceId", loginDetails.get("deviceId").getAsString());
              
            httpRequest.setBody(requestBody);
            httpRequest.addHeader("Content-Type", "application/json");
            httpRequest.addHeader("Authorization",loginDetails.get("authToken").getAsString() );
            
            Response apiResponse = HttpAdapter.request(httpRequest);
            return apiResponse;
            
        } catch (Exception ex) {
            System.out.println("#### httpAdapter Exceptions -----> " + ex.getMessage());
            LOGGER.logRecord(
                    InternalLogger.LOGGER_LEVEL.SEVERE, 
                    "Error occured while connecting api - callUserLogin", 
                    ex
            );
            return new Response(
                    true, 
                    StatusCode.APPLICATION_ERROR, 
                    "Error occured while connecting api"
            );
        }
    }
   
}
