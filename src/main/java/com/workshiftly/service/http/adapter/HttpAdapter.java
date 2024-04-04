/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.service.http.adapter;

import com.workshiftly.common.constant.HttpMethod;
import com.workshiftly.common.model.HttpRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.DotEnvUtility;
import java.util.Map;
import java.util.Map.Entry;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.RequestBodyEntity;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 *
 * @author chamara
 */
public class HttpAdapter {
    
    private enum AdapterException {
        UNKNOWN_HOST("java.net.UnknownHostException"),
        UNSPECIFIED("UNSPECIFIED");

        String needle;
        private AdapterException(String needle) {
            this.needle = needle;
        }
    }

    // API URL
    private static String API_URL = DotEnvUtility.getAPI();
    
    /**
     * Method Name: request
     * Description: Make Http request to API by given HttpRequest Config object and
     *  return HttpResponse<JsonNode> which can be easily parsed Response using
     *  google gson library
     * @param httpRequest
     * @return
     * @throws Exception 
     */
    public static Response request(HttpRequest httpRequest) throws Exception {
        
        try {
            HttpResponse<JsonObject> httpResponse;
            HttpMethod httpMethod = httpRequest.getHttpmethod();

            if (httpMethod.containedBody()) {
                RequestBodyEntity request = buildRequestWithBody(httpRequest);
                httpResponse = request.asObject(JsonObject.class);
            } else {
                GetRequest request = buildRequest(httpRequest);
                httpResponse = request.asObject(JsonObject.class);
            }
            
            boolean isErrorResponse = !httpResponse.isSuccess();
            JsonObject response = httpResponse.getBody();

            String message = response != null && response.has("message") ? 
                    response.get("message").getAsString() : "";
            JsonElement data = response != null && response.has("data") ?
                    response.get("data") : null;
            StatusCode statusCode = isErrorResponse 
                    ? StatusCode.NETWORK_ERROR : StatusCode.SUCCESS;
            return new Response (isErrorResponse, statusCode, message, data);
        } catch (Exception ex) {
            Response exceptinalResponse = handleExceptions(ex);
            return exceptinalResponse;
        }
        
    }
    
    /**
     * Method Name: buildRequest
     * Description: This is a supportive decoupled method to constructing GetRequest
     *  Object. This method should be constructed HTTP method which has no request body
     *  HTTP Methods are GET, HEAD, OPTIONS
     * @param httpRequest
     * @return
     * @throws Exception 
     */
    private static GetRequest buildRequest(HttpRequest httpRequest) throws Exception {
        
        String callingURL = buildURL(httpRequest);
        GetRequest request;
        
        switch (httpRequest.getHttpmethod()) {
            case GET:
                request = Unirest.get(callingURL);
                break;
            case OPTIONS:
                request = Unirest.options(callingURL);
                break;
            case HEAD:
                request = Unirest.head(callingURL);
                break;
            default:
                throw new Exception("Invlid Http Request method given");
        }
        
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        if (userSession != null && userSession.getAuthToken() != null) {
            request.header("Authorization", "Bearer " + userSession.getAuthToken());
        }
        
        Map<String, String> headers = httpRequest.getHeaders();
        headers.entrySet().forEach((header) -> {
            request.header(header.getKey(), header.getValue());
        });
        
        Map<String, String> queryParams = httpRequest.getQueryParameters();
        queryParams.entrySet().forEach((queryParam) -> {
            request.queryString(queryParam.getKey(), queryParam.getValue());
        });
        
        return request;
    }
    
    /**
     * Method Name: buildRequestWithBody
     * Description: This is a supportive decoupled method to constructing RequestBodyEntity
     *  Object. This method should be constructed HTTP method which has request body
     *  HTTP Methods are POST, PUT, DELETE, PATCH
     * @param httpRequest
     * @return
     * @throws Exception 
     */
    private static RequestBodyEntity buildRequestWithBody(HttpRequest httpRequest) 
            throws Exception {
     
        String callingURL = buildURL(httpRequest);
        HttpRequestWithBody request;
        
        switch (httpRequest.getHttpmethod()) {
            case POST:
                request = Unirest.post(callingURL);
                break;
            case PUT:
                request = Unirest.put(callingURL);
                break;
            case DELETE:
                request = Unirest.delete(callingURL);
                break;
            case PATCH:
                request = Unirest.patch(callingURL);
                break;
            default:
                throw new Exception("Invalid http method set for HttpReqesut Object");
        }
        
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        if (userSession != null && userSession.getAuthToken() != null) {
            request.header("Authorization", "Bearer " + userSession.getAuthToken());
        }
        
        Map<String, String> headers = httpRequest.getHeaders();
        headers.entrySet().forEach((header) -> {
            request.header(header.getKey(), header.getValue());
        });
        
        Map<String, String> queryParams = httpRequest.getQueryParameters();
        queryParams.entrySet().forEach((Entry<String, String> queryParam) -> {
            request.queryString(queryParam.getKey(), queryParam.getValue());
        });
        
        JsonElement requestBody = httpRequest.getBody();
        return request.body(requestBody);
    }
    
    /**
     * Method Name: buildURL
     * Description: build complete URL. complete URL is meant  here only combination
     *      fo API HOST URL + Resource Path
     * @param httpRequest
     * @return 
     */
    private static String buildURL(HttpRequest httpRequest) {
        
        String[] pathSegments = httpRequest.getPathSegements();
        String resourcePath = String.join("/", pathSegments);
        String completeURL = String.format("%s/%s", API_URL, resourcePath);
        return completeURL;
    }
    
    /***
     * Method Name: handle Exceptions in appropriate manner and show them clearly
     *  at presentation layer
     * Description: Unirest HttpClient dispatch its all exception as UnirestException
     *  Unirest HttpClient wraps around org.apache.httpclient library and so on and
     *  all exceptions are mostly mostly into UnirestException by UniRest and it is
     *  hard to handle exceptions from try-catch block.
     * 
     *  For Note for future development: If you noted a new exception and need to
     *  handle it in good hand or represent it at presentation layer follow the
     *  below instructions on the fly ->
     *      * Introduce AdapterException Enum value which can obtain from
     *          Unirest message
     *      * Then it should be handle in this method (follow the existing method
     *          body)
     * @param ex
     * @return 
     */
    private static Response handleExceptions(Exception ex) {
        
        AdapterException adpaterException = AdapterException.UNSPECIFIED;
        String exceptionMsg = ex.getMessage();
        
        AdapterException[] exceptions = AdapterException.values();
        for (AdapterException currentEx : exceptions) {
            if (currentEx != AdapterException.UNSPECIFIED) {
                boolean isContainGenericEx = exceptionMsg.contains(currentEx.needle);
                
                if (isContainGenericEx) {
                    adpaterException = currentEx;
                }
            }
        }
        
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("exceptionType", adpaterException.toString());
        
        Response exceptionResponse = new Response(
                true, 
                StatusCode.NETWORK_ERROR, 
                null, 
                errorObject
        );
        switch (adpaterException) {
            case UNKNOWN_HOST:
                exceptionResponse.setMessage(
                        "Plese check your internet connection and try again"
                );
                break;
                
            case UNSPECIFIED:
                 exceptionResponse.setMessage(
                       "Invalid Username or Password"
                       
                );
               break;
            default:
                exceptionResponse.setMessage(
                     "Error occurred While Connecting Remote Server"
                );      
                break;
        }
        
        return exceptionResponse;
    }
}
