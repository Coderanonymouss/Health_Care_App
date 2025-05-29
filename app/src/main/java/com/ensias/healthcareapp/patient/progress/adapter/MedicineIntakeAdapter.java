package com.ensias.healthcareapp.patient.progress.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.MedicineIntakeRow;

import java.util.List;
public class MedicineIntakeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;
    private final List<MedicineIntakeRow> data;

    public MedicineIntakeAdapter(List<MedicineIntakeRow> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ROW;
    }

    @Override
    public int getItemCount() {
        return data.size() + 1; // +1 for header
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_medicine_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_medicine_intake, parent, false);
            return new RowViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RowViewHolder) {
            MedicineIntakeRow row = data.get(position - 1); // -1 потому что 0 — header
            ((RowViewHolder) holder).tvMedName.setText(row.name);
            ((RowViewHolder) holder).tvTakenToday.setText(String.valueOf(row.takenToday));
            ((RowViewHolder) holder).tvRemainingToday.setText(String.valueOf(row.remainingToday));
            ((RowViewHolder) holder).tvDaysLeft.setText(String.valueOf(row.daysLeft));
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View view) { super(view); }
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvTakenToday, tvRemainingToday, tvDaysLeft;
        RowViewHolder(View view) {
            super(view);
            tvMedName = view.findViewById(R.id.tvMedName);
            tvTakenToday = view.findViewById(R.id.tvTakenToday);
            tvRemainingToday = view.findViewById(R.id.tvRemainingToday);
            tvDaysLeft = view.findViewById(R.id.tvDaysLeft);
        }
    }
}
