/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

/**
 *
 * @author RYZEN
 */
public class EstadoTicket {

    private String nivelPrioridad; 

    public EstadoTicket() {
    }

    public EstadoTicket(String nivelPrioridad) {
        this.nivelPrioridad = nivelPrioridad != null ? nivelPrioridad.trim() : "";
    }

    public String getNivelPrioridad() {
        return nivelPrioridad;
    }
}
