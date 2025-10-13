package capa_controladora;

import capa_vista.jFrameSeguimientoSolicitud;
import capa_vista.jFrameMenuTecnico; // Ya no se crea aquí, solo se importa
// import capa_vista.jFrameMenuCliente; // No se necesita importar si solo usamos JFrame
import base_datos.SolicitudDAO;
import entidades.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.util.Date;
import java.util.ArrayList;
import javax.swing.JFrame; 
import java.sql.SQLException;

public class SolicitudController implements ActionListener {

    private final jFrameSeguimientoSolicitud vista;
    private final SolicitudDAO dao;
    private final JFrame ventanaOrigen; // <--- CORRECCIÓN 1: Referencia a la ventana anterior
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Solicitud solicitudActual;
    private List<EstadoSolicitud> listaTodosLosEstados;

    private static final String ESTADO_CANCELADO_NOMBRE = "Cancelado";
    private static final String SELECCIONE_ITEM = "Seleccione ";

    // <--- CORRECCIÓN 2: Constructor modificado para recibir la ventana de origen
    public SolicitudController(jFrameSeguimientoSolicitud vista, JFrame ventanaOrigen) {
        this.vista = vista;
        this.dao = new SolicitudDAO();
        this.ventanaOrigen = ventanaOrigen; // <--- Asigna la referencia
        this.vista.setControlador(this);
        inicializarComponentes();
        cargarDatosIniciales();
    }

    private void inicializarComponentes() {
        
        // Enlazar botones y combos a este controlador
        vista.getCbCargo().addActionListener(this);
        vista.getCbNombre().addActionListener(this);
        vista.getCbNTicket().addActionListener(this);
        vista.getBtnActualizarSolicitud().addActionListener(this);

        // Bloquear campos de solo lectura
        vista.getTxtFechaCreacion().setEditable(false);
        vista.getTxtTipoServicio().setEditable(false);
        vista.getTxtDescripcion().setEditable(false);
        vista.getTxtNivelPrioridad().setEditable(false);
    }

    /**
     * Resetea la interfaz de usuario a su estado inicial.
     */
    private void cargarDatosIniciales() {
        // Carga de datos de la base de datos
        cargarCargos();
        cargarListaTodosLosEstados();

        // Reseteo de ComboBoxes dependientes
        vista.getCbNombre().removeAllItems();
        vista.getCbNTicket().removeAllItems();
        vista.getCbNombre().addItem(SELECCIONE_ITEM + "Nombre");
        vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
        vista.getCbEstadoSolicitud().removeAllItems();

        if (vista.getCbCargo().getItemCount() > 0) {
            vista.getCbCargo().setSelectedIndex(0);
        }

        // Limpieza de campos de detalle
        limpiarDetalleCampos();
    }

    private void cargarCargos() {
        try {
            List<TipoUsuario> cargos = dao.obtenerCargos();
            JComboBox<String> cb = vista.getCbCargo();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Cargo");
            for (TipoUsuario tu : cargos) {
                cb.addItem(tu.toString());
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar Cargos: " + ex.getMessage());
        }
    }

    private void cargarListaTodosLosEstados() {
        try {
            listaTodosLosEstados = dao.obtenerEstadosSolicitud();
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
            manejarBotonActualizar(); // Llamamos al manejador del botón
        }
    }

    private void manejarSeleccionCargo() {
        String cargoSeleccionado = (String) vista.getCbCargo().getSelectedItem();

        if (cargoSeleccionado == null || cargoSeleccionado.equals(SELECCIONE_ITEM + "Cargo")) {
            vista.getCbNombre().removeAllItems();
            vista.getCbNombre().addItem(SELECCIONE_ITEM + "Nombre");
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
            limpiarDetalleCampos();
            return;
        }

        try {
            List<Usuario> usuarios = dao.obtenerUsuariosPorCargo(cargoSeleccionado.trim());
            JComboBox<String> cb = vista.getCbNombre();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Nombre");

            for (Usuario u : usuarios) {
                cb.addItem(u.toString());
            }
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
            limpiarDetalleCampos();
        } catch (Exception ex) {
            System.err.println("Error al cargar Nombres: " + ex.getMessage());
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
            int idUsuario = dao.obtenerIdUsuarioPorNombre(nombreCompletoSeleccionado.trim());

            if (idUsuario == 0) {
                vista.getCbNTicket().removeAllItems();
                vista.getCbNTicket().addItem(SELECCIONE_ITEM + "Ticket");
                limpiarDetalleCampos();
                return;
            }

            List<Solicitud> solicitudes;
            String cargoTrim = cargo.trim();

            if ("Programador".equalsIgnoreCase(cargoTrim) || "Tecnico".equalsIgnoreCase(cargoTrim) || "Técnico".equalsIgnoreCase(cargoTrim)) {
                solicitudes = dao.obtenerSolicitudesAsignadasPorId(idUsuario);
            } else {
                solicitudes = dao.obtenerSolicitudesPorUsuarioId(idUsuario);
            }

            JComboBox<String> cb = vista.getCbNTicket();
            cb.removeAllItems();
            cb.addItem(SELECCIONE_ITEM + "Ticket");

            for (Solicitud s : solicitudes) {
                cb.addItem(s.getTicket().getNumeroTicket().trim());
            }
            limpiarDetalleCampos();
        } catch (Exception ex) {
            System.err.println("Error al cargar Tickets: " + ex.getMessage());
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
            solicitudActual = dao.obtenerSolicitudPorTicket(numeroTicket.trim());

            if (solicitudActual != null) {
                Date fechaCreacion = solicitudActual.getFechaCreacion();
                vista.getTxtFechaCreacion().setText(fechaCreacion != null ? dateFormat.format(fechaCreacion) : "");
                vista.getTxtTipoServicio().setText(solicitudActual.getTipoServicio().toString().trim());
                vista.getTxtDescripcion().setText(solicitudActual.getDescripcion().trim());
                vista.getTxtNivelPrioridad().setText(solicitudActual.getTicket().getEstadoTicket().toString().trim());
                llenarYCargarEstadoSolicitud(solicitudActual.getEstadoSolicitud().toString().trim());
            } else {
                limpiarDetalleCampos();
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar datos del Ticket: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar detalles del Ticket.", "Error BD", JOptionPane.ERROR_MESSAGE);
            limpiarDetalleCampos();
        }
    }

    private void llenarYCargarEstadoSolicitud(String estadoActual) {
        JComboBox<String> cb = vista.getCbEstadoSolicitud();
        cb.removeAllItems();

        if (listaTodosLosEstados != null) {
            cb.addItem(SELECCIONE_ITEM + "Estado");
            for (EstadoSolicitud es : listaTodosLosEstados) {
                cb.addItem(es.toString().trim());
            }
            cb.setSelectedItem(estadoActual);
        }
    }

    // ----------------------------------------------------------------------
    //  MANEJADOR DEL BOTÓN ACTUALIZAR
    // ----------------------------------------------------------------------
    /**
     * Valida los campos y llama a la lógica de ejecución. Garantiza una ÚNICA advertencia si los datos están incompletos.
     */
    public void manejarBotonActualizar() {

        // --- 1. VALIDACIÓN CON ADVERTENCIA ÚNICA ---
        // A. Advertencia 1: Si no hay ticket seleccionado
        if (solicitudActual == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un ticket para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nuevoEstadoStr = (String) vista.getCbEstadoSolicitud().getSelectedItem();

        // B. Advertencia 2: Si el ticket está seleccionado pero el estado no
        if (nuevoEstadoStr == null || nuevoEstadoStr.equals(SELECCIONE_ITEM + "Estado")) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un nuevo estado para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Si las validaciones pasan, se llama a la lógica de ejecución
        ejecutarActualizacion(nuevoEstadoStr);
    }

    // ----------------------------------------------------------------------
    // LÓGICA CENTRAL DE EJECUCIÓN (USADA POR ACTUALIZAR Y CANCELAR)
    // ----------------------------------------------------------------------
    /**
     * Ejecuta la actualización en la BD y maneja el flujo de éxito o error.
     */
    private void ejecutarActualizacion(String nuevoEstadoStr) {
        try {
            // 1. OBTENER IDS NECESARIOS
            int nuevoEstadoId = dao.obtenerIdEstadoPorNombre(nuevoEstadoStr);
            if (nuevoEstadoId == 0) {
                JOptionPane.showMessageDialog(vista, "Error: No se pudo encontrar el ID del estado seleccionado.", "Error Interno", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ticketNum = solicitudActual.getTicket().getNumeroTicket();
            int idSolicitud = dao.obtenerIdSolicitudPorTicket(ticketNum);

            if (idSolicitud == 0) {
                JOptionPane.showMessageDialog(vista, "Error: No se pudo encontrar el ID de la Solicitud para el ticket: " + ticketNum, "Error Interno", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Llamar al DAO para actualizar la base de datos
            boolean exito = dao.actualizarEstadoSolicitud(idSolicitud, nuevoEstadoId);

            if (exito) {
                // 3. Feedback de éxito 
                JOptionPane.showMessageDialog(vista,
                        "El estado de la solicitud #" + ticketNum + " ha sido actualizado a: " + nuevoEstadoStr,
                        "Actualización Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);

                // Se reinicia el formulario a su estado normal sin advertencias posteriores.
                cargarDatosIniciales();
                return; // Termina la ejecución después del éxito.
            } else {
                JOptionPane.showMessageDialog(vista, "Fallo al actualizar el estado en la base de datos.", "Error de BD", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "Ocurrió un error al intentar actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----------------------------------------------------------------------
    // LÓGICA DE CANCELACIÓN
    // ----------------------------------------------------------------------
    /**
     * Lógica de Cancelación: Advertencia única si falta el ticket.
     */
    public void cancelarSolicitudDesdeVista() {
        // Validación: debe haber un ticket seleccionado
        if (solicitudActual == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione un ticket para cancelar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Forzar la selección del estado "Cancelado"
        JComboBox<String> cbEstado = vista.getCbEstadoSolicitud();

        boolean encontrado = false;
        for (int i = 0; i < cbEstado.getItemCount(); i++) {
            if (ESTADO_CANCELADO_NOMBRE.equalsIgnoreCase(cbEstado.getItemAt(i))) {
                cbEstado.setSelectedIndex(i);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(vista, "El estado '" + ESTADO_CANCELADO_NOMBRE + "' no está disponible.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Llamar a la lógica de ejecución (el estado ya está forzado a "Cancelado")
        String estadoCancelado = (String) vista.getCbEstadoSolicitud().getSelectedItem();
        ejecutarActualizacion(estadoCancelado);
    }

    // --- Métodos de Navegación ---
    public void iniciar() {
        vista.setVisible(true);
        vista.setTitle("Seguimiento y Actualización de Solicitudes");
        vista.setLocationRelativeTo(null);
    }

    // <--- CORRECCIÓN 3: irAtras() ahora regresa a la ventana de origen
    public void irAtras() {
        vista.dispose();
        if (ventanaOrigen != null) {
            ventanaOrigen.setVisible(true);
            ventanaOrigen.setLocationRelativeTo(null);
        } else {
            // Manejo de error o comportamiento por defecto si no se pasó la ventana origen
            JOptionPane.showMessageDialog(null, "Error de navegación: No se encontró la ventana anterior.", "Error", JOptionPane.ERROR_MESSAGE);
            // Podrías forzar la apertura del menú técnico/login aquí si fuera estrictamente necesario
        }
    }
}