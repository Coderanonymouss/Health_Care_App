package com.ensias.healthcareapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.activity.doctor.DoctorDetailActivity;
import com.ensias.healthcareapp.model.Doctor;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapterFiltred
        extends RecyclerView.Adapter<DoctorAdapterFiltred.ViewHolder>
        implements Filterable {

    /** Колбэк для кнопки «Add Doctor» */
    public interface OnAddDoctorClickListener {
        void onAddDoctor(Doctor doctor);
    }

    public static boolean specialiteSearch = false;

    private final List<Doctor> fullList;
    private final List<Doctor> filteredList;
    private OnAddDoctorClickListener addDoctorListener;

    public DoctorAdapterFiltred(List<Doctor> list) {
        this.fullList = new ArrayList<>(list);
        this.filteredList = new ArrayList<>(list);
    }

    /** Позволяет Activity подписаться на клик «Add Doctor» */
    public void setOnAddDoctorClickListener(OnAddDoctorClickListener listener) {
        this.addDoctorListener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doctor_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Doctor doc = filteredList.get(pos);

        h.title.setText(doc.getFullName());
        h.specialite.setText("Specialité: " + doc.getSpecialite());

        // Подгружаем фото по email
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("DoctorProfile/" + doc.getEmail() + ".jpg");
        ref.getDownloadUrl().addOnSuccessListener(uri ->
                Picasso.get()
                        .load(uri)
                        .placeholder(R.mipmap.ic_launcher)
                        .fit()
                        .centerCrop()
                        .into(h.image)
        );

        // BOOK → открываем экран с деталями врача
        h.appointemenBtn.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent it = new Intent(ctx, DoctorDetailActivity.class);
            it.putExtra("doctorEmail",      doc.getEmail());
            it.putExtra("doctorName",       doc.getFullName());
            it.putExtra("doctorSpeciality", doc.getSpecialite());
            it.putExtra("doctorPhone",      doc.getTel());
            ctx.startActivity(it);
        });

        // Add Doctor → вызываем колбэк в Activity
        h.addDoc.setVisibility(View.VISIBLE);
        h.addDoc.setOnClickListener(v -> {
            if (addDoctorListener != null) {
                addDoctorListener.onAddDoctor(doc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence constraint) {
                String pat = (constraint == null)
                        ? ""
                        : constraint.toString().trim().toLowerCase();
                List<Doctor> tmp = new ArrayList<>();
                if (pat.isEmpty()) {
                    tmp.addAll(fullList);
                } else {
                    for (Doctor d : fullList) {
                        if (!specialiteSearch) {
                            if (d.getFullName().toLowerCase().contains(pat)) {
                                tmp.add(d);
                            }
                        } else {
                            if (d.getSpecialite().toLowerCase().contains(pat)) {
                                tmp.add(d);
                            }
                        }
                    }
                }
                FilterResults res = new FilterResults();
                res.values = tmp;
                return res;
            }
            @Override @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List<Doctor>)results.values);
                notifyDataSetChanged();
            }
        };
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  title, specialite;
        ImageView image;
        Button    appointemenBtn, addDoc;
        ViewHolder(@NonNull View iv) {
            super(iv);
            title          = iv.findViewById(R.id.doctor_view_title);
            specialite     = iv.findViewById(R.id.text_view_description);
            image          = iv.findViewById(R.id.doctor_item_image);
            appointemenBtn = iv.findViewById(R.id.appointemenBtn);
            addDoc         = iv.findViewById(R.id.addDocBtn);
        }
    }
}
