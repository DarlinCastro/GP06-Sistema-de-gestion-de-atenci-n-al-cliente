/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package base_datos;

import base_datos.ConexionBD;
import entidades.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
/**
 *
 * @author erick
 */
public class SolicitudDAO {
   
    // --- Carga Inicial (Se mantiene para consistencia) ---
    public List<EstadoSolicitud> obtenerEstadosSolicitud() {
        List<EstadoSolicitud> estados = new ArrayList<>();
        final String SQL = "SELECT ESTADOSOLICITUD FROM ESTADO_SOLICITUD";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                estados.add(new EstadoSolicitud(rs.getString("ESTADOSOLICITUD").trim()));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estados de solicitud: " + e.getMessage());
        }
        return estados;
    }

    public List<TipoUsuario> obtenerCargos() {
        List<TipoUsuario> cargos = new ArrayList<>();
        final String SQL = "SELECT CARGO FROM TIPO_USUARIO ORDER BY CARGO";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(SQL); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cargos.add(new TipoUsuario(rs.getString("CARGO").trim()));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cargos: " + e.getMessage());
        }
        return cargos;
    }

    public List<Usuario> obtenerUsuariosPorCargo(String cargo) {
        List<Usuario> usuarios = new ArrayList<>();
        final String SQL
                = "SELECT u.NOMBRES, u.APELLIDOS, tu.CARGO "
                + "FROM USUARIO u "
                + "INNER JOIN TIPO_USUARIO tu ON u.IDTIPOUSUARIO = tu.IDTIPOUSUARIO "
                + "WHERE tu.CARGO = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setString(1, cargo.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TipoUsuario tipo = new TipoUsuario(rs.getString("CARGO").trim());

                    Usuario usuario = new Usuario(
                            rs.getInt("id"),
                            rs.getString("NOMBRES").trim(),
                            rs.getString("APELLIDOS").trim(),
                            null,
                            tipo,
                            null
                    );
                    usuarios.add(usuario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios por cargo: " + e.getMessage());
        }
        return usuarios;
    }

    public List<Solicitud> obtenerSolicitudesPorUsuario(Usuario usuario) {
        List<Solicitud> solicitudes = new ArrayList<>();
        final String SQL
                = "SELECT t.NUMEROTICKET "
                + "FROM solicitudes s "
                + "INNER JOIN TICKET t ON s.IDTICKET = t.IDTICKET "
                + "INNER JOIN USUARIO u ON s.IDUSUARIO = u.IDUSUARIO "
                + "WHERE u.NOMBRES = ? AND u.APELLIDOS = ? "
                + "ORDER BY t.NUMEROTICKET DESC";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setString(1, usuario.getNombres().trim());
            ps.setString(2, usuario.getApellidos().trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket ticket = new Ticket(null, null, rs.getString("NUMEROTICKET").trim());
                    Solicitud solicitud = new Solicitud(
                            usuario, null, null, ticket, null, null,null
                    );
                    solicitudes.add(solicitud);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener solicitudes por usuario: " + e.getMessage());
        }
        return solicitudes;
    }

    /**
     * Mapeo de datos para la vista de seguimiento con correcciones.
     */
    public Solicitud obtenerSolicitudPorTicket(String numeroTicket) {
        Solicitud solicitud = null;

        final String SQL
                = "SELECT s.FECHACREACION, s.DESCRIPCION, "
                + "ts.NOMBRESERVICIO, es.ESTADOSOLICITUD, et.NIVELPRIORIDAD "
                + "FROM solicitudes s "
                + "INNER JOIN TICKET t ON s.IDTICKET = t.IDTICKET "
                + "INNER JOIN TIPO_SERVICIO ts ON s.IDTIPOSERVICIO = ts.IDTIPOSERVICIO "
                + "INNER JOIN ESTADO_SOLICITUD es ON s.IDESTADOSOLICITUD = es.IDESTADOSOLICITUD "
                + "INNER JOIN ESTADO_TICKET et ON t.IDESTADOTICKET = et.IDESTADOTICKET "
                + "WHERE t.NUMEROTICKET = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setString(1, numeroTicket.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    // 1. Mapeo del TIPO DE SERVICIO (usando NOMBRESERVICIO)
                    TipoServicio ts = new TipoServicio(rs.getString("NOMBRESERVICIO").trim());

                    // 2. Mapeo del NIVEL DE PRIORIDAD (usando NIVELPRIORIDAD)
                    EstadoTicket et = new EstadoTicket(rs.getString("NIVELPRIORIDAD").trim());

                    // Se crea el Ticket usando el NIVEL DE PRIORIDAD (EstadoTicket)
                    Ticket ticket = new Ticket(et, null, numeroTicket.trim());

                    // Mapeo del ESTADO DE LA SOLICITUD
                    EstadoSolicitud es = new EstadoSolicitud(rs.getString("ESTADOSOLICITUD").trim());

                    // Creación final de la Solicitud
                    solicitud = new Solicitud(
                            null, ts, es, ticket,
                            rs.getDate("FECHACREACION"),
                            rs.getString("DESCRIPCION").trim(),
                            null
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener solicitud por ticket: " + e.getMessage());
        }
        return solicitud;
    }

    // --------------------------------------------------------------------------------
    // --- Lógica de Actualización (¡CORRECCIÓN AQUÍ!) ---
    // --------------------------------------------------------------------------------
    public void actualizarEstadoSolicitud(String numeroTicket, String nuevoEstadoNombre) throws SQLException {
        // La consulta se ha reescrito para eliminar cualquier carácter invisible o espacio
        // innecesario que estaba causando el error de sintaxis en la posición 47.
        final String SQL_UPDATE
                = "UPDATE solicitudes s SET IDESTADOSOLICITUD = ("
                + " SELECT IDESTADOSOLICITUD FROM ESTADO_SOLICITUD WHERE TRIM(ESTADOSOLICITUD) = ?"
                + ") "
                + "WHERE s.IDTICKET = ("
                + " SELECT IDTICKET FROM TICKET WHERE TRIM(NUMEROTICKET) = ?"
                + ")";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = ConexionBD.conectar();

            // 1. Desactivar Auto-Commit temporalmente (por seguridad)
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(SQL_UPDATE);

            ps.setString(1, nuevoEstadoNombre.trim());
            ps.setString(2, numeroTicket.trim());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                // 2. FORZAR el COMMIT de la transacción
                conn.commit();
                System.out.println("DEBUG DAO: COMMIT exitoso. Filas afectadas: " + filasAfectadas);
            } else {
                // Si no se afectó ninguna fila, hacemos un ROLLBACK y lanzamos una excepción
                conn.rollback();
                throw new SQLException("La solicitud no pudo ser actualizada. Motivo: El ticket o el estado no son válidos en la base de datos.");
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de solicitud (DAO): " + e.getMessage());

            // 3. Rollback si falla
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en Rollback: " + ex.getMessage());
                }
            }
            throw e; // Re-lanza la excepción para que el Controller la capture y muestre el mensaje de error.

        } finally {
            // 4. Asegurar el cierre de recursos
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                // Reestablecer auto-commit antes de cerrar (buena práctica)
                try {
                    if (conn != null && !conn.isClosed()) {
                         conn.setAutoCommit(true);
                    }
                } catch (SQLException ex) {
                    System.err.println("Error al reestablecer auto-commit: " + ex.getMessage());
                }
                conn.close();
            }
        }
    }
    
    //Insertar solicitud en la BD
    public boolean insertarSolicitud(Solicitud s) {
       
       String sql = "INSERT INTO solicitudes (fecha_creacion, tipo_servicio, descripcion, estado) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = ConexionBD.conectar();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Convierte java.util.Date a java.sql.Date
            ps.setDate(1, new java.sql.Date(s.getFechaCreacion().getTime()));

            // Extrae el nombre del tipo de servicio
            ps.setString(2, s.getTipoServicio().toString()); 
            ps.setString(3, s.getDescripcion());

            // Extrae el estado como texto
            ps.setString(4, s.getEstadoSolicitud().toString());

            int filas = ps.executeUpdate();
            return filas > 0;
        
            } catch (SQLException e) {
                System.err.println("Error al insertar solicitud: " + e.getMessage());
                return false;
        }
    }   
   
  public List<Solicitud> obtenerTodasLasSolicitudes() {
    List<Solicitud> lista = new ArrayList<>();

    final String SQL = 
        "SELECT t.NUMEROTICKET, s.FECHACREACION, ts.NOMBRESERVICIO, s.DESCRIPCION, " +
        "es.ESTADOSOLICITUD, s.FECHAASIGNACION, et.NIVELPRIORIDAD, tu.CARGO, u.NOMBRES, u.APELLIDOS " +
        "FROM solicitudes s " +
        "INNER JOIN TICKET t ON s.IDTICKET = t.IDTICKET " +
        "INNER JOIN TIPO_SERVICIO ts ON s.IDTIPOSERVICIO = ts.IDTIPOSERVICIO " +
        "INNER JOIN ESTADO_SOLICITUD es ON s.IDESTADOSOLICITUD = es.IDESTADOSOLICITUD " +
        "INNER JOIN ESTADO_TICKET et ON t.IDESTADOTICKET = et.IDESTADOTICKET " +
        "INNER JOIN USUARIO u ON s.IDUSUARIO = u.IDUSUARIO " +
        "INNER JOIN TIPO_USUARIO tu ON u.IDTIPOUSUARIO = tu.IDTIPOUSUARIO " +
        "ORDER BY t.NUMEROTICKET DESC";

    try (Connection conn = ConexionBD.conectar(); 
         PreparedStatement ps = conn.prepareStatement(SQL); 
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            // Construcción de objetos relacionados
            EstadoTicket estadoTicket = new EstadoTicket(rs.getString("NIVELPRIORIDAD").trim());
            Ticket ticket = new Ticket(estadoTicket, null, rs.getString("NUMEROTICKET").trim());

            TipoServicio tipoServicio = new TipoServicio(rs.getString("NOMBRESERVICIO").trim());
            EstadoSolicitud estadoSolicitud = new EstadoSolicitud(rs.getString("ESTADOSOLICITUD").trim());
            TipoUsuario tipoUsuario = new TipoUsuario(rs.getString("CARGO").trim());

            Usuario usuario = new Usuario(
                0, // ID no necesario aquí
                rs.getString("NOMBRES").trim(),
                rs.getString("APELLIDOS").trim(),
                null,
                tipoUsuario,
                null
            );

            // Crear la solicitud con todos los datos
            Solicitud solicitud = new Solicitud(
                usuario,
                tipoServicio,
                estadoSolicitud,
                ticket,
                rs.getDate("FECHACREACION"),
                rs.getString("DESCRIPCION").trim(),
                rs.getDate("FECHAASIGNACION")
            );

            lista.add(solicitud);
        }

        } catch (SQLException e) {
        System.err.println("Error al obtener todas las solicitudes: " + e.getMessage());
        }

        return lista;
    }
  
   //Obtiene todos los tickets de la tabla 'solicitudes', formateados como "TCK-ID".
    public List<String> obtenerTickets() {
        List<String> tickets = new ArrayList<>();
        final String SQL = "SELECT id FROM solicitudes ORDER BY id DESC";  // Todos los tickets, ordenados por ID descendente (más recientes primero)
        
        Connection conn = ConexionBD.conectar();  // Obtén conexión
        if (conn == null) {
            System.err.println("No se pudo conectar a la BD. No se cargan tickets.");
            return tickets;  // Lista vacía en caso de fallo
        }
        
        try (PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int idTicket = rs.getInt("id");
                String numero = "TCK-" + idTicket;  // Formato: TCK-1, TCK-2, etc.
                tickets.add(numero);
            }
            System.out.println("Todos los tickets cargados: " + tickets.size());  // Debug opcional
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los tickets: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cierra la conexión (buena práctica)
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
        return tickets;
    }
}
