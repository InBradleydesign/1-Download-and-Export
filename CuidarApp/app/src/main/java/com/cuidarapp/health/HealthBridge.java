package com.cuidarapp.health;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.webkit.JavascriptInterface;

import com.cuidarapp.CuidarApplication;
import com.cuidarapp.MainActivity;
import com.cuidarapp.health.domain.ReminderMath;
import com.cuidarapp.health.domain.ValidationException;
import com.cuidarapp.health.model.Author;
import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthReminder;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.ReminderEvent;
import com.cuidarapp.health.model.SymptomRecord;
import com.cuidarapp.health.report.ReportService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ponte JavaScript do Diário de Saúde, exposta como window.CuidarHealth.
 * Contrato: call(método, json) -> {"ok":true,"data":...} ou {"ok":false,"field":...,"error":...}.
 * Nenhum dado de saúde é escrito em log.
 */
public class HealthBridge {

    private static final String GENERIC_ERROR = "Não foi possível concluir. Tente novamente.";

    private final MainActivity activity;
    private final CuidarApplication app;
    private final HealthRepository repository;
    private final Gson gson = new Gson();
    private final long patientId = HealthRepository.LOCAL_PATIENT_ID;
    private File lastReport;

    public HealthBridge(MainActivity activity) {
        this.activity = activity;
        this.app = (CuidarApplication) activity.getApplication();
        this.repository = app.getHealthRepository();
    }

    @JavascriptInterface
    public String call(String method, String argsJson) {
        try {
            JsonObject args = argsJson == null || argsJson.isEmpty()
                ? new JsonObject() : JsonParser.parseString(argsJson).getAsJsonObject();
            Object data = dispatch(method, args);
            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("data", data);
            return gson.toJson(response);
        } catch (ValidationException e) {
            return error(e.getField(), e.getMessage());
        } catch (Exception e) {
            return error(null, GENERIC_ERROR);
        }
    }

    private Object dispatch(String method, JsonObject a) throws Exception {
        switch (method) {
            case "getStatus": return status();
            case "getProfile": return repository.getProfile(patientId);
            case "giveConsent":
                return repository.giveConsent(patientId, bool(a, "caregiverMayRecord"), author(a));
            case "saveProfile":
                return repository.saveProfile(patientId, gson.fromJson(a.get("profile"), ProfileData.class), author(a));

            case "saveMeasurement":
                return repository.saveMeasurement(patientId, gson.fromJson(a.get("record"), HealthMeasurement.class), author(a));
            case "deleteMeasurement":
                repository.deleteMeasurement(patientId, a.get("id").getAsLong(), author(a));
                return true;
            case "listMeasurements":
                return repository.listMeasurements(patientId, str(a, "type"), a.get("from").getAsLong(), a.get("to").getAsLong());

            case "saveSymptom":
                return repository.saveSymptom(patientId, gson.fromJson(a.get("record"), SymptomRecord.class), author(a));
            case "deleteSymptom":
                repository.deleteSymptom(patientId, a.get("id").getAsLong(), author(a));
                return true;
            case "listSymptoms":
                return repository.listSymptoms(patientId, a.get("from").getAsLong(), a.get("to").getAsLong());

            case "recordIntake":
                return repository.recordIntake(patientId, gson.fromJson(a.get("record"), MedicationIntake.class), author(a));
            case "deleteIntake":
                repository.deleteIntake(patientId, str(a, "medRef"), str(a, "date"), author(a));
                return true;
            case "listIntakes":
                return repository.listIntakes(patientId, str(a, "fromDate"), str(a, "toDate"));

            case "listReminders": return reminders();
            case "saveReminder": {
                HealthReminder saved = repository.saveReminder(patientId,
                    gson.fromJson(a.get("record"), HealthReminder.class), author(a));
                app.getReminderScheduler().schedule(saved);
                return withNext(saved);
            }
            case "deleteReminder": {
                long id = a.get("id").getAsLong();
                app.getReminderScheduler().cancel(id);
                return repository.deleteReminder(patientId, id);
            }
            case "deleteLinkedReminder": {
                HealthReminder linked = repository.getLinkedReminder(patientId, str(a, "kind"), str(a, "refId"));
                if (linked == null) return false;
                app.getReminderScheduler().cancel(linked.id);
                return repository.deleteReminder(patientId, linked.id);
            }
            case "listReminderEvents":
                return repository.listEvents(patientId, a.get("from").getAsLong(), a.get("to").getAsLong());
            case "resolveReminderEvent": {
                ReminderEvent event = repository.resolveEvent(patientId, a.get("id").getAsLong(), str(a, "status"), author(a));
                app.getReminderNotifier().cancel(event.id);
                return event;
            }

            case "generateReport": {
                ReportService.Result result = new ReportService(activity, repository).generate(patientId,
                    str(a, "patientName"), a.get("from").getAsLong(), a.get("to").getAsLong());
                lastReport = result.file;
                Map<String, Object> out = new HashMap<>();
                out.put("fileName", result.file.getName());
                out.put("pageCount", result.pageCount);
                out.put("totalRecords", result.totalRecords);
                out.put("pages", result.previewPages);
                return out;
            }
            case "shareReport":
                if (lastReport == null || !lastReport.exists()) {
                    throw new ValidationException("report", "Gere o relatório antes de compartilhar.");
                }
                activity.shareFile(lastReport, "application/pdf", "Relatório do Diário de Saúde");
                return true;

            case "requestNotifications":
                activity.requestNotificationPermission();
                return true;
            case "openExactAlarmSettings":
                activity.openExactAlarmSettings();
                return true;
            case "openBatterySettings":
                activity.openBatterySettings();
                return true;
            case "openNotificationSettings":
                activity.openNotificationSettings();
                return true;

            case "wipe": {
                for (Long id : repository.wipe(patientId)) app.getReminderScheduler().cancel(id);
                ReportService.clear(ReportService.reportsDir(activity));
                lastReport = null;
                return true;
            }
            default:
                throw new ValidationException("method", GENERIC_ERROR);
        }
    }

    private Map<String, Object> status() {
        Map<String, Object> s = new HashMap<>();
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        s.put("available", true);
        s.put("sdk", Build.VERSION.SDK_INT);
        s.put("notificationsAllowed", app.getReminderNotifier().canNotify());
        s.put("notificationsNeedRuntimePermission", Build.VERSION.SDK_INT >= 33);
        s.put("exactAlarms", app.getReminderScheduler().canScheduleExact());
        s.put("exactAlarmsConfigurable", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);
        s.put("batteryUnrestricted", pm != null && pm.isIgnoringBatteryOptimizations(activity.getPackageName()));
        return s;
    }

    private List<Map<String, Object>> reminders() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (HealthReminder r : repository.listReminders(patientId)) list.add(withNext(r));
        return list;
    }

    private Map<String, Object> withNext(HealthReminder r) {
        Map<String, Object> map = new HashMap<>();
        map.put("reminder", r);
        map.put("nextAt", r.enabled
            ? ReminderMath.nextTrigger(r.times, r.daysMask, r.onceAt, System.currentTimeMillis(), ZoneId.systemDefault())
            : null);
        return map;
    }

    private Author author(JsonObject a) throws ValidationException {
        JsonElement el = a.get("author");
        if (el == null || !el.isJsonObject()) {
            throw new ValidationException("author", "Não foi possível identificar quem está registrando.");
        }
        return gson.fromJson(el, Author.class);
    }

    private static String str(JsonObject a, String key) {
        JsonElement el = a.get(key);
        return el == null || el.isJsonNull() ? null : el.getAsString();
    }

    private static boolean bool(JsonObject a, String key) {
        JsonElement el = a.get(key);
        return el != null && !el.isJsonNull() && el.getAsBoolean();
    }

    private String error(String field, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("ok", false);
        response.put("field", field);
        response.put("error", message);
        return gson.toJson(response);
    }
}
