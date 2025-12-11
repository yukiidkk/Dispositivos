package com.example.proyectodieta;

public class Platillo {

    private int idPlatillo;
    private int idDieta;
    private String nombre;
    private String tipoComida;
    private boolean preparado;

    public Platillo(int idPlatillo, int idDieta, String nombre, String tipoComida) {
        this.idPlatillo = idPlatillo;
        this.idDieta = idDieta;
        this.nombre = nombre;
        this.tipoComida = tipoComida;
        this.preparado = false;
    }

    public int getIdPlatillo() {
        return idPlatillo;
    }

    public void setIdPlatillo(int idPlatillo) {
        this.idPlatillo = idPlatillo;
    }

    public int getIdDieta() {
        return idDieta;
    }

    public void setIdDieta(int idDieta) {
        this.idDieta = idDieta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(String tipoComida) {
        this.tipoComida = tipoComida;
    }

    public boolean isPreparado() {
        return preparado;
    }

    public void setPreparado(boolean preparado) {
        this.preparado = preparado;
    }
}
