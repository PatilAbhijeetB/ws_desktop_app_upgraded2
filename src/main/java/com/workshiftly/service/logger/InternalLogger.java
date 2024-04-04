/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.service.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chamara
 */
public class InternalLogger {
    
    private static final LOGGER_LEVEL DEFAULT__LOGGER_LEVEL = LOGGER_LEVEL.ALL;
    
    public enum LOGGER_LEVEL {
        ALL(Level.ALL),
        SEVERE(Level.SEVERE),
        WARNING(Level.WARNING),
        INFO(Level.INFO),
        CONFIG(Level.CONFIG),
        FINE(Level.FINE),
        FINER(Level.FINER),
        FINEST(Level.FINEST),
        OFF(Level.OFF);
        
        private Level javaLoggerLevel;
        
        private LOGGER_LEVEL(Level internalLevel) {
            this.javaLoggerLevel = internalLevel;
        }
        
        Level getJavaLoggerLevel() {
            return this.javaLoggerLevel;
        }
    }
    
    private Logger logger;
       
    InternalLogger(Class loggerClass) {
        this.logger = Logger.getLogger(loggerClass.getName());
    }

    InternalLogger(Class loggerClass, LOGGER_LEVEL level) {
        this.logger = Logger.getLogger(loggerClass.getName());
        this.logger.setLevel(level.javaLoggerLevel);
    }
    
    public void logRecord(LOGGER_LEVEL level, String message, Throwable throwable) {
        this.logger.log(level.javaLoggerLevel, message, throwable);
    }
}
