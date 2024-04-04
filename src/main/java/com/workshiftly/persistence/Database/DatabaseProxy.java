/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.persistence.Database;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseProxy {
    private static final InternalLogger LOGGER = LoggerService.getLogger(DatabaseProxy.class);

    /**
     * Database entity collection for storing initialized database entities
     */
    private static final Map<String, Database> DATABASE_COLLECTION = new HashMap<>();
    /**
     * ReentrantLock collection
     */
    private static final Map<String, ReentrantLock> LOCK_COLLECTION = new HashMap<>();
    
    /**
     * MethodName: openConnection
     * 
     * Description: this method used to initialize database entity for DatabaseModel class. All the initializing
     * and database concurrent thread access will be handled from this method
     * 
     * @param <T>
     * @param modelClass should be implemented DatabaseModel interface
     * @return
     * @throws Exception 
     */
    public static <T extends DatabaseModel> Database<T> openConnection(Class<T> modelClass) throws Exception {
        
        try {
            String modelName = modelClass.getName();
            ReentrantLock databaseLock = LOCK_COLLECTION.get(modelName);
            
            if (databaseLock == null) {
                databaseLock = new ReentrantLock(true);
                LOCK_COLLECTION.put(modelName, databaseLock);
            }
            
            boolean isLockHoldByCurrentThread = databaseLock.isHeldByCurrentThread();
            
            if (isLockHoldByCurrentThread) {
                return DATABASE_COLLECTION.get(modelName);
            }
            
            boolean didLockedDatabase = databaseLock.tryLock();
            
            while(!didLockedDatabase) {
                didLockedDatabase = databaseLock.tryLock();
            }
            
            Database<T> database = new Database<>(modelClass);
            database.openConnection();
            
            LOCK_COLLECTION.put(modelName, databaseLock);
            DATABASE_COLLECTION.put(modelName, database);
            return database;
        } catch (Exception ex) {
            String msg = "Error Occurred While Opening Database Connection - " + modelClass.getName();
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, msg, ex);
            throw ex;
        }
    }
    
    /**
     * MethodName: openConnection
     * 
     * Description: this method used to close database connection and unlock concurrent access lock
     * to utilize another thread which is waiting to access database or incoming thread
     * 
     * @param <T>
     * @param modelClass should be implemented DatabaseModel interface
     */
    public static <T extends DatabaseModel> void closeConnection(Class<T> modelClass) {
        
        try {
            String modelName = modelClass.getName();
            Database<DatabaseModel> database = DATABASE_COLLECTION.get(modelName);
            if (database != null) {
                database.closeConnection();
                DATABASE_COLLECTION.remove(modelName);
            }
            
            ReentrantLock databaseLock = LOCK_COLLECTION.get(modelName);
            if (databaseLock != null) {
                databaseLock.unlock();
            }
        } catch (Exception ex) {
            String msg = "Error Occurred While Closing Database Connection - " + modelClass.getName();
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, msg, ex);
        }
    } 
}
