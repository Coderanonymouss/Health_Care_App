package com.ensias.healthcareapp.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.Interface.ITimeSlotLoadListener;
import com.ensias.healthcareapp.adapter.MyTimeSlotAdapter;
import com.ensias.healthcareapp.databinding.FragmentBookingStepTwoBinding;
import com.ensias.healthcareapp.model.TimeSlot;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class BookingStep2Fragment extends Fragment implements ITimeSlotLoadListener {

    private FragmentBookingStepTwoBinding binding;
    private ITimeSlotLoadListener iTimeSlotLoadListener;
    private AlertDialog dialog;
    private LocalBroadcastManager localBroadcastManager;
    private SimpleDateFormat simpleDateFormat;

    private final BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0);
            loadAvailableTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(date.getTime()));
        }
    };

    private void loadAvailableTimeSlotOfDoctor(String doctorId, final String bookDate) {
        dialog.show();
        FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FirebaseFirestore.getInstance()
                                .collection("Doctor")
                                .document(doctorId)
                                .collection(bookDate)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (querySnapshot.isEmpty()) {
                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                    } else {
                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc : querySnapshot)
                                            timeSlots.add(doc.toObject(TimeSlot.class));
                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                })
                                .addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
                    }
                });
    }

    static BookingStep2Fragment instance;

    public static BookingStep2Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep2Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iTimeSlotLoadListener = this;
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        localBroadcastManager.registerReceiver(displayTimeSlot, new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingStepTwoBinding.inflate(inflater, container, false);
        init();
        loadAvailableTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(Common.currentDate.getTime()));
        return binding.getRoot();
    }

    private void init() {
        binding.recycleTimeSlot.setHasFixedSize(true);
        binding.recycleTimeSlot.setLayoutManager(new GridLayoutManager(getContext(), 3));

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 5);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(binding.getRoot(), binding.calendarView.getId())
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .defaultSelectedDate(startDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.currentDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.currentDate = date;
                    loadAvailableTimeSlotOfDoctor(Common.CurreentDoctor, simpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        binding.recycleTimeSlot.setAdapter(new MyTimeSlotAdapter(getContext(), timeSlotList));
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        binding.recycleTimeSlot.setAdapter(new MyTimeSlotAdapter(getContext()));
        dialog.dismiss();
    }
}
