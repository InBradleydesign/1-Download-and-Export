package com.cuidarapp.health.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Perfil de saúde do paciente: condições, indicadores acompanhados, observações do profissional,
 * autorização do cuidador, orientações de alerta definidas pelo profissional e consentimento LGPD.
 * Listas são guardadas como JSON (ver {@link com.cuidarapp.health.HealthRepository}).
 */
@Entity(tableName = "health_profiles")
public class HealthProfile {

    @PrimaryKey
    @ColumnInfo(name = "patient_id")
    public long patientId;

    @ColumnInfo(name = "conditions_json")
    public String conditionsJson;

    @ColumnInfo(name = "conditions_other")
    public String conditionsOther;

    @ColumnInfo(name = "indicators_json")
    public String indicatorsJson;

    @ColumnInfo(name = "doctor_notes")
    public String doctorNotes;

    @ColumnInfo(name = "caregiver_may_record")
    public boolean caregiverMayRecord;

    @ColumnInfo(name = "alert_rules_json")
    public String alertRulesJson;

    @ColumnInfo(name = "consent_at")
    public Long consentAt;

    @ColumnInfo(name = "consent_by")
    public String consentBy;

    @ColumnInfo(name = "updated_at")
    public Long updatedAt;

    @ColumnInfo(name = "updated_by")
    public String updatedBy;

    /** Indicadores sugeridos para um perfil novo; o cuidador ajusta conforme o profissional. */
    public static final List<String> DEFAULT_INDICATORS = Collections.unmodifiableList(
        Arrays.asList(MeasurementType.BLOOD_PRESSURE, MeasurementType.GLUCOSE));
}
