package com.miagenda.contacto;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/agenda", produces = MediaType.APPLICATION_JSON_VALUE)
public class AgendaController {

    private final ContactoService contactoService;

    public AgendaController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @GetMapping
    public List<Contacto> listar() {
        return contactoService.listar();
    }

    @GetMapping("/{id}")
    public Contacto obtener(@PathVariable Long id) {
        return contactoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contacto crear(@Valid @RequestBody ContactoCreate datos) {
        return contactoService.crear(datos);
    }

    @PutMapping("/{id}")
    public Contacto actualizar(@PathVariable Long id, @Valid @RequestBody ContactoUpdate datos) {
        return contactoService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Long id) {
        contactoService.eliminar(id);
        return Map.of("mensaje", "Contacto " + id + " eliminado correctamente");
    }
}
