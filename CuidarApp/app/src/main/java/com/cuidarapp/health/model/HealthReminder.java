package com.cuidarapp.health.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Lembrete recorrente (medição ou remédio) ou único (consulta).
 * Não existem horários padrão: os horários são sempre informados por quem cadastra,
 * conforme orientação do profissional de saúde.
 */
@Entity(
    tableName = "health_reminders",
    indices = {@Index(value = {"patient_id", "kind", "ref_id"}, unique = true)}
)
public class HealthReminder {

    public static final String KIND_MEASUREMENT = "measurement";
    public static final String KIND_MEDICATION = "medication";
    public static final String KIND_APPOINTMENT = "appointment";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;

    @NonNull
    @ColumnInfo(name = "kind")
    public String kind = KIND_MEASUREMENT;

    /** Id do remédio/visita vinculado ou tipo de medição (null = lembrete avulso). */
    @ColumnInfo(name = "ref_id")
    public String refId;

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "detail")
    public String detail;

    /** Horários HH:mm separados por vírgula. */
    @ColumnInfo(name = "times")
    public String times;

    /** Bit 0 = domingo ... bit 6 = sábado (mesma numeração de Date.getDay() no JS). */
    @ColumnInfo(name = "days_mask")
    public int daysMask;

    /** Para lembretes únicos (consultas): instante exato do aviso. */
    @ColumnInfo(name = "once_at")
    public Long onceAt;

    @ColumnInfo(name = "enabled")
    public boolean enabled = true;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "created_by_name")
    public String createdByName;

    @ColumnInfo(name = "updated_at")
    public Long updatedAt;
}
