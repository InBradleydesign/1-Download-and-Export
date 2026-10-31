package com.cuidarapp.health.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.ReminderEvent;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insertReminder(HealthReminder reminder);

    @Update
    void updateReminder(HealthReminder reminder);

    @Query("SELECT * FROM health_reminders WHERE id = :id AND patient_id = :patientId")
    HealthReminder getReminder(long patientId, long id);

    /** Usado pelo receptor do alarme, que só conhece o id do lembrete. */
    @Query("SELECT * FROM health_reminders WHERE id = :id")
    HealthReminder getReminderById(long id);

    @Query("SELECT * FROM health_reminders WHERE patient_id = :patientId AND kind = :kind AND ref_id = :refId")
    HealthReminder getLinkedReminder(long patientId, String kind, String refId);

    @Query("SELECT * FROM health_reminders WHERE patient_id = :patientId ORDER BY kind, title")
    List<HealthReminder> listReminders(long patientId);

    @Query("SELECT * FROM health_reminders WHERE enabled = 1")
    List<HealthReminder> listEnabledReminders();

    @Query("DELETE FROM health_reminders WHERE id = :id AND patient_id = :patientId")
    int deleteReminder(long patientId, long id);

    @Query("SELECT id FROM health_reminders WHERE patient_id = :patientId")
    List<Long> listReminderIds(long patientId);

    @Query("DELETE FROM health_reminders WHERE patient_id = :patientId")
    void deleteAllReminders(long patientId);

    // ---------- Ocorrências ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertEvent(ReminderEvent event);

    @Update
    void updateEvent(ReminderEvent event);

    @Query("SELECT * FROM reminder_events WHERE id = :id")
    ReminderEvent getEventById(long id);

    @Query("SELECT * FROM reminder_events WHERE reminder_id = :reminderId AND scheduled_at = :scheduledAt")
    ReminderEvent getEvent(long reminderId, long scheduledAt);

    @Query("SELECT * FROM reminder_events WHERE patient_id = :patientId "
        + "AND scheduled_at BETWEEN :from AND :to ORDER BY scheduled_at DESC")
    List<ReminderEvent> listEvents(long patientId, long from, long to);

    @Query("SELECT * FROM reminder_events WHERE patient_id = :patientId AND kind = :kind "
        + "AND ref_id = :refId AND status = 'pending' AND scheduled_at BETWEEN :from AND :to")
    List<ReminderEvent> listPendingFor(long patientId, String kind, String refId, long from, long to);

    @Query("DELETE FROM reminder_events WHERE patient_id = :patientId")
    void deleteAllEvents(long patientId);
}
