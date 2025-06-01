package com.ensias.healthcareapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.ensias.healthcareapp.activity.MainActivity;
import com.ensias.healthcareapp.fireStoreApi.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends BaseActivity {
    TextView languageOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        MaterialButton signOutBtn;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        languageOption = findViewById(R.id.language_option);

        updateLanguageText();

        languageOption.setOnClickListener(v -> showLanguageDialog());
        // 🟢 Кнопка "Шығу"
        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

    }

    private void updateLanguageText() {
        String code = LocaleHelper.getLanguage(this);
        switch (code) {
            case "kk":
                languageOption.setText("Қазақша");
                break;
            case "ru":
                languageOption.setText("Русский");
                break;
            case "en":
                languageOption.setText("English");
                break;
            default:
                languageOption.setText("Қазақша");
        }
    }

    private void showLanguageDialog() {
        final String[] languages = {"Қазақша", "Русский", "English"};
        final String[] langCodes = {"kk", "ru", "en"};
        String currentLang = LocaleHelper.getLanguage(this);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = langCodes[which];
                    if (currentLang.equals(selectedLang)) {
                        Toast.makeText(this, getString(R.string.language_already_selected), Toast.LENGTH_SHORT).show();
                    } else {
                        LocaleHelper.setLocale(this, selectedLang);
                        Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show();

                        // ВАЖНО: restart the whole app!
                        Intent intent = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finishAffinity(); // завершает все активити

                        }
                    }
                }).show();
    }
}
