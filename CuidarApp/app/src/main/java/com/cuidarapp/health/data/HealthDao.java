package com.cuidarapp.health.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cuidarapp.health.model.HealthMeasurement;
import com.cuidarapp.health.model.HealthProfile;
import com.cuidarapp.health.model.MedicationIntake;
import com.cuidarapp.health.model.SymptomRecord;

import java.util.List;

/** Acesso a perfil, medições, sintomas e doses. Toda consulta é filtrada por paciente. */
@Dao
public interface HealthDao {

    // ---------- Perfil ----------
    @Query("SELECT * FROM health_profiles WHERE patient_id = :patientId")
    HealthProfile getProfile(long patientId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProfile(HealthProfile profile);

    // ---------- Medições ----------
    @Insert
    long insertMeasurement(HealthMeasurement measurement);

    @Update
    void updateMeasurement(HealthMeasurement measurement);

    @Query("SELECT * FROM health_measurements WHERE id = :id AND patient_id = :patientId")
    HealthMeasurement getMeasurement(long patientId, long id);

    @Query("DELETE FROM health_measurements WHERE id = :id AND patient_id = :patientId")
    int deleteMeasurement(long patientId, long id);

    @Query("SELECT * FROM health_measurements WHERE patient_id = :patientId "
        + "AND measured_at BETWEEN :from AND :to ORDER BY measured_at ASC")
    List<HealthMeasurement> listMeasurements(long patientId, long from, long to);

    @Query("SELECT * FROM health_measurements WHERE patient_id = :patientId AND type = :type "
        + "AND measured_at BETWEEN :from AND :to ORDER BY measured_at ASC")
    List<HealthMeasurement> listMeasurementsByType(long patientId, String type, long from, long to);

    // ---------- Sintomas ----------
    @Insert
    long insertSymptom(SymptomRecord symptom);

    @Update
    void updateSymptom(SymptomRecord symptom);

    @Query("SELECT * FROM health_symptoms WHERE id = :id AND patient_id = :patientId")
    SymptomRecord getSymptom(long patientId, long id);

    @Query("DELETE FROM health_symptoms WHERE id = :id AND patient_id = :patientId")
    int deleteSymptom(long patientId, long id);

    @Query("SELECT * FROM health_symptoms WHERE patient_id = :patientId "
        + "AND occurred_at BETWEEN :from AND :to ORDER BY occurred_at ASC")
    List<SymptomRecord> listSymptoms(long patientId, long from, long to);

    // ---------- Doses de remédio ----------
    @Insert
    long insertIntake(MedicationIntake intake);

    @Update
    void updateIntake(MedicationIntake intake);

    @Query("SELECT * FROM medication_intakes WHERE patient_id = :patientId "
        + "AND med_ref = :medRef AND scheduled_date = :date")
    MedicationIntake getIntake(long patientId, String medRef, String date);

    @Query("DELETE FROM medication_intakes WHERE patient_id = :patientId "
        + "AND med_ref = :medRef AND scheduled_date = :date")
    int deleteIntake(long patientId, String medRef, String date);

    @Query("SELECT * FROM medication_intakes WHERE patient_id = :patientId "
        + "AND scheduled_date BETWEEN :fromDate AND :toDate ORDER BY scheduled_date ASC, scheduled_time ASC")
    List<MedicationIntake> listIntakes(long patientId, String fromDate, String toDate);

    // ---------- Exclusão (LGPD) ----------
    @Query("DELETE FROM health_measurements WHERE patient_id = :patientId")
    void deleteAllMeasurements(long patientId);

    @Query("DELETE FROM health_symptoms WHERE patient_id = :patientId")
    void deleteAllSymptoms(long patientId);

    @Query("DELETE FROM medication_intakes WHERE patient_id = :patientId")
    void deleteAllIntakes(long patientId);

    @Query("DELETE FROM health_profiles WHERE patient_id = :patientId")
    void deleteProfile(long patientId);
}
