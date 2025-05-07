package com.ensias.healthcareapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Medicine;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private Context context;
    private List<Medicine> medicineList;

    public MedicineAdapter(Context context, List<Medicine> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }
    private void showEditDialog(Medicine med, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_medicine, null);
        EditText etName = dialogView.findViewById(R.id.etMedName);
        EditText etDosage = dialogView.findViewById(R.id.etMedDosage);
        EditText etTimesPerDay = dialogView.findViewById(R.id.etTimesPerDay);
        EditText etDurationDays = dialogView.findViewById(R.id.etDurationDays);
        LinearLayout timeContainer = dialogView.findViewById(R.id.timePickerContainer);

        etName.setText(med.getName());
        etDosage.setText(med.getDosage());
        etTimesPerDay.setText(String.valueOf(med.getTimes().size())); // если редактируешь
        etDurationDays.setVisibility(View.GONE); // не нужно при редактировании

        // Добавляем таймпикеры для каждого времени
        for (String timeStr : med.getTimes()) {
            String[] parts = timeStr.split(":");
            TimePicker tp = new TimePicker(context);
            tp.setIs24HourView(true);
            tp.setHour(Integer.parseInt(parts[0]));
            tp.setMinute(Integer.parseInt(parts[1]));
            timeContainer.addView(tp);
        }

        new AlertDialog.Builder(context)
                .setTitle("Редактировать лекарство")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    med.setName(etName.getText().toString().trim());
                    med.setDosage(etDosage.getText().toString().trim());

                    List<String> updatedTimes = new ArrayList<>();
                    for (int i = 0; i < timeContainer.getChildCount(); i++) {
                        View view = timeContainer.getChildAt(i);
                        if (view instanceof TimePicker) {
                            TimePicker tp = (TimePicker) view;
                            String time = String.format("%02d:%02d", tp.getHour(), tp.getMinute());
                            updatedTimes.add(time);
                        }
                    }
                    med.setTimes(updatedTimes);
                    notifyItemChanged(position);

                    // Сохраняем изменения
                    if (context instanceof com.ensias.healthcareapp.activity.MedicinesActivity) {
                        ((com.ensias.healthcareapp.activity.MedicinesActivity) context).saveMedicines();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine med = medicineList.get(position);
        holder.tvName.setText(med.getName());
        holder.tvDosage.setText(med.getDosage());

        List<String> times = med.getTimes();
        if (times != null && !times.isEmpty()) {
            StringBuilder timeText = new StringBuilder();
            for (int i = 0; i < times.size(); i++) {
                timeText.append(times.get(i));
                if (i < times.size() - 1) timeText.append(", ");
            }
            holder.tvTimes.setText("Время приёма: " + timeText);
        } else {
            holder.tvTimes.setText("Время приёма не указано");
        }

        // Обработка кнопки "Настроить"
        holder.btnEdit.setOnClickListener(v -> {
            showEditDialog(med, position);
        });
    }


    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTimes;
        Button btnEdit;
        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvMedDosage);
            tvTimes = itemView.findViewById(R.id.tvMedTimes);
            btnEdit= itemView.findViewById(R.id.btnEditMedicine);
        }
    }
}
