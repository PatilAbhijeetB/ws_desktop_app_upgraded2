/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.presentation.mainwindow.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.workshiftly.application.state.StateName;
import com.workshiftly.application.state.StateStorage;
import com.workshiftly.common.model.AttendanceWidgetData;
import com.workshiftly.common.model.Response;
import com.workshiftly.common.utility.TimeUtility;
import com.workshiftly.common.utility.WorkDateTime;
import com.workshiftly.domain.DashboardDataModule;
import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Hashan
 */
public class AttendanceWidgetModel implements ViewModel {
    private static final InternalLogger LOGGER = LoggerService.getLogger(AttendanceWidgetModel.class);
    Gson GSON = new Gson();

    DashboardDataModule dashboardDataModule;
    DateTimeZone companyDateTimeZone;
    ZoneId companyDateTimeZoneId;
    WorkDateTime workDateTime;
    
   
    private final SimpleBooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<LocalDate> datePickerDate = new SimpleObjectProperty();
    private final SimpleStringProperty shiftInLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty actualInLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty shiftOutLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty actualOutLabelTextProperty = new SimpleStringProperty();
    private final SimpleStringProperty statusLabelTextProperty = new SimpleStringProperty();
    private final SimpleObjectProperty<Background> statusLabelColorProperty = new SimpleObjectProperty<>();
    private final SimpleStringProperty strCurrentDayName = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayNamem1 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayNamep1 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayNamep2 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayNamep3 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayNamep4 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayNamep5 = new SimpleStringProperty();
    
    
    private final SimpleStringProperty strCurrentDay = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDaym1 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayp1 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayp2 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayp3 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayp4 = new SimpleStringProperty();
    private final SimpleStringProperty strCurrentDayp5 = new SimpleStringProperty();
    
    private final SimpleStringProperty strMonthName = new SimpleStringProperty();
    
    
    
    
    public AttendanceWidgetModel() {
        try {
            loadingProperty.set(true);

            dashboardDataModule = new DashboardDataModule();
            companyDateTimeZone = StateStorage.getCurrentState(StateName.COMPANY_TIMEZONE);
            companyDateTimeZoneId = ZoneId.of(companyDateTimeZone.getID());

            while (true) {
                workDateTime = StateStorage.getCurrentState(StateName.CURRENT_WORKDATETIME_INSTANCE);
                if (workDateTime != null)
                    break;
            }
            long epochDay = workDateTime.getDateStartedTimestamp();
            Instant instant = Instant.ofEpochSecond(epochDay);
            LocalDate localDate = LocalDate.ofInstant(instant, this.companyDateTimeZoneId);
            datePickerDate.set(localDate);

            onChangeDate();

            loadingProperty.set(false);

            PublishSubject<Long> publisher = StateStorage.getCurrentState(StateName.LAST_WORK_STATUS_LOG_SYNCED_TIME);
            if (publisher == null) {
                publisher = PublishSubject.create();
                StateStorage.set(StateName.LAST_WORK_STATUS_LOG_SYNCED_TIME, PublishSubject.class, publisher);
            }
            publisher.subscribe((value) -> {
                if (value != null) {
                    LocalDate date = LocalDate.parse(Instant.ofEpochSecond(value)
                            .atZone(companyDateTimeZoneId)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE));
                    boolean isPublisherDateEqualToTheUserSelectedDate = date.atStartOfDay(companyDateTimeZoneId)
                            .equals(datePickerDate.get().atStartOfDay(companyDateTimeZoneId));
                    if (isPublisherDateEqualToTheUserSelectedDate) {
                        onChangeDate();
                    }
                }
            });

        } catch (Exception ex) {
            LOGGER.logRecord(InternalLogger.LOGGER_LEVEL.WARNING, "Error occurred when initializing Attendance Widget Model constructor", ex);
        }
    }

     
    
    public SimpleBooleanProperty getLoadingProperty() {
        return loadingProperty;
    }

    public SimpleObjectProperty getDatePickerDate() {
        return datePickerDate;
    }

    public SimpleStringProperty getShiftInLabelTextProperty() {
        return shiftInLabelTextProperty;
    }

    public SimpleStringProperty getActualInLabelTextProperty() {
        return actualInLabelTextProperty;
    }

    public SimpleStringProperty getShiftOutLabelTextProperty() {
        return shiftOutLabelTextProperty;
    }

    public SimpleStringProperty getActualOutLabelTextProperty() {
        return actualOutLabelTextProperty;
    }

    public SimpleStringProperty getStatusLabelTextProperty() {
        return statusLabelTextProperty;
    }

    public SimpleObjectProperty<Background> getStatusLabelColorProperty() {
        return statusLabelColorProperty;
    }
    
    public final void onChangeDate() {
        Service<Response> getUserAttendanceDataService;
        getUserAttendanceDataService = new Service<Response>() {
            @Override
            protected Task<Response> createTask() {
                return new Task<Response>() {
                    @Override
                    protected Response call() throws Exception {
                        long date = datePickerDate.get()
                                .atStartOfDay(companyDateTimeZoneId)
                                .toEpochSecond();
                       
                        LocalDate selectedDate = datePickerDate.getValue();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");
                        String strCurrentdayName = selectedDate.format(formatter);
                        strCurrentDayName.set(strCurrentdayName);
                        int dayCurrentOfMonth = selectedDate.getDayOfMonth();
                        strCurrentDay.set(String.valueOf(dayCurrentOfMonth));
                        
                        String strMName = selectedDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
                        strMonthName.set(strMName);
                        
                        LocalDate previousDate = selectedDate.minusDays(1);
                        String strCurrentdayNamem1 = previousDate.format(formatter);
                        strCurrentDayNamem1.set(strCurrentdayNamem1);
                        int dayCurrentOfMonthm1 = previousDate.getDayOfMonth();
                        strCurrentDaym1.set(String.valueOf(dayCurrentOfMonthm1));
                        
                        LocalDate cDatep1 = selectedDate.plusDays(1);
                        String strCurrentdayNamep1 = cDatep1.format(formatter);
                        strCurrentDayNamep1.set(strCurrentdayNamep1);
                        int dayCurrentOfMonthp1 = cDatep1.getDayOfMonth();
                        strCurrentDayp1.set(String.valueOf(dayCurrentOfMonthp1));
                        
                        LocalDate cDatep2 = selectedDate.plusDays(2);
                        String strCurrentdayNamep2 = cDatep2.format(formatter);
                        strCurrentDayNamep2.set(strCurrentdayNamep2);
                        int dayCurrentOfMonthp2 = cDatep2.getDayOfMonth();
                        strCurrentDayp2.set(String.valueOf(dayCurrentOfMonthp2));
                        
                        LocalDate cDatep3 = selectedDate.plusDays(3);
                        String strCurrentdayNamep3 = cDatep3.format(formatter);
                        strCurrentDayNamep3.set(strCurrentdayNamep3);
                        int dayCurrentOfMonthp3 = cDatep3.getDayOfMonth();
                        strCurrentDayp3.set(String.valueOf(dayCurrentOfMonthp3));
                        
                        LocalDate cDatep4 = selectedDate.plusDays(4);
                        String strCurrentdayNamep4 = cDatep4.format(formatter);
                        strCurrentDayNamep4.set(strCurrentdayNamep4);
                        int dayCurrentOfMonthp4 = cDatep4.getDayOfMonth();
                        strCurrentDayp4.set(String.valueOf(dayCurrentOfMonthp4));
                        
                        LocalDate cDatep5 = selectedDate.plusDays(5);
                        String strCurrentdayNamep5 = cDatep5.format(formatter);
                        strCurrentDayNamep5.set(strCurrentdayNamep5);
                        int dayCurrentOfMonthp5 = cDatep5.getDayOfMonth();
                        strCurrentDayp5.set(String.valueOf(dayCurrentOfMonthp5));
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        WorkDateTime workDateTime = StateStorage.getCurrentState(StateName.CURRENT_WORKDATETIME_INSTANCE);
                        String todayString = workDateTime.getFormattedDate();
                        long today = LocalDate.parse(todayString)
                                .atStartOfDay()
                                .atZone(companyDateTimeZoneId)
                                .toInstant()
                                .getEpochSecond();
                        
                        
                        Response response = dashboardDataModule.getUserAttendanceData(date, today);
                        return response;
                    }
                };
            }
        };

        getUserAttendanceDataService.setOnRunning((WorkerStateEvent workerStateEvent) -> {
         //   loadingProperty.set(true);
        });

        getUserAttendanceDataService.setOnSucceeded((WorkerStateEvent workerStateEvent) -> {
            Response result = getUserAttendanceDataService.getValue();

            JsonElement resultObj = result.getData();
            Type AttendanceWidgetDataType = new TypeToken<AttendanceWidgetData>() {}.getType();
            AttendanceWidgetData attendanceWidgetData = GSON.fromJson(resultObj, AttendanceWidgetDataType);

            String shiftIn = attendanceWidgetData.getStartShift() != null
                    ? TimeUtility.getHumanReadbleTimeOnly(attendanceWidgetData.getStartShift(), companyDateTimeZone) : "-";
            String shiftOut = attendanceWidgetData.getEndShift() != null
                    ? TimeUtility.getHumanReadbleTimeOnly(attendanceWidgetData.getEndShift(), companyDateTimeZone) : "-";
            String actualIn = attendanceWidgetData.getStartWorking() != null
                    ? TimeUtility.getHumanReadbleTimeOnly(attendanceWidgetData.getStartWorking(), companyDateTimeZone) : "-";
            String actualOut = attendanceWidgetData.getEndWorking() != null
                    ? TimeUtility.getHumanReadbleTimeOnly(attendanceWidgetData.getEndWorking(), companyDateTimeZone) : "-";

            shiftInLabelTextProperty.set(shiftIn);
            shiftOutLabelTextProperty.set(shiftOut);
            actualInLabelTextProperty.set(actualIn);
            actualOutLabelTextProperty.set(actualOut);

            Color color;
            switch (attendanceWidgetData.getStatus()) {
                case COMPLETE:
                    color = Color.web("#0CC075");
                    break;
                case INCOMPLETE:
                    color = Color.web("#FDE089");
                    break;
                case HOLIDAY:
                    color = Color.web("#C7CDD2");
                    break;
                default:
                    color = Color.web("#FC657C");
                    break;
            }

            CornerRadii cornerRadii = new CornerRadii(10);
            BackgroundFill backgroundFill = new BackgroundFill(color, cornerRadii, Insets.EMPTY);
            Background background = new Background(backgroundFill);
            statusLabelColorProperty.set(background);

            String status = attendanceWidgetData.getStatus().name().substring(0, 1).toUpperCase()
                    + attendanceWidgetData.getStatus().name().substring(1).toLowerCase();
            statusLabelTextProperty.set(status);

          //  loadingProperty.set(false);
        });

        getUserAttendanceDataService.start();
    }
    
     public SimpleStringProperty getDayNameProperty() {
        return strCurrentDayName;
    }
      public SimpleStringProperty getDayNamem1Property() {
        return strCurrentDayNamem1;
    }
       public SimpleStringProperty getDayNamep1Property() {
        return strCurrentDayNamep1;
    }
        public SimpleStringProperty getDayNamep2Property() {
        return strCurrentDayNamep2;
    }
         public SimpleStringProperty getDayNamep3Property() {
        return strCurrentDayNamep3;
    }
          public SimpleStringProperty getDayNamep4Property() {
        return strCurrentDayNamep4;
    }
           public SimpleStringProperty getDayNamep5Property() {
        return strCurrentDayNamep5;
    }
    public SimpleStringProperty getDayNumProperty() {
        return strCurrentDay;
    }
    public SimpleStringProperty getDayNumm1Property() {
        return strCurrentDaym1;
    }
    public SimpleStringProperty getDayNump1Property() {
        return strCurrentDayp1;
    }
    public SimpleStringProperty getDayNump2Property() {
        return strCurrentDayp2;
    }
    public SimpleStringProperty getDayNump3Property() {
        return strCurrentDayp3;
    }
    public SimpleStringProperty getDayNump4Property() {
        return strCurrentDayp4;
    }
    public SimpleStringProperty getDayNump5Property() {
        return strCurrentDayp5;
    }
    public SimpleStringProperty getMonthNameProperty() {
        return strMonthName;
    }
}
