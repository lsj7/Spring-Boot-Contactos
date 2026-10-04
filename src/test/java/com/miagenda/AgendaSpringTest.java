package com.miagenda;

import com.miagenda.contacto.ContactoRepository;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.HtmlUtils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class AgendaSpringTest {

    static final Path DATABASE = Path.of(
            System.getProperty("java.io.tmpdir"),
            "agenda-spring-" + ProcessHandle.current().pid() + ".db");

    private static final Pattern CSRF_TOKEN = Pattern.compile("name=\"csrf-token\" content=\"([^\"]+)\"");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ContactoRepository contactoRepository;

    @Autowired
    DataSource dataSource;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        registry.add("app.admin.username", () -> "admin");
        registry.add("app.admin.password", () -> "clave-segura");
    }

    @BeforeEach
    void vaciarAgenda() {
        contactoRepository.deleteAll();
    }

    protected MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(formLogin("/login").user("admin").password("clave-segura"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/views/agenda"))
                .andExpect(authenticated())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected String csrfToken(MockHttpSession session) throws Exception {
        MvcResult page = mockMvc.perform(get("/views/agenda").session(session))
                .andExpect(status().isOk())
                .andReturn();
        Matcher matcher = CSRF_TOKEN.matcher(page.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("La página no incluye el token CSRF");
        }
        return HtmlUtils.htmlUnescape(matcher.group(1));
    }
}
