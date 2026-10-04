package com.miagenda.contacto;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactoService {

    private final ContactoRepository repository;

    public ContactoService(ContactoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Contacto> listar() {
        return repository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Contacto obtener(Long id) {
        return repository.findById(id).orElseThrow(ContactoNotFoundException::new);
    }

    @Transactional
    public Contacto crear(ContactoCreate datos) {
        Contacto contacto = new Contacto();
        contacto.setNombre(datos.getNombre());
        contacto.setTelefono(datos.getTelefono());
        contacto.setMail(datos.getMail());
        return repository.save(contacto);
    }

    @Transactional
    public Contacto actualizar(Long id, ContactoUpdate datos) {
        Contacto contacto = obtener(id);
        if (datos.getNombre() != null) {
            contacto.setNombre(datos.getNombre());
        }
        if (datos.getTelefono() != null) {
            contacto.setTelefono(datos.getTelefono());
        }
        if (datos.getMail() != null) {
            contacto.setMail(datos.getMail());
        }
        return repository.save(contacto);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}
