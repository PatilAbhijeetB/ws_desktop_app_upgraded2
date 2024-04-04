/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.urlcapture;

import com.sun.jna.Platform;
import com.workshiftly.common.model.ActiveWindow;
import com.workshiftly.common.model.WebBrowserLog;
import com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers.Browser;
import com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers.UrlCaptureBrowserBaseHandler;
import com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers.WindowsDotNetHander;

/**
 *
 * @author chanakya
 */
public class UrlCaptureUility {
    public static class PlatformNotSupportedException extends Exception {

        private PlatformNotSupportedException(String message) {
            super(message);
        }
    }

    public static class BrowserNotExistException extends Exception {

        private BrowserNotExistException(String message) {
            super(message);
        }
    }

    public static class UrlCaptureUilityException extends Exception {

        UrlCaptureUilityException(String message) {
            super(message);
        }
    }

    public static WebBrowserLog getCurrentURLs(Browser browser, ActiveWindow activeWindow) throws Exception {

        UrlCaptureBrowserBaseHandler urlCaptureBrowserBaseHandler = null;

        if (Platform.isLinux()) {
            throw new PlatformNotSupportedException("Platform is not supported to retrieve URL");
        } else if (Platform.isWindows()) {
            urlCaptureBrowserBaseHandler = WindowsDotNetHander.getHandler(browser, activeWindow);
        } else if (Platform.isMac()) {
            throw new PlatformNotSupportedException("Platform is not supported to retrieve URL");
        } else {
            throw new PlatformNotSupportedException("Platform is not supported to retrieve URL");
        }
        
        WebBrowserLog browserLog = urlCaptureBrowserBaseHandler.getURL();
        return browserLog;
    }

}
