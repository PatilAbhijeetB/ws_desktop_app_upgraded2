/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.io;

import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 *
 * @author chamara
 */
import org.jnativehook.keyboard.NativeKeyEvent;/**
 *
 * @author chamara
 */
public class KeyboardEventListener implements NativeKeyListener {
    private static final InternalLogger LOGGER = LoggerService.getLogger(KeyboardEventListener.class);

    private KeyboardKeyEvent keyTypeLastEvent;
    private KeyboardKeyEvent keyPressedLastEvent;
    private KeyboardKeyEvent keyReleaseLastEvent;
    
    public KeyboardEventListener() {
        
        long currentTimeStamp = TimeUtility.getSystemClockTimestamp();
        
        this.keyPressedLastEvent = new KeyboardKeyEvent(null, currentTimeStamp);
        this.keyTypeLastEvent = new KeyboardKeyEvent(null, currentTimeStamp);
        this.keyReleaseLastEvent = new KeyboardKeyEvent(null, currentTimeStamp);
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent keyEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        
        if (keyTypeLastEvent == null) {
            keyTypeLastEvent = new KeyboardKeyEvent(keyEvent, timestamp);
        } else {
            keyTypeLastEvent.setValues(keyEvent, timestamp);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent keyEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        
        if (keyPressedLastEvent == null) {
            keyPressedLastEvent = new KeyboardKeyEvent(keyEvent, timestamp);
        } else {
            keyPressedLastEvent.setValues(keyEvent, timestamp);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent keyEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        
        if (keyReleaseLastEvent == null) {
            keyReleaseLastEvent = new KeyboardKeyEvent(keyEvent, timestamp);
        } else {
            keyReleaseLastEvent.setValues(keyEvent, timestamp);
        }
    }

    public KeyboardKeyEvent getKeyTypeLastEvent() throws CloneNotSupportedException {
        return keyTypeLastEvent != null ? (KeyboardKeyEvent) keyTypeLastEvent.clone() : null;
    }

    public KeyboardKeyEvent getKeyPressedLastEvent() throws CloneNotSupportedException {
        return keyPressedLastEvent != null ? (KeyboardKeyEvent) keyPressedLastEvent.clone() : null;
    }

    public KeyboardKeyEvent getKeyReleaseLastEvent() throws CloneNotSupportedException {
        return keyReleaseLastEvent != null ? (KeyboardKeyEvent) keyReleaseLastEvent.clone() : null;
    }
    
}
