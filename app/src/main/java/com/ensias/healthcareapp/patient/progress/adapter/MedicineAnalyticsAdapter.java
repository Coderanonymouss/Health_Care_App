package com.ensias.healthcareapp.patient.progress.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.MedicineAnalyticsRow;

import java.util.List;

public class MedicineAnalyticsAdapter extends RecyclerView.Adapter<MedicineAnalyticsAdapter.ViewHolder> {
    private final List<MedicineAnalyticsRow> data;
    public MedicineAnalyticsAdapter(List<MedicineAnalyticsRow> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_analytics, parent, false);
        return new ViewHolder(view);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineAnalyticsRow row = data.get(position);
        holder.tvName.setText(row.name);
        holder.tvDays.setText("Курс: " + row.totalDays + " д.");
        holder.tvTaken.setText("Принято: " + row.takenCount);
        holder.tvMissed.setText("Пропущено: " + row.missedCount);
        holder.tvPercent.setText(String.format("%.1f%%", row.progressPercent));
    }
    @Override
    public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDays, tvTaken, tvMissed, tvPercent;
        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvMedicineName);
            tvDays = view.findViewById(R.id.tvCourseDays);
            tvTaken = view.findViewById(R.id.tvTakenCount);
            tvMissed = view.findViewById(R.id.tvMissedCount);
            tvPercent = view.findViewById(R.id.tvPercent);
        }
    }
}
