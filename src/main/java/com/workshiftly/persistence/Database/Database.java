/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.persistence.Database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;
import com.workshiftly.common.constant.AppDirectory;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.FileUtility;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Main entry point for database component of persistent layer
 * Database related model should be implements DatabaseModel interface unless that
 * model could not be able to access database persistence
 * @param <T>
 */
public final class Database<T extends DatabaseModel> {
    
    private static final boolean SQLITE_ENCRYPTION_MODE;
    private static final String JDBC_CONNECTION_URL;
    
    private static final BaseDatabaseType DATABASE_TYPE = new SqliteDatabaseType();
    private static final String ENCRYPTION_CIPHER = "aes128cbc";
    private static final String ENCRYPTION_KEY = "Q4MrfxaArVwlTzM9TAzYyaO6G4stdICX";
    private static final String FIELD_ROW_ID = "row_id";
    
    static {
        SQLITE_ENCRYPTION_MODE = DotEnvUtility.SQLITE_ENCRYPTION_MODE();
        JDBC_CONNECTION_URL = SQLITE_ENCRYPTION_MODE 
                ? "jdbc:sqlite:file:%s?cipher=%s&key=%s"
                : "jdbc:sqlite:file:%s";
    }
    
    private Class<T> modelClass;
    private String databaseName;
    private String connectionUrl;
    private JdbcConnectionSource connectionSource;
    private Dao<T, Long> dbAccessObj;
    
    Database(Class<T> modelClass) {
        this.modelClass = modelClass;
    }
    
    /**
     * Method Name: getDatabaseName
     * Description: obtain database name respective model name.
     * @return
     * @throws Exception 
     */
    private String getDatabaseName() throws Exception {
        
        String fieldName = "DATABASE_NAME";
        Field classField = this.modelClass.getField(fieldName);
        
        if (classField.getType() != String.class) {
            throw new Exception("DATABASE_NAME of database model is not String type");
        }
        
        return (String) classField.get(null);
    }
    
    /**
     * Method Name: initialize and open database source connection with respect to database
     * type file
     * @throws Exception 
     */
    void openConnection() throws Exception {
        
        this.databaseName = getDatabaseName() + ".db";
        File appDataDirectory = FileUtility.getApplicationResourceDirectory(AppDirectory.DATABASE_FILES);
        String databasePath = String.format("%s/%s", appDataDirectory, this.databaseName);
        this.connectionUrl = String.format(
                JDBC_CONNECTION_URL, 
                databasePath,
                ENCRYPTION_CIPHER,
                ENCRYPTION_KEY
        );
        
        this.connectionSource = new JdbcConnectionSource(connectionUrl, DATABASE_TYPE);
        this.dbAccessObj = DaoManager.createDao(connectionSource, modelClass);
        
        if (!this.dbAccessObj.isTableExists()) {
            TableUtils.createTable(dbAccessObj);
        }
    }
    
    /**
     * Method Name: closeConnection
     * Description: close and release resources associated with connection source.
     * @throws Exception 
     */
    void closeConnection() throws Exception {
        
        if (this.connectionSource != null) {
            connectionSource.close();
        }
    }
    
    /**
     * Method Name: getDao
     * Description: get database access object DAO and this visibility allowed only 
     * for queryService and package Database
     * @return 
     */
    Dao<T,Long> getDao() {
        return this.dbAccessObj;
    }
    
    /**
     * Method Name: flushData
     * Description: delete all data in database
     * @throws Exception 
     */
    public void flushData() throws Exception {
        TableUtils.clearTable(connectionSource, modelClass);
    }
    
    /**
     * Method Name: create
     * Description: create entity and persist it into database
     * @param entity
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response create(T entity) {
        try {  
            this.dbAccessObj.create(entity);
            String message = "Successfully created data";
            return new Response(false, StatusCode.SUCCESS, message);
            
        } catch (SQLException ex) {
            String errorMsg = "Error Occurred While creating data";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, errorMsg);
        }
    }
    
    @SuppressWarnings("unckecked")
    public Response create(T entity, boolean shouldReturnDbEntity) {
        try {
            Response createResponse = create(entity);
            if (createResponse.isError() || !shouldReturnDbEntity) {
                return createResponse;
            }
            
            T createdEntity;
            QueryBuilder<T, Long> queryBuilder = this.dbAccessObj.queryBuilder();
            if (entity.getId() == null) {
                createdEntity = queryBuilder.orderBy(FIELD_ROW_ID, false)
                        .queryForFirst();
            } else {
                createdEntity = queryBuilder.where()
                        .eq("id", entity.getId())
                        .queryForFirst();
            }
            
            createResponse.setData(new Gson().toJsonTree(createdEntity));
            return createResponse;
            
        } catch (SQLException ex) {
            String errorMsg = "Error Occurred While creating data";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, errorMsg);
        } catch (UnsupportedOperationException ex) {
            return new Response(true, StatusCode.DATABASE_EXCEPTION, ex.getMessage());
        }
    }
    
    /**
     * Method Name: create
     * Description: create entities and persist it into database
     * @param entites
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response create(List<T> entites) {
        try {
            this.dbAccessObj.create(entites);
            String message = "Successfully created data";
            return new Response(false, StatusCode.SUCCESS, message);
        } catch (SQLException ex) {
            String message = "Error occurred while creating data";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, message);
        }
    }
    
    /**
     * Method Name: update
     * Description: update entities and persist it into database
     * @param entity 
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response update(T entity) {
        try {
            Long rowId = entity.getRowId();
            boolean didIdExisting = dbAccessObj.idExists(rowId);
            
            if (!didIdExisting) {
                String message = "id of updating entity does not exist";
                return new Response(true, StatusCode.DATABASE_ERROR, message);
            }
            
            dbAccessObj.update(entity);
            return new Response(false, StatusCode.SUCCESS, "successfully updated data entity");
        } catch (SQLException ex) {
            String message = "Error ocurred while updaing entity";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, message);
        }
    }
    
    /**
     * Method Name: update
     * Description: update entities and persist it into database
     * @param preparedUpdate
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response update(PreparedUpdate<T> preparedUpdate) {
        try {
            int updatedRows = dbAccessObj.update(preparedUpdate);
            JsonObject result = new JsonObject();
            result.addProperty("updatedRows", updatedRows);
            return new Response(false, StatusCode.SUCCESS, "successfully updated data", result);
        } catch (SQLException ex) {
            String message = "Error ocurred while updaintg data";
            return new Response(true, StatusCode.APPLICATION_ERROR, message);
        }
    }
    
    /**
     * Method Name: delete
     * Description: delete entity from database
     * @param entity 
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response delete(T entity) {
        try {
            Long rowId = entity.getRowId();
            boolean idExist = dbAccessObj.idExists(rowId);
            
            if (!idExist) {
                String message = "id of deleting entity does not exist";
                return new Response(true, StatusCode.DATABASE_ERROR, message);
            }
            
            dbAccessObj.delete(entity);
            return new Response(false, StatusCode.SUCCESS, "Successfully deleted entity");
        } catch (SQLException ex) {
            String message = "Error occurred while deleting entity";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, message);
        }
    }
    
    /**
     * Method Name: delete
     * Description: delete entities from database
     * @param entities  
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response delete(List<T> entities) {
        try {
            dbAccessObj.delete(entities);
            return new Response(false, StatusCode.SUCCESS, "Successfully deleted entities");
        } catch (SQLException ex) {
            String message = "Error ouccrred while deleting entites";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, message);
        }
    }
    
    /**
     * Method Name: delete
     * Description: delete entities from database by Collection
     * @param entities  
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response delete(Collection<T> entities) {
        try {
            int deletedRecordsCount = dbAccessObj.delete(entities);
            return new Response(false, StatusCode.SUCCESS, "Successfully deleted entities");
        } catch (SQLException ex) {
            String message = "Error ouccrred while deleting entites";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, message);
        }
    }
    
    /**
     * Method Name: getAllEntities
     * Description: Retrieve all entities from database  
     * @return 
     */
    @SuppressWarnings("unchecked")
    public Response getAllEntities() {
        try {
            List<T> entities = dbAccessObj.queryForAll();
            String message = "Sucessfully retrieved all the records";
            return new Response(false, StatusCode.SUCCESS, message, new Gson().toJsonTree(entities));
        } catch (SQLException ex) {
            String message = "Error occured while retrieving all entites";
            return new Response(true, StatusCode.DATABASE_EXCEPTION, message);
        }
    }
    
    /**
     * Method Name: getQueryBuilder
     * Description: Retrieve query builder related to database class  
     * @return 
     */
    public QueryBuilder<T, Long> getQueryBuilder() {
        QueryBuilder<T, Long> queryBuilder = this.dbAccessObj.queryBuilder();
        return queryBuilder;
    }
    
    /**
     * Method Name: getUpdateBuilder
     * Description: Retrieve update builder related to database class  
     * @return 
     */
    public UpdateBuilder<T,Long> getUpdateBuilder() {
        UpdateBuilder<T, Long> updateBuilder = this.dbAccessObj.updateBuilder();
        return updateBuilder;
    }
    
    /**
     * Method Name: getDeleteBuilder
     * Description: Retrieve DeleteBuilder instance relavant to model 
     * @return 
     */
    public DeleteBuilder<T, Long> getDeleteBuilder() {
        return this.dbAccessObj.deleteBuilder();
    }
}
