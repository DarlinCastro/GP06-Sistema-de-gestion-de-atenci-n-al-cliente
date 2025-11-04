package capa_controladora;

import base_datos.ConexionBD;
import capa_modelo.TipoServicio;
import java.awt.Component; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JDialog; 
import javax.swing.JOptionPane;
import javax.swing.JScrollPane; 
import javax.swing.JTable; 
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;

public class GenerarReporteController {
    
    // CLASE ESTRUCTURAL DE DATOS (DTO)
    public static class ReporteData {
        private String numeroTicket;
        private Date fechaCreacion;
        private String estadoTicket;
        private String tipoServicio;
        private String descripcionServicio;
        private String nombreCliente;
        private Date fechaAsignacion;
        private String cargoEncargado;
        private String nombreEncargadoSoporte;

        public ReporteData(String numeroTicket, Date fechaCreacion, String estadoTicket, String tipoServicio, String descripcionServicio, String nombreCliente, Date fechaAsignacion, String cargoEncargado, String nombreEncargadoSoporte) {
            this.numeroTicket = numeroTicket;
            this.fechaCreacion = fechaCreacion;
            this.estadoTicket = estadoTicket;
            this.tipoServicio = tipoServicio;
            this.descripcionServicio = descripcionServicio;
            this.nombreCliente = nombreCliente;
            this.fechaAsignacion = fechaAsignacion;
            this.cargoEncargado = cargoEncargado;
            this.nombreEncargadoSoporte = nombreEncargadoSoporte;
        }

        // Getters
        public String getNumeroTicket() {
            return numeroTicket;
        }
        public Date getFechaCreacion() {
            return fechaCreacion;
        }
        public String getEstadoTicket() {
            return estadoTicket;
        }
        public String getTipoServicio() {
            return tipoServicio;
        }
        public String getDescripcionServicio() {
            return descripcionServicio;
        }
        public String getNombreCliente() {
            return nombreCliente;
        }
        public Date getFechaAsignacion() {
            return fechaAsignacion;
        }
        public String getCargoEncargado() {
            return cargoEncargado;
        }
        public String getNombreEncargadoSoporte() {
            return nombreEncargadoSoporte;
        }
    }

    // --- Consulta SQL --- //
    private static final String CONSULTA_BASE_REPORTE
            = "SELECT "
            + "   TK.NUMEROTICKET, S.FECHACREACION, ES.ESTADOSOLICITUD, "
            + "   TS.NOMBRESERVICIO, S.DESCRIPCION, C.NOMBRES || ' ' || C.APELLIDOS AS NOMBRE_CLIENTE, "
            + "   TK.FECHAASIGNACION, TU.CARGO AS CARGO_ENCARGADO, E.NOMBRES || ' ' || E.APELLIDOS AS NOMBRE_ENCARGADO "
            + "FROM SOLICITUD S "
            + "JOIN TICKET TK ON S.IDTICKET = TK.IDTICKET "
            + "JOIN ESTADO_SOLICITUD ES ON S.IDESTADOSOLICITUD = ES.IDESTADOSOLICITUD "
            + "JOIN TIPO_SERVICIO TS ON S.IDTIPOSERVICIO = TS.IDTIPOSERVICIO "
            + "JOIN USUARIO C ON S.IDUSUARIO = C.IDUSUARIO "
            + "LEFT JOIN USUARIO E ON TK.IDUSUARIO = E.IDUSUARIO "
            + "LEFT JOIN TIPO_USUARIO TU ON E.IDTIPOUSUARIO = TU.IDTIPOUSUARIO ";

    // --- Métodos de Acceso a Datos --- //
    public List<TipoServicio> cargarTiposServicio() {
        List<TipoServicio> listaServicios = new ArrayList<>();
        String sql = "SELECT NOMBRESERVICIO FROM TIPO_SERVICIO ORDER BY NOMBRESERVICIO";
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString("NOMBRESERVICIO");
                listaServicios.add(new TipoServicio(nombre));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener Tipos de Servicio: " + e.getMessage());
        }
        return listaServicios;
    }

    private List<ReporteData> ejecutarConsultaReporte(String sql, String filtroServicio) {
        List<ReporteData> listaReportes = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filtroServicio != null) {
                ps.setString(1, filtroServicio.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteData data = new ReporteData(
                            rs.getString("NUMEROTICKET").trim(),
                            rs.getDate("FECHACREACION"),
                            rs.getString("ESTADOSOLICITUD").trim(),
                            rs.getString("NOMBRESERVICIO").trim(),
                            rs.getString("DESCRIPCION").trim(),
                            rs.getString("NOMBRE_CLIENTE").trim(),
                            rs.getDate("FECHAASIGNACION"),
                            (rs.getString("CARGO_ENCARGADO") != null) ? rs.getString("CARGO_ENCARGADO").trim() : "N/A",
                            (rs.getString("NOMBRE_ENCARGADO") != null) ? rs.getString("NOMBRE_ENCARGADO").trim() : "PENDIENTE"
                    );
                    listaReportes.add(data);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al ejecutar consulta de Reportes: " + e.getMessage());
        }
        return listaReportes;
    }

    public List<ReporteData> obtenerReporteGeneral() {
        String sql = CONSULTA_BASE_REPORTE + " ORDER BY S.FECHACREACION DESC";
        return ejecutarConsultaReporte(sql, null);
    }

    public List<ReporteData> obtenerReporteFiltrado(String nombreServicio) {
        String sql = CONSULTA_BASE_REPORTE
                + " WHERE TS.NOMBRESERVICIO = ? ORDER BY S.FECHACREACION DESC";
        return ejecutarConsultaReporte(sql, nombreServicio);
    }

    // --- MÉTODOS DE GENERACIÓN DE REPORTES ---

    public void generarPDFReporteGeneral(DefaultTableModel modelo) {
        // 1. Crear una nueva JTable con el modelo de datos actual
        JTable tablaReporte = new JTable(modelo);
        // Permite el scroll horizontal
        tablaReporte.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 

        // 2. Envolver la tabla en un JScrollPane para que sea scrollable
        JScrollPane scrollPane = new JScrollPane(tablaReporte);

        // 3. Definir un tamaño preferido para el scrollPane dentro del JOptionPane
        scrollPane.setPreferredSize(new java.awt.Dimension(800, 250)); 

        // 4. Crear el componente personalizado con un mensaje de encabezado
        Component[] componentes = {
            new javax.swing.JLabel("<html><b>--- REPORTE GENERAL ---</b></html>"),
            new javax.swing.JLabel("Total de Filas Reportadas: " + modelo.getRowCount()),
            scrollPane,
            new javax.swing.JLabel("<html><i>¡Se ha generado un reporte con los datos tabulados!</i></html>")
        };

        // 5. Mostrar el JOptionPane usando el componente array (Multi-line)
        JOptionPane.showMessageDialog(null,
                componentes,
                "Reporte General Generado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Generacion del ticket
    public void generarPDFReporteIndividual(ReporteData reporte) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fechaCreacion = (reporte.getFechaCreacion() != null) ? dateFormat.format(reporte.getFechaCreacion()) : "N/A";
        String fechaAsignacion = (reporte.getFechaAsignacion() != null) ? dateFormat.format(reporte.getFechaAsignacion()) : "N/A";

        String contenido = "--- REPORTE TICKET ---\n"
                + "N° TICKET: " + reporte.getNumeroTicket() + "\n"
                + "ESTADO: " + reporte.getEstadoTicket() + "\n"
                + "CLIENTE: " + reporte.getNombreCliente() + "\n"
                + "TIPO DE SERVICIO: " + reporte.getTipoServicio() + "\n"
                + "DESCRIPCIÓN: " + reporte.getDescripcionServicio() + "\n"
                + "ENCARGADO: " + reporte.getNombreEncargadoSoporte() + " (" + reporte.getCargoEncargado() + ")\n"
                + "FECHA CREACIÓN: " + fechaCreacion + "\n"
                + "FECHA ASIGNACIÓN: " + fechaAsignacion + "\n";

        JOptionPane.showMessageDialog(null, contenido, "Ticket Generado", JOptionPane.INFORMATION_MESSAGE);
    }
}