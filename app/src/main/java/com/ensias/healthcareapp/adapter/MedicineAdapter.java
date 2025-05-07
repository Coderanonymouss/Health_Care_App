package com.ensias.healthcareapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.MedicinesActivity;
import com.ensias.healthcareapp.model.Medicine;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private Context context;
    private List<Medicine> list;

    public MedicineAdapter(Context context, List<Medicine> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = list.get(position);
        holder.tvName.setText(medicine.getName());
        holder.tvDosage.setText(medicine.getDosage());
        holder.tvTime.setText("Время приёма: " + medicine.getTime());

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Удалить лекарство?")
                    .setMessage("Вы уверены, что хотите удалить \"" + medicine.getName() + "\"?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        list.remove(position);
                        notifyItemRemoved(position);

                        // Обновить SharedPreferences через Activity
                        if (context instanceof MedicinesActivity) {
                            ((MedicinesActivity) context).saveMedicines();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvMedDosage);
            tvTime = itemView.findViewById(R.id.tvMedTime);
        }
    }
}

