package capa_controladora;

import capa_modelo.Usuario;
import capa_modelo.TipoUsuario;
import capa_modelo.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; 

/**
 * Controlador para la gestión de usuarios, incluyendo la validación de credenciales
 * y la obtención del objeto Usuario completo para la sesión.
 * @author Adam
 */
public class UsuarioController {

    private Connection conexion;

    public UsuarioController(Connection conexion) {
        this.conexion = conexion;
    }

    //-------------------------------------------------------------------------
    // MÉTODO ORIGINAL: Solo verifica credenciales y devuelve el cargo.
    //-------------------------------------------------------------------------

    public TipoUsuario introducirCredenciales(String identificador, String clave) {
        TipoUsuario tipoUsuario = null;
        try {
            String sql = "SELECT tu.cargo\n"+
                         "FROM usuario u\n"+
                         "INNER JOIN pasword p ON u.idpasword = p.idpasword\n"+
                         "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario\n"+
                         "WHERE p.identificador = ? AND p.claveacceso = ?";

            try (PreparedStatement pst = conexion.prepareStatement(sql)) {
                pst.setString(1, identificador);
                pst.setString(2, clave);
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        tipoUsuario = new TipoUsuario();
                        // El .trim() es importante si el campo CHAR(10) tiene espacios sobrantes.
                        tipoUsuario.setCargo(rs.getString("cargo").trim());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en la validacion: " + e.getMessage());
        }
        return tipoUsuario;
    }
    
    //-------------------------------------------------------------------------
    // MÉTODO CLAVE: Obtiene el Usuario COMPLETO
    //-------------------------------------------------------------------------

    /**
     * Obtiene el objeto Usuario completo de la base de datos, incluyendo
     * el TipoUsuario y los datos de Password (identificador y claveacceso).
     * @param identificador El nombre de usuario o identificador.
     * @param clave La clave de acceso.
     * @return Objeto Usuario si las credenciales son correctas, null en caso contrario.
     */
    public Usuario obtenerUsuarioPorCredenciales(String identificador, String clave) {
        Usuario usuario = null;
        try {
            // Se agregan p.identificador y p.claveacceso a la SELECT, CRUCIALES para el SeguimientoController.
            String sql = "SELECT u.nombres, u.apellidos, u.correoelectronico, tu.cargo, p.identificador, p.claveacceso " +
                        "FROM usuario u " +
                        "INNER JOIN pasword p ON u.idpasword = p.idpasword " +
                        "INNER JOIN tipo_usuario tu ON u.idtipousuario = tu.idtipousuario " +
                        "WHERE p.identificador = ? AND p.claveacceso = ?";

            try (PreparedStatement pst = conexion.prepareStatement(sql)) {
                pst.setString(1, identificador);
                pst.setString(2, clave);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        usuario = new Usuario();
                        
                        // 1. Asignación de datos del Usuario
                        usuario.setNombres(rs.getString("nombres").trim());
                        usuario.setApellidos(rs.getString("apellidos").trim());
                        usuario.setCorreoElectronico(rs.getString("correoelectronico").trim());

                        // 2. Asignación del TipoUsuario
                        TipoUsuario tipo = new TipoUsuario();
                        tipo.setCargo(rs.getString("cargo").trim());
                        usuario.setTipoUsuario(tipo);

                        // 3. Asignación de Password (Crucial para el ID numérico)
                        Password password = new Password();
                        password.setIdentificador(rs.getString("identificador").trim());
                        password.setClaveAcceso(rs.getString("claveacceso").trim());
                        usuario.setPassword(password); // <--- REQUIERE setPassword() en capa_modelo.Usuario
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener usuario: " + e.getMessage());
        }
        return usuario;
    }
}