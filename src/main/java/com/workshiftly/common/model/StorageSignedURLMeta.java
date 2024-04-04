/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;

/**
 *
 * @author jade_m
 */
@DatabaseTable(tableName = "storage_meta")
public class StorageSignedURLMeta implements DatabaseModel {
    
    public static final String DATABASE_NAME = "storage_meta";
    
    // field names
    public static final String FIELD_OBJECT_TYPE = "object_type";
    public static final String FIELD_KEY = "key";
    public static final String FIELD_CONTENT_TYPE = "content_type";
    public static final String FIELD_CONTENT_ENCODEING = "content_encoding";
    public static final String FIELD_EXPIRATION = "expiration";
    public static final String FIELD_EXPIRATION_TIMESTAMP = "expiration_timestamp";
    public static final String FIELD_URL = "url";
    public static final String FIELD_COMPLETED = "completed";
    public static final String FIELD_ACTION = "action";
    public static final String FIELD_DATA = "data";
    
    @DatabaseField(generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_OBJECT_TYPE)
    private String objectType;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_KEY)
    private String key;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_CONTENT_TYPE)
    private String contentType;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_CONTENT_ENCODEING)
    private String contentEncoding;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_EXPIRATION)
    private Long expiration;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_EXPIRATION_TIMESTAMP)
    private Long expirationTimestamp;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_URL)
    private String url;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_COMPLETED)
    private boolean completed;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_ACTION)
    private String action;
    
    @DatabaseField(columnName = StorageSignedURLMeta.FIELD_DATA)
    private String data;

    @Override
    public String getDatabaseName() {
        return StorageSignedURLMeta.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    @Override
    public String getId() {
        return key;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(Long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }
}
