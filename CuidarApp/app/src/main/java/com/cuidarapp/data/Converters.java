package com.cuidarapp.data;

import androidx.room.TypeConverter;

import com.cuidarapp.model.StatusAlerta;
import com.cuidarapp.model.StatusVisita;
import com.cuidarapp.model.TipoPerfil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Converters {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // LocalDate
    @TypeConverter
    public static LocalDate fromDateString(String value) {
        return value == null ? null : LocalDate.parse(value, DATE_FORMATTER);
    }
    
    @TypeConverter
    public static String dateToString(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }
    
    // LocalTime
    @TypeConverter
    public static LocalTime fromTimeString(String value) {
        return value == null ? null : LocalTime.parse(value, TIME_FORMATTER);
    }
    
    @TypeConverter
    public static String timeToString(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }
    
    // LocalDateTime
    @TypeConverter
    public static LocalDateTime fromDateTimeString(String value) {
        return value == null ? null : LocalDateTime.parse(value, DATETIME_FORMATTER);
    }
    
    @TypeConverter
    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }
    
    // TipoPerfil
    @TypeConverter
    public static TipoPerfil fromTipoPerfilString(String value) {
        return value == null ? null : TipoPerfil.valueOf(value);
    }
    
    @TypeConverter
    public static String tipoPerfilToString(TipoPerfil tipoPerfil) {
        return tipoPerfil == null ? null : tipoPerfil.name();
    }
    
    // StatusVisita
    @TypeConverter
    public static StatusVisita fromStatusVisitaString(String value) {
        return value == null ? null : StatusVisita.valueOf(value);
    }
    
    @TypeConverter
    public static String statusVisitaToString(StatusVisita status) {
        return status == null ? null : status.name();
    }
    
    // StatusAlerta
    @TypeConverter
    public static StatusAlerta fromStatusAlertaString(String value) {
        return value == null ? null : StatusAlerta.valueOf(value);
    }
    
    @TypeConverter
    public static String statusAlertaToString(StatusAlerta status) {
        return status == null ? null : status.name();
    }
}
