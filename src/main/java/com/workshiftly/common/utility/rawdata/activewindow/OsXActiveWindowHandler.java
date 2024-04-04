/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.activewindow;

import com.google.gson.JsonObject;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.rawdata.activewindow.ActiveWindowUtility.ActiveWindowUtilityException;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author chamara
 */
public class OsXActiveWindowHandler extends ActivityWindowBaseHandler {
    private static final InternalLogger LOGGER = LoggerService.getLogger(OsXActiveWindowHandler.class);

    private final static String OS_NAME = "MAC_OSX";
    private final static OsXActiveWindowHandler INSTANCE = new OsXActiveWindowHandler();
    
    private final static String SUCUESS_KEY = "successTxt";
    private final static String ERROR_KEY = "errorTxt";
    
    private Runtime runtime;
    
    private OsXActiveWindowHandler() {
        runtime = Runtime.getRuntime();
    }
    
    static synchronized OsXActiveWindowHandler getInstance() {
        return OsXActiveWindowHandler.INSTANCE;
    }

    @Override
    public ActiveWindow getCurrentActivityWindow() {
        
        ActiveWindow activeWindow = new ActiveWindow();
        activeWindow.setOperatingSystem(OS_NAME);
        
        try {
            JsonObject frontmostWindow = getCurrentFrontmostWindowDetails();
            System.out.println(frontmostWindow);
            
            activeWindow.setAppName(frontmostWindow.get(ActiveWindow.FIELD_APP_NAME).getAsString());
            activeWindow.setTitle(frontmostWindow.get(ActiveWindow.FIELD_TITLE).getAsString());
            activeWindow.setProcessId(frontmostWindow.get(ActiveWindow.FIELD_PROCESS_ID).getAsString());
            
        } catch (ActiveWindowUtilityException | IOException ex) {
            activeWindow.setIsPartial(false);
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Fail to get Current Activity Window - OsXActiveWindowHandler", ex);
        }
        return activeWindow;
    }
    
    private JsonObject executeAppleScript() throws IOException {
        
        final String[] scriptArgs = {
            "osascript", "-e",
            "﻿global front_app, window_name, process_id", "-e",
            "tell application \"System Events\"", "-e",
            "set front_app to item 1 of (get name of processes whose frontmost is true)", "-e",
            "set process_id to unix id of process front_app", "-e",
            "tell process front_app", "-e",
            "try", "-e",
            "tell (1st window whose value of attribute \"AXMain\" is true)", "-e",
            "set window_name to value of attribute \"AXTitle\"", "-e",
            "end tell", "-e",
            "on error", "-e",
            "set window_name to front_app", "-e",
            "end try", "-e",
            "end tell", "-e",
            "end tell", "-e",
            "return {front_app, window_name, process_id}"
        };
        
        JsonObject resultObject = executeScript(scriptArgs);
        return resultObject;
    }
    
    private JsonObject executeScript(String[] scriptArgs) throws IOException {
        
        Process process = runtime.exec(scriptArgs);
        CompletableFuture<Process> completableFuture = process.onExit();

        while (!completableFuture.isDone()) {}

        InputStream errorStream = process.getErrorStream();
        String errorTxt = getStringFromInputStream(errorStream);

        InputStream outputStream = process.getInputStream();
        String successTxt = getStringFromInputStream(outputStream);
        
        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(ERROR_KEY, errorTxt);
        outputObject.addProperty(SUCUESS_KEY, successTxt);
        return outputObject;
    }
    
    private String getStringFromInputStream(InputStream inputStream) throws IOException {
        
        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputReader);
        StringBuilder builder = new StringBuilder();
        
        String currentLine = null;
        while ((currentLine = bufferedReader.readLine()) != null) {
            builder.append(currentLine);
        }
        
        return builder.toString();
    }
    
    private JsonObject getCurrentFrontmostWindowDetails() throws IOException, ActiveWindowUtilityException {
        
        JsonObject scriptResult = executeAppleScript();
        
        boolean isErrorResult = !AppValidator.isNullOrEmptyOrBlank(scriptResult.get(ERROR_KEY).getAsString());
        if (isErrorResult) {
            throw new ActiveWindowUtilityException("Applescript dispatched error stream");
        }
        
        String resultTxt = scriptResult.get(SUCUESS_KEY).getAsString();
        final String delimiter = ",";
        String[] stringParticles = resultTxt.split(delimiter);
        
        for (int currentIdx = 0; currentIdx < stringParticles.length; currentIdx++) {
            stringParticles[currentIdx] = stringParticles[currentIdx].strip();
        }
        
        JsonObject outputObject = new JsonObject();
        outputObject.addProperty(ActiveWindow.FIELD_APP_NAME, stringParticles[0]);
        outputObject.addProperty(ActiveWindow.FIELD_TITLE, stringParticles[1]);
        outputObject.addProperty(ActiveWindow.FIELD_PROCESS_ID, stringParticles[2]);
        
        return outputObject;
    }
}
