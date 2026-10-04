package com.miagenda;

import com.miagenda.contacto.Contacto;
import com.miagenda.contacto.ContactoCreate;
import com.miagenda.contacto.ContactoService;
import com.miagenda.contacto.ContactoUpdate;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactoPersistenceTest extends AgendaSpringTest {

    @Autowired
    private ContactoService contactoService;

    @Test
    void guardaActualizaYEliminaEnSqliteTemporal() throws Exception {
        assertTrue(DATABASE.startsWith(System.getProperty("java.io.tmpdir")));
        assertTrue(Files.exists(DATABASE));
        assertTrue(Files.size(DATABASE) > 0);

        ContactoCreate datos = new ContactoCreate();
        datos.setNombre("Ana López");
        datos.setTelefono("8095551234");
        datos.setMail("ana@ejemplo.com");
        Contacto creado = contactoService.crear(datos);

        assertFila(creado.getId(), "Ana López", "8095551234", "ana@ejemplo.com");

        ContactoUpdate cambios = new ContactoUpdate();
        cambios.setTelefono("8095550000");
        contactoService.actualizar(creado.getId(), cambios);

        assertFila(creado.getId(), "Ana López", "8095550000", "ana@ejemplo.com");

        contactoService.eliminar(creado.getId());
        assertFalse(existe(creado.getId()));
    }

    private void assertFila(Long id, String nombre, String telefono, String mail) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "select nombre, telefono, mail from contacto where id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                assertEquals(nombre, rows.getString("nombre"));
                assertEquals(telefono, rows.getString("telefono"));
                assertEquals(mail, rows.getString("mail"));
            }
        }
    }

    private boolean existe(Long id) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "select count(*) from contacto where id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                return rows.getInt(1) > 0;
            }
        }
    }
}
