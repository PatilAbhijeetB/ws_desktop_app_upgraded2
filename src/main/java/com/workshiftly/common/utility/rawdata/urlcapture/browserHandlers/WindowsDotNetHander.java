/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers;

import com.workshiftly.common.constant.AppDirectory;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.WebBrowserLog;
import com.workshiftly.common.utility.FileUtility;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Chanakya
 */
public class WindowsDotNetHander extends UrlCaptureBrowserBaseHandler {
    
    // final class variables
    private static final InternalLogger LOGGER;
    private static final int APP_WAITING_TIMEOUT_SECONDS;
    private static final String EXECUTABLE_FILE_PATH;
    private static final String EXECUTABLE_FILE_NAME;
    private static final String PARAMETERIZED_COMMAND;
    private static final String OS_NAME;
    private static final Map<Browser, WindowsDotNetHander> BROWSER_HANDLERS;
    
    // non-final class variables
    private static File EXECUTABLE_FILE;
    
    private final String command;
    private final Browser webBrowser;
    
    // constuctors
    private WindowsDotNetHander() {
        this.command = null;
        this.webBrowser = null;
    }
    
    // constructor
    private WindowsDotNetHander(Browser browser, ActiveWindow activeWindow) {
        this.webBrowser = browser;
        this.command = String.format( 
                PARAMETERIZED_COMMAND, EXECUTABLE_FILE.getParent(),
                EXECUTABLE_FILE_NAME, webBrowser.name(), activeWindow.getProcessId()
        );
        System.out.println("##### command >>>>> " + this.command);
    }
    
    // static initializer
    static {
        LOGGER = LoggerService.getLogger(WindowsDotNetHander.class);
        APP_WAITING_TIMEOUT_SECONDS = 5;
        EXECUTABLE_FILE_PATH = "/scripts/windows/WS_URL.exe";
        EXECUTABLE_FILE_NAME = "WS_URL.exe";
        PARAMETERIZED_COMMAND = "cmd.exe /c cd \"%s\" && %s \"%s\" \"%s\"";
        OS_NAME = "windows";
        BROWSER_HANDLERS = new HashMap<>();
        init();   
    }
    
    // init method to initialize static variables
    private static void init() {
        try {            
            File scriptDirectory = FileUtility.getApplicationResourceDirectory(
                    AppDirectory.SCRIPTS
            );
            EXECUTABLE_FILE = new File(scriptDirectory, EXECUTABLE_FILE_NAME);
            
            if (!EXECUTABLE_FILE.exists()) {
                InputStream fileInputStream = WindowsDotNetHander.class.getResourceAsStream(
                    EXECUTABLE_FILE_PATH
                );
                byte[] fileContent = new byte[fileInputStream.available()];
                fileInputStream.read(fileContent);
                FileOutputStream fileOutStream = new FileOutputStream(EXECUTABLE_FILE);
                
                try {
                    fileOutStream.write(fileContent);
                } finally {
                    fileInputStream.close();
                    fileOutStream.close();
                }
            }
        } catch (Exception ex) {
            String errorMsg = "Unable to initialize WindowsDotNetHandler";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }
    }
    
    // Get browser handler corresponeded to borwser
    public static UrlCaptureBrowserBaseHandler getHandler(Browser browser, ActiveWindow activeWindow) {
        WindowsDotNetHander browserHandler = BROWSER_HANDLERS.get(browser);
        
        if (browserHandler == null) {
            WindowsDotNetHander dotNetHander = new WindowsDotNetHander(browser, activeWindow);
            BROWSER_HANDLERS.put(browser, dotNetHander);
        }
        return browserHandler;
    }

    // Get WebBrowserLog which contains captured URL, Domain Name and etc
    @Override
    public WebBrowserLog getURL() {
        
        WebBrowserLog webBrowserLog = new WebBrowserLog();
        try {
            String url = captureCurrentURL();
System.out.println("##### command  visited Url>>>>> " + url);
            webBrowserLog.setUrl(url);
            webBrowserLog.setBrowser(webBrowser.name());
            
            String domainName = super.getDomainName(url);
            webBrowserLog.setDomain(domainName);
            webBrowserLog.setOperatingSystem(OS_NAME);
        } catch (Exception ex) {
            String errorMsg = "Unable to capture current URL";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }

        return webBrowserLog;
    }

    @Override
    public boolean isBrowserFileExist() {
        return true;
    }

    // capure current URL
    private String captureCurrentURL() throws Exception {

        Process process = Runtime.getRuntime().exec(command);
        Future<Process> future = process.onExit();

        future.get(APP_WAITING_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        InputStream errorStream = process.getErrorStream();
        String errorTxt = getStringFromInputStream(errorStream);
        
        if (errorTxt != null && !errorTxt.isEmpty()) {
            throw new Exception("Unable to capture current URL");
        }

        InputStream outputStream = process.getInputStream();
        String currentURL = getStringFromInputStream(outputStream);
        return currentURL;
    }

    // Get String from Process runtime
    private String getStringFromInputStream(InputStream inputStream) throws IOException {

        StringBuilder builder;
        try ( InputStreamReader inputReader = new InputStreamReader(inputStream)) {
            BufferedReader bufferedReader = new BufferedReader(inputReader);
            builder = new StringBuilder();
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                builder.append(currentLine);
            }
            bufferedReader.close();
        }

        return builder.toString();
    }

}
