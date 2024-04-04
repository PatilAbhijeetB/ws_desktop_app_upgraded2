/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.service;

import com.google.gson.JsonObject;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.domain.RawDataModule;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

/**
 *
 * @author jade_m
 */
public class SlientModeWorkStatusActivator extends ScheduledService<Response> {
     
    private static final int NO_CHECKSUM_SLOTS;
    private static final int WORKING_IDLE_TIME_UPPER_BOUND;
    private static final InternalLogger LOGGER; 
    
    static {
        NO_CHECKSUM_SLOTS = 15;
        WORKING_IDLE_TIME_UPPER_BOUND = 10;
        LOGGER = LoggerService.getLogger(SlientModeWorkStatusActivator.class);
    }
    
    private final RawDataModule rawDataModule;
    private final Long[] checksumSlots;
    private final JsonObject output;
    
    private int currentSlotId;
    
    public SlientModeWorkStatusActivator() {
        this.rawDataModule = new RawDataModule();
        
        this.checksumSlots = new Long[NO_CHECKSUM_SLOTS];
        Arrays.fill(this.checksumSlots, null);
        
        this.currentSlotId = 0;
        output = new JsonObject();
        output.addProperty("isUserWorking", false);
    }
    
    @Override
    protected Task<Response> createTask() {
        return new Task() {
            @Override
            protected Response call() throws Exception {
                Long userIdleTime = rawDataModule.getUserIdleTime();
                
                if (currentSlotId == checksumSlots.length) {
                    currentSlotId = 0;
                }
                
                checksumSlots[currentSlotId] = userIdleTime;
                boolean isFilledChecksums = Arrays.stream(checksumSlots)
                        .allMatch((var element) -> element != null);
                
                if (!isFilledChecksums) {
                    currentSlotId++;
                    return new Response(false, StatusCode.SUCCESS, "", output);
                }
                
                return analyzeCheckSumSlots();
            }
        };
    }
    
    private Response analyzeCheckSumSlots() {
        try {
            List<Long> checkSumList = Arrays.asList(checksumSlots);
            List<Long> analyticalList = new ArrayList<>();
            boolean isFilledLastSlot = currentSlotId == checksumSlots.length - 1;

            if (isFilledLastSlot) {
                analyticalList.addAll(checkSumList);
            } else {
                analyticalList.addAll(
                        checkSumList.subList(currentSlotId + 1, checksumSlots.length)
                );
                analyticalList.addAll(checkSumList.subList(0, currentSlotId + 1));
            }
            Long maxIdleDuration = Collections.max(analyticalList);
            output.addProperty("isUserWorking", maxIdleDuration <= WORKING_IDLE_TIME_UPPER_BOUND);
            currentSlotId++;
        } catch (Exception ex) {
            String errorMsg = "Error occurred while analyze checkSum slots";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
        }
        
        return new Response(false, StatusCode.SUCCESS, "", output);
    }
    
}
