/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.sun.jna.Platform;

/**
 *
 * @author hashan
 */

public enum OSUtility {
    WINDOWS("windows"),
    MACOS("macos"),
    LINUX("linux"),
    OTHER("other");

    private final String strValue;
    
    private OSUtility(String value) {
        this.strValue = value;
    }
    
    @Override 
    public String toString() { 
        return this.strValue; 
    }
    
    public static OSUtility getSystemOS() {
        if (Platform.isLinux()) {
            return LINUX;
        } else if (Platform.isWindows()) {
            return WINDOWS;
        } else if (Platform.isMac()) {
            return MACOS;
        } else {
            return OTHER;
        }
    }
}
