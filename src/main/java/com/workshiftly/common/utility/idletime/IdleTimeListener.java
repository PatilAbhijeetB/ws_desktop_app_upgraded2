/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.idletime;

import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.io.KeyboardEventListener;
import com.workshiftly.common.utility.io.KeyboardKeyEvent;
import com.workshiftly.common.utility.io.MouseEvent;
import com.workshiftly.common.utility.io.MouseEventListener;
import com.workshiftly.common.utility.io.MouseWheelEvent;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;





public class IdleTimeListener implements Callable<Long> {
    
    private static final InternalLogger LOGGER = LoggerService.getLogger(IdleTimeListener.class);
    private static final Logger GLOBAL_SCREEN_LOGGER = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    
    static {
        GLOBAL_SCREEN_LOGGER.setLevel(Level.WARNING);
        GLOBAL_SCREEN_LOGGER.setUseParentHandlers(false);
    }
    
    private final KeyboardEventListener keyboardEventListener;
    private final MouseEventListener mouseEventListener;
    
    
    public IdleTimeListener() {
        
        this.keyboardEventListener = new KeyboardEventListener();
        this.mouseEventListener = new MouseEventListener();
    }
    
    public void start() throws NativeHookException {
        
        if (!GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.registerNativeHook();
        }
        
        // keyboard event listener register in global screen
        GlobalScreen.addNativeKeyListener(keyboardEventListener);
        
        // Mouse event listeners register into global screen 
        GlobalScreen.addNativeMouseListener(mouseEventListener);
        GlobalScreen.addNativeMouseWheelListener(mouseEventListener);
        GlobalScreen.addNativeMouseMotionListener(mouseEventListener);
    }
    
    /**
     * Do not this method this will be causes application crash in Ubuntu
     * @throws NativeHookException 
     */
    public void stop() throws NativeHookException {
        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.unregisterNativeHook();
        }
    }
    
    public KeyboardKeyEvent getRecentKeyStoke(KeyboardEventListener eventListener) 
            throws Exception
    {
        List<KeyboardKeyEvent> keyboardKeyEventTypes = new ArrayList<>();
        keyboardKeyEventTypes.add(eventListener.getKeyTypeLastEvent());
        keyboardKeyEventTypes.add(eventListener.getKeyReleaseLastEvent());
        keyboardKeyEventTypes.add(eventListener.getKeyPressedLastEvent());
        
        KeyboardKeyEvent lastestEvent = null;
        long timestamp = 0;
        
        for (KeyboardKeyEvent currentKeyEvent : keyboardKeyEventTypes) {
            long currentEventTimestamp = currentKeyEvent != null ? currentKeyEvent.getTimestamp() : 0;
            if (currentEventTimestamp > timestamp) {
                timestamp = currentEventTimestamp;
                lastestEvent = currentKeyEvent;
            }
        }
        return lastestEvent;
    }
    
    public MouseEvent getRecentMouseEvent(MouseEventListener eventListener) throws Exception {
        
        List<MouseEvent> mouseEventTypes = new ArrayList<>();
        
        // mouse click events
        mouseEventTypes.add(eventListener.getMouseClickedEvent());
        mouseEventTypes.add(eventListener.getMousePressedEvent());
        mouseEventTypes.add(eventListener.getMouseReleasedEvent());
        
        // mouse motion events
        mouseEventTypes.add(eventListener.getMouseDraggedEvent());
        mouseEventTypes.add(eventListener.getMouseMovedEvent());
        
        MouseEvent latestMouseEvent = null;
        long timestamp = 0;
        
        for (MouseEvent currentMouseEvent : mouseEventTypes) {
            long currentEventTimestamp = currentMouseEvent != null ? currentMouseEvent.getTimestamp() : 0;
            if (currentEventTimestamp > timestamp) {
                timestamp = currentEventTimestamp;
                latestMouseEvent = currentMouseEvent;
            }
        }
        return latestMouseEvent;
    }
    
    public MouseWheelEvent getRecentMouseWheelEvent(MouseEventListener eventListener) throws Exception {
        return eventListener.getMouseWheelEvent();
    }

    @Override
    public Long call() throws Exception {
        
        KeyboardKeyEvent keyboardKeyStrokeLatestEvent = null;
        MouseEvent mouseKeyStrokeLastestEvent = null;
        MouseWheelEvent mouseWheelLastestEvent = null;

        try {
            keyboardKeyStrokeLatestEvent = getRecentKeyStoke(keyboardEventListener);
        } catch (CloneNotSupportedException ex) {
            String exceptionMsg = "Unable to retrieve mouse recent event due to CloneNotSupportedException";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, exceptionMsg, ex);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE,"Unable to start idleTime listening",ex);
        }

        try {
            mouseKeyStrokeLastestEvent = getRecentMouseEvent(mouseEventListener);
        } catch (CloneNotSupportedException ex) {
            String exceptionMsg = "Unable to retrieve mouse recent event due to CloneNotSupportedException";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, exceptionMsg, ex);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Unable to retrieve mouse recent event",ex);
        }

        try {
            mouseWheelLastestEvent = getRecentMouseWheelEvent(mouseEventListener);
        } catch (CloneNotSupportedException ex) {
            String exceptionMsg = "Unable to retrieve mouse wheel recent event due to CloneNotSupportedException";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, exceptionMsg, ex);
        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Unable to retrieve mouse wheel recent event",ex);
        }

        long systemCurrentTimestamp = TimeUtility.getSystemClockTimestamp();

        long recentKeyboardEventTimestamp = keyboardKeyStrokeLatestEvent != null
                ? keyboardKeyStrokeLatestEvent.getTimestamp() : systemCurrentTimestamp;

        long mouseKeyStrokeEventTimestamp = mouseKeyStrokeLastestEvent != null
                ? mouseKeyStrokeLastestEvent.getTimestamp() : systemCurrentTimestamp;

        long mouseWheelEventTimestamp = mouseWheelLastestEvent != null
                ? mouseWheelLastestEvent.getTimestamp() : systemCurrentTimestamp;

        long keyboardKeyTimeDifference = Math.abs(systemCurrentTimestamp - recentKeyboardEventTimestamp);
        long mouseKeyStrokeTimeDifference = Math.abs(systemCurrentTimestamp - mouseKeyStrokeEventTimestamp);
        long mouseWheelStrokeTimeDifference = Math.abs(systemCurrentTimestamp - mouseWheelEventTimestamp);
        
        long minValueOfInputEvents = Math.min(keyboardKeyTimeDifference, mouseKeyStrokeTimeDifference);
        minValueOfInputEvents = Math.min(minValueOfInputEvents, mouseWheelStrokeTimeDifference);
        return minValueOfInputEvents;
    }
}
