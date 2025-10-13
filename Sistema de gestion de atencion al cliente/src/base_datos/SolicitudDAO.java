/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package base_datos;

import entidades.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

public class SolicitudDAO {

    // ----------------------------------------------------------------------
    // --- MÉTODOS DE BÚSQUEDA DE ID (CRÍTICOS PARA LA ACTUALIZACIÓN) ---
    // ----------------------------------------------------------------------
    /**
     *  Obtiene el ID de la Solicitud usando el número de Ticket. 
     */
    public int obtenerIdSolicitudPorTicket(String numeroTicket) throws SQLException {
        int idSolicitud = 0;
        String sql = "SELECT s.idsolicitud FROM SOLICITUD s JOIN TICKET t ON s.idticket = t.idticket WHERE LOWER(TRIM(t.numeroticket)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numeroTicket);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idSolicitud = rs.getInt("idsolicitud");
                }
            }
        }
        return idSolicitud;
    }

    /**
     * Obtiene el ID del Estado de Solicitud usando el nombre. 
     */
    public int obtenerIdEstadoPorNombre(String estadoNombre) throws SQLException {
        int idEstado = 0;
        String sql = "SELECT idestadosolicitud FROM ESTADO_SOLICITUD WHERE LOWER(TRIM(estadosolicitud)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, estadoNombre);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idEstado = rs.getInt("idestadosolicitud");
                }
            }
        }
        return idEstado;
    }

    // ----------------------------------------------------------------------
    // --- MÉTODO DE ACTUALIZACIÓN (USADO POR EL CONTROLADOR) ---
    // ----------------------------------------------------------------------
    /**
     * Actualiza el estado de la solicitud en la base de datos. Recibe las IDs.
     */
    public boolean actualizarEstadoSolicitud(int idSolicitud, int nuevoEstadoId) throws Exception {
        String sql = "UPDATE SOLICITUD SET IDESTADOSOLICITUD = ? WHERE IDSOLICITUD = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoEstadoId);
            pstmt.setInt(2, idSolicitud);

            int filasAfectadas = pstmt.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error de SQL al actualizar el estado de la solicitud: " + e.getMessage());
            throw new Exception("Error en la Base de Datos al actualizar solicitud.", e);
        }
    }

    // ----------------------------------------------------------------------
    // --- MÉTODOS DE BÚSQUEDA DE DATOS ---
    // ----------------------------------------------------------------------
    /**
     * 1. Obtiene el ID del usuario por nombre completo.
     */
    public int obtenerIdUsuarioPorNombre(String nombreCompleto) throws SQLException {
        String[] partes = nombreCompleto.trim().split(" ", 2);
        if (partes.length < 2) {
            return 0;
        }

        String nombres = partes[0];
        String apellidos = partes[1];
        int idUsuario = 0;

        String sql = "SELECT idusuario FROM USUARIO "
                + "WHERE LOWER(TRIM(nombres)) = LOWER(TRIM(?)) "
                + "AND LOWER(TRIM(apellidos)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombres);
            pstmt.setString(2, apellidos);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("idusuario");
                }
            }
        }
        return idUsuario;
    }

    /**
     * 2. Obtener tickets CREADOS por el Cliente.
     */
    public List<Solicitud> obtenerSolicitudesPorUsuarioId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE s.idusuario = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(crearObjetoSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    /**
     * 3. Obtener tickets ASIGNADOS a Soporte.
     */
    public List<Solicitud> obtenerSolicitudesAsignadasPorId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE t.idusuario = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(crearObjetoSolicitud(rs));
                }
            }
        }
        return solicitudes;
    }

    /**
     * 4. Obtener una Solicitud completa por su número de ticket.
     */
    public Solicitud obtenerSolicitudPorTicket(String numeroTicket) throws SQLException {
        Solicitud solicitud = null;
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE LOWER(TRIM(t.numeroticket)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numeroTicket);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    solicitud = crearObjetoSolicitud(rs);
                }
            }
        }
        return solicitud;
    }

    // ----------------------------------------------------------------------
    // --- Métodos de Carga Inicial ---
    // ----------------------------------------------------------------------
    /**
     * Obtiene todos los tipos de usuario (cargos).
     */
    public List<TipoUsuario> obtenerCargos() throws SQLException {
        List<TipoUsuario> cargos = new ArrayList<>();
        String sql = "SELECT cargo FROM TIPO_USUARIO";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                TipoUsuario tu = new TipoUsuario();
                // Ya no intentamos llamar a tu.setIdTipoUsuario()
                tu.setCargo(rs.getString("cargo").trim());
                cargos.add(tu);
            }
        }
        return cargos;
    }

    /**
     * Obtiene todos los usuarios por cargo seleccionado.
     */
    public List<Usuario> obtenerUsuariosPorCargo(String cargo) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.nombres, u.apellidos, u.correoelectronico FROM USUARIO u "
                + "JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario "
                + "WHERE LOWER(TRIM(tu.cargo)) = LOWER(TRIM(?)) "
                + "ORDER BY u.apellidos";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cargo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setNombres(rs.getString("nombres").trim());
                    u.setApellidos(rs.getString("apellidos").trim());
                    u.setCorreoElectronico(rs.getString("correoelectronico").trim());
                    usuarios.add(u);
                }
            }
        }
        return usuarios;
    }

    /**
     * Obtiene todos los estados de solicitud.
     */
    public List<EstadoSolicitud> obtenerEstadosSolicitud() throws SQLException {
        List<EstadoSolicitud> estados = new ArrayList<>();
        String sql = "SELECT estadosolicitud FROM ESTADO_SOLICITUD";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                EstadoSolicitud es = new EstadoSolicitud();
                // Ya no intentamos llamar a es.setIdEstadoSolicitud()
                es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
                estados.add(es);
            }
        }
        return estados;
    }

    // ----------------------------------------------------------------------
    // --- Mapeo de Entidad ---
    // ----------------------------------------------------------------------
    /**
     * Función utilitaria para crear el objeto Solicitud desde un ResultSet.
     */
    private Solicitud crearObjetoSolicitud(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();

        // Mapeo de Solicitud: 
        s.setFechaCreacion(rs.getDate("fechacreacion"));
        s.setDescripcion(rs.getString("descripcion").trim());

        // Mapeo de EstadoSolicitud
        EstadoSolicitud es = new EstadoSolicitud();
        es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
        s.setEstadoSolicitud(es);

        // Mapeo de TipoServicio
        TipoServicio ts = new TipoServicio();
        ts.setNombreServicio(rs.getString("nombreservicio").trim());
        s.setTipoServicio(ts);

        // Mapeo de Ticket
        Ticket t = new Ticket();
        t.setNumeroTicket(rs.getString("numeroticket").trim());

        // Mapeo de EstadoTicket
        EstadoTicket et = new EstadoTicket();
        et.setNivelPrioridad(rs.getString("nivelprioridad").trim());
        t.setEstadoTicket(et);

        s.setTicket(t);

        return s;
    }
}
