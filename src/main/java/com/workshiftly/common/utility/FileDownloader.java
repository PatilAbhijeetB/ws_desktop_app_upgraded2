/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.workshiftly.common.model.FileDownload;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jade_m
 */
public class FileDownloader {
    // global static final variables
    private static final FileDownloader SINGLETON;
    private static final Map<String, FileDownload> FILE_DOWNLOADS;
    
    // static initializer
    static {
        SINGLETON = new FileDownloader();
        FILE_DOWNLOADS = new HashMap<>();
    }
    
    // default contructor
    private FileDownloader() {}
    
    // add download job to file download manager
    public static final FileDownload add(FileDownload fileDownload) {
        
        String fileName = fileDownload.getFileName();
        
        if (FILE_DOWNLOADS.containsKey(fileName)) {
            // indicate whether it is alreay in this queue
            //TODO: enhance the later
            FILE_DOWNLOADS.remove(fileName);
        }
        FILE_DOWNLOADS.put(fileName, fileDownload);
        fileDownload.setStatus(FileDownload.Status.PENDING);
        return fileDownload;
    }
    
    // get FileDownload object related to the fileName
    public static final FileDownload get(String fileName) {
        return FILE_DOWNLOADS.get(fileName);
    }
    
    // check FileDownload job is already exists or not in download queue
    public static final boolean contains(String fileName) {
        return FILE_DOWNLOADS.containsKey(fileName);
    }
}
