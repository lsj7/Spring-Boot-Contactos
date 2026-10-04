package com.miagenda;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgendaMvcTest extends AgendaSpringTest {

    private static final String CONTACTO = """
            {"nombre":"Ana López","telefono":"8095551234","mail":"ana@ejemplo.com"}
            """;

    @Test
    void muestraListadoVacioFormularioYContacto() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/views/agenda").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Buscar por nombre o correo")))
                .andExpect(content().string(containsString("Aún no hay contactos")))
                .andExpect(content().string(containsString("Salir")));

        mockMvc.perform(get("/views/contacts/new").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nuevo contacto")))
                .andExpect(content().string(containsString("data-feedback")))
                .andExpect(content().string(containsString("Guardar contacto")));

        String token = csrfToken(session);
        mockMvc.perform(post("/agenda")
                        .session(session)
                        .header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONTACTO))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/views/agenda").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-contact-row")))
                .andExpect(content().string(containsString("Ana López")))
                .andExpect(content().string(containsString("Eliminar")));

        String editPath = mockMvc.perform(get("/views/agenda").session(session))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String editUrl = editPath.replaceAll("(?s).*href=\"(/views/contacts/\\d+/edit)\".*", "$1");

        mockMvc.perform(get(editUrl).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar contacto")))
                .andExpect(content().string(containsString("value=\"Ana López\"")));

        mockMvc.perform(get("/views/contacts/999999/edit").session(session))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Contacto no encontrado")));
    }

    @Test
    void creaActualizaYElimina() throws Exception {
        MockHttpSession session = login();
        String token = csrfToken(session);

        String body = mockMvc.perform(post("/agenda")
                        .session(session)
                        .header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONTACTO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Ana López"))
                .andExpect(jsonPath("$.telefono").value("8095551234"))
                .andExpect(jsonPath("$.mail").value("ana@ejemplo.com"))
                .andExpect(header().string("X-Process-Time", matchesPattern("\\d+\\.\\d{4} s")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number id = com.jayway.jsonpath.JsonPath.read(body, "$.id");

        mockMvc.perform(get("/agenda").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].nombre").value("Ana López"));

        mockMvc.perform(get("/agenda/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value("ana@ejemplo.com"));

        mockMvc.perform(put("/agenda/" + id)
                        .session(session)
                        .header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefono\":\"8095559999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana López"))
                .andExpect(jsonPath("$.telefono").value("8095559999"))
                .andExpect(jsonPath("$.mail").value("ana@ejemplo.com"));

        mockMvc.perform(delete("/agenda/" + id)
                        .session(session)
                        .header("X-CSRF-TOKEN", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contacto " + id + " eliminado correctamente"));

        mockMvc.perform(get("/agenda").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rechazaDatosInvalidos() throws Exception {
        MockHttpSession session = login();
        String token = csrfToken(session);

        mockMvc.perform(post("/agenda")
                        .session(session)
                        .header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"A\",\"telefono\":\"123\",\"mail\":\"no-es-correo\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").isArray())
                .andExpect(jsonPath("$.detail[?(@.msg == 'El nombre debe tener entre 2 y 50 caracteres')]").exists())
                .andExpect(jsonPath("$.detail[?(@.msg == 'El teléfono debe tener entre 7 y 15 dígitos')]").exists())
                .andExpect(jsonPath("$.detail[?(@.msg == 'El correo no es válido')]").exists());

        mockMvc.perform(get("/agenda").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void responde404SiElContactoNoExiste() throws Exception {
        MockHttpSession session = login();
        String token = csrfToken(session);

        mockMvc.perform(get("/agenda/404").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Contacto no encontrado"));

        mockMvc.perform(put("/agenda/404")
                        .session(session)
                        .header("X-CSRF-TOKEN", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana López\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Contacto no encontrado"));

        mockMvc.perform(delete("/agenda/404")
                        .session(session)
                        .header("X-CSRF-TOKEN", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Contacto no encontrado"));
    }
}
