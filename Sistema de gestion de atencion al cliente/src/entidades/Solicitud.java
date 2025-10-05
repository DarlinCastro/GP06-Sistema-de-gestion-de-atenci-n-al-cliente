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
public class Solicitud {

    private Usuario usuario;
    private TipoServicio tipoServicio;
    private EstadoSolicitud estadoSolicitud;
    private Ticket ticket;
    private Date fechaCreacion; 
    private String descripcion; 

    public Solicitud() {
    }

    public Solicitud(Usuario usuario, TipoServicio tipoServicio, EstadoSolicitud estadoSolicitud, Ticket ticket, Date fechaCreacion, String descripcion) {
        this.usuario = usuario;
        this.tipoServicio = tipoServicio;
        this.estadoSolicitud = estadoSolicitud;
        this.ticket = ticket;
        this.fechaCreacion = fechaCreacion;
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    public Ticket getTicket() {
        return ticket;
    }

    public EstadoSolicitud getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Método CRUCIAL: Lo que se muestra en cbNTicket.
    @Override
    public String toString() {
        return this.ticket != null ? this.ticket.getNumeroTicket() : "N/A";
    }
}
