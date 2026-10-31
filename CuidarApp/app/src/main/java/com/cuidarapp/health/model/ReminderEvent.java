package com.cuidarapp.health.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Ocorrência de um lembrete que já disparou: pendente, feita ou ignorada. */
@Entity(
    tableName = "reminder_events",
    indices = {
        @Index(value = {"reminder_id", "scheduled_at"}, unique = true),
        @Index(value = {"patient_id", "scheduled_at"})
    }
)
public class ReminderEvent {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_IGNORED = "ignored";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;

    @ColumnInfo(name = "reminder_id")
    public long reminderId;

    @NonNull
    @ColumnInfo(name = "kind")
    public String kind = HealthReminder.KIND_MEASUREMENT;

    @ColumnInfo(name = "ref_id")
    public String refId;

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "detail")
    public String detail;

    @ColumnInfo(name = "scheduled_at")
    public long scheduledAt;

    @NonNull
    @ColumnInfo(name = "status")
    public String status = STATUS_PENDING;

    @ColumnInfo(name = "resolved_at")
    public Long resolvedAt;

    @ColumnInfo(name = "resolved_by")
    public String resolvedBy;
}
