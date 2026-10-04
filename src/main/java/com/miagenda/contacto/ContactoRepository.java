package com.miagenda.contacto;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findAllByOrderByIdAsc();
}
