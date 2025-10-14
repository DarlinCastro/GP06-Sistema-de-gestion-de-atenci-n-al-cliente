/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package base_datos;

import entidades.Solicitud;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;
/**
 *
 * @author erick
 */
public class GestorSolicitudesDAO {
    private Connection conn;

    public GestorSolicitudesDAO(Connection conn) {
        this.conn = conn;
    }
    
    // --- Búsqueda de IDs (Usados por el Controlador) ---
    
    public int obtenerIdTipoServicioPorNombre(String nombreServicio) {
        String sql = "SELECT idtiposervicio FROM tipo_servicio WHERE nombreservicio = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreServicio.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idtiposervicio");
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID de tipo de servicio: " + e.getMessage());
        }
        return -1;
    }

    public int obtenerIdEstadoSolicitudPorNombre(String nombreEstado) {
        String sql = "SELECT idestadosolicitud FROM estado_solicitud WHERE estadosolicitud = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreEstado.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idestadosolicitud");
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID de estado de solicitud: " + e.getMessage());
        }
        return -1;
    }
    
    public int obtenerIdUsuarioPorCorreo(String correo) {
        String sql = "SELECT idusuario FROM usuario WHERE correoelectronico = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idusuario");
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID de usuario por correo: " + e.getMessage());
        }
        return -1;
    }

    // --- Generación y Creación de Ticket ---

    public int obtenerCantidadTickets() {
        String sql = "SELECT COUNT(*) AS total FROM ticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("❌ Error al contar tickets: " + e.getMessage());
        }
        return -1;
    }

    public int crearTicket(int idUsuario, String numeroTicket) {
        // Asumiendo PostgreSQL (ajustar si usas MySQL/otra BD)
        String sql = "INSERT INTO ticket (idestadoticket, idusuario, fechaasignacion, numeroticket) VALUES (?, ?, ?, ?) RETURNING idticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 1); // ID 1 = Estado inicial de ticket (Ej: Abierto)
            stmt.setInt(2, idUsuario); 
            stmt.setDate(3, new java.sql.Date(new Date().getTime()));
            stmt.setString(4, numeroTicket);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idticket");
        } catch (SQLException e) {
            System.err.println("❌ Error al crear ticket: " + e.getMessage());
        }
        return -1;
    }

    // --- Creación de Solicitud ---

    public boolean guardarSolicitud(Solicitud solicitud, int idUsuario, int idTipoServicio, int idEstadoSolicitud, int idTicket) {
        // Usando los nombres de columna de tu esquema (ej: fechacreacion)
        String sql = "INSERT INTO solicitud (idusuario, idtiposervicio, idestadosolicitud, idticket, fechacreacion, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idTipoServicio);
            stmt.setInt(3, idEstadoSolicitud);
            stmt.setInt(4, idTicket);
            // Usamos el getter de tu Entidad Solicitud (NO se modifica la Entidad)
            stmt.setDate(5, new java.sql.Date(solicitud.getFechaCreacion().getTime()));

            String descripcion = solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : "";
            // Validación de longitud (asumiendo 30 caracteres máximo en BD)
            if (descripcion.length() > 30) {
                descripcion = descripcion.substring(0, 30);
            }
            stmt.setString(6, descripcion);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar solicitud: " + e.getMessage());
            return false;
        }
    }

}
