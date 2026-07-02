package com.innovatech.ms_consultorias.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultoriaRequestDTO {

    @Positive(message = "El ID del usuario debe ser mayor a cero")
    private Long usuarioId;

    @NotBlank(message = "El tema es obligatorio")
    @Size(max = 200, message = "El tema no puede exceder los 200 caracteres")
    private String tema;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha sugerida es obligatoria")
    @FutureOrPresent(message = "La fecha sugerida debe ser presente o futura")
    private LocalDate fechaSugerida;
}
