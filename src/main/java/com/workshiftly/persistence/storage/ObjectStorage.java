/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.persistence.storage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.sun.jna.platform.win32.WinGDI;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.model.StorageSignedURLMeta;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseModel;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jade_m
 */
public class ObjectStorage {
    private static final InternalLogger LOGGER = LoggerService.getLogger(ObjectStorage.class);
    
    UserSession userSession;
    
    private ObjectStorage() {}
    
    public ObjectStorage(UserSession userSession) throws AuthenticationException {
        AppValidator.validateUserSession(userSession);
        this.userSession = userSession;
    }
    
    public static class StorageOperationNotSupportException extends Exception {
        public StorageOperationNotSupportException(String msg) {
            super(msg);
        }
    }
    
    public void initScreenshotStorageURLMeta(
            List<Screenshot> screenshots, 
            String action, 
            String contentEncoding, 
            Long expiration
    ) throws Exception {
        
        Gson gson = new Gson();
        List<StorageSignedURLMeta> metaObjs = new ArrayList<>();
        Map<String, Screenshot> screenshotMap = new HashMap<>();
                
        screenshots.forEach((Screenshot screenshot) -> {
            StorageSignedURLMeta metaObj = new StorageSignedURLMeta();
            metaObj.setObjectType(Screenshot.DATABASE_NAME);
            metaObj.setAction(action);
            metaObj.setExpiration(expiration);
            metaObj.setKey(screenshot.getFileName());
//            metaObj.setContentType(screenshot.getMimeType());
//            metaObj.setContentEncoding(contentEncoding);
            metaObjs.add(metaObj);
            
            screenshotMap.put(screenshot.getFileName(), screenshot);
        });

        JsonArray metaObjects = gson.toJsonTree(metaObjs).getAsJsonArray();
        
        HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
        Response apiResponse = httpRequestCaller.callGetScreenshotUploadPresignedURLs(
                userSession, metaObjects
        );

        if (apiResponse.isError())
            throw new Exception("Network Exception");

        Type metaObjListType = new TypeToken<List<StorageSignedURLMeta>>(){}.getType();
        List<StorageSignedURLMeta> storageMetaObjs = gson.fromJson(
                apiResponse.getData(), metaObjListType
        );

        storageMetaObjs.forEach((var metaObj) -> {
            String fileName = metaObj.getKey();
            Screenshot screenshot = screenshotMap.get(fileName);
            
            String data = screenshot != null ? screenshot.getData() : null;
            metaObj.setData(data);
            
            Long expirationTimestamp = TimeUtility.getCurrentTimestamp() + expiration;
            metaObj.setExpirationTimestamp(expirationTimestamp);
        });

        try {
            Database<StorageSignedURLMeta> _database = 
                    DatabaseProxy.openConnection(StorageSignedURLMeta.class);
            _database.create(storageMetaObjs);
        } catch (Exception ex) {
            String errorMsg = "Unable to init storage requests";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        } finally {
            DatabaseProxy.closeConnection(StorageSignedURLMeta.class);
        }
    }
    
    public <T extends DatabaseModel> void  cleanCompletedJobs(
            String action, 
            Class<T> refClass
    ) {
        try {
            String objectType = null;
            if (refClass == Screenshot.class) {
                objectType = Screenshot.DATABASE_NAME;
            }
            
            Database<StorageSignedURLMeta> database = 
                    DatabaseProxy.openConnection(StorageSignedURLMeta.class);
            QueryBuilder<StorageSignedURLMeta, Long> queryBuilder = database.getQueryBuilder();
            queryBuilder.where()
                    .eq(StorageSignedURLMeta.FIELD_ACTION, action)
                    .and()
                    .eq(StorageSignedURLMeta.FIELD_OBJECT_TYPE, objectType)
                    .and()
                    .eq(StorageSignedURLMeta.FIELD_COMPLETED, true);
            List<StorageSignedURLMeta> completedJobs = queryBuilder.query();
            
            if (!completedJobs.isEmpty()) {
                if (refClass == Screenshot.class) {
                    try {
                        Database<Screenshot> screnshotDB 
                            = DatabaseProxy.openConnection(Screenshot.class);
                        UpdateBuilder<Screenshot, Long> updateBuilder = screnshotDB.getUpdateBuilder();

                        List<String> completedFileNames = completedJobs.stream()
                                .map(StorageSignedURLMeta::getKey).collect(Collectors.toList());
                        updateBuilder.updateColumnValue(Screenshot.FIELD_IS_SYNCED, true);
                        updateBuilder.where().in(Screenshot.FIELD_FILE_NAME, completedFileNames);
                        updateBuilder.update();
                        
                        QueryBuilder<Screenshot, Long> screenshotQB = screnshotDB.getQueryBuilder();
                        screenshotQB.where().in(Screenshot.FIELD_FILE_NAME, completedFileNames);
                        List<Screenshot> completedScreenshots = screenshotQB.query();
                        
                        if (!completedScreenshots.isEmpty()) {
                            // sync screenshots from here
                            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
                            Response apiResponse = httpRequestCaller
                                    .callPostSyncScreenshots(userSession, completedScreenshots);
                            
                            if (!apiResponse.isError()) {
                                List<Long> completedRowIds = completedScreenshots.stream()
                                        .map(Screenshot::getRowId)
                                        .collect(Collectors.toList());
                                UpdateBuilder<Screenshot, Long> _UpdateBuilder 
                                        = screnshotDB.getUpdateBuilder();
                                _UpdateBuilder.updateColumnValue(Screenshot.FIELD_IS_SYNCED, true);
                                _UpdateBuilder.where().in(Screenshot.FIELD_ROW_ID, completedRowIds);
                                _UpdateBuilder.update();
                            }
                        }
                    } finally {
                        DatabaseProxy.closeConnection(Screenshot.class);
                    }
                }
            
                database.delete(completedJobs);
            }               
        } catch (Exception ex) {
            String errorMsg = "Unable to clean completed job at storage";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        } finally {
            DatabaseProxy.closeConnection(StorageSignedURLMeta.class);
        }
    }
    
    public <T extends DatabaseModel> void completePendingJobs(
            String action, 
            Class<T> refClass
    ) throws Exception {
        try {
            String objectType = null;
            if (refClass == Screenshot.class) {
                objectType = Screenshot.DATABASE_NAME;
            }
            
            List<StorageSignedURLMeta> pendingJobs = null;
            try {
                Database<StorageSignedURLMeta> database = 
                    DatabaseProxy.openConnection(StorageSignedURLMeta.class);
                QueryBuilder<StorageSignedURLMeta, Long> queryBuilder = 
                        database.getQueryBuilder();
                queryBuilder.where()
                        .eq(StorageSignedURLMeta.FIELD_OBJECT_TYPE, objectType)
                        .and()
                        .eq(StorageSignedURLMeta.FIELD_ACTION, action)
                        .and()
                        .eq(StorageSignedURLMeta.FIELD_COMPLETED, false);
                pendingJobs = queryBuilder.query();
            } finally {
                DatabaseProxy.closeConnection(StorageSignedURLMeta.class);
            }
            
            if (pendingJobs != null) {
                switch (action) {
                    case "putObject":
                        handlePutObjectJobs(refClass, pendingJobs);
                        break;
                    case "getObject":
                        break;
                    default:
                        throw new StorageOperationNotSupportException(
                                "Operation is not supported"
                        );
                }
            }
        } catch (StorageOperationNotSupportException ex) {
            throw ex;
        } catch (Exception ex) {
            String errorMsg = "Unable to complete storage jobs";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
           throw ex;
        }
    }
    
    private <T extends DatabaseModel> void handlePutObjectJobs(
            Class<T> refClass, 
            List<StorageSignedURLMeta> pendingJobs
    ) {
        if (refClass == Screenshot.class) {
            completeScreenshotPutObjectJobs(pendingJobs);
        }
    }
    
    private void completeScreenshotPutObjectJobs(List<StorageSignedURLMeta> pendingJobs) {
        
        try {
            Long currentTimestamp = TimeUtility.getCurrentTimestamp();
            
            Database<StorageSignedURLMeta> database = 
                    DatabaseProxy.openConnection(StorageSignedURLMeta.class);
            
            for (StorageSignedURLMeta currentJob : pendingJobs) {
                Long expirationTimestamp = currentJob.getExpirationTimestamp();
                if (expirationTimestamp <= currentTimestamp) {
                    database.delete(currentJob);
                    continue;
                }
                
                String base64Data = currentJob.getData();
                byte[] decodeBase64 = Base64.decodeBase64(base64Data);
                
                HttpResponse<String> httpResponse = Unirest.put(currentJob.getUrl())
                        .header("Content-Type", currentJob.getContentType())
                        .header("Content-Encoding", currentJob.getContentEncoding())
                        .body(decodeBase64)
                        .asString();
                
                if (httpResponse.isSuccess()) {
                    currentJob.setCompleted(true);
                    database.update(currentJob);
                }
                
            }
        } catch (Exception ex) {
            String errorMsg = "Unable to handle stotage putObject jobs";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        } finally {
            DatabaseProxy.closeConnection(StorageSignedURLMeta.class);
        }
    }
}
