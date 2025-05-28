package com.ensias.healthcareapp.patient.medicine.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.model.Intake;

import java.util.List;

public class IntakeAdapter extends RecyclerView.Adapter<IntakeAdapter.IntakeViewHolder> {

    private final List<Intake> intakeList;

    public IntakeAdapter(List<Intake> intakeList) {
        this.intakeList = intakeList;
    }

    @NonNull
    @Override
    public IntakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_intake, parent, false);
        return new IntakeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IntakeViewHolder holder, int position) {
        Intake intake = intakeList.get(position);
        holder.tvTime.setText(intake.getDatetime()); // Было getDateTimeKey(), стало getDatetime()
        holder.tvStatus.setText(intake.isStatus() ? "✅" : "❌");
    }

    @Override
    public int getItemCount() {
        return intakeList.size();
    }

    static class IntakeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvStatus;

        IntakeViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvIntakeTime);
            tvStatus = itemView.findViewById(R.id.tvIntakeStatus);
        }
    }
}
