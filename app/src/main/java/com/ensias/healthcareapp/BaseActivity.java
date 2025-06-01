package com.ensias.healthcareapp;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.ensias.healthcareapp.fireStoreApi.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
