package com.example.proyectodieta;

public class Dieta {

    private int idDieta;
    private String nombre;
    private int imagen;

    public Dieta(int idDieta, String nombre, int imagen) {
        this.idDieta = idDieta;
        this.nombre = nombre;
        this.imagen = imagen;
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

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }
}

