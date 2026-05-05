package com.innovatech.ms_consultorias.model;

import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 200)
    private String tema;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_sugerida", nullable = false)
    private LocalDate fechaSugerida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConsultoria estado;

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoConsultoria.PENDIENTE;
        }
    }
}
