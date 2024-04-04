/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.constant;

/**
 *
 * @author chamara
 */
public enum AppDirectory {
    
    DATABASE_FILES("bin"),
    TEMP_FILES("temp"),
    LOGS_FILES("logs"),
    RAW_SCREENSHOTS("screenshots"),
    SCRIPTS("scripts");
    
    public String directoryName;
    
    private AppDirectory(String directoryName) {
        this.directoryName = directoryName; 
    }
}
