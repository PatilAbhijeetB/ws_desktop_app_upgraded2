/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility.rawdata.screenshot;

import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.AppDirectory;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.Screenshot;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.FileUtility;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.persistence.Database.Database;
import com.workshiftly.persistence.Database.DatabaseProxy;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 *
 * @author chamara
 */
public class TimeslotDefinition {
    private static final InternalLogger LOGGER = LoggerService.getLogger(TimeslotDefinition.class);

    private static final Duration ONE_HOUR_DURATION = Duration.ofHours(1);
    private static final int MAX_VALUE_SCREENSHOT_PER_HOUR = 15;
    private static final int CAPTURE_POINT_MULTIPLIER = 3;
    private static final Duration ACTIVE_THRESOLD_MULTIPLIER = Duration.ofSeconds(60);
    private static final boolean PERSIST_SCREEN_SHOTS_LOCAL_DISK;
    
    private Duration startUnixTimestamp;
    private Duration endUnixTimestamp;
    private int screenshotsPerHour;
    private int noOfCapturePoints;
    private Duration activeThresold;
    
    private CapturePoint startCapturePoint;
    private CapturePoint endCapturePoint;
    private boolean isCompleted;
    private final LinkedList<CapturePoint> capturePoints = new LinkedList<>();
    private CapturePoint nextCapturePoint;
    
    static {
        PERSIST_SCREEN_SHOTS_LOCAL_DISK = DotEnvUtility.persistScreenshotsInLocalDisk();
    }
    
    private TimeslotDefinition() {
    }
    
    TimeslotDefinition(
            Duration startUnixTimestamp, 
            int screenshotsPerHour
    ) {
        
        this.startUnixTimestamp = startUnixTimestamp;
        long slotDuration = ONE_HOUR_DURATION.getSeconds() / screenshotsPerHour;
        this.endUnixTimestamp = startUnixTimestamp.plusSeconds(slotDuration);
        
        this.screenshotsPerHour = screenshotsPerHour;
        this.noOfCapturePoints = configureNoOfCapturePoints(screenshotsPerHour);
        
        this.activeThresold = configureActiveThresold(screenshotsPerHour);
        
        Duration captureActiveStartpoint = this.startUnixTimestamp.plus(activeThresold);
        this.startCapturePoint = new CapturePoint(captureActiveStartpoint.getSeconds());
        
        Duration captureActiveEndPoint = this.endUnixTimestamp.minus(activeThresold);
        this.endCapturePoint = new CapturePoint(captureActiveEndPoint.getSeconds());
        
        this.isCompleted = false;
        initializeCapturePoints();
    }
    
    public CapturePoint getNextCapturePoint() {
        return this.nextCapturePoint;
    }
    
    public boolean isCompleted() {
        return this.isCompleted;
    }
    
    private int configureNoOfCapturePoints(int screenshotsPerHour) {
        return (MAX_VALUE_SCREENSHOT_PER_HOUR / screenshotsPerHour) * CAPTURE_POINT_MULTIPLIER;
    }
    
    private Duration configureActiveThresold(int screenshotsPerHour) {
        int factor = MAX_VALUE_SCREENSHOT_PER_HOUR / screenshotsPerHour;
        return ACTIVE_THRESOLD_MULTIPLIER.multipliedBy(factor);
    }
    
    private void initializeCapturePoints() {
        
        int noOfCapturePoints = this.noOfCapturePoints;
        long captureActivatedTimestamp = this.startCapturePoint.getCaptureTimestamp();
        long captureInactivedTimestamp = this.endCapturePoint.getCaptureTimestamp();
        long capturedSeconds = captureInactivedTimestamp - captureActivatedTimestamp;
        
        capturePoints.addFirst(this.startCapturePoint);
        capturePoints.addLast(this.endCapturePoint);
        
       Duration tempDuration = Duration.ofSeconds(this.startCapturePoint.getCaptureTimestamp());
       long incrementUpperlimit = capturedSeconds / noOfCapturePoints;
       
       for (int step = 1; step <= noOfCapturePoints; step++) {
           
           Double currentIncrement = incrementUpperlimit * Math.random();
           long capturingTimestamp = tempDuration.getSeconds() + currentIncrement.longValue();
           CapturePoint capturePoint = new CapturePoint(capturingTimestamp);
           capturePoints.add(capturePoint);
           tempDuration = tempDuration.plusSeconds(incrementUpperlimit);
           
       }
       
       Collections.sort(capturePoints);
       this.nextCapturePoint = this.startCapturePoint;    
    }
    
    public Response commitScreenshotToCurrentCapturePoint(Screenshot screenshot) {
        
        CapturePoint currentCapturePoint = this.nextCapturePoint;
        currentCapturePoint.setScreenshot(screenshot);
        
        if (currentCapturePoint == this.endCapturePoint) {
            Response response = finalizeTimeslot();
            return response;
        }
        
        int nextIdx = this.capturePoints.indexOf(this.nextCapturePoint) + 1;
        this.nextCapturePoint = this.capturePoints.get(nextIdx);
        return new Response(false, StatusCode.SUCCESS, "Successfully committed screenshot to capture point");
    }
    
    private boolean isValidResultPoint(CapturePoint capturePoint) {
        return capturePoint != null && !capturePoint.isCommitted() && capturePoint.getScreenshot() != null;
    }
    
    private Screenshot getResult() {
        
        List<CapturePoint> randomCapturePoints = this.capturePoints.stream().filter((CapturePoint element) -> 
                this.capturePoints.getFirst() != element && this.capturePoints.getLast() != element
        ).collect(Collectors.toList());
        
        Collections.shuffle(randomCapturePoints);
        
         for (CapturePoint currentPoint : randomCapturePoints) {
             if (isValidResultPoint(currentPoint)) {
                 return currentPoint.getScreenshot();
             }
         }
         
         CapturePoint startedCapturePoint = this.capturePoints.getFirst();
         if (isValidResultPoint(startedCapturePoint)) {
             return startedCapturePoint.getScreenshot();
         }
         
         CapturePoint endedCapturePoint = this.capturePoints.getLast();
         return isValidResultPoint(endedCapturePoint) ? endedCapturePoint.getScreenshot() : null;
    }
    
    
    private Response finalizeTimeslot() {
        
        try {
            this.isCompleted = true;
            Screenshot finalizedScreenshot = getResult();
            
            if (finalizedScreenshot == null) {
                return new Response(false, StatusCode.SUCCESS, "No screenshot captured for this slot");
            }
            
            Database<Screenshot> database = DatabaseProxy.openConnection(Screenshot.class);
            Response response = database.create(finalizedScreenshot);
            
            if (response.isError()) {
                return response;
            }
            
            if (PERSIST_SCREEN_SHOTS_LOCAL_DISK) {
                persistImageToLocalDisk(finalizedScreenshot);
            }
            
            return response;
        } catch (Exception ex) {
            LoggerService.LogRecord(TimeslotDefinition.class, "Failed to finalize timeslot", InternalLogger.LOGGER_LEVEL.SEVERE, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, ex.getMessage());
        } finally {
            DatabaseProxy.closeConnection(Screenshot.class);
        }
    }
    
    private void persistImageToLocalDisk(Screenshot screenshot) throws Exception {
        
        File imageDirPath = FileUtility.getApplicationResourceDirectory(AppDirectory.RAW_SCREENSHOTS);
        Long currentTimestamp = TimeUtility.getCurrentTimestamp();
        String iamgeFilePath = imageDirPath.getAbsolutePath() + FileUtility.FILE_SEPARATOR + currentTimestamp;
        File imageFile = new File(iamgeFilePath);
        ImageIO.write(screenshot.getBufferedImage(), "jpg", imageFile);
    }
    
    public Response forceToFinalize() {
        return finalizeTimeslot();
    }
}
