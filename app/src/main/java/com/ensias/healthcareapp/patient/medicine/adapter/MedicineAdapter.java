package com.ensias.healthcareapp.patient.medicine.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Medicine;
import com.ensias.healthcareapp.patient.medicine.MedicinesActivity;

import java.text.SimpleDateFormat;
import java.util.*;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Medicine medicine, int position);
    }

    private final MedicinesActivity activity;
    private final Context context;
    private final List<Medicine> medicineList;
    private final OnDeleteListener onDeleteListener;
    private static final int WINDOW_MINUTES = 45;

    public MedicineAdapter(Context context, List<Medicine> medicineList, OnDeleteListener onDeleteListener, MedicinesActivity activity, String userId) {
        this.context = context;
        this.medicineList = medicineList;
        this.onDeleteListener = onDeleteListener;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine med = medicineList.get(position);
        holder.tvName.setText(med.getName());
        holder.tvDosage.setText(med.getDosage());
        holder.tvTimes.setText("Қабылдау уақыты: " + (med.getTimes() == null || med.getTimes().isEmpty()
                ? "белгісіз"
                : String.join(", ", med.getTimes())));

        // --- Логика отображения кнопки ---
        Calendar now = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.getTime());

        String matchedSlot = null;
        for (String timeStr : med.getTimes()) {
            String[] parts = timeStr.split(":");
            Calendar sc = Calendar.getInstance();
            sc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            sc.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            sc.set(Calendar.SECOND, 0);
            sc.set(Calendar.MILLISECOND, 0);

            Calendar lower = (Calendar) sc.clone(); lower.add(Calendar.MINUTE, -WINDOW_MINUTES);
            Calendar upper = (Calendar) sc.clone(); upper.add(Calendar.MINUTE, WINDOW_MINUTES);

            if (!now.before(lower) && !now.after(upper)) {
                matchedSlot = timeStr; break;
            }
        }

        String displayKey = matchedSlot != null ? today + " " + matchedSlot : null;

        boolean taken = displayKey != null && Boolean.TRUE.equals(activity.getIntakeStatusLocally(med.getId(), displayKey));
        holder.btnTake.setVisibility(taken || displayKey == null ? View.GONE : View.VISIBLE);

        holder.tvLastTaken.setText(taken ? ("Соңғы қабылдау: " + displayKey) : "Соңғы қабылдау: --:--");

        holder.btnTake.setOnClickListener(v -> {
            if (displayKey != null) {
                activity.saveIntakeLocally(med.getId(), displayKey, true);
                activity.saveIntakeToFirestore(med.getId(), med.getName(), displayKey, true);
                notifyItemChanged(position);
                Toast.makeText(context, med.getName() + " қабылданды ✅", Toast.LENGTH_SHORT).show();
            }
        });

        // --- История последних 5 приёмов ---
        holder.historyContainer.removeAllViews();
        SharedPreferences prefs = context.getSharedPreferences("MedPrefs_" + activity.userId, Context.MODE_PRIVATE);
        List<String> recent = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, -i);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
            for (String t : med.getTimes()) {
                String k = date + " " + t;
                if (prefs.contains(med.getId() + "_" + k)) {
                    boolean st = prefs.getBoolean(med.getId() + "_" + k, false);
                    recent.add((st ? "✅" : "❌") + " " + k);
                }
            }
        }
        for (String s : recent) {
            TextView tv = new TextView(context);
            tv.setText(s);
            tv.setTextSize(13);
            tv.setPadding(8, 2, 8, 2);
            holder.historyContainer.addView(tv);
        }

        // --- Удаление ---
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteListener != null) onDeleteListener.onDelete(med, position);
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTimes, tvLastTaken;
        Button btnTake;
        LinearLayout historyContainer;
        ImageButton btnDelete;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvMedDosage);
            tvTimes = itemView.findViewById(R.id.tvMedTimes);
            tvLastTaken = itemView.findViewById(R.id.tvLastTaken);
            btnTake = itemView.findViewById(R.id.btnTakeMedicine);
            historyContainer = itemView.findViewById(R.id.historyContainer);
            btnDelete = itemView.findViewById(R.id.btnDeleteMedicine);
        }
    }
}
