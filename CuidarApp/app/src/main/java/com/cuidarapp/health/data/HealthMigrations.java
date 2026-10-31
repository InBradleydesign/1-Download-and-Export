package com.cuidarapp.health.data;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Versão 1 -> 2: cria as tabelas do Diário de Saúde sem tocar nas tabelas existentes.
 * O SQL é o mesmo gerado pelo Room em schemas/com.cuidarapp.data.AppDatabase/2.json.
 */
public final class HealthMigrations {

    private HealthMigrations() {
    }

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            for (String sql : CREATE_V2) {
                db.execSQL(sql);
            }
        }
    };

    static final String[] CREATE_V2 = {
        "CREATE TABLE IF NOT EXISTS `health_profiles` (`patient_id` INTEGER NOT NULL, `conditions_json` TEXT, "
            + "`conditions_other` TEXT, `indicators_json` TEXT, `doctor_notes` TEXT, `caregiver_may_record` INTEGER NOT NULL, "
            + "`alert_rules_json` TEXT, `consent_at` INTEGER, `consent_by` TEXT, `updated_at` INTEGER, `updated_by` TEXT, "
            + "PRIMARY KEY(`patient_id`))",

        "CREATE TABLE IF NOT EXISTS `health_measurements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + "`patient_id` INTEGER NOT NULL, `type` TEXT NOT NULL, `value1` REAL NOT NULL, `value2` REAL, `value3` REAL, "
            + "`context` TEXT, `measured_at` INTEGER NOT NULL, `note` TEXT, `created_at` INTEGER NOT NULL, "
            + "`created_by_role` TEXT, `created_by_name` TEXT, `updated_at` INTEGER, `updated_by_role` TEXT, "
            + "`updated_by_name` TEXT)",
        "CREATE INDEX IF NOT EXISTS `index_health_measurements_patient_id_type_measured_at` "
            + "ON `health_measurements` (`patient_id`, `type`, `measured_at`)",

        "CREATE TABLE IF NOT EXISTS `health_symptoms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + "`patient_id` INTEGER NOT NULL, `symptom` TEXT NOT NULL, `custom_label` TEXT, `intensity` INTEGER NOT NULL, "
            + "`duration_minutes` INTEGER, `occurred_at` INTEGER NOT NULL, `note` TEXT, `created_at` INTEGER NOT NULL, "
            + "`created_by_role` TEXT, `created_by_name` TEXT, `updated_at` INTEGER, `updated_by_role` TEXT, "
            + "`updated_by_name` TEXT)",
        "CREATE INDEX IF NOT EXISTS `index_health_symptoms_patient_id_occurred_at` "
            + "ON `health_symptoms` (`patient_id`, `occurred_at`)",

        "CREATE TABLE IF NOT EXISTS `medication_intakes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + "`patient_id` INTEGER NOT NULL, `med_ref` TEXT NOT NULL, `med_name` TEXT, `dosage` TEXT, "
            + "`scheduled_date` TEXT NOT NULL, `scheduled_time` TEXT, `status` TEXT NOT NULL, `taken_at` INTEGER, "
            + "`created_at` INTEGER NOT NULL, `created_by_role` TEXT, `created_by_name` TEXT, `updated_at` INTEGER, "
            + "`updated_by_role` TEXT, `updated_by_name` TEXT)",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_medication_intakes_patient_id_med_ref_scheduled_date` "
            + "ON `medication_intakes` (`patient_id`, `med_ref`, `scheduled_date`)",
        "CREATE INDEX IF NOT EXISTS `index_medication_intakes_patient_id_scheduled_date` "
            + "ON `medication_intakes` (`patient_id`, `scheduled_date`)",

        "CREATE TABLE IF NOT EXISTS `health_reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + "`patient_id` INTEGER NOT NULL, `kind` TEXT NOT NULL, `ref_id` TEXT, `title` TEXT NOT NULL, `detail` TEXT, "
            + "`times` TEXT, `days_mask` INTEGER NOT NULL, `once_at` INTEGER, `enabled` INTEGER NOT NULL, "
            + "`created_at` INTEGER NOT NULL, `created_by_name` TEXT, `updated_at` INTEGER)",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_health_reminders_patient_id_kind_ref_id` "
            + "ON `health_reminders` (`patient_id`, `kind`, `ref_id`)",

        "CREATE TABLE IF NOT EXISTS `reminder_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + "`patient_id` INTEGER NOT NULL, `reminder_id` INTEGER NOT NULL, `kind` TEXT NOT NULL, `ref_id` TEXT, "
            + "`title` TEXT NOT NULL, `detail` TEXT, `scheduled_at` INTEGER NOT NULL, `status` TEXT NOT NULL, "
            + "`resolved_at` INTEGER, `resolved_by` TEXT)",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_reminder_events_reminder_id_scheduled_at` "
            + "ON `reminder_events` (`reminder_id`, `scheduled_at`)",
        "CREATE INDEX IF NOT EXISTS `index_reminder_events_patient_id_scheduled_at` "
            + "ON `reminder_events` (`patient_id`, `scheduled_at`)"
    };
}
