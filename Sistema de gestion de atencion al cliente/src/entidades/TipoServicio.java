/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

/**
 *
 * @author RYZEN
 */
public class TipoServicio {

    private String nombreServicio; 

    public TipoServicio() {
    }

    public TipoServicio(String nombreServicio) {
        // Aseguramos que el constructor guarde el valor limpio
        this.nombreServicio = nombreServicio != null ? nombreServicio.trim() : "";
    }

    public String getNombreServicio() {
        return nombreServicio;
    }
    
    @Override
    public String toString() {
        // Retorna el nombre del servicio, asegurando que no tenga espacios extra
        return this.nombreServicio != null ? this.nombreServicio.trim() : "";
    }
}
