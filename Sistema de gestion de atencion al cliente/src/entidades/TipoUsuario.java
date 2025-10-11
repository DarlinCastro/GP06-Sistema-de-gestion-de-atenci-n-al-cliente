/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

/**
 *
 * @author RYZEN
 */
public class TipoUsuario {

    private String cargo;

    public TipoUsuario() {
    }

    public TipoUsuario(String cargo) {
        this.cargo = cargo != null ? cargo.trim() : "";
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    @Override
    public String toString() {
        return cargo;
    }
}
