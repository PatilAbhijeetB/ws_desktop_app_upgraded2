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
 * @author chanakya
 */
@DatabaseTable(tableName = "web_browser_log")
public class WebBrowserLog implements DatabaseModel{
    public static final String DATABASE_NAME = "web_browser_log";
    
    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_BROWSER = "browser";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_URL = "url";
    public static final String FIELD_DOMAIN = "domain";
    public static final String FIELD_STARTED_TIMESTAMP = "started_timestamp";
    public static final String FIELD_END_TIMESTAMP = "end_timestamp";
    public static final String FIELD_DURATION = "duration";
    public static final String FIELD_IS_SYNCED = "is_synced";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_COMPANY_ID = "compayn_id";
    public static final String FIELD_OPERATING_SYSTEM = "operating_system";

    @DatabaseField(columnName = WebBrowserLog.FIELD_ROW_ID, generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_BROWSER)
    private String browser;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_TITLE)
    private String title;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_URL)
    private String url;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_DOMAIN)
    private String domain;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_STARTED_TIMESTAMP)
    private Long startedTimestamp;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_END_TIMESTAMP)
    private Long endTimestamp;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_DURATION)
    private Long duration;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_IS_SYNCED, index = true)
    private boolean isSynced;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_USER_ID, index = true)
    private String userId;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_COMPANY_ID)
    private String companyId;
    
    @DatabaseField(columnName = WebBrowserLog.FIELD_OPERATING_SYSTEM)
    private String operatingSystem;
    
    @Override
    public String getDatabaseName() {
        return WebBrowserLog.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }
    public static String getDATABASE_NAME() {
        return DATABASE_NAME;
    }

    public static String getFIELD_ROW_ID() {
        return FIELD_ROW_ID;
    }

    public static String getFIELD_BROWSER() {
        return FIELD_BROWSER;
    }

    public static String getFIELD_TITLE() {
        return FIELD_TITLE;
    }

    public static String getFIELD_URL() {
        return FIELD_URL;
    }

    public static String getFIELD_DOMAIN() {
        return FIELD_DOMAIN;
    }

    public static String getFIELD_STARTED_TIMESTAMP() {
        return FIELD_STARTED_TIMESTAMP;
    }

    public static String getFIELD_END_TIMESTAMP() {
        return FIELD_END_TIMESTAMP;
    }

    public static String getFIELD_DURATION() {
        return FIELD_DURATION;
    }

    public static String getFIELD_IS_SYNCED() {
        return FIELD_IS_SYNCED;
    }

    public static String getFIELD_USER_ID() {
        return FIELD_USER_ID;
    }

    public static String getFIELD_COMPANY_ID() {
        return FIELD_COMPANY_ID;
    }

    public static String getFIELD_OPERATING_SYSTEM() {
        return FIELD_OPERATING_SYSTEM;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setStartedTimestamp(Long startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
