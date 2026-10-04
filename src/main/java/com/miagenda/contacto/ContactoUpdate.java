package com.miagenda.contacto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactoUpdate {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre es obligatorio")
    private String nombre;

    @Pattern(regexp = "^\\d{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String telefono;

    @Size(max = 254, message = "El correo es demasiado largo")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "El correo no es válido")
    private String mail;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
