package com.ensias.healthcareapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.ensias.healthcareapp.Common.Common;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.adapter.MyViewPagerAdapter;
import com.ensias.healthcareapp.databinding.ActivityTestBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ensias.healthcareapp.Common.Common.step;
import static com.ensias.healthcareapp.fragment.BookingStep1Fragment.spinner;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;
    private LocalBroadcastManager localBroadcastManager;

    private final BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (step == 2) {
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT, -1);
            }
            binding.btnNextStep.setEnabled(true);
            setColorButton();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStepView();
        setColorButton();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));

        binding.viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        binding.viewPager.setOffscreenPageLimit(2);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setColorButton();
            }

            @Override
            public void onPageSelected(int position) {
                binding.stepView.go(position, true);

                binding.btnPreviousStep.setEnabled(position != 0);
                binding.btnNextStep.setEnabled(position != 2);
                setColorButton();
            }

            @Override public void onPageScrollStateChanged(int state) {}
        });

        binding.btnNextStep.setOnClickListener(v -> {
            if (step < 3 || step == 0) {
                step++;
                Common.Currentaappointementatype = spinner.getSelectedItem().toString();
                Log.e("Spinnr", Common.Currentaappointementatype);

                if (step == 1 && Common.CurreentDoctor != null) {
                    Common.currentTimeSlot = -1;
                    Common.currentDate = Calendar.getInstance();
                    loadTimeSlotOfDoctor(Common.CurreentDoctor);
                } else if (step == 2) {
                    confirmeBooking();
                }

                binding.viewPager.setCurrentItem(step);
            }
        });

        binding.btnPreviousStep.setOnClickListener(v -> {
            if (step == 3 || step > 0) {
                step--;
                binding.viewPager.setCurrentItem(step);
            }
        });

        loadTimeSlotOfDoctor("testdoc@testdoc.com");
    }

    private void confirmeBooking() {
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        step = 0;
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }

    private void loadTimeSlotOfDoctor(String doctorId) {
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void setColorButton() {
        binding.btnPreviousStep.setBackgroundResource(
                binding.btnPreviousStep.isEnabled() ? R.color.design_default_color_primary_dark : R.color.colorAccent
        );

        binding.btnNextStep.setBackgroundResource(
                binding.btnNextStep.isEnabled() ? R.color.design_default_color_primary_dark : R.color.colorAccent
        );
    }

    private void setupStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Purpose");
        stepList.add("Time and Date");
        stepList.add("Finish");
        binding.stepView.setSteps(stepList);
    }
}
