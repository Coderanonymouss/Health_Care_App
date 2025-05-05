package com.ensias.healthcareapp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.databinding.FragmentBookingStep3Binding;
import com.ensias.healthcareapp.model.ApointementInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BookingStep3Fragment extends Fragment {

    private FragmentBookingStep3Binding binding;
    private SimpleDateFormat simpleDateFormat;
    private LocalBroadcastManager localBroadcastManager;

    private final BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TAG", "onReceive: confirmed");
            setData();
        }
    };

    public BookingStep3Fragment() {}

    public static BookingStep3Fragment getInstance() {
        return new BookingStep3Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingStep3Binding.inflate(inflater, container, false);

        binding.btnConfirm.setOnClickListener(v -> confirmAppointment());

        return binding.getRoot();
    }

    private void confirmAppointment() {
        ApointementInformation apInfo = new ApointementInformation();
        apInfo.setApointementType(Common.Currentaappointementatype);
        apInfo.setDoctorId(Common.CurreentDoctor);
        apInfo.setDoctorName(Common.CurrentDoctorName);
        apInfo.setPatientName(Common.CurrentUserName);
        apInfo.setPatientId(Common.CurrentUserid);
        apInfo.setChemin("Doctor/" + Common.CurreentDoctor + "/" + Common.simpleFormat.format(Common.currentDate.getTime()) + "/" + Common.currentTimeSlot);
        apInfo.setType("Checked");
        apInfo.setTime(Common.convertTimeSlotToString(Common.currentTimeSlot) + " at " + simpleDateFormat.format(Common.currentDate.getTime()));
        apInfo.setSlot((long) Common.currentTimeSlot);

        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("Doctor")
                .document(Common.CurreentDoctor)
                .collection(Common.simpleFormat.format(Common.currentDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        bookingDate.set(apInfo)
                .addOnSuccessListener(unused -> {
                    requireActivity().finish();
                    Toast.makeText(getContext(), "Успешно!", Toast.LENGTH_SHORT).show();
                    Common.currentTimeSlot = -1;
                    Common.currentDate = Calendar.getInstance();
                    Common.step = 0;
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    FirebaseFirestore.getInstance()
                            .collection("Doctor")
                            .document(Common.CurreentDoctor)
                            .collection("apointementrequest")
                            .document(apInfo.getTime().replace("/", "_"))
                            .set(apInfo);

                    FirebaseFirestore.getInstance()
                            .collection("Patient")
                            .document(apInfo.getPatientId())
                            .collection("calendar")
                            .document(apInfo.getTime().replace("/", "_"))
                            .set(apInfo);
                });
    }

    private void setData() {
        binding.txtBookingBerberText.setText(Common.CurrentDoctorName);
        binding.txtBookingTimeText.setText(Common.convertTimeSlotToString(Common.currentTimeSlot)
                + " at " + simpleDateFormat.format(Common.currentDate.getTime()));
        binding.txtBookingPhone.setText(Common.CurrentPhone);
        binding.txtBookingType.setText(Common.Currentaappointementatype);
    }
}
