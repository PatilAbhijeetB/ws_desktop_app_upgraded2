/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.io;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import org.jnativehook.mouse.NativeMouseEvent;

/**
 *
 * @author chamara
 */
public class MouseEvent implements Cloneable {
    private static final InternalLogger LOGGER = LoggerService.getLogger(MouseEvent.class);

    private NativeMouseEvent nativeMouseEvent;
    private long timestamp;

    MouseEvent(NativeMouseEvent nativeMouseEvent, long timestamp) {
        this.nativeMouseEvent = nativeMouseEvent;
        this.timestamp = timestamp;
    }
    
    void setValues(NativeMouseEvent nativeMouseEvent, long timestamp) {
        this.nativeMouseEvent = nativeMouseEvent;
        this.timestamp = timestamp;
    }

    public NativeMouseEvent getNativeMouseEvent() {
        return nativeMouseEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    protected  Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
