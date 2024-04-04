/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.persistence.Database.DatabaseModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author chamara
 */
@DatabaseTable(tableName = "activity_windows")
public class ActiveWindow implements DatabaseModel, Comparable<ActiveWindow> {

    public static final String DATABASE_NAME = "activity_windows";
    
    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_PROCESS_ID = "process_id";
    public static final String FIELD_APP_NAME = "app_name";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_STARTED_TIMESTAMP = "started_timestamp";
    public static final String FIELD_END_TIMESTAMP = "end_timestamp";
    public static final String FIELD_FOCUS_DURATION = "focus_duration";
    public static final String FIELD_IS_SYNCED = "is_synced";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_COMPANY_ID = "compayn_id";
    public static final String FIELD_IS_PARTIAL = "is_partial";
    public static final String FIELD_OPERATING_SYSTEM = "operating_system";
    public static final String FIELD_DEVICE_ID = "deviceId";
   
    @DatabaseField(columnName = ActiveWindow.FIELD_ROW_ID, generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_PROCESS_ID)
    private String processId;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_APP_NAME)
    private String appName;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_TITLE)
    private String title;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_STARTED_TIMESTAMP)
    private Long startedTimestamp;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_END_TIMESTAMP)
    private Long endTimestamp;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_FOCUS_DURATION)
    private Long focusDuration;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_IS_SYNCED, index = true)
    private boolean isSynced;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_USER_ID, index = true)
    private String userId;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_COMPANY_ID)
    private String companyId;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_IS_PARTIAL)
    private boolean isPartial = false;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_OPERATING_SYSTEM)
    private String operatingSystem;
    
    @DatabaseField(columnName = ActiveWindow.FIELD_DEVICE_ID)
    private String deviceId;
    
    @Override
    public String getDatabaseName() {
        return ActiveWindow.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStartedTimestamp() {
        return startedTimestamp;
    }

    public void setStartedTimestamp(Long startedTimestamp) {
        this.startedTimestamp = startedTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Long getFocusDuration() {
        return focusDuration;
    }

    public void setFocusDuration(Long focusDuration) {
        this.focusDuration = focusDuration;
    }

    public boolean getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public boolean isIsPartial() {
        return isPartial;
    }

    public void setIsPartial(boolean isPartial) {
        this.isPartial = isPartial;
    }

    public String isOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
    public String getdeviceId() {
           return deviceId;
       }

   public void setdeviceId(String deviceId) {
     this.deviceId = deviceId;
   }
    
    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(ActiveWindow t) {
       
        ArrayList<Integer> comparisonBits = new ArrayList<>();
        comparisonBits.add(Math.abs(this.processId.compareTo(t.processId)));
        comparisonBits.add(Math.abs(this.appName.compareTo(t.appName)));
        comparisonBits.add(Math.abs(this.title.compareTo(t.title)));
        
        int result = comparisonBits.stream().reduce(0, Integer::sum);
        return result;
    }
    
    public boolean isValidRecord() {
        return !AppValidator.isNullOrEmptyOrBlank(this.processId)
                && !AppValidator.isNullOrEmptyOrBlank(this.appName)
                && !AppValidator.isNullOrEmptyOrBlank(this.title)
                && this.startedTimestamp > 0 && this.endTimestamp > 0
                && this.startedTimestamp < this.endTimestamp;
    }
    
    public static boolean isDuplicateRecord(ActiveWindow obj1, ActiveWindow obj2) {
        
        return obj1.compareTo(obj2) == 0 
                && Objects.equals(obj1.getStartedTimestamp(), obj2.getStartedTimestamp())
                && Objects.equals(obj1.getEndTimestamp(), obj2.getEndTimestamp());
    }
}
