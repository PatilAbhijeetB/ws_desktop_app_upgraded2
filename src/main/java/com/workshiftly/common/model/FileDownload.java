/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.model;

import com.workshiftly.common.utility.FileUtility;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 *
 * @author jade_m
 */
public class FileDownload {
    
    public enum Status {
        PENDING,
        STARTED,
        DONWLOADING,
        CANCELED,
        COMPLETED,
        FAILED;
    }
    
    private String fileName;
    private final PublishSubject<Double> progress;
    private Status status;
    private String url;
    private Long totalBytes; 
    
    // default constructor private due to file name should be provided
    private FileDownload() {
        this.progress =  PublishSubject.create();
        this.status = Status.PENDING;
    }
    
    // single argument constructors
    public FileDownload(String fileName) {
        this();
        this.fileName = fileName;
    }
    
    // double argumenet constructors
    public FileDownload(String fileName, String url) {
        this(fileName);
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public Status getStatus() {
        return status;
    }

    public PublishSubject<Double> getProgress() {
        return progress;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }
    
    // start download the file relevant to this object
    public FileDownload start(boolean shouldAsync, boolean deleteIfExists) {
        
        
        
        
        if (status.equals(Status.DONWLOADING) || status.equals(Status.COMPLETED)) {
            return this;
        }
        
        try {
            if (status.equals(Status.PENDING) || status.equals(Status.FAILED)) {
                boolean isAvailable = peek();
                status = isAvailable ? Status.STARTED : Status.FAILED;

                if (status.equals(Status.FAILED)) {
                    return this;
                }
            }
            
            File downloadFile = FileUtility.getApplicationDownloadDirectoryFile(fileName);
            
            if  (deleteIfExists && downloadFile.exists()) {
                boolean isRemoved = downloadFile.delete();
                
                if (!isRemoved) {
                    status = Status.FAILED;
                    return this;
                }
            }
            
            Unirest.get(url).downloadMonitor((
                    var field, var _fileName, var bytesWritten, var _totalBytes) -> {
                
                if (totalBytes == null) {
                    totalBytes = _totalBytes;
                }
                
                status = Status.DONWLOADING;
                double currentProgress = (double) bytesWritten / totalBytes;
                this.progress.onNext(currentProgress);
                
                if (currentProgress >= 1.0) {
                    status = Status.COMPLETED;
                }
            }).asFile(downloadFile.getAbsolutePath());
            
            
        } catch (Exception ex) {
            status = Status.FAILED;
        }
        return this;
    }
    
    // peek object is exists error check: HTTP HEAD method request
    public boolean peek() {
        
        HttpResponse httpResponse = Unirest.head(this.url).asEmpty();
        int statusCode = httpResponse.getStatus();
        boolean isSuccess = statusCode >= 200 && statusCode < 299;
        status = isSuccess ? Status.STARTED : Status.STARTED;
        return isSuccess;
    }
    
   }
