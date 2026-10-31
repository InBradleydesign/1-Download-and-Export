package com.cuidarapp.health.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cuidarapp.CuidarApplication;

/**
 * Alarmes são apagados pelo Android ao reiniciar o aparelho e precisam ser recalculados quando a
 * hora ou o fuso mudam, quando o app é atualizado ou quando a permissão de alarme exato muda.
 */
public class RescheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CuidarApplication app = (CuidarApplication) context.getApplicationContext();
        PendingResult pending = goAsync();
        app.getHealthExecutor().execute(() -> {
            try {
                app.getReminderScheduler().rescheduleAll(app.getHealthRepository());
            } finally {
                pending.finish();
            }
        });
    }
}
