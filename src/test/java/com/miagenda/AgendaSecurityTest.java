package com.miagenda;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgendaSecurityTest extends AgendaSpringTest {

    @Test
    void muestraElLoginYLosEstaticosSinSesion() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Iniciar sesión")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        mockMvc.perform(get("/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("--green")));

        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk());
    }

    @Test
    void rechazaCredencialesIncorrectas() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin").password("incorrecta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void exigeSesionParaPaginasYApi() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/views/agenda"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/views/contacts/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/agenda"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("No autenticado"));

        mockMvc.perform(post("/agenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana López\",\"telefono\":\"8095551234\",\"mail\":\"ana@ejemplo.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cierraLaSesion() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());

        mockMvc.perform(get("/views/agenda").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void rechazaCambiosSinCsrf() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/agenda")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana López\",\"telefono\":\"8095551234\",\"mail\":\"ana@ejemplo.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No se pudo validar la solicitud"));
    }
}
