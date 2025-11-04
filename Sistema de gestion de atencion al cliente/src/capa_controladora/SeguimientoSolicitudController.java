package capa_controladora;

import capa_modelo.EstadoSolicitud;
import capa_modelo.Usuario;
import capa_modelo.Ticket;
import capa_modelo.TipoUsuario;
import capa_modelo.EstadoTicket;
import capa_modelo.TipoServicio;
import capa_modelo.Solicitud;
import capa_vista.jFrameSeguimientoSolicitud;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import javax.swing.JFrame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date; 
import base_datos.ConexionBD; 

public class SeguimientoSolicitudController implements ActionListener {

    private final jFrameSeguimientoSolicitud vista;
    private final JFrame ventanaOrigen; 
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Solicitud solicitudActual;
    private List<EstadoSolicitud> listaTodosLosEstados;

    private static final String ESTADO_CANCELADO_NOMBRE = "Cancelado";
    private static final String SELECCIONE_ITEM = "Seleccione ";

    public SeguimientoSolicitudController(jFrameSeguimientoSolicitud vista, JFrame ventanaOrigen) {
        this.vista = vista;
        this.ventanaOrigen = ventanaOrigen; 
        this.vista.setControlador(this);
        inicializarComponentes();
        cargarDatosIniciales();
    }

    private void inicializarComponentes() {
        vista.getCbCargo().addActionListener(this);
        vista.getCbNombre().addActionListener(this);
        vista.getCbNTicket().addActionListener(this);
        vista.getBtnActualizarSolicitud().addActionListener(this);
        // Botón Cancelar tiene su propio ActionPerformed en la vista, que llama a cancelarSolicitudDesdeVista()

        // Bloquear campos de solo lectura
        vista.getTxtFechaCreacion().setEditable(false);
        vista.getTxtTipoServicio().setEditable(false);
        vista.getTxtDescripcion().setEditable(false);
        vista.getTxtNivelPrioridad().setEditable(false);
    }
    
    private void resetearCombosSecundarios() {
        // Reinicia Nombre
        vista.getCbNombre().removeAllItems();
        vista.getCbNombre().addItem(SELECCIONE_ITEM + "Nombre");
        
        // Reinicia N° Ticket
        vista.getCbNTicket().removeAllItems();
        vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
        
        limpiarDetalleCampos();
    }

    private void cargarDatosIniciales() {
        cargarCargosEnVista();
        cargarListaTodosLosEstados();
        resetearCombosSecundarios();

        if (vista.getCbCargo().getItemCount() > 0) {
            vista.getCbCargo().setSelectedIndex(0);
        }
        limpiarDetalleCampos();
    }

    private void cargarCargosEnVista() {
        try {
            List<TipoUsuario> cargos = obtenerCargos();
            JComboBox<String> cb = vista.getCbCargo();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Cargo");
            for (TipoUsuario tu : cargos) {
                cb.addItem(tu.toString().trim());
            }
        } catch (Exception ex) {
            // El propio método obtenerCargos ya notifica error BD.
            System.err.println("Error al cargar Cargos en vista: " + ex.getMessage());
        }
    }
    
    private void cargarListaTodosLosEstados() {
        try {
            listaTodosLosEstados = obtenerEstadosSolicitud(); 
        } catch (Exception ex) {
            System.err.println("Error al cargar Lista de Estados: " + ex.getMessage());
            listaTodosLosEstados = new ArrayList<>();
        }
    }

    private void limpiarDetalleCampos() {
        vista.getTxtFechaCreacion().setText("");
        vista.getTxtTipoServicio().setText("");
        vista.getTxtDescripcion().setText("");
        vista.getTxtNivelPrioridad().setText("");
        vista.getCbEstadoSolicitud().removeAllItems();
        solicitudActual = null;
    }

    // ------------------- Manejo de Eventos -------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getCbCargo()) {
            manejarSeleccionCargo();
        } else if (e.getSource() == vista.getCbNombre()) {
            manejarSeleccionNombre();
        } else if (e.getSource() == vista.getCbNTicket()) {
            manejarSeleccionTicket();
        } else if (e.getSource() == vista.getBtnActualizarSolicitud()) {
            manejarBotonActualizar();
        }
    }

    private void manejarSeleccionCargo() {
        String cargoSeleccionado = (String) vista.getCbCargo().getSelectedItem();

        if (cargoSeleccionado == null || cargoSeleccionado.equals(SELECCIONE_ITEM + "Cargo")) {
            resetearCombosSecundarios();
            return;
        }

        try {
            List<Usuario> usuarios = obtenerUsuariosPorCargo(cargoSeleccionado.trim()); 
            JComboBox<String> cb = vista.getCbNombre();
            
            // Reemplazamos removeAllItems y addItem(Seleccione) para no interferir con el listener
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Nombre");

            for (Usuario u : usuarios) {
                // Asumiendo que u.toString() retorna Nombre y Apellido
                cb.addItem(u.getNombres().trim() + " " + u.getApellidos().trim()); 
            }
            
            // Forzamos el reinicio de los tickets y campos de detalle
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
            limpiarDetalleCampos();
            
        } catch (SQLException ex) {
            System.err.println("Error al cargar Nombres: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los usuarios: " + ex.getMessage(), 
                                          "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            resetearCombosSecundarios();
        }
    }

    private void manejarSeleccionNombre() {
        String nombreCompletoSeleccionado = (String) vista.getCbNombre().getSelectedItem();
        String cargo = (String) vista.getCbCargo().getSelectedItem();

        if (nombreCompletoSeleccionado == null || nombreCompletoSeleccionado.equals(SELECCIONE_ITEM + "Nombre")) {
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
            limpiarDetalleCampos();
            return;
        }

        try {
            int idUsuario = obtenerIdUsuarioPorNombre(nombreCompletoSeleccionado.trim());

            if (idUsuario == 0) {
                vista.getCbNTicket().removeAllItems();
                vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
                limpiarDetalleCampos();
                return;
            }

            List<Solicitud> solicitudes;
            String cargoTrim = cargo.trim();

            // Lógica para determinar si se buscan tickets creados (Cliente) o asignados (Soporte/Tecnico)
            if ("Programador".equalsIgnoreCase(cargoTrim) || "Tecnico".equalsIgnoreCase(cargoTrim) || "Técnico".equalsIgnoreCase(cargoTrim)) {
                solicitudes = obtenerSolicitudesAsignadasPorId(idUsuario);
            } else {
                // Asume que todos los demás roles solo ven los tickets que crearon (Clientes)
                solicitudes = obtenerSolicitudesPorUsuarioId(idUsuario);
            }

            JComboBox<String> cb = vista.getCbNTicket();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Ticket");

            for (Solicitud s : solicitudes) {
                cb.addItem(s.getTicket().getNumeroTicket().trim());
            }
            limpiarDetalleCampos();
        } catch (SQLException ex) {
            System.err.println("Error al cargar Tickets: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los tickets del usuario: " + ex.getMessage(), 
                                          "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
            limpiarDetalleCampos();
        }
    }

    private void manejarSeleccionTicket() {
        String numeroTicket = (String) vista.getCbNTicket().getSelectedItem();

        if (numeroTicket == null || numeroTicket.equals(SELECCIONE_ITEM + "Ticket")) {
            limpiarDetalleCampos();
            return;
        }

        try {
            solicitudActual = obtenerSolicitudPorTicket(numeroTicket.trim());

            if (solicitudActual != null) {
                java.util.Date fechaCreacion = solicitudActual.getFechaCreacion();
                vista.getTxtFechaCreacion().setText(fechaCreacion != null ? dateFormat.format(fechaCreacion) : "");
                vista.getTxtTipoServicio().setText(solicitudActual.getTipoServicio().getNombreServicio().trim());
                vista.getTxtDescripcion().setText(solicitudActual.getDescripcion().trim());
                vista.getTxtNivelPrioridad().setText(solicitudActual.getTicket().getEstadoTicket().getNivelPrioridad().trim());
                llenarYCargarEstadoSolicitud(solicitudActual.getEstadoSolicitud().getEstadoSolicitud().trim());
            } else {
                limpiarDetalleCampos();
                JOptionPane.showMessageDialog(vista, "No se encontró la información completa de la solicitud.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar datos del Ticket: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar detalles del Ticket. " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            limpiarDetalleCampos();
        }
    }

    private void llenarYCargarEstadoSolicitud(String estadoActual) {
        JComboBox<String> cb = vista.getCbEstadoSolicitud();
        cb.removeAllItems();

        if (listaTodosLosEstados != null) {
            cb.addItem(SELECCIONE_ITEM + "Estado");
            for (EstadoSolicitud es : listaTodosLosEstados) {
                cb.addItem(es.getEstadoSolicitud().trim());
            }
            cb.setSelectedItem(estadoActual);
        }
    }

    // ----------------------------------------------------------------------
    // MANEJADOR DEL BOTÓN ACTUALIZAR Y CANCELAR (Lógica de la UI)
    // ----------------------------------------------------------------------
    
    public void manejarBotonActualizar() {
        if (solicitudActual == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un ticket para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nuevoEstadoStr = (String) vista.getCbEstadoSolicitud().getSelectedItem();

        if (nuevoEstadoStr == null || nuevoEstadoStr.equals(SELECCIONE_ITEM + "Estado")) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un nuevo estado para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ejecutarActualizacion(nuevoEstadoStr);
    }
    
    public void cancelarSolicitudDesdeVista() {
        if (solicitudActual == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un ticket para cancelar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista, 
                "¿Está seguro que desea CANCELAR la solicitud #" + solicitudActual.getTicket().getNumeroTicket() + "?",
                "Confirmar Cancelación", JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutarActualizacion(ESTADO_CANCELADO_NOMBRE); 
    }

    private void ejecutarActualizacion(String nuevoEstadoStr) {
        try {
            int nuevoEstadoId = obtenerIdEstadoPorNombre(nuevoEstadoStr);
            if (nuevoEstadoId == 0) {
                JOptionPane.showMessageDialog(vista, "Error: No se pudo encontrar el ID del estado seleccionado.", "Error Interno", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ticketNum = solicitudActual.getTicket().getNumeroTicket();
            int idSolicitud = obtenerIdSolicitudPorTicket(ticketNum);

            if (idSolicitud == 0) {
                JOptionPane.showMessageDialog(vista, "Error: No se pudo encontrar el ID de la Solicitud para el ticket: " + ticketNum, "Error Interno", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean exito = actualizarEstadoSolicitud(idSolicitud, nuevoEstadoId);

            if (exito) {
                JOptionPane.showMessageDialog(vista,
                                "El estado de la solicitud #" + ticketNum + " ha sido actualizado a: " + nuevoEstadoStr,
                                "Actualización Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                cargarDatosIniciales();
            } else {
                JOptionPane.showMessageDialog(vista, "Fallo al actualizar el estado en la base de datos.", "Error de BD", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Ocurrió un error al intentar actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Métodos de Navegación ---
    public void iniciar() {
        vista.setVisible(true);
        vista.setTitle("Seguimiento y Actualización de Solicitudes");
        vista.setLocationRelativeTo(null);
    }

    public void irAtras() {
        vista.dispose();
        if (ventanaOrigen != null) {
            ventanaOrigen.setVisible(true);
            ventanaOrigen.setLocationRelativeTo(null);
        } else {
            JOptionPane.showMessageDialog(null, "Error de navegación: No se encontró la ventana anterior.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ----------------------------------------------------------------------
    // --- MÉTODOS DE ACCESO A DATOS (DAO) - MOVIDOS Y COMPLETOS ---
    // ----------------------------------------------------------------------
    
    // --- Carga Inicial ---
    private List<TipoUsuario> obtenerCargos() throws SQLException {
        List<TipoUsuario> cargos = new ArrayList<>();
        String sql = "SELECT cargo FROM TIPO_USUARIO";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                TipoUsuario tu = new TipoUsuario();
                tu.setCargo(rs.getString("cargo").trim());
                cargos.add(tu);
            }
        } catch (SQLException ex) {
            System.err.println("Error SQL al cargar Cargos: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los cargos desde la BD.", "Error de Conexión/BD", JOptionPane.ERROR_MESSAGE);
            throw ex;
        }
        return cargos;
    }
    
    private List<EstadoSolicitud> obtenerEstadosSolicitud() throws SQLException {
        List<EstadoSolicitud> estados = new ArrayList<>();
        String sql = "SELECT estadosolicitud FROM ESTADO_SOLICITUD";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                EstadoSolicitud es = new EstadoSolicitud();
                es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
                estados.add(es);
            }
        } catch (SQLException ex) {
            System.err.println("Error SQL al cargar Estados: " + ex.getMessage());
             JOptionPane.showMessageDialog(vista, "Error al cargar los estados de solicitud desde la BD.", "Error de Conexión/BD", JOptionPane.ERROR_MESSAGE);
             throw ex;
        }
        return estados;
    }
    
    // --- Búsqueda de IDs (Críticos para el flujo) ---
    private int obtenerIdEstadoPorNombre(String estadoNombre) throws SQLException {
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

    private int obtenerIdSolicitudPorTicket(String numeroTicket) throws SQLException {
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
    
    private int obtenerIdUsuarioPorNombre(String nombreCompleto) throws SQLException {
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

    // --- Búsqueda de Usuarios y Solicitudes ---
    private List<Usuario> obtenerUsuariosPorCargo(String cargo) throws SQLException {
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

    private List<Solicitud> obtenerSolicitudesPorUsuarioId(int idUsuario) throws SQLException {
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

    private List<Solicitud> obtenerSolicitudesAsignadasPorId(int idUsuario) throws SQLException {
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

    private Solicitud obtenerSolicitudPorTicket(String numeroTicket) throws SQLException {
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

    // --- Mapeo de Entidad ---
    private Solicitud crearObjetoSolicitud(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();

        s.setFechaCreacion(rs.getDate("fechacreacion"));
        s.setDescripcion(rs.getString("descripcion").trim());

        EstadoSolicitud es = new EstadoSolicitud();
        es.setEstadoSolicitud(rs.getString("estadosolicitud").trim());
        s.setEstadoSolicitud(es);

        TipoServicio ts = new TipoServicio();
        ts.setNombreServicio(rs.getString("nombreservicio").trim());
        s.setTipoServicio(ts);

        Ticket t = new Ticket();
        t.setNumeroTicket(rs.getString("numeroticket").trim());

        EstadoTicket et = new EstadoTicket();
        et.setNivelPrioridad(rs.getString("nivelprioridad").trim());
        t.setEstadoTicket(et);

        s.setTicket(t);

        return s;
    }

    // --- Método de Actualización ---
    private boolean actualizarEstadoSolicitud(int idSolicitud, int nuevoEstadoId) throws Exception {
        String sql = "UPDATE SOLICITUD SET IDESTADOSOLICITUD = ? WHERE IDSOLICITUD = ?";

        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevoEstadoId);
            pstmt.setInt(2, idSolicitud);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            System.err.println("Error de SQL al actualizar el estado de la solicitud: " + e.getMessage());
            throw new Exception("Error en la Base de Datos al actualizar solicitud. Detalles: " + e.getMessage(), e);
        }
    }
}