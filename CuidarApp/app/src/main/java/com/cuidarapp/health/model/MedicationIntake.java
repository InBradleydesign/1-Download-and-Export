package com.cuidarapp.health.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Confirmação de uma dose prevista. O remédio em si continua cadastrado no armazenamento
 * da interface (med_ref = id do remédio); nome e dose são copiados para preservar o histórico
 * mesmo que o cadastro seja alterado ou excluído depois.
 */
@Entity(
    tableName = "medication_intakes",
    indices = {
        @Index(value = {"patient_id", "med_ref", "scheduled_date"}, unique = true),
        @Index(value = {"patient_id", "scheduled_date"})
    }
)
public class MedicationIntake {

    public static final String STATUS_TAKEN = "taken";
    public static final String STATUS_MISSED = "missed";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;

    @NonNull
    @ColumnInfo(name = "med_ref")
    public String medRef = "";

    @ColumnInfo(name = "med_name")
    public String medName;

    @ColumnInfo(name = "dosage")
    public String dosage;

    /** Data local no formato AAAA-MM-DD. */
    @NonNull
    @ColumnInfo(name = "scheduled_date")
    public String scheduledDate = "";

    /** Horário previsto HH:mm, quando o remédio tem horário. */
    @ColumnInfo(name = "scheduled_time")
    public String scheduledTime;

    @NonNull
    @ColumnInfo(name = "status")
    public String status = STATUS_TAKEN;

    @ColumnInfo(name = "taken_at")
    public Long takenAt;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "created_by_role")
    public String createdByRole;

    @ColumnInfo(name = "created_by_name")
    public String createdByName;

    @ColumnInfo(name = "updated_at")
    public Long updatedAt;

    @ColumnInfo(name = "updated_by_role")
    public String updatedByRole;

    @ColumnInfo(name = "updated_by_name")
    public String updatedByName;
}
