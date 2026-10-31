package com.cuidarapp.health.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.domain.ReminderMath;
import com.cuidarapp.health.model.HealthReminder;

import java.time.ZoneId;

/**
 * Agenda um alarme por lembrete (sempre só a próxima ocorrência). Quando o alarme dispara,
 * o receptor registra a ocorrência e agenda a seguinte.
 * Com permissão de alarme exato (Android 12+) o aviso sai no minuto certo; sem ela o Android
 * pode atrasar alguns minutos para economizar bateria.
 */
public class ReminderScheduler {

    static final String ACTION_FIRE = "com.cuidarapp.health.REMINDER_FIRE";
    static final String EXTRA_REMINDER_ID = "reminder_id";
    static final String EXTRA_SCHEDULED_AT = "scheduled_at";

    private final Context context;
    private final AlarmManager alarmManager;

    public ReminderScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
    }

    public boolean canScheduleExact() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms();
    }

    /** Agenda a próxima ocorrência; retorna o horário agendado ou null se não houver. */
    public Long schedule(HealthReminder reminder) {
        cancel(reminder.id);
        if (!reminder.enabled) return null;
        Long next = ReminderMath.nextTrigger(reminder.times, reminder.daysMask, reminder.onceAt,
            System.currentTimeMillis(), ZoneId.systemDefault());
        if (next == null) return null;

        PendingIntent pi = firePendingIntent(reminder.id, next, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi);
            }
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi);
        }
        return next;
    }

    public void cancel(long reminderId) {
        PendingIntent pi = firePendingIntent(reminderId, 0, PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public void rescheduleAll(HealthRepository repository) {
        for (HealthReminder reminder : repository.listEnabledReminders()) {
            schedule(reminder);
        }
    }

    private PendingIntent firePendingIntent(long reminderId, long scheduledAt, int flags) {
        Intent intent = new Intent(context, ReminderReceiver.class)
            .setAction(ACTION_FIRE)
            .putExtra(EXTRA_REMINDER_ID, reminderId)
            .putExtra(EXTRA_SCHEDULED_AT, scheduledAt);
        return PendingIntent.getBroadcast(context, requestCode(reminderId), intent, flags | PendingIntent.FLAG_IMMUTABLE);
    }

    static int requestCode(long reminderId) {
        return (int) (reminderId % Integer.MAX_VALUE);
    }
}
