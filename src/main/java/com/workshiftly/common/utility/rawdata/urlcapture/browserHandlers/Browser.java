/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.urlcapture.browserHandlers;

import java.util.Arrays;

/**
 *
 * @author chanakya
 */
public enum Browser {

    OPERA("opera"),
    CHROME("chrome"),
    FIREFOX("firefox"),
    EDGE("msedge"),
    IEXPLORER("iexplorer");

    String simpleName;

    private Browser(String name) {
        simpleName = name;
    }

    public static Browser getBrowser(String appName) {
        Browser[] values = Browser.values();
        Browser browser = Arrays.stream(values).filter((Browser browserObj) -> {
            return appName.toLowerCase().contains(browserObj.simpleName);
        }).findFirst().orElseThrow();
        return browser;
    }

    public static boolean isWebBrowser(String appName) {
        Browser[] values = Browser.values();
        Browser browser = Arrays.stream(values).filter((Browser browserObj) -> {
            return appName.toLowerCase().contains(browserObj.simpleName);
        }).findFirst().orElse(null);
        return browser != null;
    }
}
