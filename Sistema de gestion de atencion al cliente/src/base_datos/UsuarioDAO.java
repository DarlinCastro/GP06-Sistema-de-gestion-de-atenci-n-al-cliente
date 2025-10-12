/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package base_datos;

import base_datos.ConexionBD;
import entidades.Solicitud;
import entidades.TipoUsuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import entidades.Usuario;
/**
 *
 * @author erick
 */
public class UsuarioDAO {   
    
    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        
        try (Connection conn = ConexionBD.conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombres(rs.getString("nombres"));
                u.setApellidos(rs.getString("apellidos"));
                u.setCorreoElectronico(rs.getString("correo"));
                u.setCargo(rs.getString("cargo"));
                u.setIdentificador(rs.getString("identificador"));
                u.setContrasena(rs.getString("contrasena"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    //Inserta un nuevo usuario en la BD y retorna true si éxito.
    public boolean insertarUsuario(Usuario u) {
        String sql = "INSERT INTO usuarios (nombres, apellidos, correo, cargo, identificador, contrasena) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getCorreoElectronico());
            ps.setString(4, u.getCargo());
            ps.setString(5, u.getIdentificador());
            ps.setString(6, u.getContrasena());  // Idealmente, hashea antes
            
            int filas = ps.executeUpdate();
            if (filas > 0) {
                // Obtiene el ID generado y lo setea en el objeto
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
                System.out.println("Usuario insertado con ID: " + u.getId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            // Ej: Si identificador o correo duplicado, e.getMessage() lo dirá
        }
        return false;
    }
    
    //Actualiza un usuario existente en la BD por ID.
    public boolean actualizarUsuario(Usuario u) {
        if (u.getId() <= 0) {
            System.err.println("ID inválido para actualizar.");
            return false;
        }
        String sql = "UPDATE usuarios SET nombres = ?, apellidos = ?, correo = ?, cargo = ?, identificador = ?, contrasena = ? WHERE id = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getCorreoElectronico());
            ps.setString(4, u.getCargo());
            ps.setString(5, u.getIdentificador());
            ps.setString(6, u.getContrasena());
            ps.setInt(7, u.getId());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }
        return false;
    }
    
    //Elimina un usuario por ID numérico de BD.
    public boolean eliminarUsuario(int id) {
        if (id <= 0) {
            System.err.println("ID inválido para eliminar.");
            return false;
        }
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }
        return false;
    }
    
    //Busca usuarios por texto (en nombres, apellidos, correo, identificador).
    public List<Usuario> buscarUsuarios(String texto) {
        List<Usuario> lista = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return listarUsuarios();  // Si vacío, retorna todos
        }
    String sql = "SELECT * FROM usuarios WHERE nombres LIKE ? OR apellidos LIKE ? OR correo LIKE ? OR cargo LIKE ? OR identificador LIKE ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String patron = "%" + texto.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setString(4, patron);
            ps.setString(5, patron);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombres(rs.getString("nombres"));
                    u.setApellidos(rs.getString("apellidos"));
                    u.setCorreoElectronico(rs.getString("correo"));
                    u.setCargo(rs.getString("cargo"));
                    u.setIdentificador(rs.getString("identificador"));
                    u.setContrasena(rs.getString("contrasena"));
                    lista.add(u);
                }
                }
        } catch (SQLException e) {
            System.err.println("Error en búsqueda: " + e.getMessage());
        }
        return lista;
    }
    
    //Busca un usuario exacto por identificador (para cargar en formulario al clic).
    public Usuario buscarPorIdentificador(String identificador) {
        if (identificador == null || identificador.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM usuarios WHERE identificador = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identificador.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombres(rs.getString("nombres"));
                    u.setApellidos(rs.getString("apellidos"));
                    u.setCorreoElectronico(rs.getString("correo"));
                    u.setCargo(rs.getString("cargo"));
                    u.setIdentificador(rs.getString("identificador"));
                    u.setContrasena(rs.getString("contrasena"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por identificador: " + e.getMessage());
        }
        return null;
    }    
    
    // Método para obtener usuarios con cargo "Técnico"
    public List<Usuario> obtenerUsuariosPorCargo(String cargo) {
    List<Usuario> usuarios = new ArrayList<>();
    final String SQL = "SELECT u.id, u.nombres, u.apellidos, u.correoelectronico, tu.cargo " +
                       "FROM usuarios u " +
                       "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario " +
                       "WHERE tu.cargo = ?";

    try (Connection conn = ConexionBD.conectar();
         PreparedStatement ps = conn.prepareStatement(SQL)) {

        ps.setString(1, cargo.trim());

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoUsuario tipo = new TipoUsuario(rs.getString("cargo").trim());
                Usuario usuario = new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombres").trim(),
                    rs.getString("apellidos").trim(),
                    rs.getString("correoelectronico").trim(),
                    tipo,
                    null // puedes pasar null si no necesitas la contraseña aquí
                );
                usuarios.add(usuario);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al obtener usuarios por cargo: " + e.getMessage());
    }

    return usuarios;
}


}
