/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.workshiftly.common.model;

/**
 *
 * @author Niroshan
 */
public class LoggedInDevice {
    private String deviceId;
    private String userId;
    private String isLogging;
    private String macAddress;
    private String ipAddress;
    private String machineName;
    private String machineUserName;
    private String platform;
    private String version;
    private String osName;
    private String osVersionMajor;
    private String osVersionMinor;
    private Long createdAt;
    private Long updatedAt;
    
    
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }
        
    public String getmacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getMachineName() {
        return this.machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
    
     public String getMachineUserName() {
        return machineUserName;
    }

    public void setMachineUserName(String machineUserName) {
        this.machineUserName = machineUserName;
    }
    
    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getOsName() {
        return this.osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }
    public String getOsVersionMajor() {
        return this.osVersionMajor;
    }

    public void setOsVersionMajor(String osVersionMajor) {
        this.osVersionMajor = osVersionMajor;
    }
    public String getOsVersionMinor() {
        return this.osVersionMinor;
    }

    public void setOsVersionMinor(String osVersionMinor) {
        this.osVersionMinor = osVersionMinor;
    }
    
    public Long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAtr(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
