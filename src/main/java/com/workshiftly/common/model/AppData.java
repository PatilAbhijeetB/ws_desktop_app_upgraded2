/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.workshiftly.persistence.Database.DatabaseModel;

@DatabaseTable(tableName = "appData")
public class AppData implements DatabaseModel {
    
    public static final String DATABASE_NAME = "appData";
    public static final String TYPE_FIELD_NAME = "type";
    public static final String DATA_FIELD_NAME = "data";
    
    @DatabaseField(generatedId = true)
    private Long rowId;
    
    @DatabaseField(canBeNull = false)
    private String type;
    
    @DatabaseField(canBeNull = false)
    private String data;
    
    public AppData(String type, String data) {
        this.type = type;
        this.data = data;
    }
    
    public AppData() {
    }
    
    public Long getRowId() {
        return rowId;
    }

    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    public String getId() {
        return this.type;
    }
}
