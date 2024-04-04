/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

/**
 *
 * @author chamara
 */
public class Company {
    
    private String id;
    private String name;
    private String timezone;
    private String timeFormat;
    private String ownerId;
    private String email;
    private String configurationId;
    private String createdAt;
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getEmail() {
        return email;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
    
            
}
