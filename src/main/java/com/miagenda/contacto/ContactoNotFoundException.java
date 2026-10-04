package com.miagenda.contacto;

public class ContactoNotFoundException extends RuntimeException {

    public ContactoNotFoundException() {
        super("Contacto no encontrado");
    }
}
