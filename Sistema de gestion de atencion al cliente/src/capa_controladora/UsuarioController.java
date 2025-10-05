package capa_controladora;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import base_datos.ConexionBD;
import capa_vista.jFrameLogin;
import entidades.Usuario;
import entidades.TipoUsuario;
import entidades.Password;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Adam
 */
public class UsuarioController {

    private Connection conexion;

    public UsuarioController(Connection conexion) {
        this.conexion = conexion;
    }

    public TipoUsuario introducirCredenciales(String identificador, String clave) {
        TipoUsuario tipoUsuario = null;
        try {
            String sql = "SELECT tu.cargo\n"+
                     "FROM usuario u\n"+
                     "INNER JOIN pasword p ON u.idpasword = p.idpasword\n"+
                     "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario\n"+
                     "WHERE p.identificador = ? AND p.claveacceso = ?";

            PreparedStatement pst = conexion.prepareStatement(sql);
            pst.setString(1, identificador);
            pst.setString(2, clave);
            //
            ResultSet rs = pst.executeQuery();
            //
            if (rs.next()) {
                tipoUsuario = new TipoUsuario();
                tipoUsuario.setCargo(rs.getString("cargo"));
            }
            //
            rs.close();
            pst.close();

        } catch (Exception e) {
            System.out.println("Error en la validacion: " + e.getMessage());
        }
        return tipoUsuario;
    }

}
