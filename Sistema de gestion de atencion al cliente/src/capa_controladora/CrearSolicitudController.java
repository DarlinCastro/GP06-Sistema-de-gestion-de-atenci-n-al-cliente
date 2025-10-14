/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import entidades.Solicitud;
import entidades.Usuario;
import entidades.TipoServicio;
import entidades.EstadoSolicitud;
import base_datos.GestorSolicitudesDAO;
import entidades.Ticket;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 *
 * @author erick
 */
    public class CrearSolicitudController {
    private GestorSolicitudesDAO gestor;

    public CrearSolicitudController(Connection conn) {
        this.gestor = new GestorSolicitudesDAO(conn);
    }

    private String generarNuevoNumeroTicket() {
        int cantidadTickets = gestor.obtenerCantidadTickets();
        int nuevoId = cantidadTickets + 1;
        
        LocalDate hoy = LocalDate.now();
        String fechaStr = hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String idFormateado = String.format("%04d", nuevoId); // Máximo 999,999 tickets

        return "T" + idFormateado;
    }

    public String crearSolicitudConTicket(Usuario usuario, TipoServicio tipoServicio, String descripcion) {
        
        // 1. Obtener IDs
        int idUsuario = gestor.obtenerIdUsuarioPorCorreo(usuario.getCorreoElectronico());
        int idTipoServicio = gestor.obtenerIdTipoServicioPorNombre(tipoServicio.getNombreServicio());
        int idEstadoSolicitud = gestor.obtenerIdEstadoSolicitudPorNombre("Pendiente"); 

        if (idUsuario == -1 || idTipoServicio == -1 || idEstadoSolicitud == -1) {
            System.err.println("Error: Datos de entrada no válidos.");
            return null;
        }

        // 2. Generar Ticket y obtener ID del BD
        String numeroTicket = generarNuevoNumeroTicket();
        int idTicket = gestor.crearTicket(idUsuario, numeroTicket);
        if (idTicket == -1) {
            return null;
        }

        // 3. Crear Entidad Solicitud (Usando setters)
        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(new Date());

        String descGuardar = descripcion.length() > 30 ? descripcion.substring(0, 30) : descripcion;
        solicitud.setDescripcion(descGuardar);
        
        // 🚨 CORRECCIÓN AQUÍ: Usamos el constructor vacío y el setter para NO modificar la clase Ticket.
        Ticket ticketCreado = new Ticket();
        ticketCreado.setNumeroTicket(numeroTicket); 
        solicitud.setTicket(ticketCreado); 

        // 4. Guardar Solicitud
        boolean solicitudGuardada = gestor.guardarSolicitud(solicitud, idUsuario, idTipoServicio, idEstadoSolicitud, idTicket);
        
        return solicitudGuardada ? numeroTicket : null;
    }
}
