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
public class UserSession {
    
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String companyId;
    private String authToken;
    private String deviceId;
    private boolean isActive;
    private boolean isClientActive;
    private boolean isAllowOfflineTask;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
     public String getDeviceId() {
        return deviceId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isIsClientActive() {
        return isClientActive;
    }

    public void setIsClientActive(boolean isClientActive) {
        this.isClientActive = isClientActive;
    }

    public boolean isIsAllowOfflineTask() {
        return isAllowOfflineTask;
    }

    public void setIsAllowOfflineTask(boolean isAllowOfflineTask) {
        this.isAllowOfflineTask = isAllowOfflineTask;
    }
}
