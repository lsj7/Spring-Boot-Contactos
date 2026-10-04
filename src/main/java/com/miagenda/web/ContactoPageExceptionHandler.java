package com.miagenda.web;

import com.miagenda.contacto.ContactoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = ContactoPageController.class)
public class ContactoPageExceptionHandler {

    @ExceptionHandler(ContactoNotFoundException.class)
    public ModelAndView contactoNoEncontrado() {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("status", 404);
        return modelAndView;
    }
}
