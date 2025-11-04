/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import capa_modelo.EstadoSolicitud; 
import capa_modelo.Usuario;
import capa_modelo.Ticket;
import capa_modelo.TipoUsuario;
import capa_modelo.EstadoTicket;
import capa_modelo.TipoServicio;
import capa_modelo.Solicitud;
import base_datos.ConexionBD; 
import capa_vista.jFrameAsignarSolicitud; 

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel; 

public class AsignacionController {

    private final jFrameAsignarSolicitud vista;
    private Solicitud solicitudSeleccionada;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private List<Solicitud> listaTodasSolicitudes;
    private List<String> listaTodosCargos;
    private List<Usuario> listaTodosTecnicos; 

    // --- SQL CONSTANTES ---
    private static final String SQL_OBTENER_CARGOS = "SELECT DISTINCT cargo FROM TIPO_USUARIO";
    private static final String SQL_OBTENER_TECNICOS_POR_CARGO = "SELECT u.nombres, u.apellidos, tu.cargo FROM USUARIO u JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario WHERE tu.cargo = ?";
    private static final String SQL_OBTENER_ID_TECNICO_BY_NAME = 
        "SELECT idusuario FROM USUARIO WHERE nombres = ? AND apellidos = ?";

    private static final String SQL_OBTENER_SOLICITUDES = 
        "SELECT s.idsolicitud, s.fechacreacion, s.descripcion, t.numeroticket, t.fechaasignacion, " + 
        "   u.idusuario AS idtecnico, u.nombres AS tecnico_nombres, u.apellidos AS tecnico_apellidos, " +
        "   tu.cargo AS tecnico_cargo, t.idestadoticket, es.estadosolicitud, et.nivelprioridad, ts.nombreservicio " + 
        "FROM SOLICITUD s " +
        "JOIN TICKET t ON s.idticket = t.idticket " +
        "JOIN ESTADO_SOLICITUD es ON s.idestadosolicitud = es.idestadosolicitud " +
        "JOIN TIPO_SERVICIO ts ON s.idtiposervicio = ts.idtiposervicio " +
        "LEFT JOIN ESTADO_TICKET et ON t.idestadoticket = et.idestadoticket " + 
        "LEFT JOIN USUARIO u ON t.idusuario = u.idusuario " + 
        "LEFT JOIN TIPO_USUARIO tu ON u.idtipousuario = tu.idtipousuario " + 
        "ORDER BY s.idsolicitud DESC";

    private static final String SQL_ID_PRIORIDAD = "SELECT idestadoticket FROM ESTADO_TICKET WHERE TRIM(nivelprioridad) = ?";
    private static final String SQL_ID_ESTADO_SOLICITUD = "SELECT idestadosolicitud FROM ESTADO_SOLICITUD WHERE TRIM(estadosolicitud) = ?";
    
    private static final String SQL_UPDATE_TICKET = "UPDATE TICKET SET idestadoticket = ?, idusuario = ?, fechaasignacion = ? WHERE numeroticket = ?";
    private static final String SQL_UPDATE_SOLICITUD = "UPDATE SOLICITUD SET idestadosolicitud = ? WHERE idticket = (SELECT idticket FROM TICKET WHERE numeroticket = ?)";
    
    private static final String SQL_OBTENER_PRIORIDADES = "SELECT NIVELPRIORIDAD FROM ESTADO_TICKET";
    private static final String SQL_OBTENER_ESTADOS_SOLICITUD = "SELECT ESTADOSOLICITUD FROM ESTADO_SOLICITUD";


    public AsignacionController(jFrameAsignarSolicitud vista) {
        this.vista = vista;
        this.solicitudSeleccionada = null;
        this.listaTodosTecnicos = new ArrayList<>();
    }
    
    // ----------------------------------------------------
    // MÉTODOS DAL/BLL (ACCESO A DATOS Y LÓGICA)
    // ----------------------------------------------------
    
    private int obtenerId(Connection conn, String sql, String valor) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valor.trim()); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Error: No se encontró ID para el valor: " + valor);
    }
    
    private int obtenerIdTecnico(Connection conn, String nombres, String apellidos) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_ID_TECNICO_BY_NAME)) {
            ps.setString(1, nombres.trim()); 
            ps.setString(2, apellidos.trim()); 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idusuario");
                }
            }
        }
        throw new SQLException("Error: No se encontró ID para el técnico: " + nombres + " " + apellidos);
    }
    
    private Solicitud buscarSolicitudPorTicket(String numeroTicket) {
        if (numeroTicket == null || listaTodasSolicitudes == null) return null;
        
        for (Solicitud s : listaTodasSolicitudes) {
            if (s.getTicket().getNumeroTicket().equals(numeroTicket)) {
                return s;
            }
        }
        return null;
    }
    
    private Usuario buscarTecnicoPorNombre(String nombreCompleto) {
        if (nombreCompleto == null || listaTodosTecnicos == null) return null;
        
        String[] partes = nombreCompleto.split(" ", 2);
        if (partes.length < 2) return null;

        String nombres = partes[0].trim();
        String apellidos = partes[1].trim();
        
        for (Usuario u : listaTodosTecnicos) {
            if (u.getNombres().trim().equals(nombres) && u.getApellidos().trim().equals(apellidos)) {
                return u;
            }
        }
        return null;
    }

    private List<String> obtenerCargos() {
         List<String> cargos = new ArrayList<>();
         try (Connection conn = ConexionBD.conectar();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(SQL_OBTENER_CARGOS)) {

             if (conn == null) return cargos;

             while (rs.next()) {
                 cargos.add(rs.getString("cargo").trim());
             }
         } catch (SQLException e) {
             System.err.println("Error al obtener cargos: " + e.getMessage());
         }
         return cargos;
    }
    
    private List<String> obtenerPrioridades() {
         List<String> prioridades = new ArrayList<>();
         try (Connection conn = ConexionBD.conectar();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(SQL_OBTENER_PRIORIDADES)) {

             if (conn == null) return prioridades;

             while (rs.next()) {
                 prioridades.add(rs.getString("NIVELPRIORIDAD").trim());
             }
         } catch (SQLException e) {
             System.err.println("Error al obtener prioridades: " + e.getMessage());
         }
         return prioridades;
    }

    private List<String> obtenerEstadosSolicitud() {
         List<String> estados = new ArrayList<>();
         try (Connection conn = ConexionBD.conectar();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(SQL_OBTENER_ESTADOS_SOLICITUD)) {

             if (conn == null) return estados;

             while (rs.next()) {
                 estados.add(rs.getString("ESTADOSOLICITUD").trim());
             }
         } catch (SQLException e) {
             System.err.println("Error al obtener estados de solicitud: " + e.getMessage());
         }
         return estados;
    }

    private List<String> obtenerNombresTecnicosPorCargo(String cargo) {
        List<String> nombresTecnicos = new ArrayList<>();
        listaTodosTecnicos.clear(); 
        String cargoPadded = String.format("%-10s", cargo); 

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_TECNICOS_POR_CARGO)) {

            if (conn == null) return nombresTecnicos;

            ps.setString(1, cargoPadded);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    
                    TipoUsuario tipo = new TipoUsuario(rs.getString("cargo").trim()); 
                    
                    Usuario tecnico = new Usuario();
                    tecnico.setNombres(rs.getString("nombres").trim());
                    tecnico.setApellidos(rs.getString("apellidos").trim());
                    tecnico.setTipoUsuario(tipo);
                    
                    listaTodosTecnicos.add(tecnico); 
                    
                    nombresTecnicos.add(tecnico.getNombres().trim() + " " + tecnico.getApellidos().trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener técnicos por cargo: " + e.getMessage());
        }
        return nombresTecnicos;
    }
    
    private List<Solicitud> obtenerTodasSolicitudes() {
        List<Solicitud> solicitudes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_OBTENER_SOLICITUDES)) {

            if (conn == null) return solicitudes;

            while (rs.next()) {
                EstadoSolicitud estado = new EstadoSolicitud(rs.getString("estadosolicitud").trim());
                TipoServicio servicio = new TipoServicio(rs.getString("nombreservicio").trim());
                
                Usuario tecnicoAsignado = null;
                Date fechaAsignacion = rs.getDate("fechaasignacion");
                EstadoTicket nivelPrioridad = null;

                if (rs.getString("nivelprioridad") != null) {
                    nivelPrioridad = new EstadoTicket(rs.getString("nivelprioridad").trim());
                }
                
                if (rs.getInt("idtecnico") != 0) {
                    TipoUsuario tipoTecnico = new TipoUsuario(rs.getString("tecnico_cargo").trim());
                    tecnicoAsignado = new Usuario();
                    tecnicoAsignado.setNombres(rs.getString("tecnico_nombres").trim());
                    tecnicoAsignado.setApellidos(rs.getString("tecnico_apellidos").trim());
                    tecnicoAsignado.setTipoUsuario(tipoTecnico);
                }

                Ticket ticket = new Ticket(nivelPrioridad, fechaAsignacion, rs.getString("numeroticket").trim(), tecnicoAsignado);
                
                Solicitud s = new Solicitud(null, servicio, estado, ticket, rs.getDate("fechacreacion"), rs.getString("descripcion").trim());
                solicitudes.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener solicitudes: " + e.getMessage());
        }
        return solicitudes;
    }
    
    private boolean ejecutarAsignacion(Solicitud solicitud) {
        Connection conn = ConexionBD.conectar();
        if (conn == null) return false;

        boolean exito = false;

        try {
            conn.setAutoCommit(false); 
            
            String prioridad = solicitud.getTicket().getEstadoTicket().getNivelPrioridad();
            String estado = solicitud.getEstadoSolicitud().getEstadoSolicitud();
            Usuario tecnicoAsignado = solicitud.getTicket().getTecnicoAsignado();
            
            int idPrioridad = obtenerId(conn, SQL_ID_PRIORIDAD, prioridad);
            int idNuevoEstado = obtenerId(conn, SQL_ID_ESTADO_SOLICITUD, estado);
            
            int idTecnico = obtenerIdTecnico(conn, tecnicoAsignado.getNombres(), tecnicoAsignado.getApellidos());

            try (PreparedStatement psTicket = conn.prepareStatement(SQL_UPDATE_TICKET)) {
                psTicket.setInt(1, idPrioridad);
                psTicket.setInt(2, idTecnico); 
                psTicket.setDate(3, new java.sql.Date(solicitud.getTicket().getFechaAsignacion().getTime())); 
                psTicket.setString(4, solicitud.getTicket().getNumeroTicket());
                psTicket.executeUpdate();
            }

            try (PreparedStatement psSolicitud = conn.prepareStatement(SQL_UPDATE_SOLICITUD)) {
                psSolicitud.setInt(1, idNuevoEstado);
                psSolicitud.setString(2, solicitud.getTicket().getNumeroTicket());
                psSolicitud.executeUpdate();
            }

            conn.commit(); 
            exito = true;

        } catch (SQLException e) {
            System.err.println("Error en la transacción de asignación: " + e.getMessage());
            try {
                conn.rollback(); 
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        return exito;
    }
    
    // ----------------------------------------------------
    // LÓGICA DE CONTROL DE VISTA
    // ----------------------------------------------------
    
    private void limpiarCamposAsignacion() {
        this.vista.getTxtFechaCreacion().setText("");
        this.vista.getTxtDescripcion().setText("");
        this.vista.getTxtTipoServicio().setText("");
        this.vista.getTxtFechaAsignacion().setText("");
        
        this.vista.getCbNombre().setModel(new DefaultComboBoxModel<>()); 
        
        // Vuelve a poner el texto de selección en todos los ComboBox
        this.vista.getCbCargo().setSelectedIndex(0);
        this.vista.getCbNivelPrioridad().setSelectedIndex(0);
        this.vista.getCbEstadoSolicitud().setSelectedIndex(0);
        
        // Reinicio del ComboBox del Ticket
        this.vista.getCbTicket().setSelectedIndex(0); 
        
        this.solicitudSeleccionada = null;
    }


    public void inicializarVista() {
        try {
            listaTodasSolicitudes = obtenerTodasSolicitudes();
            listaTodosCargos = obtenerCargos(); 

            // 1. Carga ComboBox Cargo 
            DefaultComboBoxModel<String> modeloCargos = new DefaultComboBoxModel<>();
            modeloCargos.addElement("-- Seleccione Cargo --"); 
            for (String cargo : listaTodosCargos) {
                modeloCargos.addElement(cargo);
            }
            this.vista.getCbCargo().setModel(modeloCargos);
            
            // 2. Carga ComboBox Ticket 
            DefaultComboBoxModel<String> modeloTickets = new DefaultComboBoxModel<>();
            modeloTickets.addElement("-- Seleccione Ticket --"); 
            for (Solicitud s : listaTodasSolicitudes) {
                modeloTickets.addElement(s.getTicket().getNumeroTicket());
            }
            this.vista.getCbTicket().setModel(modeloTickets);

            // 3. Carga ComboBox de Nivel de Prioridad
            List<String> prioridades = obtenerPrioridades();
            DefaultComboBoxModel<String> modeloPrioridad = new DefaultComboBoxModel<>();
            modeloPrioridad.addElement("-- Seleccione Prioridad --"); 
            for (String p : prioridades) {
                modeloPrioridad.addElement(p);
            }
            this.vista.getCbNivelPrioridad().setModel(modeloPrioridad);
            
            // 4. Carga ComboBox de Estado de Solicitud
            List<String> estados = obtenerEstadosSolicitud();
            DefaultComboBoxModel<String> modeloEstados = new DefaultComboBoxModel<>();
            modeloEstados.addElement("-- Seleccione Estado --"); 
            for (String e : estados) {
                modeloEstados.addElement(e);
            }
            this.vista.getCbEstadoSolicitud().setModel(modeloEstados);

            cargarTabla();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al cargar datos iniciales: " + e.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public void manejarSeleccionTicket(String numeroTicket) {
        if (numeroTicket == null || numeroTicket.equals("-- Seleccione Ticket --")) {
            limpiarCamposAsignacion();
            return;
        }
        
        Solicitud s = buscarSolicitudPorTicket(numeroTicket); 
        if (s == null) {
            limpiarCamposAsignacion();
            return;
        }
        
        this.solicitudSeleccionada = s;
        Ticket t = s.getTicket();

        this.vista.getTxtFechaCreacion().setText(dateFormat.format(s.getFechaCreacion()));
        this.vista.getTxtDescripcion().setText(s.getDescripcion());
        this.vista.getTxtTipoServicio().setText(s.getTipoServicio().getNombreServicio().trim());
        
        // ** RELLENO DE DATOS DE ASIGNACIÓN **
        if (t.getFechaAsignacion() != null) {
            
            this.vista.getCbEstadoSolicitud().setSelectedItem(s.getEstadoSolicitud().getEstadoSolicitud().trim());
            
            if(t.getEstadoTicket() != null)
                 this.vista.getCbNivelPrioridad().setSelectedItem(t.getEstadoTicket().getNivelPrioridad().trim());
            
            this.vista.getTxtFechaAsignacion().setText(dateFormat.format(t.getFechaAsignacion()));
            
            if(t.getTecnicoAsignado() != null && t.getTecnicoAsignado().getTipoUsuario() != null){
                Usuario tecnicoAsignado = t.getTecnicoAsignado();
                this.vista.getCbCargo().setSelectedItem(tecnicoAsignado.getTipoUsuario().getCargo().trim());
                
                manejarSeleccionCargo(); 
                
                String nombreCompletoTecnico = tecnicoAsignado.getNombres().trim() + " " + tecnicoAsignado.getApellidos().trim();
                this.vista.getCbNombre().setSelectedItem(nombreCompletoTecnico);
            }

        } else {
            // Valores por defecto al no estar asignado
            this.vista.getTxtFechaAsignacion().setText(dateFormat.format(new Date()));
            this.vista.getCbCargo().setSelectedIndex(0);
            this.vista.getCbNivelPrioridad().setSelectedIndex(0);
            this.vista.getCbEstadoSolicitud().setSelectedIndex(0);
        }
    }
    
    public void manejarSeleccionFilaTabla() {
        int fila = this.vista.getTabTablaAsignaciones().getSelectedRow();
        if (fila == -1) return;

        DefaultTableModel modelo = this.vista.getModeloTabla();
        String numeroTicket = (String) modelo.getValueAt(fila, 0);
        
        DefaultComboBoxModel<String> modeloTickets = (DefaultComboBoxModel<String>) this.vista.getCbTicket().getModel();
        modeloTickets.setSelectedItem(numeroTicket); 
        
        manejarSeleccionTicket(numeroTicket);
    }

    public void manejarSeleccionCargo() {
        String cargoSeleccionado = (String) this.vista.getCbCargo().getSelectedItem();
        
        if (cargoSeleccionado == null || cargoSeleccionado.equals("-- Seleccione Cargo --")) {
            this.vista.getCbNombre().setModel(new DefaultComboBoxModel<>());
            return;
        }
        
        List<String> nombresTecnicos = obtenerNombresTecnicosPorCargo(cargoSeleccionado);
        
        DefaultComboBoxModel<String> modeloTecnicos = new DefaultComboBoxModel<>();
        for (String nombre : nombresTecnicos) {
            modeloTecnicos.addElement(nombre);
        }
        this.vista.getCbNombre().setModel(modeloTecnicos);
    }
    
    public void asignarTicket() {
        if (solicitudSeleccionada == null) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un Ticket para asignar.", "Error de Asignación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validación para que no se asigne con el valor de selección
        if (this.vista.getCbNivelPrioridad().getSelectedItem().toString().equals("-- Seleccione Prioridad --")) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un Nivel de Prioridad.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (this.vista.getCbEstadoSolicitud().getSelectedItem().toString().equals("-- Seleccione Estado --")) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un Estado de Solicitud.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }


        try {
            String nombreTecnicoSeleccionado = (String) this.vista.getCbNombre().getSelectedItem();
            
            if (nombreTecnicoSeleccionado == null || this.vista.getCbCargo().getSelectedItem().toString().equals("-- Seleccione Cargo --")) {
                 JOptionPane.showMessageDialog(vista, "Debe seleccionar un Cargo y un Técnico.", "Error de Asignación", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            Usuario tecnicoAsignado = buscarTecnicoPorNombre(nombreTecnicoSeleccionado);
            if(tecnicoAsignado == null){
                JOptionPane.showMessageDialog(vista, "Error al encontrar el objeto Usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            EstadoTicket nivelPrioridad = new EstadoTicket(this.vista.getCbNivelPrioridad().getSelectedItem().toString().trim());
            EstadoSolicitud nuevoEstado = new EstadoSolicitud(this.vista.getCbEstadoSolicitud().getSelectedItem().toString().trim());
            Date fechaAsignacion = dateFormat.parse(this.vista.getTxtFechaAsignacion().getText());
            
            solicitudSeleccionada.getTicket().setTecnicoAsignado(tecnicoAsignado);
            solicitudSeleccionada.getTicket().setEstadoTicket(nivelPrioridad);
            solicitudSeleccionada.getTicket().setFechaAsignacion(fechaAsignacion);
            solicitudSeleccionada.setEstadoSolicitud(nuevoEstado);

            if (ejecutarAsignacion(solicitudSeleccionada)) {
                
                JOptionPane.showMessageDialog(vista, "Ticket " + solicitudSeleccionada.getTicket().getNumeroTicket() + " asignado/actualizado correctamente.", "Asignación Exitosa", JOptionPane.INFORMATION_MESSAGE);

                // 1. Recargar datos y tabla (Para ver el cambio reflejado)
                listaTodasSolicitudes = obtenerTodasSolicitudes(); 
                cargarTabla();
                
                // 2. Limpiar todos los campos después del éxito
                limpiarCamposAsignacion(); 
                
            } else {
                 JOptionPane.showMessageDialog(vista, "Error al guardar la asignación en la base de datos. Verifique la conexión.", "Error de Persistencia", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(vista, "Error en el formato de fecha.", "Error de Fecha", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Ocurrió un error al asignar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void cargarTabla() {
        DefaultTableModel modeloTabla = this.vista.getModeloTabla();
        modeloTabla.setRowCount(0);

        for (Solicitud s : listaTodasSolicitudes) {
            Ticket t = s.getTicket();
            
            String tecnicoNombre = t.getTecnicoAsignado() != null ? t.getTecnicoAsignado().getNombres().trim() + " " + t.getTecnicoAsignado().getApellidos().trim() : "";
            String cargo = t.getTecnicoAsignado() != null && t.getTecnicoAsignado().getTipoUsuario() != null ? t.getTecnicoAsignado().getTipoUsuario().getCargo() : "";
            String fechaAsignacion = t.getFechaAsignacion() != null ? dateFormat.format(t.getFechaAsignacion()) : "";
            String nivelPrioridad = t.getEstadoTicket() != null ? t.getEstadoTicket().getNivelPrioridad() : "";

            Object[] fila = {
                t.getNumeroTicket(),
                dateFormat.format(s.getFechaCreacion()),
                s.getTipoServicio().getNombreServicio().trim(),
                s.getDescripcion().trim(),
                s.getEstadoSolicitud().getEstadoSolicitud().trim(),
                fechaAsignacion,
                nivelPrioridad,
                cargo,
                tecnicoNombre
            };
            modeloTabla.addRow(fila);
        }
    }
}