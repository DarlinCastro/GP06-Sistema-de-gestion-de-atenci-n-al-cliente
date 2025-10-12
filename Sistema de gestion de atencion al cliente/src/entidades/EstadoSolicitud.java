/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

/**
 *
 * @author erick
 */
public class EstadoSolicitud {
     private String estadoSolicitud;

    public EstadoSolicitud() {
    }

    public EstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud != null ? estadoSolicitud.trim() : "";
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    @Override
    public String toString() {
        return estadoSolicitud;
    }
}
