package com.cuidarapp.health.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.cuidarapp.CuidarApplication;
import com.cuidarapp.data.AppDatabase;
import com.cuidarapp.health.HealthRepository;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.ReminderEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Agendamento, notificação e respostas pela notificação, com a aplicação real. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ReminderFlowTest {

    private static final long P = HealthRepository.LOCAL_PATIENT_ID;

    private CuidarApplication app;
    private HealthRepository repo;
    private AlarmManager alarmManager;
    private NotificationManager notificationManager;

    @Before
    public void setUp() throws Exception {
        app = ApplicationProvider.getApplicationContext();
        repo = app.getHealthRepository();
        alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        notificationManager = app.getSystemService(NotificationManager.class);
        flushHealthExecutor();
        repo.giveConsent(P, true, new Author(Author.ROLE_PATIENT, "José"));
    }

    @After
    public void tearDown() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false);
        app.getDatabase().close();
        AppDatabase.destroyInstance();
    }

    private void flushHealthExecutor() throws Exception {
        app.getHealthExecutor().submit(() -> { }).get(5, TimeUnit.SECONDS);
        shadowOf(Looper.getMainLooper()).idle();
    }

    private HealthReminder medicationReminder(boolean enabled) throws Exception {
        HealthReminder r = new HealthReminder();
        r.kind = HealthReminder.KIND_MEDICATION;
        r.refId = "7";
        r.title = "Losartana";
        r.detail = "50 mg";
        r.times = "08:00,20:00";
        r.daysMask = 0x7F;
        r.enabled = enabled;
        return repo.saveReminder(P, r, new Author(Author.ROLE_CAREGIVER, "Ana"));
    }

    @Test
    public void schedule_setsExactAlarmWhenAllowed() throws Exception {
        ShadowAlarmManager.setCanScheduleExactAlarms(true);
        HealthReminder reminder = medicationReminder(true);
        Long next = app.getReminderScheduler().schedule(reminder);

        assertNotNull(next);
        assertTrue(next > System.currentTimeMillis());
        ShadowAlarmManager.ScheduledAlarm alarm = shadowOf(alarmManager).getNextScheduledAlarm();
        assertNotNull(alarm);
        assertEquals((long) next, alarm.triggerAtTime);
        assertEquals(AlarmManager.RTC_WAKEUP, alarm.type);
    }

    @Test
    public void schedule_fallsBackToInexactWithoutPermission() throws Exception {
        ShadowAlarmManager.setCanScheduleExactAlarms(false);
        assertFalse(app.getReminderScheduler().canScheduleExact());
        HealthReminder reminder = medicationReminder(true);
        assertNotNull(app.getReminderScheduler().schedule(reminder));
        assertNotNull(shadowOf(alarmManager).getNextScheduledAlarm());
    }

    @Test
    public void disabledOrCancelled_hasNoAlarm() throws Exception {
        HealthReminder reminder = medicationReminder(true);
        app.getReminderScheduler().schedule(reminder);
        app.getReminderScheduler().cancel(reminder.id);
        assertNull(shadowOf(alarmManager).getNextScheduledAlarm());

        reminder.enabled = false;
        assertNull(app.getReminderScheduler().schedule(reminder));
        assertNull(shadowOf(alarmManager).getNextScheduledAlarm());
    }

    @Test
    public void rescheduleAfterBoot() throws Exception {
        medicationReminder(true);
        assertNull(shadowOf(alarmManager).getNextScheduledAlarm());
        app.sendBroadcast(new Intent(Intent.ACTION_BOOT_COMPLETED).setPackage(app.getPackageName()));
        shadowOf(Looper.getMainLooper()).idle();
        flushHealthExecutor();
        assertNotNull(shadowOf(alarmManager).getNextScheduledAlarm());
    }

    @Test
    public void channel_isPrivateOnLockScreen() {
        NotificationChannel channel = notificationManager.getNotificationChannel(ReminderNotifier.CHANNEL_ID);
        assertNotNull(channel);
        assertEquals(Notification.VISIBILITY_PRIVATE, channel.getLockscreenVisibility());
    }

    @Test
    public void withoutPermission_noNotification_butEventStaysPending() throws Exception {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS);
        HealthReminder reminder = medicationReminder(true);
        fire(reminder, System.currentTimeMillis() - 1000);

        assertTrue(shadowOf(notificationManager).getAllNotifications().isEmpty());
        List<ReminderEvent> events = repo.listEvents(P, 0, Long.MAX_VALUE);
        assertEquals(1, events.size());
        assertEquals(ReminderEvent.STATUS_PENDING, events.get(0).status);
    }

    private void fire(HealthReminder reminder, long scheduledAt) throws Exception {
        app.sendBroadcast(new Intent(app, ReminderReceiver.class)
            .setAction(ReminderScheduler.ACTION_FIRE)
            .putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
            .putExtra(ReminderScheduler.EXTRA_SCHEDULED_AT, scheduledAt));
        shadowOf(Looper.getMainLooper()).idle();
        flushHealthExecutor();
    }

    private void resolve(long eventId, String status) throws Exception {
        app.sendBroadcast(new Intent(app, ReminderReceiver.class)
            .setAction(ReminderNotifier.ACTION_RESOLVE)
            .putExtra(ReminderNotifier.EXTRA_EVENT_ID, eventId)
            .putExtra(ReminderNotifier.EXTRA_STATUS, status));
        shadowOf(Looper.getMainLooper()).idle();
        flushHealthExecutor();
    }

    @Test
    public void fire_showsPrivateNotification_andSchedulesNext() throws Exception {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        HealthReminder reminder = medicationReminder(true);
        long scheduledAt = System.currentTimeMillis() - 1000;
        fire(reminder, scheduledAt);

        List<ReminderEvent> events = repo.listEvents(P, 0, Long.MAX_VALUE);
        assertEquals(1, events.size());
        assertEquals(ReminderEvent.STATUS_PENDING, events.get(0).status);

        List<Notification> shown = shadowOf(notificationManager).getAllNotifications();
        assertEquals(1, shown.size());
        Notification n = shown.get(0);
        assertEquals("Hora do remédio: Losartana", n.extras.getString(Notification.EXTRA_TITLE));
        assertEquals(Notification.VISIBILITY_PRIVATE, n.visibility);
        assertEquals("Lembrete do CuidarApp", n.publicVersion.extras.getString(Notification.EXTRA_TITLE));
        assertEquals(2, n.actions.length);
        assertEquals("Tomei", n.actions[0].title.toString());
        assertEquals("Não vou tomar agora", n.actions[1].title.toString());

        // Nenhuma dose é registrada só porque o lembrete tocou.
        assertTrue(repo.listIntakes(P, "2000-01-01", "2100-01-01").isEmpty());
        assertNotNull(shadowOf(alarmManager).getNextScheduledAlarm());
    }

    @Test
    public void tomei_recordsIntakeByNotification() throws Exception {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        HealthReminder reminder = medicationReminder(true);
        long scheduledAt = System.currentTimeMillis() - 1000;
        fire(reminder, scheduledAt);
        ReminderEvent event = repo.listEvents(P, 0, Long.MAX_VALUE).get(0);

        resolve(event.id, ReminderEvent.STATUS_DONE);

        assertEquals(ReminderEvent.STATUS_DONE, repo.getEvent(event.id).status);
        List<MedicationIntake> intakes = repo.listIntakes(P, repo.dateKey(scheduledAt), repo.dateKey(scheduledAt));
        assertEquals(1, intakes.size());
        assertEquals(MedicationIntake.STATUS_TAKEN, intakes.get(0).status);
        assertEquals(Author.ROLE_NOTIFICATION, intakes.get(0).createdByRole);
        assertTrue(shadowOf(notificationManager).getAllNotifications().isEmpty());
    }

    @Test
    public void ignore_doesNotRecordIntake() throws Exception {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        HealthReminder reminder = medicationReminder(true);
        fire(reminder, System.currentTimeMillis() - 1000);
        ReminderEvent event = repo.listEvents(P, 0, Long.MAX_VALUE).get(0);

        resolve(event.id, ReminderEvent.STATUS_IGNORED);

        assertEquals(ReminderEvent.STATUS_IGNORED, repo.getEvent(event.id).status);
        assertTrue(repo.listIntakes(P, "2000-01-01", "2100-01-01").isEmpty());
    }

    @Test
    public void firingTwice_doesNotDuplicateEvent() throws Exception {
        HealthReminder reminder = medicationReminder(true);
        long scheduledAt = System.currentTimeMillis() - 1000;
        fire(reminder, scheduledAt);
        fire(reminder, scheduledAt);
        assertEquals(1, repo.listEvents(P, 0, Long.MAX_VALUE).size());
    }
}
