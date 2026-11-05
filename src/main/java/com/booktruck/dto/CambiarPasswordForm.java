package com.booktruck.dto;

public class CambiarPasswordForm {

    private String passwordActual;
    private String passwordNueva;
    private String confirmarPasswordNueva;

    public String getPasswordActual() { return passwordActual; }
    public void setPasswordActual(String passwordActual) { this.passwordActual = passwordActual; }

    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }

    public String getConfirmarPasswordNueva() { return confirmarPasswordNueva; }
    public void setConfirmarPasswordNueva(String confirmarPasswordNueva) { this.confirmarPasswordNueva = confirmarPasswordNueva; }
}
