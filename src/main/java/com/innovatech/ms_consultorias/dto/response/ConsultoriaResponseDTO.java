package com.innovatech.ms_consultorias.dto.response;

import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultoriaResponseDTO {

    private Long id;
    private Long usuarioId;
    private String tema;
    private String descripcion;
    private LocalDateTime fechaSolicitud;
    private LocalDate fechaSugerida;
    private EstadoConsultoria estado;
}
