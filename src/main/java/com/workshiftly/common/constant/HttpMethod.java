/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.constant;


public enum HttpMethod {
    GET("GET", false),
    POST("POST", true),
    PUT("PUT", true),
    DELETE("PUT", true),
    HEAD("HEAD", false),
    OPTIONS("OPTIONS", false),
    PATCH("PATCH", true),
    TRACE("TRACE", true),
    CONNECT("CONNECT", true);
    
    private final String name;
    private boolean hasBody;

    HttpMethod(String name, boolean hasBody) {
        this.name = name;
        this.hasBody = hasBody;
    }
    
    public boolean containedBody() {
        return this.hasBody;
    }
}
