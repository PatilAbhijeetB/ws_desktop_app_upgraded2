/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.io;

import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;

/**
 *
 * @author chamara
 */
public class MouseEventListener implements NativeMouseListener, NativeMouseWheelListener, NativeMouseMotionListener {
    private static final InternalLogger LOGGER = LoggerService.getLogger(MouseEventListener.class);

    private MouseEvent mouseClickedEvent;
    private MouseEvent mousePressedEvent;
    private MouseEvent mouseReleasedEvent;
    private MouseEvent mouseMovedEvent;
    private MouseEvent mouseDragEvent;
    private MouseWheelEvent mouseWheelEvent;
    
    public MouseEventListener() {
        
        long currentTimestamp = TimeUtility.getSystemClockTimestamp();
        
        this.mouseClickedEvent = new MouseEvent(null, currentTimestamp);
        this.mousePressedEvent = new MouseEvent(null, currentTimestamp);
        this.mouseReleasedEvent = new MouseEvent(null, currentTimestamp);
        this.mouseMovedEvent = new MouseEvent(null, currentTimestamp);
        this.mouseDragEvent = new MouseEvent(null, currentTimestamp);
        this.mouseWheelEvent = new MouseWheelEvent(null, currentTimestamp);
    }
    
    @Override
    public void nativeMouseClicked(NativeMouseEvent mouseEvent) {
        long timestamp = TimeUtility.getSystemClockTimestamp();
        
        if (mouseClickedEvent == null) {
            mouseClickedEvent = new MouseEvent(mouseEvent, timestamp);
        } else {
            mouseClickedEvent.setValues(mouseEvent, timestamp);
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent mouseEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        if (mousePressedEvent == null) {
            mousePressedEvent = new MouseEvent(mouseEvent, timestamp);
        } else {
            mousePressedEvent.setValues(mouseEvent, timestamp);
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent mouseEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        if (mouseReleasedEvent == null) {
            mouseReleasedEvent = new MouseEvent(mouseEvent, timestamp);
        } else {
            mouseReleasedEvent.setValues(mouseEvent, timestamp);
        }
    }
    
    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent mouseEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        if (mouseWheelEvent == null) {
            mouseWheelEvent = new MouseWheelEvent(mouseEvent, timestamp);
        } else {
            mouseWheelEvent.setValues(mouseEvent, timestamp);
        }
    }
    
    @Override
    public void nativeMouseMoved(NativeMouseEvent mouseEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        if (mouseMovedEvent == null) {
            mouseMovedEvent = new MouseEvent(mouseEvent, timestamp);
        } else {
            mouseMovedEvent.setValues(mouseEvent, timestamp);
        }
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent mouseEvent) {
        
        long timestamp = TimeUtility.getSystemClockTimestamp();
        if (mouseDragEvent == null) {
            mouseDragEvent = new MouseEvent(mouseEvent, timestamp);
        } else {
            mouseDragEvent.setValues(mouseEvent, timestamp);
        }
    }

    public MouseEvent getMouseClickedEvent() throws CloneNotSupportedException {
        return mouseClickedEvent != null ? (MouseEvent) mouseClickedEvent.clone() : null;
    }

    public MouseEvent getMousePressedEvent() throws CloneNotSupportedException {
        return mousePressedEvent != null ? (MouseEvent) mousePressedEvent.clone() : null;
    }

    public MouseEvent getMouseReleasedEvent() throws CloneNotSupportedException {
        return mouseReleasedEvent != null ? (MouseEvent) mouseReleasedEvent.clone() : null;
    }
    
    public MouseWheelEvent getMouseWheelEvent() throws CloneNotSupportedException {
        return mouseWheelEvent != null ? (MouseWheelEvent) mouseWheelEvent.clone() : null;
    }
    
    public MouseEvent getMouseMovedEvent() throws CloneNotSupportedException {
        return mouseMovedEvent != null ? (MouseEvent) mouseMovedEvent.clone() : null;
    }
    
    public MouseEvent getMouseDraggedEvent() throws CloneNotSupportedException {
        return mouseDragEvent != null ? (MouseEvent) mouseDragEvent.clone() : null;
    }
}
