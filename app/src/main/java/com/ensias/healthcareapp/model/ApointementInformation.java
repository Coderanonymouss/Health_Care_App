package com.ensias.healthcareapp.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ApointementInformation {
    private String patientName;
    private String patientPhone; // <--- добавлено!
    private String time;
    private String doctorId;
    private String doctorName;
    private String patientId;
    private String type;
    private String apointementType;
    private String chemin;
    private long slot;
    @ServerTimestamp
    private Date requestedAt;

    public ApointementInformation() {}

    public ApointementInformation(String patientName, String patientPhone, String time, String doctorId, String doctorName, long slot) {
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.time = time;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.slot = slot;
    }

    // --- геттеры/сеттеры ---
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getApointementType() { return apointementType; }
    public void setApointementType(String apointementType) { this.apointementType = apointementType; }

    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }

    public long getSlot() { return slot; }
    public void setSlot(long slot) { this.slot = slot; }

    public Date getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Date requestedAt) { this.requestedAt = requestedAt; }
}
