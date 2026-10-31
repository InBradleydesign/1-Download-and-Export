package com.cuidarapp.health.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Medição domiciliar (pressão, glicemia, peso, temperatura, SpO2, frequência cardíaca, urina).
 * Para pressão: value1 = sistólica, value2 = diastólica, value3 = pulso (opcional).
 * Para os demais tipos apenas value1 é usado.
 */
@Entity(
    tableName = "health_measurements",
    indices = {@Index(value = {"patient_id", "type", "measured_at"})}
)
public class HealthMeasurement {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;

    @NonNull
    @ColumnInfo(name = "type")
    public String type = "";

    @ColumnInfo(name = "value1")
    public double value1;

    @ColumnInfo(name = "value2")
    public Double value2;

    @ColumnInfo(name = "value3")
    public Double value3;

    @ColumnInfo(name = "context")
    public String context;

    @ColumnInfo(name = "measured_at")
    public long measuredAt;

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
