/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.io;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import org.jnativehook.mouse.NativeMouseWheelEvent;

/**
 *
 * @author chamara
 */
public class MouseWheelEvent implements Cloneable {
    private static final InternalLogger LOGGER = LoggerService.getLogger(MouseWheelEvent.class);

    private NativeMouseWheelEvent mouseWheelEvent;
    long timestamp;
    
    private MouseWheelEvent() {}
    
    MouseWheelEvent(NativeMouseWheelEvent mouseWheelEvent, long timestamp) {
        this.mouseWheelEvent = mouseWheelEvent;
        this.timestamp = timestamp;
    }
    
    void setValues(NativeMouseWheelEvent mouseWheelEvent, long timestamp) {
        this.mouseWheelEvent = mouseWheelEvent;
        this.timestamp = timestamp;
    }

    public NativeMouseWheelEvent getMouseWheelEvent() {
        return mouseWheelEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
