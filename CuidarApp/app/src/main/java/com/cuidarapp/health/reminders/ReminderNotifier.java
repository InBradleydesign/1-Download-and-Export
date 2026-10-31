package com.cuidarapp.health.reminders;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.cuidarapp.MainActivity;
import com.cuidarapp.R;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.ReminderEvent;

/** Notificações dos lembretes. Na tela bloqueada não aparece o nome do remédio nem da medição. */
public class ReminderNotifier {

    public static final String CHANNEL_ID = "lembretes_saude";
    static final String ACTION_RESOLVE = "com.cuidarapp.health.REMINDER_RESOLVE";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STATUS = "status";
    public static final String EXTRA_OPEN_HEALTH = "open_health";

    private final Context context;

    public ReminderNotifier(Context context) {
        this.context = context.getApplicationContext();
    }

    public void ensureChannel() {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Lembretes de saúde",
            NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Avisos de remédios, medições e consultas cadastrados no CuidarApp");
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        manager.createNotificationChannel(channel);
    }

    public boolean canNotify() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
            Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public void show(HealthReminder reminder, ReminderEvent event) {
        ensureChannel();
        if (!canNotify()) return;

        String title;
        if (HealthReminder.KIND_MEDICATION.equals(reminder.kind)) title = "Hora do remédio: " + reminder.title;
        else if (HealthReminder.KIND_APPOINTMENT.equals(reminder.kind)) title = "Lembrete de consulta: " + reminder.title;
        else title = "Hora de medir: " + reminder.title;
        String text = reminder.detail != null ? reminder.detail : "Toque para abrir o CuidarApp";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_reminder)
                .setContentTitle("Lembrete do CuidarApp")
                .setContentText("Desbloqueie o celular para ver")
                .build())
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(event.id));

        if (HealthReminder.KIND_MEDICATION.equals(reminder.kind)) {
            builder.addAction(0, "Tomei", resolveIntent(event.id, ReminderEvent.STATUS_DONE));
            builder.addAction(0, "Não vou tomar agora", resolveIntent(event.id, ReminderEvent.STATUS_IGNORED));
        } else if (HealthReminder.KIND_APPOINTMENT.equals(reminder.kind)) {
            builder.addAction(0, "Ciente", resolveIntent(event.id, ReminderEvent.STATUS_DONE));
        } else {
            builder.addAction(0, "Ignorar", resolveIntent(event.id, ReminderEvent.STATUS_IGNORED));
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId(event.id), builder.build());
        } catch (SecurityException ignored) {
            // Permissão revogada entre a verificação e o envio: o lembrete continua pendente no app.
        }
    }

    public void cancel(long eventId) {
        NotificationManagerCompat.from(context).cancel(notificationId(eventId));
    }

    private PendingIntent openAppIntent(long eventId) {
        Intent intent = new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_OPEN_HEALTH, true);
        return PendingIntent.getActivity(context, notificationId(eventId), intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent resolveIntent(long eventId, String status) {
        Intent intent = new Intent(context, ReminderReceiver.class)
            .setAction(ACTION_RESOLVE)
            .putExtra(EXTRA_EVENT_ID, eventId)
            .putExtra(EXTRA_STATUS, status);
        int requestCode = notificationId(eventId) * 2 + (ReminderEvent.STATUS_DONE.equals(status) ? 1 : 0);
        return PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static int notificationId(long eventId) {
        return 10_000 + (int) (eventId % 1_000_000);
    }
}
