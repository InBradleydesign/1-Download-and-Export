package com.cuidarapp.health.model;

/** Quem está fazendo o registro, informado pela interface conforme o perfil em uso. */
public class Author {

    public static final String ROLE_PATIENT = "patient";
    public static final String ROLE_CAREGIVER = "caregiver";
    public static final String ROLE_NOTIFICATION = "notification";

    public String role;
    public String name;

    public Author() {
    }

    public Author(String role, String name) {
        this.role = role;
        this.name = name;
    }

    public boolean isCaregiver() {
        return ROLE_CAREGIVER.equals(role);
    }

    public static boolean isValidRole(String role) {
        return ROLE_PATIENT.equals(role) || ROLE_CAREGIVER.equals(role) || ROLE_NOTIFICATION.equals(role);
    }
}
