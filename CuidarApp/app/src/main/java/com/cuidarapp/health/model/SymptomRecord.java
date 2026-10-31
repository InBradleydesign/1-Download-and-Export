package com.cuidarapp.health.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "health_symptoms",
    indices = {@Index(value = {"patient_id", "occurred_at"})}
)
public class SymptomRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;

    /** Código do catálogo {@link com.cuidarapp.health.domain.SymptomCatalog}. */
    @NonNull
    @ColumnInfo(name = "symptom")
    public String symptom = "";

    /** Descrição livre quando o código é "other". */
    @ColumnInfo(name = "custom_label")
    public String customLabel;

    /** 1 = leve, 2 = moderada, 3 = forte. */
    @ColumnInfo(name = "intensity")
    public int intensity;

    @ColumnInfo(name = "duration_minutes")
    public Integer durationMinutes;

    @ColumnInfo(name = "occurred_at")
    public long occurredAt;

    @ColumnInfo(name = "note")
    public String note;

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
