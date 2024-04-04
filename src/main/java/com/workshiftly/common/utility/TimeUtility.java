/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.google.gson.JsonObject;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.service.concurrent.ThreadExecutorService;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.time.Clock;
import java.time.ZoneId;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.LogRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author chamara
 */
public final class TimeUtility {
    private static final InternalLogger LOGGER = LoggerService.getLogger(TimeUtility.class);

    private static final ZoneId HOST_COMPUTER_ZONE_ID = ZoneId.systemDefault();

    public static final DateTimeZone DEFAULT_DATETIME_ZONE = DateTimeZone.UTC;
    public static final DateTimeZone HOST_COMPUTER_DATETIME_ZONE = DateTimeZone.forID(HOST_COMPUTER_ZONE_ID.getId());
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy.MMMMM.dd hh:mm aaa";
    public static final String DEFAULT_TIME_PATTERN = "hh:mm aaa";

    private static final TimeUtility INSTANCE = new TimeUtility();
    private static boolean  INITAILIZED = false;
    
    private Long unixTimestampUTC;
    private Long pcUnixTimestampUTC;
    
    private DateTimeFormatter dateTimeFormatter;

    private TimeUtility() {
        this.dateTimeFormatter = ISODateTimeFormat.dateTime();
    }
    
    public static boolean isInitialized() {
        return TimeUtility.INITAILIZED;
    }
    
    public static Long getCurrentTimestamp() {
        return INSTANCE.unixTimestampUTC;
    }

    public static Long getCurrentPCTimestamp() {
        return INSTANCE.pcUnixTimestampUTC;
    }

    // Initialized TimeUtlity instance
    public static void initailize() throws Exception {
        
        UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
        
        if (userSession == null) {
            return;
        }
        
        INSTANCE.pcUnixTimestampUTC = Clock.systemUTC().millis() / 1000;
        INSTANCE.unixTimestampUTC = getRemoteUTCTimestamp();
        
        startupTimer();
        registerTimePerodicChecker();
        INITAILIZED = true;
    }
    
    // Get UTC timestamp from remote server
    private static Long getRemoteUTCTimestamp() {
        
        HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
        Response apiUTCResponse = httpRequestCaller.callgetUTCtimestamp();
        
        if (apiUTCResponse.isError()) {
            return INSTANCE.pcUnixTimestampUTC;
        }
        
        JsonObject responseData = apiUTCResponse.getData().getAsJsonObject();
        return responseData.get("timestamp").getAsLong();
    }

    // Timer executor service
    private static void startupTimer() {
        
        final int initialDelay = 1;
        final int interval = 1;
        
        ThreadExecutorService.executePeriodicTask(() -> {
            INSTANCE.unixTimestampUTC++;
            INSTANCE.pcUnixTimestampUTC++;
        }, initialDelay, interval);
    }
    
    // Register worker for determine accuracy of the timers
    private static void registerTimePerodicChecker() {
        
        final int initialDelay = 60;
        final int interval = 60;
        
        ThreadExecutorService.executePeriodicTask(() -> {
            
            Long systemUTCTimestamp = Clock.systemUTC().millis() / 1000;
            Long difference = systemUTCTimestamp - INSTANCE.pcUnixTimestampUTC;
            
            if (difference > 0 && difference < 60) {
                INSTANCE.pcUnixTimestampUTC += difference;
                INSTANCE.unixTimestampUTC = INSTANCE.pcUnixTimestampUTC;
            } else {
                INSTANCE.unixTimestampUTC = getRemoteUTCTimestamp();
            }
            
            // reinitialize WorkDateTime object when passing mid-night
            WorkDateTime workDateTime = StateStorage
                    .getCurrentState(StateName.CURRENT_WORKDATETIME_INSTANCE);
            if (workDateTime != null && 
                    workDateTime.getDateEndedTimestamp() < INSTANCE.unixTimestampUTC) {
                try {
                    WorkDateTime workDateTimeObj = TimeUtility.initWorkDateTime();
                    StateStorage.set(
                            StateName.CURRENT_WORKDATETIME_INSTANCE, 
                            WorkDateTime.class, 
                            workDateTimeObj
                    );
                } catch (Exception e) {
                    LoggerService.LogRecord(
                            TimeUtility.class, 
                            DEFAULT_TIME_PATTERN, 
                            InternalLogger.LOGGER_LEVEL.ALL, 
                            e
                    );
                }
            }
        }, initialDelay, interval);
    }

    public static void setDateTimeFormat(String dateTimeFormat) {
        INSTANCE.dateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormat);
    }

    public static String getHumanReadbleDateTime(long timestamp, DateTimeZone dateTimeZone) {

        long timestampInMilles = timestamp * 1000L;
        DateTime defaultDateTime = new DateTime(timestampInMilles, DEFAULT_DATETIME_ZONE);
        DateTimeFormatter outputFormatter = DateTimeFormat.forPattern(DEFAULT_DATETIME_PATTERN)
                .withZone(dateTimeZone);
        return outputFormatter.print(defaultDateTime);
    }
    
    public static String getHumanReadbleDateTime(Duration duration, DateTimeZone dateTimeZone, String pattern) {
        
        long durationMilliseconds = duration.toMillis();
        DateTime convertDateTimeElement = new DateTime(durationMilliseconds, DEFAULT_DATETIME_ZONE);
        DateTimeFormatter dateTimeOutputFormatter = DateTimeFormat.forPattern(pattern)
                .withZone(dateTimeZone);
        return dateTimeOutputFormatter.print(convertDateTimeElement);
    }
    
    public static String formatDuration(Duration duration) {

        long totalSeconds = duration.getSeconds();
        long absoluteValueSeconds = Math.abs(totalSeconds);

        long hours = absoluteValueSeconds / 3600;
        long minutes = (absoluteValueSeconds % 3600) / 60;
        long seconds = absoluteValueSeconds % 60;

        String polorSign = totalSeconds < 0 ? "-" : "";
        String formattedValue = String.format("%s %d:%02d:%02d", polorSign, hours, minutes, seconds);
        return formattedValue;
    }
    
    public static WorkDateTime initWorkDateTime() throws Exception {
        ZoneId requestedZoneId = HOST_COMPUTER_ZONE_ID;
        return WorkDateTime.init(requestedZoneId);
    }
    
    public static long getSystemClockTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }
    
    public static String getHumanReadbleTimeOnly(long timestamp, DateTimeZone dateTimeZone) {
        long timestampInMilles = timestamp * 1000L;
        DateTime defaultDateTime = new DateTime(timestampInMilles);

        if (dateTimeZone == null)
            dateTimeZone = HOST_COMPUTER_DATETIME_ZONE;

        DateTimeFormatter outputFormatter = DateTimeFormat.forPattern(DEFAULT_TIME_PATTERN)
                    .withZone(dateTimeZone);
        return outputFormatter.print(defaultDateTime);
    }
    
    public static DateTimeZone initDateTimeZone(String timezoneIdentifier) {
        
        String zoneId = null;
        if (timezoneIdentifier != null && !timezoneIdentifier.isEmpty()) {
            
            String tempZoneId = timezoneIdentifier.split(" ")[0];
            Set<String> availableDateTimeZoneIds = DateTimeZone.getAvailableIDs();
            zoneId = availableDateTimeZoneIds.contains(tempZoneId) ? tempZoneId : null;
        }
        
        DateTimeZone dateTimeZone = zoneId == null ? 
                DateTimeZone.getDefault() : DateTimeZone.forID(zoneId);
        return dateTimeZone;
    }
}
