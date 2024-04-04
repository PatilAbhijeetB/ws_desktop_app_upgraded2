/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonElement;
import com.workshiftly.common.constant.HttpMethod;

/**
 * HttpRequest is a model used as a configuration model
 * This class only consisting Http request high level attributes
 * 
 */
public class HttpRequest {
    
    private HttpMethod httpmethod;
    private String[] pathSegements;
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> requestHeaders = new HashMap<>();
    private JsonElement body;
    
    private HttpRequest() {}
    
    public HttpRequest(HttpMethod httpMethod, String[] pathSegements) {
        
        this.httpmethod = httpMethod;
        this.pathSegements = pathSegements;
       
        // default Content-Type header
        this.requestHeaders.put("Content-Type", "application/json; charset=UTF-8");
    }
    
    public HttpMethod getHttpmethod() {
        return httpmethod;
    }

    public String[] getPathSegements() {
        return pathSegements;
    }

    public void addHeader(String header, String value) {
        this.requestHeaders.put(header, value);
    }
    
    public void removeHeader(String header) {
        this.requestHeaders.remove(header);
    }
    
    public void addQueryParameter(String key, String value) {
        this.queryParams.put(key, value);
    }
    
    public void removeQueryParameter(String key) {
        this.queryParams.remove(key);
    }
    
    public Map<String, String> getHeaders() {
        return this.requestHeaders;
    }
    
    public Map<String, String> getQueryParameters() {
        return this.queryParams;
    }

    public JsonElement getBody() {
        return body;
    }

    public void setBody(JsonElement body) {
        this.body = body;
    }
}
