/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package capa_controladora;

import base_datos.UsuarioDAO;
import capa_vista.jFrameGestionarUsuarios;
import entidades.Usuario;
import base_datos.UsuarioDAO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
/**
 *
 * @author erick
 */
    public class GestionarController {

    private UsuarioDAO usuarioDAO;

    // Constructor
    public GestionarController() {
        usuarioDAO = new UsuarioDAO();
    }

    // === MÉTODOS PRINCIPALES ===

    /**
     * Agrega un nuevo usuario a la base de datos.
     * @param usuario Objeto Usuario con los datos a registrar.
     */
    public void agregarUsuario(Usuario usuario) {
        usuarioDAO.agregarUsuario(usuario);
    }

    /**
     * Obtiene la lista de todos los usuarios registrados.
     * @return Lista de usuarios desde la base de datos.
     */
    public List<Usuario> obtenerUsuarios() {
        return usuarioDAO.obtenerUsuarios();
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     * @param usuario Objeto Usuario con los datos actualizados.
     */
    public void actualizarUsuario(Usuario usuario) {
        usuarioDAO.actualizarUsuario(usuario);
    }

    /**
     * Elimina un usuario según su identificador único.
     * @param identificador Identificador del usuario a eliminar.
     */
    public void eliminarUsuario(String identificador) {
        usuarioDAO.eliminarUsuario(identificador);
    }
}
