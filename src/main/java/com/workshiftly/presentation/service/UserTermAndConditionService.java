/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.service;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.HashMap;
import java.util.Map;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 
 * @author Chamara Munasinghe
 * 
 * Class: UserTermAndConditionService
 * Description: This class intends to behave as service provider to presentation
 *  in asynchronous way and used for handling services related to Term and
 *  condition related service functionality.
 */
public class UserTermAndConditionService extends Service<Response> {
    
    /**
     * enum RequestType indicate service request Type
     * -> GET_LAST_TnC_STATUS GET users/:userId/term-and-condition-acceptance"
     * -> POST_TNC_ACCEPTANCE POST users/:userId/term-and-condition-acceptance"

     */
    public static enum RequestType {
        GET_LAST_TNC_STATUS,
        POST_TNC_ACCEPTANCE;
    }
    
    private RequestType requestType;
    // Data provider for service execution
    private Map<String, Object> serviceData;
    
    // constructor
    private UserTermAndConditionService(){}
    
    // constructor
    public UserTermAndConditionService(
            RequestType requestType, 
            Map<String, Object> serviceData 
    ) {
        this.requestType = requestType;
        this.serviceData = serviceData;
    }
    
    /**
     * Class: UserTnCAcceptanceRecord
     * Extended parent class: Task<T>
     * Description: handle POST_TNC_ACCEPTANCE service type
     */
    private static class UserTnCAcceptanceRecord extends Task<Response> {

        Map<String, Object> serviceData;
        UserTnCAcceptanceRecord() {
            this.serviceData = new HashMap<>();
        }

        UserTnCAcceptanceRecord(Map<String, Object> serviceData) {
            this();
            if (serviceData != null) {
                this.serviceData.putAll(serviceData);
            }
        }
        
        @Override
        protected Response call() throws Exception {
            try {
                String userId = (String) serviceData.get("userId");
                if (userId == null) {
                    UserSession userSession = StateStorage.getCurrentState(
                            StateName.USER_SESSION
                    );
                    userId = userSession != null ? userSession.getId() : null;
                    if (userId == null || userId.isBlank()) {
                        return new Response(
                                true, StatusCode.BAD_REQUEST, "Session invalidated"
                        );
                    }
                }
                
                String action = (String) serviceData.get("action");
                String version = (String) serviceData.get("version");
                
                HttpRequestCaller httpCaller = new HttpRequestCaller();
                return httpCaller.PostUserTOCAcceptance(userId, action, version);
            } catch (Exception ex) {
                String errorMessage = "Failed to proceed Term And Condtions";
                LoggerService.LogRecord(
                        this.getClass(), 
                        errorMessage, 
                        InternalLogger.LOGGER_LEVEL.ALL, 
                        ex
                );
                return new Response(
                        false, 
                        StatusCode.APPLICATION_ERROR, 
                        errorMessage
                );
            }   
        }
    } 
    
    /**
     * Class: UserTnCAcceptanceQuery
     * Extended parent class: Task<T>
     * Description: handle GET_LAST_TNC_STATUS service type
     */
    private static class UserTnCAcceptanceQuery extends Task<Response> {
        
        // service data provider
        private final Map<String, Object> queryOptions;

        // constructor
        UserTnCAcceptanceQuery(Map<String, Object> queryOptions) {
            this();
            if (queryOptions != null) {
                this.queryOptions.putAll(queryOptions);
            }
        }
        
        // constructor
        public UserTnCAcceptanceQuery() {
            this.queryOptions = new HashMap<>();
        }
        
        // add query key value into service data provider
        private <T> void putQueryOption(String key, T value) {
            this.queryOptions.put(key, value);
        }
        
        @Override
        protected Response call() throws Exception {
            try {
                String userId = (String) queryOptions.get("userId");
                if (userId == null) {
                    UserSession userSession = StateStorage.getCurrentState(
                            StateName.USER_SESSION
                    );
                    userId = userSession != null ? userSession.getId() : null;
                    if (userId == null || userId.isBlank()) {
                        throw new Exception("User session invalid, please login again");
                    }
                }

                HttpRequestCaller httpCaller = new HttpRequestCaller();
                Response response = httpCaller.getLatestUserTOCAcceptance(userId);
                return response;
            } catch (Exception ex) {
                String errorMessage = "Failed to proceed Term And Condtions";
                LoggerService.LogRecord(
                        this.getClass(), 
                        errorMessage, 
                        InternalLogger.LOGGER_LEVEL.ALL, 
                        ex
                );
                return new Response(
                        false, 
                        StatusCode.APPLICATION_ERROR, 
                        errorMessage
                );
            }
        }
    }
    
    @Override
    protected Task<Response> createTask() throws UnsupportedOperationException {
        
        switch (this.requestType) {
            case GET_LAST_TNC_STATUS:
                UserTnCAcceptanceQuery task;
                task = new UserTnCAcceptanceQuery();
                task.putQueryOption("userId", this.serviceData.get("userId"));
                return task;
            case POST_TNC_ACCEPTANCE:
                return new UserTnCAcceptanceRecord(this.serviceData);
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
