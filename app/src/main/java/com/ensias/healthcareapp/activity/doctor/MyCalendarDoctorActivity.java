package com.ensias.healthcareapp.activity.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.Interface.ITimeSlotLoadListener;
import com.ensias.healthcareapp.adapter.MyTimeSlotAdapter;
import com.ensias.healthcareapp.databinding.ActivityMyCalendarDoctorBinding;
import com.ensias.healthcareapp.model.TimeSlot;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class MyCalendarDoctorActivity extends AppCompatActivity implements ITimeSlotLoadListener {

    private ActivityMyCalendarDoctorBinding binding;

    private DocumentReference doctorDoc;
    private ITimeSlotLoadListener iTimeSlotLoadListener;
    private AlertDialog alertDialog;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyCalendarDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        iTimeSlotLoadListener = this;
        alertDialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();

        Calendar date = Calendar.getInstance();
        loadAvailabelTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(date.getTime()));

        binding.recycleTimeSlot2.setHasFixedSize(true);
        binding.recycleTimeSlot2.setLayoutManager(new GridLayoutManager(this, 3));

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 5);

        HorizontalCalendar calendar = new HorizontalCalendar.Builder(this, binding.calendarView2.getId())
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .defaultSelectedDate(startDate)
                .build();

        calendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.currentDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.currentDate = date;
                    loadAvailabelTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    private void loadAvailabelTimeSlotOfDoctor(String curreentDoctor, String bookDate) {
        alertDialog.show();

        doctorDoc = FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(curreentDoctor);

        doctorDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot docSnap = task.getResult();
                if (docSnap != null && docSnap.exists()) {
                    CollectionReference dateRef = FirebaseFirestore.getInstance()
                            .collection("Doctor")
                            .document(curreentDoctor)
                            .collection(bookDate);

                    dateRef.get()
                            .addOnCompleteListener(snapshotTask -> {
                                if (snapshotTask.isSuccessful()) {
                                    List<TimeSlot> timeSlots = new ArrayList<>();
                                    for (QueryDocumentSnapshot doc : snapshotTask.getResult()) {
                                        timeSlots.add(doc.toObject(TimeSlot.class));
                                    }

                                    if (timeSlots.isEmpty()) {
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    } else {
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
                }
            }
        });
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        binding.recycleTimeSlot2.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        binding.recycleTimeSlot2.setAdapter(adapter);
        alertDialog.dismiss();
    }
}
