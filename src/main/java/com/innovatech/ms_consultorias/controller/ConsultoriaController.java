package com.innovatech.ms_consultorias.controller;

import com.innovatech.ms_consultorias.dto.request.ConsultoriaRequestDTO;
import com.innovatech.ms_consultorias.dto.response.ConsultoriaResponseDTO;
import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import com.innovatech.ms_consultorias.service.ConsultoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultorias")
@RequiredArgsConstructor
@Tag(name = "Consultorías", description = "API para gestión de consultorías")
public class ConsultoriaController {

    private final ConsultoriaService consultoriaService;

    @Operation(summary = "Registrar nueva consultoría", description = "Crea una nueva solicitud de consultoría y emite evento Consultoria_Solicitada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consultoría creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en los datos de entrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<ConsultoriaResponseDTO> registrarConsultoria(
            @Valid @RequestBody ConsultoriaRequestDTO requestDTO) {
        ConsultoriaResponseDTO response = consultoriaService.registrarConsultoria(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener consultoría por ID", description = "Recupera los detalles de una consultoría específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultoría encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consultoría no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultoriaResponseDTO> obtenerConsultoria(
            @Parameter(description = "ID de la consultoría") @PathVariable Long id) {
        ConsultoriaResponseDTO response = consultoriaService.obtenerConsultoriaPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar consultorías por usuario", description = "Obtiene el historial de consultorías de un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de consultorías recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarConsultoriasPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        List<ConsultoriaResponseDTO> response = consultoriaService.listarConsultoriasPorUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar consultorías por usuario y estado", description = "Filtra consultorías de un usuario por estado específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de consultorías filtrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarConsultoriasPorUsuarioYEstado(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
            @Parameter(description = "Estado de la consultoría (PENDIENTE, APROBADA, FINALIZADA, CANCELADA)")
            @PathVariable EstadoConsultoria estado) {
        List<ConsultoriaResponseDTO> response = consultoriaService.listarConsultoriasPorUsuarioYEstado(usuarioId, estado);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar estado de consultoría", description = "Cambia el estado de una consultoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consultoría no encontrada"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/{id}/estado/{estado}")
    public ResponseEntity<ConsultoriaResponseDTO> actualizarEstado(
            @Parameter(description = "ID de la consultoría") @PathVariable Long id,
            @Parameter(description = "Nuevo estado (PENDIENTE, APROBADA, FINALIZADA, CANCELADA)")
            @PathVariable EstadoConsultoria estado) {
        ConsultoriaResponseDTO response = consultoriaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(response);
    }
}
