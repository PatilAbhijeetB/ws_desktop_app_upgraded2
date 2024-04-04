/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author chamara
 */
public class WorkDateTime {
    private static final InternalLogger LOGGER = LoggerService.getLogger(WorkDateTime.class);
    private static WorkDateTime INSTANCE;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private ZoneId zoneId;
    private long dateStartedTimestamp;
    private long dateEndedTimestamp;
    private String formattedDate;
    private String formattedTommorowDate;
    
    private ZonedDateTime zoneLocalDateTime;
    
    private WorkDateTime() {}
    
    private WorkDateTime(ZoneId zoneId) throws Exception {
        
        this.zoneId = zoneId;
        
        boolean didInitializedTimeUtility = TimeUtility.isInitialized();
        
        if (!didInitializedTimeUtility) {
            TimeUtility.initailize();
        }
        
        long UTCUnixTimestamp = TimeUtility.getCurrentTimestamp();
        Instant UTCInstant = Instant.ofEpochSecond(UTCUnixTimestamp);
        this.zoneLocalDateTime = ZonedDateTime.ofInstant(UTCInstant, zoneId);
        
        this.formattedDate = this.zoneLocalDateTime.format(DATE_FORMATTER);
        LocalDate zoneLocalDate = this.zoneLocalDateTime.toLocalDate();
        
        LocalDate zoneTommorowDate = zoneLocalDate.plusDays(1);
        this.formattedTommorowDate = zoneTommorowDate.format(DATE_FORMATTER);

        LocalDateTime todayDateTime = LocalDate.parse(this.formattedDate).atStartOfDay();
        Instant todayInstant = todayDateTime.atZone(ZoneId.of("UTC")).toInstant();
        this.dateStartedTimestamp = todayInstant.getEpochSecond();

        LocalDateTime tommorowDateTime = LocalDate.parse(this.formattedTommorowDate).atStartOfDay();
        Instant tommorowInstant = tommorowDateTime.atZone(ZoneId.of("UTC")).toInstant();
        this.dateEndedTimestamp = tommorowInstant.getEpochSecond();
    }
    
    public static WorkDateTime init(ZoneId zoneId) throws Exception {
        WorkDateTime.INSTANCE = new WorkDateTime(zoneId);
        return WorkDateTime.INSTANCE;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public long getDateStartedTimestamp() {
        return dateStartedTimestamp;
    }

    public long getDateEndedTimestamp() {
        return dateEndedTimestamp;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public String getFormattedTommorowDate() {
        return formattedTommorowDate;
    }
    
    public static WorkDateTime getInstance() throws NullPointerException {
        
        if (WorkDateTime.INSTANCE == null) {
            throw new NullPointerException("Worktime instance should be initialize before access it");
        }
        return WorkDateTime.INSTANCE;
    }
    
    
}
