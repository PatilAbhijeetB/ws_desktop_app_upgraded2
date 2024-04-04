/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.io;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import org.jnativehook.keyboard.NativeKeyEvent;

/**
 *
 * @author chamara
 */
public class KeyboardKeyEvent implements Cloneable {
    private static final InternalLogger LOGGER = LoggerService.getLogger(KeyboardKeyEvent.class);

    private NativeKeyEvent nativeKeyEvent;
    private long timestamp;
    
    KeyboardKeyEvent(NativeKeyEvent keyEvent, long timestamp) {
        this.nativeKeyEvent = keyEvent;
        this.timestamp = timestamp;
    }
    
    void setValues(NativeKeyEvent keyEvent, long timestamp) {
        this.nativeKeyEvent = keyEvent;
        this.timestamp = timestamp;
    }
    
    public NativeKeyEvent getNativeKeyEvent() {
        return nativeKeyEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
     protected Object clone() throws CloneNotSupportedException { 
        return super.clone(); 
    } 
}
