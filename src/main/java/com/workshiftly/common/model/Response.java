/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.google.gson.JsonElement;
import com.workshiftly.common.constant.StatusCode;


public class Response {
    
    private boolean error;
    private StatusCode statusCode;
    private String message;
    private JsonElement data;
    
    public Response(boolean hasError, StatusCode statusCode, String message, JsonElement data) {
        this.error = hasError;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }
    
    public Response(boolean hasError, StatusCode statusCode, String message) {
        this.error = hasError;
        this.message = message;
        this.statusCode = statusCode;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
    
    
}
