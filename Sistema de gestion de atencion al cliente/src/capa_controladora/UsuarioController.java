package capa_controladora;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import base_datos.ConexionBD;
import capa_vista.jFrameLogin;
import capa_modelo.Usuario;
import capa_modelo.TipoUsuario;
import capa_modelo.Password;

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
    
    //UNICO METODO AGREGADO
    //Nuevo método para obtener el usuario logueado completo
    public Usuario obtenerUsuarioPorCredenciales(String identificador, String clave) {
       Usuario usuario = null;
    try {
        String sql = "SELECT u.nombres, u.apellidos, u.correoelectronico, tu.cargo " +
                     "FROM usuario u " +
                     "INNER JOIN pasword p ON u.idpasword = p.idpasword " +
                     "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario " +
                     "WHERE p.identificador = ? AND p.claveacceso = ?";

        PreparedStatement pst = conexion.prepareStatement(sql);
        pst.setString(1, identificador);
        pst.setString(2, clave);

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            usuario = new Usuario();
            usuario.setNombres(rs.getString("nombres"));
            usuario.setApellidos(rs.getString("apellidos"));
            usuario.setCorreoElectronico(rs.getString("correoelectronico"));

            TipoUsuario tipo = new TipoUsuario();
            tipo.setCargo(rs.getString("cargo"));
            usuario.setTipoUsuario(tipo);
        }
        rs.close();
        pst.close();

    } catch (Exception e) {
        System.out.println("Error al obtener usuario: " + e.getMessage());
    }
    return usuario;
    }
}
