/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.application.state;


public final class StateName {
    
    private StateName() {}
    
    public static final String APPLICATION_INSTALLER_DOWNLOAD = "APPLICATION_INSTALLER_DOWNLOAD";
    public static final String TIME_UTILITY_INIT_STATE = "TIME_UTILITY_INIT_STATE";
    public static final String CURRENT_WORKDATETIME_INSTANCE = "CURRENT_WORKDATETIME_INSTANCE";
    
    public static final String AUTH_TOKEN = "AUTH_TOKEN";
    public static final String USER_SESSION = "USER_SESSION";
    public static final String COMPANY_SETTINGS = "COMPANY_SETTINGS";
    public static final String DEVICE_SETTINGS = "DEVICE_SETTINGS";
    public static final String LOGGED_IN_DEVICE_SETTINGS = "LOGGED_IN_DEVICE_SETTINGS";
    public static final String USER_INITIAL_PASSWORD = "USER_INITIAL_PASSWORD";
    public static final String USER_COMPANY_INSTANCE = "USER_COMPANY_INSTANCE";
    public static final String COMPANY_CONFIGURATION = "COMPANY_CONFIGURATION";
    public static final String COMPANY_TIMEZONE = "COMPANY_TIMEZONE";
    public static final String USER_TIMEZONE = "USER_TIMEZONE";
    public static final String USER_PROFILE_PICTURE = "USER_PROFILE_PICTURE";
    public static final String LATEST_TNC_DOCUMENT = "LATEST_TNC_DOCUMENT";
    
    public static final String ACTIVE_WINDOW = "ACTIVE_WINDOW";
    public static final String ACTIVE_WINDOW_LIST = "ACTIVE_WINDOW_LIST";
    public static final String ACTIVE_WINDOW_WRITING_SKIPS = "ACTIVE_WINDOW_WRITING_SKIPS";
    public static final String ACTIVE_WINDOW_WRITING_THRESHOLD = "ACTIVE_WINDOW_WRITING_THRESHOLD";
    public static final String ACTIVE_WINDOW_MAX_FOCUS_DURATION = "ACTIVE_WINDOW_MAX_FOCUS_DURATION";
    
    public static final String SCREENSHOT_LIST = "SCREENSHOT_LIST";
    public static final String PERSIST_SCREEN_SHOTS_LOCAL_DISK = "PERSIST_SCREEN_SHOTS_LOCAL_DISK";
    public static final String SCREENSHOT = "SCREENSHOT";
    public static final String CURRENT_SCREENSHOT_TIMESLOT_DEFINITION = "CURRENT_SCREENSHOT_TIMESLOT_DEFINITION";
    
    public static final String URL = "URL";
    public static final String URL_LIST = "URL_LIST";
    public static final String URL_LIST_WRITING_SKIPS = "URL_LIST_WRITING_SKIPS";
    public static final String URL_LIST_WRITING_THRESHOLD = "URL_LIST_WRITING_THRESHOLD";
    
    public static final String CURRENT_MAIN_SCREEN = "CURRENT_MAIN_SCREEN";
    public static final String NO_MAIN_SCREEN = "NO_MAIN_SCREEN";
    public static final String MAIN_LOGIN_SCREEN = "MAIN_LOGIN_SCREEN";
    public static final String TERMS_OF_SERVICE_SCREEN = "TERMS_OF_CONDITION_SCREEN";
    public static final String LOADING_SPLASH_SCREEN = "LOADING_SPLASH_SCREEN";
    public static final String FORGET_PASSWORD_SCREEN = "FORGET_PASSWORD_SCREEN";
    public static final String CHANGE_PASSWORD_SCREEN = "CHANGE_PASSWORD_SCREEN";
    public static final String AUTHENTICATED_MAIN_WINDOW = "AUTHENTICATED_MAIN_WINDOW";
    
    public static final String AUTHENTICATED_MAIN_WINDOW_VIEWMODEL = "AUTHENTICATED_MAIN_WINDOW_VIEW_MODEL";
    public static final String PROJECT_VIEWMODEL = "PROJECT_VIEWMODEL";
    
    public static final String LAST_WORK_STATUS_LOG = "LAST_WORK_STATUS_LOG";
    public static final String WORK_STATUS_LOG_LIST = "WORK_STATUS_LOG_LIST";

    public static final String LOGOUT_NAVIGATION_ITEM = "Logout";
    public static final String DASHBOARD_NAVIGATION_ITEM = "Dashboard";
    public static final String PROJECT_NAVIGATION_ITEM = "Projects";
    public static final String SETTINGS_NAVIGATION_ITEM = "Settings";
    public static final String DEFAULT_ACTIVATED_TAB = DASHBOARD_NAVIGATION_ITEM;

    public static final String CURRENT_SELECTED_PROJECT = "CURRENT_SELECTED_PROJECT";
    public static final String CURRENT_TASK = "CURRENT_TASK";
    public static final String LAST_TASK_STATUS_LOG = "LAST_TASK_STATUS_LOG";
    public static final String TASK_STATUS_LOG_LIST = "TASK_STATUS_LOG_LIST";
    public static final String IS_SUCCESS_GET_REMOTE_PROJECT_TASK = "IS_SUCCESSED_FETCHED_REMOTE_PROJECT_TASK";
    public static final String RECENT_WORKING_TASK = "RECENT_WORKING_TASK";
    
    public static final String IS_SYNCING = "IS_SYNCING";
    public static final String LAST_SYNCED_TIME = "LAST_SYNCED_TIME";
    public static final String IS_PERIODIC_SYNC_ACTIVE = "IS_STARTED_PERIODIC_SYNC";
    public static final String LAST_WORK_STATUS_LOG_SYNCED_TIME = "LAST_WORK_STATUS_LOG_SYNCED_TIME";

    public static final String OTHER_BREAK_REASON_TITLE = "Other";
    public static final String CURRENT_BREAK_REASON = "CURRENT_BREAK_REASON";
    public static final String LATEST_TASK_UPDATED_TIMESTAMP = "LATEST_TASK_UPDATED_TIMESTAMP";
    public static final String IsLogout = "Logout";
    
    
    
}
