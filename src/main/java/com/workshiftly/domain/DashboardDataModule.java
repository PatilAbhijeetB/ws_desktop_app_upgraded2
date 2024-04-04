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
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.constant.ProductivityStatus;
import com.workshiftly.common.constant.StatusCode;
import com.workshiftly.common.exception.AuthenticationException;
import com.workshiftly.common.model.ActivitySummaryWidgetData;
import com.workshiftly.common.model.AttendanceWidgetData;
import com.workshiftly.common.model.ProductivityWidgetData;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.model.UserSession;
import com.workshiftly.common.utility.AppValidator;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.service.http.HttpRequestCaller;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Hashan
 */
public class DashboardDataModule {
    private static final InternalLogger LOGGER = LoggerService.getLogger(DashboardDataModule.class);

    int MAXIMUM_ACTIVITY_SUMMARY_ITEM_COUNT = 5;

    Gson GSON = new Gson();
    HttpRequestCaller httpRequestCaller = new HttpRequestCaller();

    public Response getUserAttendanceData(long date, long today) {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);

            String companyId = userSession.getCompanyId();
            String userId = userSession.getId();

            Instant dateInstant = Instant.ofEpochSecond(date);
            Instant dateEndInstant = dateInstant.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.SECONDS);
            long dateEnd = dateEndInstant.getEpochSecond();

            String from = Long.toString(date);
            String to = Long.toString(dateEnd);
            String todayString = Long.toString(today);

            Response response = httpRequestCaller.callGetUserAttendanceData(companyId, userId, from, to, todayString);

            if (response.isError()) {
                String errorMsg = "Error occured while retrieving user attendance data. (" + response.getMessage() + ")";
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, null);
                return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
            }

            JsonArray userAttendanceArray = response.getData().getAsJsonObject()
                    .get("userAttendance").getAsJsonArray();

            if (userAttendanceArray.size() < 1) {
                String errorMsg = "Empty array. User attendance data missing.";
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, null);
                return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
            }

            JsonObject userAttendanceObject = userAttendanceArray.get(0).getAsJsonObject();
            AttendanceWidgetData attendanceWidgetData = GSON.fromJson(userAttendanceObject, AttendanceWidgetData.class);

            AttendanceWidgetData.Status status;
            if (attendanceWidgetData.getStartWorking() == null) {
                status = attendanceWidgetData.getIsHoliday()
                        ? AttendanceWidgetData.Status.HOLIDAY
                        : AttendanceWidgetData.Status.ABSENT;
            } else if (attendanceWidgetData.getEndWorking() == null) {
                status = AttendanceWidgetData.Status.INCOMPLETE;
            } else {
                boolean isShiftComplete = attendanceWidgetData.getEndShift() - attendanceWidgetData.getStartShift()
                        <= attendanceWidgetData.getEndWorking() - attendanceWidgetData.getStartWorking();

                status = isShiftComplete
                        ? AttendanceWidgetData.Status.COMPLETE
                        : AttendanceWidgetData.Status.INCOMPLETE;
            }

            attendanceWidgetData.setDate(date);
            attendanceWidgetData.setUserId(userId);
            attendanceWidgetData.setStatus(status);

            JsonElement responseData = GSON.toJsonTree(attendanceWidgetData);
            return new Response(false, StatusCode.SUCCESS, "Successfully retrieved user attendance data.", responseData);
        } catch (JsonSyntaxException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user attendance data", e);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while retriving user attendance data");
        } catch (AuthenticationException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user attendance data - Session invalidated", e);
            return new Response(true, StatusCode.SESSION_INVALID, "Session invalidated");
        } catch (Exception e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user attendance data", e);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while retriving user attendance data");
        }
    }

    public Response getUserActivitySummaryData(LocalDate weekStartedDate) {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);

            String companyId = userSession.getCompanyId();
            String userId = userSession.getId();
            
           // LocalDate weekStartedDate = LocalDate.now(); // Example date

        Instant weekStartingInstant1 = Instant.ofEpochSecond(
                weekStartedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond()
        );
        Instant weekEndingInstant1 = weekStartingInstant1.plus(7, java.time.temporal.ChronoUnit.DAYS).minus(1, java.time.temporal.ChronoUnit.SECONDS);

        // Convert Instant to LocalDate to format it
        LocalDate weekStartingDate1 = weekStartingInstant1.atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate weekEndingDate1 = weekEndingInstant1.atZone(ZoneId.of("UTC")).toLocalDate();

        // Define date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format the dates
        String formattedWeekStartingDate = weekStartingDate1.format(formatter);
        String formattedWeekEndingDate = weekEndingDate1.format(formatter);

            
            

            Instant weekStartingInstant = Instant.ofEpochSecond(
                    weekStartedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond()
            );
            Instant weekEndingInstant = weekStartingInstant.plus(7, ChronoUnit.DAYS).minus(1, ChronoUnit.SECONDS);
            long weekEndingTimestamp = weekEndingInstant.getEpochSecond();
            
            long weekStartingTimestamp = weekStartingInstant.getEpochSecond();
            
             String hfromDate = formattedWeekStartingDate;
             String htoDate = formattedWeekEndingDate;
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
            String fromDate = weekStartedDate.format(dateFormatter);
            
            LocalDate weekEndDate = weekStartedDate.plusDays(7);
            String toDate = weekEndDate.format(dateFormatter);
            
           // Response response = httpRequestCaller.callGetUserActivitySummaryData(
           //         companyId, userId, fromDate, toDate
          // );
            
             Response response = httpRequestCaller.callGetUserActivitySummaryData(
                    companyId, userId, hfromDate, htoDate
            );

            if (response.isError()) {
                String errorMsg = "Error occured while retrieving user activity summary data. (" + response.getMessage() + ")";
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, null);
                return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
            }

            JsonObject userActivitySummaryObject = response.getData().getAsJsonObject()
                    .get("dataList").getAsJsonArray().get(0).getAsJsonObject();

            userActivitySummaryObject.remove("user");
            userActivitySummaryObject.remove("workTime");
            userActivitySummaryObject.remove("idleTime");
            userActivitySummaryObject.remove("_userId");
            userActivitySummaryObject.remove("companyId");

            HashMap<String, Double> processedActivitySummaryDataMap = new HashMap<>();
            if (!userActivitySummaryObject.isJsonNull()) {
                HashMap<String, Double> rawDataMap = new Gson().fromJson(userActivitySummaryObject.toString(), HashMap.class);

                rawDataMap.values().removeAll(Collections.singleton(null));

                HashMap<String, Double> sortedDataMap = rawDataMap.entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

                int processedCount = 0;
                Double totalActivityTime = sortedDataMap.values().stream().mapToDouble(f -> f.doubleValue()).sum();
                Double processedActivityTime = 0.0;

                Iterator<Map.Entry<String, Double>> iterator = sortedDataMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Double> entry = iterator.next();

                    if (processedCount > (MAXIMUM_ACTIVITY_SUMMARY_ITEM_COUNT - 1)) {
                        break;
                    }

                    if (entry.getValue() > 0) {
                        processedCount++;
                        processedActivityTime += entry.getValue();

                        Double processedValue = (entry.getValue() / totalActivityTime) * 100;
                        processedActivitySummaryDataMap.put(entry.getKey(), processedValue);
                    }
                }

                Double otherValue = ((totalActivityTime - processedActivityTime) / totalActivityTime) * 100;
                if (otherValue > 0) {
                    processedActivitySummaryDataMap.put("Other", otherValue);
                }
            }

            ActivitySummaryWidgetData activitySummaryWidgetData = new ActivitySummaryWidgetData();
            activitySummaryWidgetData.setUserId(userId);
            activitySummaryWidgetData.setWeek(weekEndingTimestamp);
            activitySummaryWidgetData.setSummary(processedActivitySummaryDataMap);

            JsonElement responseData = GSON.toJsonTree(activitySummaryWidgetData);
            return new Response(false, StatusCode.SUCCESS, "Successfully retrieved user activity summary data.", responseData);
        } catch (IndexOutOfBoundsException e) {
            return new Response(true, StatusCode.APPLICATION_ERROR, "No data");
        } catch (JsonSyntaxException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user activity summary data", e);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while retriving user activity summary data");
        } catch (AuthenticationException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user activity summary data - Session invalidated", e);
            return new Response(true, StatusCode.SESSION_INVALID, "Session invalidated");
        } catch (Exception e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user activity summary data", e);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while retriving user activity summary data");
        }
    }
    
    /*
     * 
     * Fetch User Productivity Data related to the week
     */
    public Response getUserProductivityData(long weekStartingTimestamp) {
        try {
            UserSession userSession = StateStorage.getCurrentState(StateName.USER_SESSION);
            AppValidator.validateUserSession(userSession);

            String companyId = userSession.getCompanyId();
            String userId = userSession.getId();

            Instant weekStartingInstant = Instant.ofEpochSecond(weekStartingTimestamp);
            Instant weekEndingInstant = weekStartingInstant.plus(7, ChronoUnit.DAYS).minus(1, ChronoUnit.SECONDS);
            long weekEndingTimestamp = weekEndingInstant.getEpochSecond();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date weekStartingInstantDate = Date.from(weekStartingInstant);
            Date weekEndingInstantDate = Date.from(weekEndingInstant);

            String from = formatter.format(weekStartingInstantDate);
            String to = formatter.format(weekEndingInstantDate);
            
            LocalDate startDate = LocalDate.parse(from);
            LocalDate endDate = LocalDate.parse(to).plusDays(1);

            List<LocalDate> listOfDates = startDate.datesUntil(endDate)
                    .collect(Collectors.toList());
            
            Response response = httpRequestCaller.callGetUserProductivityWidgetSummery(companyId, "users", userId, from, to);

            if (response.isError()) {
                String errorMsg = "Error occured while retrieving user activity summary data. (" + response.getMessage() + ")";
                LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, errorMsg, null);
                return new Response(true, StatusCode.APPLICATION_ERROR, errorMsg);
            }
            
            JsonArray userproductivityArray = response.getData().getAsJsonObject()
                    .get("productivityData").getAsJsonArray();
            HashMap<String, HashMap<String, Double>> processedActivitySummaryDataMap = new HashMap<>();
            HashMap<String, HashMap<String, Double>> processedActivitySummaryDataMaptemp = new HashMap<>();
            if (userproductivityArray.size() != 0 ) {
                JsonArray userproductivityObjectArray = userproductivityArray.get(0).getAsJsonObject().get("applicationDailyData").getAsJsonArray();
                if (userproductivityObjectArray.size() != 0 ) {
                    userproductivityObjectArray.forEach(productivity -> {
                        JsonArray productivityArray = productivity.getAsJsonObject().get("applicationData").getAsJsonArray();
                        HashMap<String, Double> DataMap = new HashMap<>();
                        DataMap.put(ProductivityStatus.PRODUCTIVE.name(), 0.0);
                        DataMap.put(ProductivityStatus.UNPRODUCTIVE.name(), 0.0);
                        DataMap.put(ProductivityStatus.NEUTRAL.name(), 0.0);
                        DataMap.put(ProductivityStatus.UNRATED.name(), 0.0);
                        if (!productivityArray.isJsonNull()) {
                            productivityArray.forEach(productivityDate -> {
                                JsonObject productivityObj = productivityDate.getAsJsonObject();

                                switch (productivityObj.get("status").getAsString()) {
                                    case "PRODUCTIVE":
                                        DataMap.put(ProductivityStatus.PRODUCTIVE.name(), productivityObj.get("percentage").getAsDouble());
                                        break;
                                    case "UNPRODUCTIVE":
                                        DataMap.put(ProductivityStatus.UNPRODUCTIVE.name(), productivityObj.get("percentage").getAsDouble());
                                        break;
                                    case "NEUTRAL":
                                        DataMap.put(ProductivityStatus.NEUTRAL.name(), productivityObj.get("percentage").getAsDouble());
                                        break;
                                    case "UNRATED":
                                    default:
                                        DataMap.put(ProductivityStatus.UNRATED.name(), productivityObj.get("percentage").getAsDouble());
                                        break;
                                }
                            });
                        }
                        processedActivitySummaryDataMaptemp.put(productivity.getAsJsonObject().get("date").getAsString(), DataMap);
                    }); 
                }
            }
            for(LocalDate localDate: listOfDates){
                String lDate = localDate.toString();
                if(processedActivitySummaryDataMaptemp.containsKey(lDate)){
                    processedActivitySummaryDataMap.put(lDate.substring(5,10), processedActivitySummaryDataMaptemp.get(lDate));
                }else{
                    HashMap<String, Double> DataMap = new HashMap<>();
                    DataMap.put(ProductivityStatus.PRODUCTIVE.name(), 0.0);
                    DataMap.put(ProductivityStatus.UNPRODUCTIVE.name(), 0.0);
                    DataMap.put(ProductivityStatus.NEUTRAL.name(), 0.0);
                    DataMap.put(ProductivityStatus.UNRATED.name(), 0.0);
                    processedActivitySummaryDataMap.put(lDate.substring(5,10), DataMap);
                }
            }
            ProductivityWidgetData productivityWidgetData = new ProductivityWidgetData();
            productivityWidgetData.setUserId(userId);
            productivityWidgetData.setWeek(weekEndingTimestamp);
            productivityWidgetData.setSummary(processedActivitySummaryDataMap);

            JsonElement responseData = GSON.toJsonTree(productivityWidgetData);
            return new Response(false, StatusCode.SUCCESS, "Successfully retrieved user activity summary data.", responseData);
        } catch (IndexOutOfBoundsException e) {
            return new Response(true, StatusCode.APPLICATION_ERROR, "No data");
        } catch (JsonSyntaxException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user activity summary data", e);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while retriving user activity summary data");
        } catch (AuthenticationException e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user activity summary data - Session invalidated", e);
            return new Response(true, StatusCode.SESSION_INVALID, "Session invalidated");
        } catch (Exception e) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.SEVERE, "Error occurred while retriving user activity summary data", e);
            return new Response(true, StatusCode.APPLICATION_ERROR, "Error occurred while retriving user activity summary data");
        }
    }
}
