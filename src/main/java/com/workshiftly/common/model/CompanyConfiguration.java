/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import java.util.List;

/**
 *
 * @author chamara
 */
public final class CompanyConfiguration {
    
    private String id;
    private Integer appIdleTime;
    private boolean isScreenCapturing;
    private Integer numberOfScreenshotsPerHour;
    private boolean isTrackWithinShift;
    private boolean allowUsersToAddBreakReasons;
    private List<BreakReason> breakReasons;
    private boolean activeSilentTracking;
    private boolean allowNotification;
    private long createdAt;
    private long updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAppIdleTime() {
        return appIdleTime;
    }

    public void setAppIdleTime(int appIdleTime) {
        this.appIdleTime = appIdleTime;
    }

    public boolean isIsScreenCapturing() {
        return isScreenCapturing;
    }

    public void setIsScreenCapturing(boolean isScreenCapturing) {
        this.isScreenCapturing = isScreenCapturing;
    }

    public Integer getNumberOfScreenshotsPerHour() {
        return numberOfScreenshotsPerHour;
    }

    public void setNumberOfScreenshotsPerHour(Integer numberOfScreenshotsPerHour) {
        this.numberOfScreenshotsPerHour = numberOfScreenshotsPerHour;
    }

    public boolean isIsTrackWithinShift() {
        return isTrackWithinShift;
    }

    public void setIsTrackWithinShift(boolean isTrackWithinShift) {
        this.isTrackWithinShift = isTrackWithinShift;
    }

    public boolean isAllowUsersToAddBreakReasons() {
        return allowUsersToAddBreakReasons;
    }

    public void setAllowUsersToAddBreakReasons(boolean allowUsersToAddBreakReasons) {
        this.allowUsersToAddBreakReasons = allowUsersToAddBreakReasons;
    }

    public List<BreakReason> getBreakReasons() {
        return breakReasons;
    }

    public void setBreakReasons(List<BreakReason> breakReasons) {
        this.breakReasons = breakReasons;
    }

    public boolean isAllowNotification() {
        return allowNotification;
    }

    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActiveSilentTracking() {
        return activeSilentTracking;
    }

    public void setActiveSilentTracking(boolean activeSlientTracking) {
        this.activeSilentTracking = activeSlientTracking;
    }
}
