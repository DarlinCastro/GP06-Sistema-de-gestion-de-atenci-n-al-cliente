/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_vista.jFrameSeguimientoSolicitud;
import capa_vista.jFrameMenuTecnico; 
import base_datos.SolicitudDAO;
import entidades.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.util.Optional;

public class SolicitudController implements ActionListener{

    private final jFrameSeguimientoSolicitud vista;
    private final SolicitudDAO dao;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Usuario usuarioSeleccionado;
    private Solicitud solicitudActual;  
    
    // Lista de estados cargada una sola vez para llenar el ComboBox al seleccionar un ticket.
    private List<EstadoSolicitud> listaTodosLosEstados; 

    public SolicitudController(jFrameSeguimientoSolicitud vista) {
        this.vista = vista;
        this.dao = new SolicitudDAO();
        this.vista.setControlador(this); 
        inicializarComponentes(); 
        cargarDatosIniciales(); 
    }

// -----------------------------------------------------------------------------
// --- Inicialización y Carga de Datos ---
// -----------------------------------------------------------------------------
    private void inicializarComponentes() {

        vista.getCbCargo().addActionListener(this); 
        vista.getCbNombre().addActionListener(this);
        vista.getCbNTicket().addActionListener(this);
        vista.getTxtFechaCreacion().setEditable(false);
        vista.getTxtTipoServicio().setEditable(false);
        vista.getTxtDescripcion().setEditable(false);
        vista.getTxtNivelPrioridad().setEditable(false);
    }

    private void cargarDatosIniciales() {
        cargarCargos();
        
        // CORRECCIÓN CLAVE 1: Inicializamos la lista de estados aquí, 
        // pero NO cargamos el ComboBox de la Vista.
        cargarListaTodosLosEstados(); 
        
        // Limpia combos dependientes al inicio
        vista.getCbNombre().removeAllItems();
        vista.getCbNTicket().removeAllItems();
        vista.getCbNombre().addItem("Seleccione Nombre");
        vista.getCbNTicket().addItem("Seleccione Ticket");
        
        // CORRECCIÓN CLAVE 2: Aseguramos que el combo de Estado de Solicitud esté vacío
        vista.getCbEstadoSolicitud().removeAllItems(); 
    }

    private void cargarCargos() {
        try {
            List<TipoUsuario> cargos = dao.obtenerCargos();
            JComboBox<String> cb = vista.getCbCargo();
            cb.removeAllItems();
            cb.addItem("Seleccione Cargo"); 
            for (TipoUsuario tu : cargos) {
                cb.addItem(tu.toString().trim());
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar Cargos: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar Cargos desde la BD.", "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarListaTodosLosEstados() {
        // Obtenemos la lista completa de estados desde la BD y la guardamos
        try {
            listaTodosLosEstados = dao.obtenerEstadosSolicitud();
        } catch (Exception ex) {
            System.err.println("Error al cargar Lista de Estados: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar estados de solicitud.", "Error de Carga", JOptionPane.ERROR_MESSAGE);
            listaTodosLosEstados = null;
        }
    }
    
    private void limpiarVista() {
        // 1. Resetear/limpiar campos de texto (detalle de la solicitud)
        vista.getTxtFechaCreacion().setText("");
        vista.getTxtTipoServicio().setText("");
        vista.getTxtDescripcion().setText("");
        vista.getTxtNivelPrioridad().setText("");

        // 2. Resetear el estado de la solicitud cargada
        solicitudActual = null;
        usuarioSeleccionado = null;

        // 3. Resetear ComboBoxes dependientes (Nombre y Ticket)
        vista.getCbNombre().removeAllItems();
        vista.getCbNTicket().removeAllItems();
        vista.getCbNombre().addItem("Seleccione Nombre");
        vista.getCbNTicket().addItem("Seleccione Ticket");
        
        // 4. CORRECCIÓN CLAVE 3: Asegurar que el combo de Estado esté VACÍO
        vista.getCbEstadoSolicitud().removeAllItems();

        // 5. Resetear Cargo y FORZAR la limpieza de dependencias.
        if (vista.getCbCargo().getItemCount() > 0) {
            vista.getCbCargo().setSelectedIndex(0);
        }
        
        manejarSeleccionCargo(); 
    }
        @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getCbCargo()) {
            manejarSeleccionCargo();
        } else if (e.getSource() == vista.getCbNombre()) {
            manejarSeleccionNombre();
        } else if (e.getSource() == vista.getCbNTicket()) {
            manejarSeleccionTicket();
        } 
        // El botón Actualizar/Cancelar es llamado directamente por la Vista (ver abajo).
    }
    
    // ... (manejarSeleccionCargo y manejarSeleccionNombre sin cambios) ...

    private void manejarSeleccionCargo() {
        String cargoSeleccionado = (String) vista.getCbCargo().getSelectedItem();
        if (cargoSeleccionado == null || cargoSeleccionado.trim().isEmpty() || cargoSeleccionado.equals("Seleccione Cargo")) {
            vista.getCbNombre().removeAllItems();
            vista.getCbNTicket().removeAllItems();
            vista.getCbNombre().addItem("Seleccione Nombre");
            vista.getCbNTicket().addItem("Seleccione Ticket");
            limpiarDetalleCampos();
            return;
        }

        try {
            List<Usuario> usuarios = dao.obtenerUsuariosPorCargo(cargoSeleccionado);
            JComboBox<String> cb = vista.getCbNombre();
            cb.removeAllItems();
            cb.addItem("Seleccione Nombre");
            for (Usuario u : usuarios) {
                cb.addItem(u.toString().trim());
            }
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem("Seleccione Ticket");
            limpiarDetalleCampos();
        } catch (Exception ex) {
            System.err.println("Error al cargar Nombres: " + ex.getMessage());
        }
    }

    private void manejarSeleccionNombre() {
        String nombreCompletoSeleccionado = (String) vista.getCbNombre().getSelectedItem();
        String cargo = (String) vista.getCbCargo().getSelectedItem();

        if (nombreCompletoSeleccionado == null || nombreCompletoSeleccionado.trim().isEmpty() || nombreCompletoSeleccionado.equals("Seleccione Nombre")) {
            vista.getCbNTicket().removeAllItems();
            vista.getCbNTicket().addItem("Seleccione Ticket");
            limpiarDetalleCampos();
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = dao.obtenerUsuariosPorCargo(cargo)
                    .stream()
                    .filter(u -> u.toString().trim().equals(nombreCompletoSeleccionado.trim()))
                    .findFirst();

            if (usuarioOpt.isEmpty()) {
                usuarioSeleccionado = null;
                return;
            }

            usuarioSeleccionado = usuarioOpt.get();

            List<Solicitud> solicitudes = dao.obtenerSolicitudesPorUsuario(usuarioSeleccionado);
            JComboBox<String> cb = vista.getCbNTicket();
            cb.removeAllItems();
            cb.addItem("Seleccione Ticket");

            for (Solicitud s : solicitudes) {
                cb.addItem(s.getTicket().getNumeroTicket().trim());
            }
            solicitudActual = null; 
            limpiarDetalleCampos();
        } catch (Exception ex) {
            System.err.println("Error al cargar Tickets: " + ex.getMessage());
        }
    }

    private void manejarSeleccionTicket() {
        String numeroTicket = (String) vista.getCbNTicket().getSelectedItem();
        
        // Si se deselecciona el ticket o es el placeholder
        if (numeroTicket == null || numeroTicket.trim().isEmpty() || numeroTicket.equals("Seleccione Ticket")) {
            solicitudActual = null;
            limpiarDetalleCampos();
            return;
        }

        try {
            solicitudActual = dao.obtenerSolicitudPorTicket(numeroTicket.trim());

            if (solicitudActual != null) {
                // Rellenar campos de lectura
                vista.getTxtFechaCreacion().setText(dateFormat.format(solicitudActual.getFechaCreacion()));
                vista.getTxtTipoServicio().setText(solicitudActual.getTipoServicio().toString().trim());
                vista.getTxtDescripcion().setText(solicitudActual.getDescripcion().trim());
                vista.getTxtNivelPrioridad().setText(solicitudActual.getTicket().getEstadoTicket().toString().trim()); 
                
                // CORRECCIÓN CLAVE 4: Llenar el ComboBox y seleccionar el estado
                llenarYCargarEstadoSolicitud(solicitudActual.getEstadoSolicitud().toString().trim());
                
            } else {
                 limpiarDetalleCampos();
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar datos del Ticket: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error al cargar detalles del Ticket.", "Error BD", JOptionPane.ERROR_MESSAGE);
            solicitudActual = null;
            limpiarDetalleCampos();
        }
    }
    
    /**
     * Llena el ComboBox de Estado y selecciona el estado actual del ticket.
     */
    private void llenarYCargarEstadoSolicitud(String estadoActual) {
        JComboBox<String> cb = vista.getCbEstadoSolicitud();
        cb.removeAllItems();
        
        if (listaTodosLosEstados != null) {
             // Agregamos un placeholder inicial, aunque la lógica de actualización lo valide
            cb.addItem("Seleccione Estado"); 
            for (EstadoSolicitud es : listaTodosLosEstados) {
                cb.addItem(es.toString().trim());
            }
            // Seleccionamos el estado que vino de la base de datos
            cb.setSelectedItem(estadoActual);
        }
    }
    
    // Método auxiliar para limpiar solo los campos de detalle
    private void limpiarDetalleCampos(){
        vista.getTxtFechaCreacion().setText("");
        vista.getTxtTipoServicio().setText("");
        vista.getTxtDescripcion().setText("");
        vista.getTxtNivelPrioridad().setText("");
        solicitudActual = null;
        
        // CORRECCIÓN CLAVE 5: Vaciar el combo de estado al limpiar los detalles
        vista.getCbEstadoSolicitud().removeAllItems();
    }

// -----------------------------------------------------------------------------
// --- Lógica de Actualización (Botones) ---
// -----------------------------------------------------------------------------
    private void manejarActualizarSolicitud() {
        if (solicitudActual == null || solicitudActual.getTicket() == null) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un ticket para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nuevoEstadoString = (String) vista.getCbEstadoSolicitud().getSelectedItem();
        String numeroTicket = solicitudActual.getTicket().getNumeroTicket().trim();

        // Validación de que se haya seleccionado un estado válido.
        if (nuevoEstadoString == null || nuevoEstadoString.trim().isEmpty() || nuevoEstadoString.equals("Seleccione Estado")) {  
            JOptionPane.showMessageDialog(vista, "Seleccione un estado válido para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String estadoAEnviar = nuevoEstadoString.trim();

        try {
            dao.actualizarEstadoSolicitud(numeroTicket, estadoAEnviar);

            JOptionPane.showMessageDialog(vista, "Estado de Solicitud actualizado con éxito a '" + estadoAEnviar + "'.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // LIMPIEZA DESPUÉS DEL ÉXITO, volviendo al estado de búsqueda (combo de estado se vacía).
            limpiarVista(); 

        } catch (SQLException ex) {
            System.err.println("Error al actualizar estado de solicitud (Controller): " + ex.getMessage());
            JOptionPane.showMessageDialog(vista, "Error de base de datos al actualizar el estado: " + ex.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarDesdeVista() {
        manejarActualizarSolicitud();
    }

    public void cancelarSolicitudDesdeVista() {
        if (solicitudActual == null || solicitudActual.getTicket() == null) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un ticket para cancelar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String ESTADO_CANCELADO = "Cancelado";
        String numeroTicket = solicitudActual.getTicket().getNumeroTicket().trim();

        try {
            dao.actualizarEstadoSolicitud(numeroTicket, ESTADO_CANCELADO);

            JOptionPane.showMessageDialog(vista, "Solicitud N° " + numeroTicket + " ha sido CANCELADA con éxito.", "Éxito de Cancelación", JOptionPane.INFORMATION_MESSAGE);
            
            // LIMPIEZA DESPUÉS DEL ÉXITO
            limpiarVista(); 

        } catch (SQLException ex) {
            System.err.println("Error al cancelar solicitud: " + ex.getMessage());
            JOptionPane.showMessageDialog(vista,
                    "Error al cancelar la solicitud: " + ex.getMessage(),
                    "Error de Cancelación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

// -----------------------------------------------------------------------------
// --- Navegación ---
// -----------------------------------------------------------------------------
    public void iniciar() {
        vista.setVisible(true);
        vista.setTitle("Seguimiento y Actualización de Solicitudes");
        vista.setLocationRelativeTo(null);
    }

    public void irAtras() {
        vista.dispose();
        jFrameMenuTecnico vTecnico = new jFrameMenuTecnico(); 
        vTecnico.setVisible(true);
    }
}