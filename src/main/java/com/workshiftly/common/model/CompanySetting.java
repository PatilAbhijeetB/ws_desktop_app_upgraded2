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
public class CompanySetting {
    
    private String id;
    private String name;
    private String timezone;
    private String timeFormat;
    private String ownerId;
    private String configurationId;
    private String packageId;
    private Long expiredAt;
    private Long startShift;
    private Long endShift;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public Long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Long getStartShift() {
        return startShift;
    }

    public void setStartShift(Long startShift) {
        this.startShift = startShift;
    }

    public Long getEndShift() {
        return endShift;
    }

    public void setEndShift(Long endShift) {
        this.endShift = endShift;
    }
    
    
}
