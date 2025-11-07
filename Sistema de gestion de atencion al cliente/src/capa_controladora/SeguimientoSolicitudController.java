package capa_controladora;

import capa_modelo.EstadoSolicitud;
import capa_modelo.Usuario;
import capa_modelo.Ticket;
import capa_modelo.TipoServicio;
import capa_modelo.Solicitud;
import capa_modelo.EstadoTicket; 
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
import base_datos.ConexionBD;


public class SeguimientoSolicitudController implements ActionListener {

    private final jFrameSeguimientoSolicitud vista;
    private final JFrame ventanaOrigen;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Solicitud solicitudActual;
    private List<EstadoSolicitud> listaTodosLosEstados;

    private static final String ESTADO_CANCELADO_NOMBRE = "Cancelado";
    private static final String SELECCIONE_ITEM = "Seleccione ";

    // CONSTRUCTOR
    public SeguimientoSolicitudController(jFrameSeguimientoSolicitud vista, JFrame ventanaOrigen) {
        this.vista = vista;
        this.ventanaOrigen = ventanaOrigen;
        
        // **Validación de Sesión**
        if (SesionActual.usuarioActual == null) {
            JOptionPane.showMessageDialog(vista, "Error: No hay usuario logeado en la sesión.", "Error Grave", JOptionPane.ERROR_MESSAGE);
            vista.dispose();
            return;
        }

        this.vista.setControlador(this);
        inicializarComponentes();
        cargarDatosIniciales();
        adaptarVistaSegunRol(); 
    }

    private void inicializarComponentes() {
        // Agregar Listeners
        vista.getCbCargo().addActionListener(this);
        vista.getCbNombre().addActionListener(this);
        vista.getCbNTicket().addActionListener(this); 
        vista.getBtnActualizarSolicitud().addActionListener(this);
        vista.getBtnCancelarSolicitud().addActionListener(this);

        // Bloquear campos de solo lectura
        vista.getTxtFechaCreacion().setEditable(false);
        vista.getTxtTipoServicio().setEditable(false);
        vista.getTxtDescripcion().setEditable(false);
        vista.getTxtNivelPrioridad().setEditable(false);
    }
    
    // MÉTODO CORREGIDO para carga directa de tickets según el rol
    private void adaptarVistaSegunRol() {
        Usuario usuarioLogeado = SesionActual.usuarioActual;
        
        if (usuarioLogeado == null || usuarioLogeado.getTipoUsuario() == null) return;
        
        String cargo = usuarioLogeado.getTipoUsuario().getCargo().trim();
        String nombreCompleto = usuarioLogeado.getNombres().trim() + " " + usuarioLogeado.getApellidos().trim();
        int idUsuario = 0;
        
        try {
            String identificador = usuarioLogeado.getPassword().getIdentificador();
            idUsuario = obtenerIdUsuarioNumerico(identificador);
        } catch (Exception e) {
            System.err.println("Error al obtener el ID numérico del usuario: " + e.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar datos de usuario desde la BD.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Configuración de la interfaz para todos los roles: Cargo y Nombre fijo y deshabilitado
        vista.getCbCargo().removeAllItems();
        vista.getCbCargo().addItem(cargo);
        vista.getCbCargo().setSelectedItem(cargo);
        vista.getCbCargo().setEnabled(false); 

        vista.getCbNombre().removeAllItems();
        vista.getCbNombre().addItem(nombreCompleto);
        vista.getCbNombre().setSelectedItem(nombreCompleto);
        vista.getCbNombre().setEnabled(false); 
        
        // 2. Carga directa de tickets y adaptación de botones según el rol
        if ("Cliente".equalsIgnoreCase(cargo)) {
            // CLIENTE: Solo puede cancelar.
            vista.getTxtNivelPrioridad().setVisible(false);
            vista.getLblNivelPrioridad().setVisible(false);
            vista.getCbEstadoSolicitud().setEnabled(false);
            vista.getBtnActualizarSolicitud().setVisible(false);
            vista.getBtnCancelarSolicitud().setVisible(true);
            
            // Carga los tickets que él creó
            cargarTicketsSegunRol(idUsuario, cargo);

        } else if ("Programador".equalsIgnoreCase(cargo) || "Tecnico".equalsIgnoreCase(cargo) || "Técnico".equalsIgnoreCase(cargo)) {
            // SOPORTE (Programador/Técnico): Pueden actualizar el estado.
            vista.getTxtNivelPrioridad().setVisible(true);
            vista.getLblNivelPrioridad().setVisible(true);
            vista.getCbEstadoSolicitud().setEnabled(true);
            vista.getBtnActualizarSolicitud().setVisible(true);
            vista.getBtnCancelarSolicitud().setVisible(false);
            
            // Carga los tickets que están asignados a él
            cargarTicketsSegunRol(idUsuario, cargo);
        }
    }
    // FIN MÉTODO CORREGIDO

    private void cargarDatosIniciales() {
        cargarListaTodosLosEstados();

        // Inicializar los combos de selección vacíos
        // Solo aplica si el rol tuviera el combo habilitado (lo cual ya se corrigió para que no ocurra)
        vista.getCbNTicket().removeAllItems();
        vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");

        limpiarDetalleCampos();
    }
    
    // Método ya no se usa, pero se mantiene por si se quiere un administrador que vea todos los cargos
    private void cargarCargosParaSoporte() {
        try {
            List<String> cargos = obtenerTiposUsuarioParaCargos();
            JComboBox<String> cb = vista.getCbCargo();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Cargo");
            
            for (String c : cargos) {
                if ("Cliente".equalsIgnoreCase(c) || "Programador".equalsIgnoreCase(c) || "Tecnico".equalsIgnoreCase(c) || "Técnico".equalsIgnoreCase(c)) {
                    cb.addItem(c.trim());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error al cargar Cargos: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los cargos desde la BD.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
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

    private void cargarTicketsSegunRol(int idUsuario, String cargo) {
        if (idUsuario == 0) return;
        
        try {
            List<Solicitud> solicitudes;

            if ("Programador".equalsIgnoreCase(cargo) || "Tecnico".equalsIgnoreCase(cargo) || "Técnico".equalsIgnoreCase(cargo)) {
                // Soporte ve tickets asignados a su ID
                solicitudes = obtenerSolicitudesAsignadasPorId(idUsuario);
            } else if ("Cliente".equalsIgnoreCase(cargo)) {
                // Cliente ve tickets creados por su ID
                solicitudes = obtenerSolicitudesPorUsuarioId(idUsuario);
            } else {
                return;
            }

            JComboBox<String> cb = vista.getCbNTicket();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Ticket");

            for (Solicitud s : solicitudes) {
                cb.addItem(s.getTicket().getNumeroTicket().trim());
            }
        } catch (SQLException ex) {
            System.err.println("Error al cargar Tickets: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los tickets desde la BD.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
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
        // Los eventos de CbCargo y CbNombre ya no deberían ocurrir si están deshabilitados
        if (e.getSource() == vista.getCbCargo()) {
            manejarSeleccionCargo();
        } else if (e.getSource() == vista.getCbNombre()) {
            manejarSeleccionNombre();
        } else if (e.getSource() == vista.getCbNTicket()) {
            manejarSeleccionTicket();
        } else if (e.getSource() == vista.getBtnActualizarSolicitud()) {
            manejarBotonActualizar(); 
        } else if (e.getSource() == vista.getBtnCancelarSolicitud()) { 
            cancelarSolicitudDesdeVista(); 
        }
    }
    
    // Métodos para los combos que ahora están deshabilitados
    private void manejarSeleccionCargo() {
        // Implementación anterior, ahora solo se usa si se habilitan los combos de nuevo
        String cargoSeleccionado = (String) vista.getCbCargo().getSelectedItem();
        JComboBox<String> cbNombre = vista.getCbNombre();
        
        cbNombre.removeAllItems();
        vista.getCbNTicket().removeAllItems();
        vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
        limpiarDetalleCampos();

        if (cargoSeleccionado == null || cargoSeleccionado.equals(SELECCIONE_ITEM + "Cargo")) {
            cbNombre.addItem(SELECCIONE_ITEM + "Nombre");
            return;
        }

        try {
            List<Usuario> usuarios = obtenerUsuariosPorTipo(cargoSeleccionado.trim());
            cbNombre.addItem(SELECCIONE_ITEM + "Nombre");
            
            for (Usuario u : usuarios) {
                String nombreCompleto = u.getNombres().trim() + " " + u.getApellidos().trim();
                cbNombre.addItem(nombreCompleto);
            }
        } catch (SQLException ex) {
            System.err.println("Error al cargar Nombres por Cargo: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los nombres desde la BD.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manejarSeleccionNombre() {
        // Implementación anterior, ahora solo se usa si se habilitan los combos de nuevo
        String cargoSeleccionado = (String) vista.getCbCargo().getSelectedItem();
        String nombreSeleccionado = (String) vista.getCbNombre().getSelectedItem();
        JComboBox<String> cbTicket = vista.getCbNTicket();
        
        cbTicket.removeAllItems();
        limpiarDetalleCampos();

        if (nombreSeleccionado == null || nombreSeleccionado.equals(SELECCIONE_ITEM + "Nombre")) {
            cbTicket.addItem(SELECCIONE_ITEM + "Ticket");
            return;
        }

        try {
            int idUsuarioSeleccionado = obtenerIdUsuarioPorNombreCompleto(nombreSeleccionado);
            if (idUsuarioSeleccionado == 0) {
                JOptionPane.showMessageDialog(vista, "Error: No se encontró el ID del usuario seleccionado.", "Error Interno", JOptionPane.ERROR_MESSAGE);
                cbTicket.addItem(SELECCIONE_ITEM + "Ticket");
                return;
            }
            
            // Cargar tickets según el ID y el rol seleccionado
            cargarTicketsSegunRol(idUsuarioSeleccionado, cargoSeleccionado);
            
        } catch (SQLException ex) {
            System.err.println("Error al cargar Tickets por Nombre: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar los tickets para el usuario seleccionado.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
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
                
                if (vista.getTxtNivelPrioridad().isVisible()) {
                    vista.getTxtNivelPrioridad().setText(solicitudActual.getTicket().getEstadoTicket().getNivelPrioridad().trim());
                } else {
                    vista.getTxtNivelPrioridad().setText("");
                }
                
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
    // MANEJADOR DEL BOTÓN ACTUALIZAR Y CANCELAR (Lógica de la UI) - ¡PÚBLICOS!
    // ----------------------------------------------------------------------
    
    /**
     * Maneja el evento de actualización de estado para el personal de soporte.
     * Debe ser PUBLIC para ser llamado desde la Vista.
     */
    public void manejarBotonActualizar() {
        if (solicitudActual == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un ticket para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String cargo = SesionActual.usuarioActual.getTipoUsuario().getCargo().trim();
        if ("Cliente".equalsIgnoreCase(cargo)) {
            JOptionPane.showMessageDialog(vista, "Los Clientes solo pueden cancelar una solicitud, no actualizarla.", "Permiso Denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nuevoEstadoStr = (String) vista.getCbEstadoSolicitud().getSelectedItem();

        if (nuevoEstadoStr == null || nuevoEstadoStr.equals(SELECCIONE_ITEM + "Estado")) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un nuevo estado para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ejecutarActualizacion(nuevoEstadoStr);
    }
    
    /**
     * Maneja el evento de cancelación de solicitud para el cliente.
     * Debe ser PUBLIC para ser llamado desde la Vista.
     */
    public void cancelarSolicitudDesdeVista() {
        if (solicitudActual == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un ticket para cancelar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String estadoActual = solicitudActual.getEstadoSolicitud().getEstadoSolicitud().trim();
        if (ESTADO_CANCELADO_NOMBRE.equalsIgnoreCase(estadoActual) || "Finalizado".equalsIgnoreCase(estadoActual) || "Cerrado".equalsIgnoreCase(estadoActual)) {
              JOptionPane.showMessageDialog(vista, "Esta solicitud ya está en estado '" + estadoActual + "' y no se puede cancelar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
              return;
        }
        
        String cargo = SesionActual.usuarioActual.getTipoUsuario().getCargo().trim();
        if (!"Cliente".equalsIgnoreCase(cargo)) {
              JOptionPane.showMessageDialog(vista, "Solo el cliente puede cancelar su solicitud.", "Permiso Denegado", JOptionPane.ERROR_MESSAGE);
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
                adaptarVistaSegunRol(); 
            } else {
                JOptionPane.showMessageDialog(vista, "Fallo al actualizar el estado en la base de datos.", "Error de BD", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Ocurrió un error al intentar actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Métodos de Navegación --- - ¡PÚBLICOS!

    /**
     * Muestra la vista.
     * Debe ser PUBLIC para ser llamado desde los JFrames de menú.
     */
    public void iniciar() {
        vista.setVisible(true);
        vista.setTitle("Seguimiento y Actualización de Solicitudes");
        vista.setLocationRelativeTo(null);
    }

    /**
     * Cierra la vista actual y regresa a la ventana de origen.
     * Debe ser PUBLIC para ser llamado desde la Vista.
     */
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
    // --- MÉTODOS DE ACCESO A DATOS (DAO)
    // ----------------------------------------------------------------------
    
    /**
     * Resuelve el ID numérico de la base de datos a partir del identificador de texto.
     */
    private int obtenerIdUsuarioNumerico(String identificador) throws SQLException {
        int idUsuario = 0;
        String sql = "SELECT u.IDUSUARIO FROM USUARIO u JOIN PASWORD p ON u.IDPASWORD = p.IDPASWORD WHERE LOWER(TRIM(p.IDENTIFICADOR)) = LOWER(TRIM(?))";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identificador);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("IDUSUARIO");
                }
            }
        }
        return idUsuario;
    }
    
    private List<EstadoSolicitud> obtenerEstadosSolicitud() throws SQLException {
        List<EstadoSolicitud> estados = new ArrayList<>();
        String sql = "SELECT estadosolicitud FROM ESTADO_SOLICITUD";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
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
    
    /**
     * Obtiene solicitudes creadas por un ID de Usuario (usado para Cliente).
     */
    private List<Solicitud> obtenerSolicitudesPorUsuarioId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE s.idusuario = ? AND es.estadosolicitud NOT IN ('Cancelado', 'Finalizado')"; 
        

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
     * Obtiene solicitudes asignadas a un ID de Usuario (usado para Soporte: Programador/Técnico).
     */
    private List<Solicitud> obtenerSolicitudesAsignadasPorId(int idUsuario) throws SQLException {
        List<Solicitud> solicitudes = new ArrayList<>();
        String sql = "SELECT s.idsolicitud, s.idestadosolicitud, s.idtiposervicio, s.idticket, s.fechacreacion, s.descripcion, "
                + "t.numeroticket, t.idestadoticket, et.nivelprioridad, ts.nombreservicio, es.estadosolicitud "
                + "FROM SOLICITUD s "
                + "JOIN TICKET t ON s.idticket = t.idticket "
                + "JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket "
                + "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio "
                + "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud "
                + "WHERE t.idusuario = ? AND es.estadosolicitud NOT IN ('Cancelado', 'Finalizado')"; 
        

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
    
    /**
     * Obtiene todos los cargos de la tabla TIPO_USUARIO.
     */
    private List<String> obtenerTiposUsuarioParaCargos() throws SQLException {
        List<String> cargos = new ArrayList<>();
        String sql = "SELECT DISTINCT cargo FROM TIPO_USUARIO ORDER BY cargo";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cargos.add(rs.getString("cargo").trim());
            }
        }
        return cargos;
    }
    
    /**
     * Obtiene la lista de usuarios (nombre y apellido) para un cargo específico.
     */
    private List<Usuario> obtenerUsuariosPorTipo(String cargo) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.nombres, u.apellidos FROM USUARIO u JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario WHERE LOWER(TRIM(tu.cargo)) = LOWER(TRIM(?)) ORDER BY u.nombres";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cargo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setNombres(rs.getString("nombres"));
                    u.setApellidos(rs.getString("apellidos"));
                    usuarios.add(u);
                }
            }
        }
        return usuarios;
    }
    
    /**
     * Resuelve el ID del usuario buscando por su nombre completo.
     */
    private int obtenerIdUsuarioPorNombreCompleto(String nombreCompleto) throws SQLException {
        int idUsuario = 0;
        // Se asume que el nombre completo viene como "Nombre Apellido"
        String[] partes = nombreCompleto.split(" ", 2); 
        if (partes.length < 2) return 0; 

        String sql = "SELECT idusuario FROM USUARIO WHERE LOWER(TRIM(nombres)) = LOWER(TRIM(?)) AND LOWER(TRIM(apellidos)) = LOWER(TRIM(?))";
        
        try (Connection conn = ConexionBD.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, partes[0].trim());
            pstmt.setString(2, partes[1].trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("idusuario");
                }
            }
        }
        return idUsuario;
    }
}