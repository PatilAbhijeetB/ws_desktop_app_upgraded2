/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import org.apache.commons.lang3.time.StopWatch;

/**
 *
 * @author chamara
 */
public final class ActivityTimer {
    
    private String activityId;
    private StopWatch stopWatch;
    
    private ActivityTimer() {
    }
    
    public ActivityTimer(String activityId, boolean isStartwithCreate) {
        this.activityId = activityId;
        this.stopWatch = isStartwithCreate ? StopWatch.createStarted() : StopWatch.create();
    }
    
    
}
