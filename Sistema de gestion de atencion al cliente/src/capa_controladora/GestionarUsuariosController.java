package capa_controladora;

import capa_modelo.Usuario;
import capa_modelo.TipoUsuario;
import capa_modelo.Password;
import base_datos.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Clase que actúa como Controlador y DAO unificado para la gestión de usuarios.
 * Contiene la lógica CRUD (Crear, Leer, Actualizar, Eliminar) para Usuario.
 */
public class GestionarUsuariosController {

    // Constructor simple
    public GestionarUsuariosController() {
    }

    // ===================================================
    // 💾 MÉTODO: AGREGAR USUARIO (CREATE)
    // ===================================================
    /**
     * Agrega un nuevo usuario a la base de datos, manejando la transacción.
     * @param u Objeto Usuario con los datos a registrar.
     */
    public void agregarUsuario(Usuario u) {
        Connection con = null;
        PreparedStatement psPass = null;
        PreparedStatement psUsuario = null;

        try {
            con = ConexionBD.conectar(); 
            con.setAutoCommit(false); // Inicia Transacción

            // 1️⃣ Insertar en pasword
            String sqlPass = "INSERT INTO pasword (claveacceso, identificador) VALUES (?, ?) RETURNING idpasword";
            psPass = con.prepareStatement(sqlPass);
            psPass.setString(1, u.getPassword().getClaveAcceso());
            psPass.setString(2, u.getPassword().getIdentificador());

            ResultSet rsPass = psPass.executeQuery();
            int idPasword = -1;
            if (rsPass.next()) {
                idPasword = rsPass.getInt("idpasword");
            }

            // 2️⃣ Obtener idtipousuario por cargo
            String sqlTipo = "SELECT idtipousuario FROM tipo_usuario WHERE cargo = ?";
            PreparedStatement psTipo = con.prepareStatement(sqlTipo);
            psTipo.setString(1, u.getTipoUsuario().getCargo());
            ResultSet rsTipo = psTipo.executeQuery();
            int idTipoUsuario = -1;
            if (rsTipo.next()) {
                idTipoUsuario = rsTipo.getInt("idtipousuario");
            } else {
                throw new SQLException("Cargo no encontrado en tipo_usuario");
            }

            // 3️⃣ Insertar en usuario
            String sqlUsuario = """
                INSERT INTO usuario (nombres, apellidos, correoelectronico, idtipousuario, idpasword)
                VALUES (?, ?, ?, ?, ?)
                """;
            psUsuario = con.prepareStatement(sqlUsuario);
            psUsuario.setString(1, u.getNombres());
            psUsuario.setString(2, u.getApellidos());
            psUsuario.setString(3, u.getCorreoElectronico());
            psUsuario.setInt(4, idTipoUsuario);
            psUsuario.setInt(5, idPasword);

            psUsuario.executeUpdate();
            con.commit(); // Confirma Transacción

        } catch (SQLException e) {
            System.out.println("❌ Error al agregar usuario: " + e.getMessage());
            try {
                if (con != null) con.rollback(); // Revierte en caso de error
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (psPass != null) psPass.close();
                if (psUsuario != null) psUsuario.close();
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    // ===================================================
    // 📖 MÉTODO: OBTENER USUARIOS (READ)
    // ===================================================
    /**
     * Obtiene la lista de todos los usuarios registrados.
     * @return Lista de usuarios desde la base de datos.
     */
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = """
            SELECT 
                u.nombres,
                u.apellidos,
                u.correoelectronico,
                tu.cargo,
                p.claveacceso,
                p.identificador
            FROM usuario u
            JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario
            JOIN pasword p ON u.idpasword = p.idpasword
            ORDER BY u.nombres ASC
            """;

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoUsuario tipo = new TipoUsuario(rs.getString("cargo"));
                Password pass = new Password(rs.getString("claveacceso"), rs.getString("identificador"));
                Usuario u = new Usuario(
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("correoelectronico"),
                        tipo,
                        pass
                );
                lista.add(u);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error al obtener usuarios: " + e.getMessage());
        }

        return lista;
    }

    // ===================================================
    // ✏️ MÉTODO: ACTUALIZAR USUARIO (UPDATE)
    // ===================================================
    /**
     * Actualiza un usuario existente en la base de datos, manejando la transacción.
     * @param u Objeto Usuario con los datos actualizados.
     */
    public void actualizarUsuario(Usuario u) {
        Connection con = null;
        try {
            con = ConexionBD.conectar();
            con.setAutoCommit(false); // Inicia Transacción

            // 1️⃣ Actualizar pasword
            String sqlPass = "UPDATE pasword SET claveacceso=? WHERE identificador=?";
            PreparedStatement psPass = con.prepareStatement(sqlPass);
            psPass.setString(1, u.getPassword().getClaveAcceso());
            psPass.setString(2, u.getPassword().getIdentificador());
            psPass.executeUpdate();

            // 2️⃣ Obtener idtipousuario
            String sqlTipo = "SELECT idtipousuario FROM tipo_usuario WHERE cargo = ?";
            PreparedStatement psTipo = con.prepareStatement(sqlTipo);
            psTipo.setString(1, u.getTipoUsuario().getCargo());
            ResultSet rsTipo = psTipo.executeQuery();
            int idTipoUsuario = -1;
            if (rsTipo.next()) {
                idTipoUsuario = rsTipo.getInt("idtipousuario");
            } else {
                throw new SQLException("Cargo no encontrado");
            }

            // 3️⃣ Actualizar usuario
            String sqlUsuario = """
                UPDATE usuario 
                     SET nombres=?, apellidos=?, correoelectronico=?, idtipousuario=?
                     WHERE idpasword IN (
                         SELECT idpasword FROM pasword WHERE identificador = ?
                     )
                """;
            PreparedStatement psUsuario = con.prepareStatement(sqlUsuario);
            psUsuario.setString(1, u.getNombres());
            psUsuario.setString(2, u.getApellidos());
            psUsuario.setString(3, u.getCorreoElectronico());
            psUsuario.setInt(4, idTipoUsuario);
            psUsuario.setString(5, u.getPassword().getIdentificador());
            psUsuario.executeUpdate();

            con.commit(); // Confirma Transacción

        } catch (SQLException e) {
            System.out.println("❌ Error al actualizar usuario: " + e.getMessage());
            try {
                if (con != null) con.rollback(); // Revierte en caso de error
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    // ===================================================
    // 🗑️ MÉTODO: ELIMINAR USUARIO (DELETE)
    // ===================================================
    /**
     * Elimina un usuario de las tablas 'usuario' y 'pasword' según su identificador único.
     * @param identificador Identificador del usuario a eliminar.
     */
    public void eliminarUsuario(String identificador) {
        
        String sqlDeleteUsuario = """
            DELETE FROM usuario 
                    WHERE idpasword IN (
                        SELECT idpasword FROM pasword WHERE identificador = ?
                    )
            """;
        
        String sqlDeletePassword = "DELETE FROM pasword WHERE identificador = ?";

        Connection con = null;
        try {
            con = ConexionBD.conectar();
            con.setAutoCommit(false); // Inicia Transacción

            // 1️⃣ Eliminar de la tabla usuario
            try (PreparedStatement psUsuario = con.prepareStatement(sqlDeleteUsuario)) {
                psUsuario.setString(1, identificador);
                psUsuario.executeUpdate();
            }

            // 2️⃣ Eliminar de la tabla pasword
             try (PreparedStatement psPass = con.prepareStatement(sqlDeletePassword)) {
                psPass.setString(1, identificador);
                psPass.executeUpdate();
            }
            
            con.commit(); // Confirma Transacción

        } catch (SQLException e) {
            System.out.println("❌ Error al eliminar usuario: " + e.getMessage());
            try {
                if (con != null) con.rollback(); // Revierte en caso de error
            } catch (SQLException ex) {
                System.out.println("⚠️ Error al hacer rollback: " + ex.getMessage());
            }
        } finally {
             try {
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("⚠️ Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
