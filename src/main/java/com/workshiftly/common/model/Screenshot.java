    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;
import java.awt.image.BufferedImage;

/**
 *
 * @author chamara
 */
@DatabaseTable(tableName = "screenshot")
public class Screenshot  implements DatabaseModel{
    
    public static final String DATABASE_NAME = "screenshot";
    
    public static final String FIELD_ROW_ID = "row_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_COMPANY_ID = "company_id";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_DATA = "data";
    public static final String FIELD_MIME_TYPE = "mime_type";
    public static final String FIELD_FILE_NAME = "file_name";
    public static final String FIELD_IS_SYNCED = "is_synced";
    
    @DatabaseField(columnName = Screenshot.FIELD_ROW_ID, generatedId = true)
    private Long rowId;
    
    @DatabaseField(columnName = Screenshot.FIELD_USER_ID, index = true)
    private String userId;
    
    @DatabaseField(columnName = Screenshot.FIELD_COMPANY_ID)
    private String companyId;
    
    @DatabaseField(columnName = Screenshot.FIELD_TIMESTAMP)
    private Long timestamp;
    
    @DatabaseField(columnName = Screenshot.FIELD_DATA, dataType = DataType.STRING)
    private String data;
    
    @DatabaseField(columnName = Screenshot.FIELD_FILE_NAME)
    private String fileName;
    
    @DatabaseField(columnName = Screenshot.FIELD_IS_SYNCED, index = true)
    private boolean synced = false;
    
    @DatabaseField(columnName = Screenshot.FIELD_MIME_TYPE)
    private String mimeType;
    
    private BufferedImage bufferedImage;

    @Override
    public String getDatabaseName() {
        return Screenshot.DATABASE_NAME;
    }

    @Override
    public Long getRowId() {
        return this.rowId;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    @Override
    public String getId() {
        return null;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
