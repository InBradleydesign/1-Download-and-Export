package com.cuidarapp.health.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cuidarapp.CuidarApplication;
import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.domain.ValidationException;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.ReminderEvent;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/** Recebe o disparo do alarme e as respostas dadas direto na notificação. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!ReminderScheduler.ACTION_FIRE.equals(action) && !ReminderNotifier.ACTION_RESOLVE.equals(action)) return;

        PendingResult pending = goAsync();
        CuidarApplication app = (CuidarApplication) context.getApplicationContext();
        app.getHealthExecutor().execute(() -> {
            try {
                if (ReminderScheduler.ACTION_FIRE.equals(action)) {
                    onFire(app, intent);
                } else {
                    onResolve(app, intent);
                }
            } finally {
                pending.finish();
            }
        });
    }

    private void onFire(CuidarApplication app, Intent intent) {
        long reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1);
        long scheduledAt = intent.getLongExtra(ReminderScheduler.EXTRA_SCHEDULED_AT, System.currentTimeMillis());
        HealthRepository repository = app.getHealthRepository();
        HealthReminder reminder = repository.getReminderById(reminderId);
        if (reminder == null || !reminder.enabled) return;

        ReminderEvent event = repository.recordReminderFired(reminder, scheduledAt);
        if (ReminderEvent.STATUS_PENDING.equals(event.status)) {
            app.getReminderNotifier().show(reminder, event);
        }
        app.getReminderScheduler().schedule(reminder);
        app.notifyHealthChanged();
    }

    private void onResolve(CuidarApplication app, Intent intent) {
        long eventId = intent.getLongExtra(ReminderNotifier.EXTRA_EVENT_ID, -1);
        String status = intent.getStringExtra(ReminderNotifier.EXTRA_STATUS);
        HealthRepository repository = app.getHealthRepository();
        ReminderEvent event = repository.getEvent(eventId);
        if (event == null) return;

        Author author = new Author(Author.ROLE_NOTIFICATION, null);
        try {
            if (HealthReminder.KIND_MEDICATION.equals(event.kind) && ReminderEvent.STATUS_DONE.equals(status)
                && event.refId != null) {
                // Só registra a dose porque a pessoa tocou em "Tomei"; nunca de forma automática.
                MedicationIntake intake = new MedicationIntake();
                intake.medRef = event.refId;
                intake.medName = event.title;
                intake.dosage = event.detail;
                intake.scheduledDate = repository.dateKey(event.scheduledAt);
                intake.scheduledTime = Instant.ofEpochMilli(event.scheduledAt).atZone(repository.zone())
                    .toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                intake.status = MedicationIntake.STATUS_TAKEN;
                repository.recordIntake(event.patientId, intake, author);
            }
            repository.resolveEvent(event.patientId, eventId, status, author);
        } catch (ValidationException e) {
            // Sem consentimento ou dados inválidos: a ocorrência continua pendente no app.
        }
        app.getReminderNotifier().cancel(eventId);
        app.notifyHealthChanged();
    }
}
