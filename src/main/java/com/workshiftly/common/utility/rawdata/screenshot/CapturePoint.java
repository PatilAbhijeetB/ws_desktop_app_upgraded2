/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.screenshot;

import com.workshiftly.common.model.Screenshot;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;

/**
 *
 * @author chamara
 */
public class CapturePoint implements Comparable {
    private static final InternalLogger LOGGER = LoggerService.getLogger(CapturePoint.class);

    private long captureTimestamp;
    private Screenshot screenshot;
    private boolean committed;
    
    private CapturePoint() {}
    
    CapturePoint(long captureTimestamp) {
        this.screenshot = null;
        this.captureTimestamp = captureTimestamp;
        this.committed = false;
    }
    
    void setScreenshot(Screenshot screenshot) {
        this.screenshot = screenshot;
    }
    
    void setCaptureTimestamp(long captureTimestamp) {
        this.captureTimestamp = captureTimestamp;
    }

    long getCaptureTimestamp() {
        return captureTimestamp;
    }

    Screenshot getScreenshot() {
        return screenshot;
    }
    
    boolean isCommitted() {
        return this.committed;
    }

    @Override
    public int compareTo(Object input) {
        CapturePoint comparator = (CapturePoint) input;
        long difference = this.captureTimestamp - comparator.captureTimestamp;
        return difference == 0 ? 0 : difference > 0 ? 1 : -1;
    }
}
