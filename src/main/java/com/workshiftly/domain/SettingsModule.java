/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.domain;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.FileDownload;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSchedule;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.model.UserShift;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.DotEnvUtility;
import com.workshiftly.common.utility.FileDownloader;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import org.joda.time.DateTimeZone;

/**
 *
 * @author dmhashan
 */
public class SettingsModule {
    private static final InternalLogger LOGGER = LoggerService.getLogger(SettingsModule.class);

    Gson GSON = new Gson();
    HttpRequestCaller httpRequestCaller;

    public SettingsModule() {
        httpRequestCaller = new HttpRequestCaller();
    }

    public Response getUserSchedule() {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);

            String userId = userSession.getId();
            Response response = httpRequestCaller.callGetUserWorkSchedules(userId);

            if (response.isError()) {
                return response;
            }

            JsonObject data = response.getData().getAsJsonObject().get("workSchedule").getAsJsonObject();
            if (data == null) {
                return new Response(true, StatusCode.NOT_FOUND, "Failed to retrieve user schedule details.");
            }

            Type userWorkScheduleType = new TypeToken<UserSchedule>() {}.getType();
            UserSchedule userWorkSchedule = GSON.fromJson(data, userWorkScheduleType);

            JsonObject userScheduleData = data.getAsJsonObject("schedule");
            if (userScheduleData == null) {
                return new Response(true, StatusCode.NOT_FOUND, "Failed to retrieve user schedule details.");
            }

            String scheduleStartAt = userScheduleData.get("scheduleStartAt").getAsString();
            userWorkSchedule.setScheduleStartAt(scheduleStartAt);
            String scheduleEndAt = userScheduleData.get("scheduleEndAt").getAsString();
            userWorkSchedule.setScheduleEndAt(scheduleEndAt);

            JsonArray userShiftsData = userScheduleData.getAsJsonArray("shifts");
            if (userShiftsData == null) {
                return new Response(true, StatusCode.NOT_FOUND, "Failed to retrieve user schedule details.");
            }

            ArrayList<UserShift> userShifts = new ArrayList<>();
            Iterator<JsonElement> userShiftsIterator = userShiftsData.iterator();
            while (userShiftsIterator.hasNext()) {
                UserShift userShift = new UserShift();
                JsonObject shiftObj = userShiftsIterator.next().getAsJsonObject();

                String index = shiftObj.get("index").getAsString();
                String workingHours = shiftObj.get("workingHours").getAsString();
                userShift.setIndex(index);
                userShift.setWorkingHours(workingHours);

                JsonObject shiftStartObj = shiftObj.getAsJsonObject("shiftStart");
                long shiftStartTime = shiftStartObj.get("time").getAsLong();
                String shiftStart = shiftStartObj.get("day").getAsString().concat(" ")
                        .concat(TimeUtility.getHumanReadbleTimeOnly(shiftStartTime, DateTimeZone.UTC));
                userShift.setShiftStart(shiftStart);

                JsonObject shiftEndObj = shiftObj.getAsJsonObject("shiftEnd");
                long shiftEndTime = shiftEndObj.get("time").getAsLong();
                String shiftEnd = shiftEndObj.get("day").getAsString().concat(" ")
                        .concat(TimeUtility.getHumanReadbleTimeOnly(shiftEndTime, DateTimeZone.UTC));
                userShift.setShiftEnd(shiftEnd);

                userShifts.add(userShift);
            }

            userWorkSchedule.setShifts(new ArrayList<>(userShifts));

            JsonElement responseData = GSON.toJsonTree(userWorkSchedule);
            return new Response(false, StatusCode.SUCCESS, "Successfully retrieved user schedule details.", responseData);
        } catch (JsonSyntaxException | NullPointerException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Failed to retrieve user schedule details.", e);
            return new Response(true, StatusCode.NOT_FOUND, "Failed to retrieve user schedule details.");
        } catch (AuthenticationException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Session invalidated", e);
            return new Response(true, StatusCode.SESSION_INVALID, "Session invalidated");
        }
    }
    
    
    public Response checkAppUpdateAvailability() {
        try {
            HttpRequestCaller httpRequestCaller = new HttpRequestCaller();
            String currentVersion = DotEnvUtility.getApplicationVersion();
            Response apiResponse = httpRequestCaller.getClientAppLatestVersion(currentVersion);
            
            if (apiResponse.isError()) {
                return apiResponse;
            }
            
            JsonObject responseData = apiResponse.getData().getAsJsonObject();
            boolean isUpdateAvialable = responseData.get("availability").getAsBoolean();
            responseData.addProperty("shouldBlockApplication", isUpdateAvialable);
            
            if (isUpdateAvialable) {
                Long allowedLatestTimestamp = responseData
                        .get("allowedLatestTimestamp").getAsLong();
                Long currentTimestamp = responseData.get("currentTimestamp").getAsLong();
                boolean isExceededAllowedUpperLimit = currentTimestamp >= allowedLatestTimestamp;
                responseData.addProperty("shouldBlockApplication", isExceededAllowedUpperLimit);    
            }
            
            return new Response(false, StatusCode.SUCCESS, "Success", responseData);
        } catch (Exception ex) {
            String errorMsg = "Unable to check app update availability";
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, ex);
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
        }
    }
    
    // Download Application Latest version directly from the storage
    public Response downloadLatestApplicationInstaller(FileDownload fileDownload) {
        
        try {
            if (!FileDownloader.contains(fileDownload.getFileName())) {
                FileDownloader.add(fileDownload).start(false, true);
                
                return new Response(
                        false, StatusCode.SUCCESS, "Application downloader started", 
                        new Gson().toJsonTree(fileDownload)
                );
            }
            
            return new Response(
                    true, StatusCode.FILE_ALREADY_DOWNLOADING, 
                    "Application installer still on downloading"
            );
            
        } catch (Exception ex) {
            String errorMSg = "Unable to download application installer. "
                    + "Unexpected error occurred";
            return new Response(true, StatusCode.APPLICATION_ERROR, errorMSg);
        }
    }
}
