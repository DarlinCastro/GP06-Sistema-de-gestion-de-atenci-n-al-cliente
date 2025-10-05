/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

import java.util.Date;

/**
 *
 * @author RYZEN
 */
public class Ticket {

    private EstadoTicket estadoTicket;
    private Date fechaAsignacion; 
    private String numeroTicket; 

    public Ticket() {
    }

    public Ticket(EstadoTicket estadoTicket, Date fechaAsignacion, String numeroTicket) {
        this.estadoTicket = estadoTicket;
        this.fechaAsignacion = fechaAsignacion;
        this.numeroTicket = numeroTicket != null ? numeroTicket.trim() : "";
    }

    public Date getFechaAsignacion() {
        return fechaAsignacion;
    }

    public EstadoTicket getEstadoTicket() {
        return estadoTicket;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }
}
