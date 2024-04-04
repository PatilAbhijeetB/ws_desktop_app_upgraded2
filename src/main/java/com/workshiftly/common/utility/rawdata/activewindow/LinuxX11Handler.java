/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.activewindow;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.ptr.IntByReference;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.utility.rawdata.activewindow.ActiveWindowUtility.ActiveWindowUtilityException;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.IOException;

/**
 *
 * @author chamara
 */
class LinuxX11Handler extends ActivityWindowBaseHandler {
    private static final InternalLogger LOGGER = LoggerService.getLogger(LinuxX11Handler.class);

    private static final String OS_NAME = "LINUX_X11";
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final LinuxX11Handler INSTANCE = new LinuxX11Handler();
    
    private LinuxX11Handler() {}
    
    synchronized static ActivityWindowBaseHandler getInstance() {
        return LinuxX11Handler.INSTANCE;
    }
    
    private interface X11Extended extends X11 {
        X11Extended X11_EXTENDED_INSTANCE = (X11Extended) Native.load("X11", X11Extended.class);
        void XGetInputFocus(X11.Display display, X11.WindowByReference focusWindow, IntByReference revertToReturn);
    }

    @Override
    public ActiveWindow getCurrentActivityWindow() {
        
        ActiveWindow activeWindow = new ActiveWindow();
        activeWindow.setOperatingSystem(OS_NAME);
        
        try {
            String windowPid = getActiveWindowPid();
            activeWindow.setProcessId(windowPid);
            
            String appName = getApplicationName(windowPid);
            activeWindow.setAppName(appName);
            
            String windowTitle = getWindowTitle();
            activeWindow.setTitle(windowTitle);
        } catch (Exception ex) {
            LOGGER.logRecord(
                InternalLogger.LOGGER_LEVEL.SEVERE, 
                "failed to get Current Activity Window - LinuxX11Handler", 
                ex
            );
            activeWindow.setIsPartial(true);
        }
        
        return activeWindow;
    }
    
    private String getCurrentWindowName_Experimental() throws Exception {
            
        X11Extended x11Lib = X11Extended.X11_EXTENDED_INSTANCE;
        X11.Display xOpenDisplay = x11Lib.XOpenDisplay(null);

        if (xOpenDisplay == null) {
            throw new ActiveWindowUtilityException("failed to get x11 display");
        }

        x11Lib.XSetErrorHandler((X11.Display display, X11.XErrorEvent errorEvent) -> {
            System.out.println("Error is occurred " + errorEvent.toString(true));
            return 0;
        });

        IntByReference revertToReturn = new IntByReference();
        X11.WindowByReference windowReference = new X11.WindowByReference();
        x11Lib.XGetInputFocus(xOpenDisplay, windowReference, revertToReturn);

        X11.XTextProperty textProperty = new X11.XTextProperty();

        x11Lib.XGetWMName(xOpenDisplay, windowReference.getValue(), textProperty);

        x11Lib.XFree(textProperty.getPointer());
        x11Lib.XFree(revertToReturn.getPointer());
        x11Lib.XFree(windowReference.getPointer());

        String wmName = textProperty.value;
        return wmName;
    }
    
    private String getApplicationName(String windowPid) throws IOException {
        
        String applicationName = null;
        final String PARAMETERIED_STRING = "xprop -id %s | grep WM_CLASS";
        final String SPLIT_DELIMITER1 = "=";

        try {
            String namedParamCommand = String.format(PARAMETERIED_STRING, windowPid);

            Process process = RUNTIME.exec(namedParamCommand);
            String output = getOutputFromCommandProcess(process);
            var outputSegments = output.split(SPLIT_DELIMITER1);
            applicationName = outputSegments[1].split("\"")[1];
            return applicationName;
        } catch (IOException ex) {
            throw ex;
        }
    }

    private String getWindowTitle() throws Exception {

        final String BASH_COMMAND = "xdotool getactivewindow getwindowname";

        try {
            Process process = RUNTIME.exec(BASH_COMMAND);
            String windowTitle = getOutputFromCommandProcess(process);
            return windowTitle;
        } catch (IOException ex) {
            throw ex;
        }
    }

    private String getActiveWindowPid() throws IOException {

        final String COMMAND = "xdotool getactivewindow";
        Process process = RUNTIME.exec(COMMAND);
        String output = getOutputFromCommandProcess(process);
        return output;
    }
    
}
