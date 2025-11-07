package capa_vista;

import capa_controladora.GenerarReporteController;
import capa_modelo.TipoServicio;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.JFrame;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author RYZEN
 */
public class jFrameGenerarReporte extends javax.swing.JFrame {

    
    private JFrame frameAnterior; 

    private final GenerarReporteController controller = new GenerarReporteController();
    private DefaultTableModel tableModel;

    // CONSTRUCTOR ESTÁNDAR (MANTENIDO POR COMPATIBILIDAD)
    public jFrameGenerarReporte() {
        initComponents();
        this.setLocationRelativeTo(null);
        inicializarComponentesPersonalizados();
    }

    // NUEVO CONSTRUCTOR SOBRECARGADO: Recibe la ventana anterior
    public jFrameGenerarReporte(JFrame frameAnterior) {
        initComponents();
        this.setLocationRelativeTo(null);
        inicializarComponentesPersonalizados();
        this.frameAnterior = frameAnterior; // Guarda la referencia del llamador
    }
    // MÉTODOS DE INICIALIZACIÓN Y CONFIGURACIÓN

    private void inicializarComponentesPersonalizados() {
        // Ocultar el botón individual al inicio
        btnGenerarReporte.setVisible(false);
        cargarComboBoxServicios();
        configurarTabla(); // Configuración de tabla NO EDITABLE
        agregarListenerSeleccionTabla(); // Lógica de alternancia de botones
    }

    private void cargarComboBoxServicios() {
        try {
            List<TipoServicio> servicios = controller.cargarTiposServicio();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("Todos los Servicios");
            for (TipoServicio ts : servicios) {
                model.addElement(ts.toString());
            }
            cbListaTipoServicio.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de servicio: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para definir las columnas y hacer la tabla NO EDITABLE
    private void configurarTabla() {
        String[] columnas = {
            "N° Ticket", "Fecha Creación", "Estado Ticket", "Tipo Servicio",
            "Descripción Servicio", "Nombre Cliente", "Fecha Asignación",
            "Cargo", "Encargado Soporte"
        };
        // Sobreescribimos DefaultTableModel para que ninguna celda sea editable
        tableModel = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(tableModel);
        cargarDatosEnTabla(controller.obtenerReporteGeneral());
    }

    private void cargarDatosEnTabla(List<GenerarReporteController.ReporteData> listaReportes) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        for (GenerarReporteController.ReporteData data : listaReportes) {
            Object[] fila = new Object[9];
            fila[0] = data.getNumeroTicket();
            fila[1] = dateFormat.format(data.getFechaCreacion());
            fila[2] = data.getEstadoTicket();
            fila[3] = data.getTipoServicio();
            fila[4] = data.getDescripcionServicio();
            fila[5] = data.getNombreCliente();
            fila[6] = (data.getFechaAsignacion() != null) ? dateFormat.format(data.getFechaAsignacion()) : "N/A";
            fila[7] = data.getCargoEncargado();
            fila[8] = data.getNombreEncargadoSoporte();
            tableModel.addRow(fila);
        }
    }

    // Método para RESTABLECER el estado inicial de la vista
    private void restablecerEstadoInicial() {
        // 1. Deseleccionar cualquier fila y volver al filtro "Todos los Servicios"
        jTable1.clearSelection();
        cbListaTipoServicio.setSelectedItem("Todos los Servicios");
        // 2. Recargar los datos generales
        cargarDatosEnTabla(controller.obtenerReporteGeneral());
        // 3. Ocultar el botón individual y mostrar el botón general
        btnGenerarReporte.setVisible(false);
        btnGenerarReporteGeneral.setVisible(true);
    }

    // Listener para ALTERNAR la visibilidad de los botones
    private void agregarListenerSeleccionTabla() {
        ListSelectionModel selectionModel = jTable1.getSelectionModel();
        selectionModel.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                boolean filaSeleccionada = jTable1.getSelectedRow() != -1;
                // Si hay fila seleccionada: Muestra Individual, Oculta General
                btnGenerarReporte.setVisible(filaSeleccionada);
                // Si no hay fila seleccionada: Muestra General, Oculta Individual
                btnGenerarReporteGeneral.setVisible(!filaSeleccionada);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel10 = new javax.swing.JPanel();
        cbListaTipoServicio = new javax.swing.JComboBox<>();
        lblTipoServicio = new javax.swing.JLabel();
        btnGenerarReporte = new javax.swing.JButton();
        btnGenerarReporteGeneral = new javax.swing.JButton();
        btnAtras = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        cbListaTipoServicio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbListaTipoServicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbListaTipoServicioActionPerformed(evt);
            }
        });

        lblTipoServicio.setText("Tipo de Servicio");

        btnGenerarReporte.setBackground(new java.awt.Color(51, 51, 255));
        btnGenerarReporte.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGenerarReporte.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerarReporte.setText("Generar Reporte");
        btnGenerarReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarReporteActionPerformed(evt);
            }
        });

        btnGenerarReporteGeneral.setBackground(new java.awt.Color(51, 51, 255));
        btnGenerarReporteGeneral.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGenerarReporteGeneral.setForeground(new java.awt.Color(255, 255, 255));
        btnGenerarReporteGeneral.setText("Generar Reporte General");
        btnGenerarReporteGeneral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarReporteGeneralActionPerformed(evt);
            }
        });

        btnAtras.setBackground(new java.awt.Color(51, 51, 255));
        btnAtras.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAtras.setForeground(new java.awt.Color(255, 255, 255));
        btnAtras.setText("Atras");
        btnAtras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtrasActionPerformed(evt);
            }
        });

        jPanel9.setBackground(new java.awt.Color(0, 51, 153));
        jPanel9.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(0, 51, 153));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("© 2025 KIA. Todos los derechos reservados");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addGap(381, 381, 381))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "N° Ticket", "Fecha Creacion", "Estado Ticket", "Tipo Servicio", "Descripcion Servicio", "Nombre Cliente", "Fecha Asignacion", "Cargo", "Encargado Soporte"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("GENERAR REPORTE");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addGap(293, 293, 293)
                                .addComponent(btnAtras))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(lblTipoServicio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbListaTipoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 507, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnGenerarReporte)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnGenerarReporteGeneral)))
                        .addGap(19, 19, 19))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 934, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(18, Short.MAX_VALUE))))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAtras)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGenerarReporteGeneral)
                    .addComponent(btnGenerarReporte)
                    .addComponent(cbListaTipoServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTipoServicio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAtrasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtrasActionPerformed
        // Si tenemos la referencia de la ventana anterior, la mostramos.
        if (frameAnterior != null) {
            frameAnterior.setVisible(true); // Muestra MenuAdmin o MenuTecnico
            this.dispose();                 // Cierra la ventana de reportes
        } else {
            // Caso de emergencia o si se abrió con el constructor vacío.
            JOptionPane.showMessageDialog(this, "No se pudo determinar la pantalla anterior. Volviendo al Login.", "Error de Navegación", JOptionPane.WARNING_MESSAGE);
            // Opción: Volver al login si no hay frameAnterior
            new jFrameLogin().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnAtrasActionPerformed

    private void btnGenerarReporteGeneralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarReporteGeneralActionPerformed
        // Recargamos la tabla con el filtro actual (opcional, pero asegura los datos)
        cbListaTipoServicioActionPerformed(evt);
        // 1. Generar Reporte General
        controller.generarPDFReporteGeneral(tableModel);
        // 2. Restaurar el estado inicial después del mensaje de éxito
        restablecerEstadoInicial();
    }//GEN-LAST:event_btnGenerarReporteGeneralActionPerformed

    private void btnGenerarReporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarReporteActionPerformed
        int filaSeleccionada = jTable1.getSelectedRow();
        if (filaSeleccionada != -1) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                // Extracción y parseo de datos de la fila
                Date fechaCreacion = dateFormat.parse((String) tableModel.getValueAt(filaSeleccionada, 1));
                String fechaAsignacionStr = (String) tableModel.getValueAt(filaSeleccionada, 6);
                Date fechaAsignacion = fechaAsignacionStr.equals("N/A") ? null : dateFormat.parse(fechaAsignacionStr);

                // Creación del objeto ReporteData
                GenerarReporteController.ReporteData reporteSeleccionado = new GenerarReporteController.ReporteData(
                    (String) tableModel.getValueAt(filaSeleccionada, 0),
                    fechaCreacion,
                    (String) tableModel.getValueAt(filaSeleccionada, 2),
                    (String) tableModel.getValueAt(filaSeleccionada, 3),
                    (String) tableModel.getValueAt(filaSeleccionada, 4),
                    (String) tableModel.getValueAt(filaSeleccionada, 5),
                    fechaAsignacion,
                    (String) tableModel.getValueAt(filaSeleccionada, 7),
                    (String) tableModel.getValueAt(filaSeleccionada, 8)
                );

                // 1. Generar El Ticket
                controller.generarPDFReporteIndividual(reporteSeleccionado);
                // 2. RESTABLECER ESTADO INICIAL DESPUÉS DEL MENSAJE DE ÉXITO
                restablecerEstadoInicial();

            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(this, "Error al parsear la fecha: " + ex.getMessage(), "Error de Datos", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnGenerarReporteActionPerformed

    private void cbListaTipoServicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbListaTipoServicioActionPerformed
        String tipoServicioSeleccionado = (String) cbListaTipoServicio.getSelectedItem();

        // 1. Lógica para filtrar o cargar reporte general
        if (tipoServicioSeleccionado == null || tipoServicioSeleccionado.equals("Todos los Servicios")) {
            cargarDatosEnTabla(controller.obtenerReporteGeneral());
        } else {
            cargarDatosEnTabla(controller.obtenerReporteFiltrado(tipoServicioSeleccionado));
        }

        // 2. Restablece el estado de los botones (Muestra General, Oculta Individual)
        jTable1.clearSelection();
        btnGenerarReporte.setVisible(false);
        btnGenerarReporteGeneral.setVisible(true);
    }//GEN-LAST:event_cbListaTipoServicioActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(jFrameGenerarReporte.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(jFrameGenerarReporte.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(jFrameGenerarReporte.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(jFrameGenerarReporte.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new jFrameGenerarReporte().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtras;
    private javax.swing.JButton btnGenerarReporte;
    private javax.swing.JButton btnGenerarReporteGeneral;
    private javax.swing.JComboBox<String> cbListaTipoServicio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblTipoServicio;
    // End of variables declaration//GEN-END:variables
}
