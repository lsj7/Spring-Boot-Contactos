package com.miagenda.web;

import com.miagenda.contacto.ContactoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ContactoPageController {

    private final ContactoService contactoService;

    public ContactoPageController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/views/agenda";
    }

    @GetMapping("/views/agenda")
    public String agenda(Model model) {
        model.addAttribute("contactos", contactoService.listar());
        return "agenda";
    }

    @GetMapping("/views/contacts/new")
    public String nuevo(Model model) {
        model.addAttribute("pageTitle", "Nuevo contacto");
        model.addAttribute("contacto", null);
        return "contact_form";
    }

    @GetMapping("/views/contacts/{id}/edit")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Editar contacto");
        model.addAttribute("contacto", contactoService.obtener(id));
        return "contact_form";
    }
}
