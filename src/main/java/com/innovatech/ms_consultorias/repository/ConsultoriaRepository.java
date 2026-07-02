package com.innovatech.ms_consultorias.repository;

import com.innovatech.ms_consultorias.model.Consultoria;
import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultoriaRepository extends JpaRepository<Consultoria, Long> {

    List<Consultoria> findByUsuarioId(Long usuarioId);

    List<Consultoria> findByUsuarioIdAndEstado(Long usuarioId, EstadoConsultoria estado);

    List<Consultoria> findByEstado(EstadoConsultoria estado);
}
