package com.ensias.healthcareapp.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.R;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ActivityCalendarActivity extends AppCompatActivity {

    MaterialCalendarView calendarView;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        Set<String> watchedDates = prefs.getStringSet("watchedDates", new HashSet<>());
        HashSet<CalendarDay> days = new HashSet<>();

        for (String dateStr : watchedDates) {
            LocalDate localDate = LocalDate.parse(dateStr);
            days.add(CalendarDay.from(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()));
        }

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return days.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.GREEN)); // Просмотренные — зелёным
            }
        });

// Добавим декоратор для сегодняшнего дня
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return day.equals(CalendarDay.today());
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new ForegroundColorSpan(Color.BLUE)); // Сегодня — синим
            }
        });

    }
}

