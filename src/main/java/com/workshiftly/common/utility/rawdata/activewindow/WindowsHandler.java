/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.activewindow;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.InternalLogger.LOGGER_LEVEL;
import com.workshiftly.service.logger.LoggerService;

/**
 *
 * @author chamara
 */
public class WindowsHandler extends ActivityWindowBaseHandler {
    private static final InternalLogger LOGGER = LoggerService.getLogger(WindowsHandler.class);

    private static final String OS_NAME = "WINDOWS";
    private static final WindowsHandler INSTANCE = new WindowsHandler();
    private static final int MAX_TITLE_LENGTH  = 1024;
    
    private final User32 user32 = User32.INSTANCE;
    private final Kernel32 kernel32 = Kernel32.INSTANCE;
    private final Psapi psapi = Psapi.INSTANCE;
    
    private WindowsHandler() {}
    
    synchronized static ActivityWindowBaseHandler getInstance() {
        return WindowsHandler.INSTANCE;
    }
    
    @Override
    public ActiveWindow getCurrentActivityWindow() {
        
        ActiveWindow activeWindow = new ActiveWindow();
        activeWindow.setOperatingSystem(OS_NAME);
        
        try {
            IntByReference processRef = getActiveWindowPid();
            activeWindow.setProcessId(Integer.toString(processRef.getValue()));
            
            String appExeName = getApplicationName(processRef);
            activeWindow.setAppName(appExeName);
            
            String windowTitle = getWindowTitle();
            activeWindow.setTitle(windowTitle);
        } catch (Exception ex) {
            activeWindow.setIsPartial(true);
            LOGGER.logRecord(LOGGER_LEVEL.SEVERE, "Fail to get Current Activity Window - WindowsHandler", ex);
        }

        return activeWindow;
    }
    
    private String getApplicationName(IntByReference intByRef) {
        
        final int PROCESS_VM_READ = 0x0010;
        final int PROCESS_QUERY_INFORMATION = 0x0400;
        
        try {
            WinNT.HANDLE processHandler = kernel32.OpenProcess(
                    PROCESS_VM_READ | PROCESS_QUERY_INFORMATION, true, intByRef.getValue()
            );
            
            char[] appNameBytesArr = new char[MAX_TITLE_LENGTH * 2];
            psapi.GetModuleBaseNameW(
                    processHandler.getPointer(), Pointer.NULL, appNameBytesArr, appNameBytesArr.length
            );
            
            return Native.toString(appNameBytesArr);
        } catch (Exception ex) {
            LOGGER.logRecord(LOGGER_LEVEL.SEVERE, "Fail to get Application Name", ex);
            return null;
        }
    }
    
    private IntByReference getActiveWindowPid() throws Exception {
        try {
            HWND windowHandler = user32.GetForegroundWindow();
            IntByReference intRef = new IntByReference();
            user32.GetWindowThreadProcessId(windowHandler, intRef);
            return intRef;
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    private String getWindowTitle() {
        try {
            char[] buffer = new char[MAX_TITLE_LENGTH * 2];
            HWND windowHandler = user32.GetForegroundWindow();
            user32.GetWindowText(windowHandler, buffer, MAX_TITLE_LENGTH);
            return Native.toString(buffer);
        } catch (Exception ex) {
            LOGGER.logRecord(LOGGER_LEVEL.SEVERE, "Fail to get Window Title", ex);
            return null;
        }
    }
    
    public interface Psapi extends StdCallLibrary {
        Psapi INSTANCE = (Psapi) Native.load("Psapi", Psapi.class);
        WinDef.DWORD GetModuleBaseNameW(Pointer hProcess, Pointer hModule, char[] lpBaseName, int nSize);
    } 
} 
