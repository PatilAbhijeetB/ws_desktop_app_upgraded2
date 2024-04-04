/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.workshiftly.common.constant.AppDirectory;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author chamara
 */
public class FileUtility {
    private static final InternalLogger LOGGER = LoggerService.getLogger(FileUtility.class);

    // System level directory usally a folder existing at user home direcotry
    private final static String SYSTEM_LEVEL_PARENT_DIRECTORY = "Documents";
    // Operating system related file path separator
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    /**
     * Method Name: getRootApplicationDataDirectory
     * Description: retrieve application root level directory where it is the parent of all
     * other specific directory types
     * @return 
     */
    private final static File getRootApplicationDataDirectory() {
        
        String userHome = System.getProperty("user.home", null);
        if (userHome == null) {
            userHome = System.getenv("Home");
        }
        
        File rootDirectory = new File(userHome).getAbsoluteFile();
        String applicationName = DotEnvUtility.ApplicationDataDirectory();
        String rootDirPath = rootDirectory.getAbsolutePath() + FileUtility.FILE_SEPARATOR 
                + SYSTEM_LEVEL_PARENT_DIRECTORY + FILE_SEPARATOR + applicationName;
        
        rootDirectory = new File(rootDirPath);
        
        if (!rootDirectory.isDirectory() || !rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }
        
        return rootDirectory;
    }
    
    
      private static void deleteFileForcefully(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            // Delete all files and subdirectories inside the directory
            Files.walk(path)
                 .sorted((p1, p2) -> -p1.compareTo(p2)) // Delete deeper paths first
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         System.out.println("Error deleting file: " + e.getMessage());
                     }
                 });
        } else {
            // Delete a single file
            Files.delete(path);
        }
    }
         
    /**
     * Method Name: getApplicationLogFile
     * Description: retrieve java.io.File reference corresponded to given file name.
     * if deleteIfExists = true, then delete the existing log file and generate new
     * empty file on it.
     * @param fileName
     * @param deleteIfExists
     * @return
     * @throws Exception 
     */
    public final static File getApplicationLogFile(String fileName, boolean deleteIfExists) throws Exception {
        
        File logFilDirectory = getApplicationResourceDirectory(AppDirectory.LOGS_FILES);
        String logFilePath = logFilDirectory.getAbsolutePath() + FILE_SEPARATOR + fileName;
        File logFile = new File(logFilePath);
        
        if (logFile.exists() && deleteIfExists) {
            logFile.delete();
        }
        return logFile;
        
    }
    
    /**
     * Method Name: getApplicationResourceDirectory
     * Description: retrieve java.io.file object which is corresponded to given directoryType.
     * directory returns from this method is an application specific directory where generated
     * files will be stored in.
     * @param directoryType
     * @return
     * @throws Exception 
     */
    public final static File getApplicationResourceDirectory(AppDirectory directoryType) throws Exception {
        
        File directory = getRootApplicationDataDirectory();
        String rootDirPath = directory.getAbsolutePath();
        String direcotryName = directoryType.directoryName;
        String directoryPath = rootDirPath + FILE_SEPARATOR + direcotryName;
        
        directory = new File(directoryPath);
        
        if (!directory.isDirectory() || !directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }
    
    /**
     * Method Name: getApplicationDownloadDirectoryFile
     * Description: Retrieve File location correspond to platform download folder
     * FilePath : systemUserHome/Downloads/workshiftly/fileName
     * files will be stored in.
     * @param fileName 
     * @return
     * @throws Exception 
     */
    public final static File getApplicationDownloadDirectoryFile(String fileName) throws Exception {
        
        File appDownloadDir = null;
        String systemUserHome = System.getProperty("user.home", null);
        
        if (systemUserHome == null) {
            return appDownloadDir;
        }
        
        String downloadsDir = systemUserHome + FILE_SEPARATOR + "Downloads";
        String applicationName = DotEnvUtility.ApplicationDataDirectory();
        String appDownloadsParentDir = downloadsDir + FILE_SEPARATOR + applicationName;
        
        appDownloadDir = new File(appDownloadsParentDir);
        
        if (!(appDownloadDir.isDirectory() && appDownloadDir.exists())) {
            boolean mkdirs = appDownloadDir.mkdirs();
        }
        else{
         appDownloadDir.delete();
         boolean mkdirs = appDownloadDir.mkdirs();
        }
        
        
        String completeFilePath = appDownloadsParentDir + FILE_SEPARATOR + fileName;
        
        File directoryFile = new File(completeFilePath);
        return directoryFile;
    }
       
    
}
