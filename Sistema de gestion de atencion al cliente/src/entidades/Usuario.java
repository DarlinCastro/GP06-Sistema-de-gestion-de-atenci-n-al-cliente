/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author erick
 */
public class Usuario {
    private TipoUsuario tipoUsuario;
    private Password password;
    private String nombres;
    private String apellidos;
    private String correoElectronico;
    private int id;  // Campo agregado para coincidir con la BD
    private String cargo;  // Campo agregado para coincidir con la BD
    private String identificador;  // Campo agregado para coincidir con la BD
    private String contrasena;  // Campo agregado (string plano; integra con Password si es necesario)
    
    

    // --- CONSTRUCTORES ---
    public Usuario() {
    
    }

    public Usuario(int id, String nombres, String apellidos, String correoElectronico, TipoUsuario tipoUsuario, Password password) {
        this.nombres = nombres != null ? nombres.trim() : "";
        this.apellidos = apellidos != null ? apellidos.trim() : "";
        this.correoElectronico = correoElectronico != null ? correoElectronico.trim() : "";
        this.id = id;
        this.tipoUsuario = tipoUsuario; // Asignación del objeto TipoUsuario
        this.password = password;       // Asignación del objeto Password
    }
    
    
    
    

    // --- GETTERS (Clave para que el Controlador acceda a los datos) ---

    public int getId() {
        return id;
    }

    public void setId(int Id) {
        this.id = Id;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
        // Ejemplo: if (this.password != null) { this.password.setValor(contrasena); }
    }
    
    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    // Getters de las composiciones
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public Password getPassword() {
        return password;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    // --- Método toString (Clave para los JComboBox de la vista) ---
    /*
     * Devuelve el nombre completo, usado para mostrar en las interfaces de usuario.
     */
    @Override
    public String toString() {
        return nombres + " " + apellidos;
}
    
}
