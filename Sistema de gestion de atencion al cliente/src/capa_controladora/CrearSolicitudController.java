package capa_controladora;

import capa_modelo.Solicitud;
import capa_modelo.Usuario;
import capa_modelo.TipoServicio;
import capa_modelo.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;


/**
 * Clase que gestiona la lógica de negocio para la creación de una Solicitud,
 * incluyendo la generación del Ticket y el acceso directo a la base de datos
 * (DAO Logic unificada).
 *
 * @author erick
 */
public class CrearSolicitudController {
    
    // La clase ahora maneja la conexión directamente, absorbiendo la función del DAO.
    private Connection conn;

    public CrearSolicitudController(Connection conn) {
        this.conn = conn;
    }
    
    // =========================================================================
    // --- LÓGICA DAO (Absorbida del GestorSolicitudesDAO) ---
    // =========================================================================

    /**
     * Busca el ID del tipo de servicio por su nombre en la base de datos.
     * @param nombreServicio El nombre del servicio.
     * @return El ID del tipo de servicio o -1 si no se encuentra o hay un error.
     */
    private int obtenerIdTipoServicioPorNombre(String nombreServicio) {
        // ✅ CORRECCIÓN CLAVE: Usamos LOWER(TRIM(nombreservicio)) para ignorar espacios de CHAR(80) y mayúsculas/minúsculas.
        String sql = "SELECT idtiposervicio FROM tipo_servicio WHERE LOWER(TRIM(nombreservicio)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Convertimos el String de Java a minúsculas
            stmt.setString(1, nombreServicio.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idtiposervicio");
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID de tipo de servicio: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Busca el ID del estado de solicitud por su nombre en la base de datos.
     * @param nombreEstado El nombre del estado.
     * @return El ID del estado o -1 si no se encuentra o hay un error.
     */
    private int obtenerIdEstadoSolicitudPorNombre(String nombreEstado) {
        // Aplicamos la corrección por ser columna CHAR(10)
        String sql = "SELECT idestadosolicitud FROM estado_solicitud WHERE LOWER(TRIM(estadosolicitud)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreEstado.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idestadosolicitud");
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID de estado de solicitud: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Busca el ID del usuario por su correo electrónico.
     * @param correo El correo electrónico del usuario.
     * @return El ID del usuario o -1 si no se encuentra o hay un error.
     */
    private int obtenerIdUsuarioPorCorreo(String correo) {
        // Aplicamos la corrección por ser columna CHAR(25)
        String sql = "SELECT idusuario FROM usuario WHERE LOWER(TRIM(correoelectronico)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("idusuario");
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID de usuario por correo: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Cuenta la cantidad total de tickets en la base de datos.
     * @return El total de tickets o -1 si hay un error.
     */
    private int obtenerCantidadTickets() {
        String sql = "SELECT COUNT(*) AS total FROM ticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("❌ Error al contar tickets: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Crea un nuevo registro de Ticket en la base de datos.
     * @param idUsuario ID del usuario asociado.
     * @param numeroTicket Número de ticket generado.
     * @return El ID generado para el nuevo ticket (idticket) o -1 si hay un error.
     */
    private int crearTicket(int idUsuario, String numeroTicket) {
        // Usando RETURNING para obtener el ID del ticket
        String sql = "INSERT INTO ticket (idestadoticket, idusuario, fechaasignacion, numeroticket) VALUES (?, ?, ?, ?) RETURNING idticket";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, 1); // ID 1 = Estado inicial de ticket (Ej: Abierto/Nuevo)
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

    /**
     * Guarda la entidad Solicitud en la base de datos, referenciando las FK.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    private boolean guardarSolicitud(Solicitud solicitud, int idUsuario, int idTipoServicio, int idEstadoSolicitud, int idTicket) {
        String sql = "INSERT INTO solicitud (idusuario, idtiposervicio, idestadosolicitud, idticket, fechacreacion, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idTipoServicio);
            stmt.setInt(3, idEstadoSolicitud);
            stmt.setInt(4, idTicket);
            
            stmt.setDate(5, new java.sql.Date(solicitud.getFechaCreacion().getTime()));

            String descripcion = solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : "";
            // ✅ CORRECCIÓN: Se usa 300 porque DESCRIPCION es CHAR(300) en el esquema SQL.
            if (descripcion.length() > 300) {
                descripcion = descripcion.substring(0, 300);
            }
            stmt.setString(6, descripcion);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error al guardar solicitud: " + e.getMessage());
            return false;
        }
    }
    
    // =========================================================================
    // --- LÓGICA DE CONTROLADORA ---
    // =========================================================================

    /**
     * Genera un nuevo número de ticket basado en el conteo actual de tickets.
     * El formato es T + ID_SECUENCIAL.
     * @return Un nuevo número de ticket único.
     */
    private String generarNuevoNumeroTicket() {
        int cantidadTickets = obtenerCantidadTickets();
        int nuevoId = cantidadTickets + 1;
        
        String idFormateado = String.format("%04d", nuevoId);

        return "T" + idFormateado;
    }

    /**
     * Crea una Solicitud y su Ticket asociado en una sola operación.
     * @param usuario El objeto Usuario solicitante.
     * @param tipoServicio El objeto TipoServicio de la solicitud.
     * @param descripcion La descripción del problema o solicitud.
     * @return El número de ticket creado si la operación es exitosa, o null si falla.
     */
    public String crearSolicitudConTicket(Usuario usuario, TipoServicio tipoServicio, String descripcion) {
        
        // 1. Obtener IDs (Llama a los métodos DAO absorbidos)
        int idUsuario = obtenerIdUsuarioPorCorreo(usuario.getCorreoElectronico());
        int idTipoServicio = obtenerIdTipoServicioPorNombre(tipoServicio.getNombreServicio());
        int idEstadoSolicitud = obtenerIdEstadoSolicitudPorNombre("Pendiente"); 

        if (idUsuario == -1 || idTipoServicio == -1 || idEstadoSolicitud == -1) {
            System.err.println("Error: Datos de entrada no válidos (Usuario, Tipo de Servicio o Estado no encontrado).");
            return null;
        }

        // 2. Generar Ticket y obtener ID del BD
        String numeroTicket = generarNuevoNumeroTicket();
        int idTicket = crearTicket(idUsuario, numeroTicket);
        if (idTicket == -1) {
            return null;
        }

        // 3. Crear Entidad Solicitud
        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(new Date());

        // Se usa 300 porque DESCRIPCION es CHAR(300)
        String descGuardar = descripcion.length() > 300 ? descripcion.substring(0, 300) : descripcion;
        solicitud.setDescripcion(descGuardar);
        
        // Creación y seteo del objeto Ticket (solo para la entidad)
        Ticket ticketCreado = new Ticket();
        ticketCreado.setNumeroTicket(numeroTicket); 
        solicitud.setTicket(ticketCreado); 

        // 4. Guardar Solicitud (Llama al método DAO absorbido)
        boolean solicitudGuardada = guardarSolicitud(solicitud, idUsuario, idTipoServicio, idEstadoSolicitud, idTicket);
        
        return solicitudGuardada ? numeroTicket : null;
    }
}