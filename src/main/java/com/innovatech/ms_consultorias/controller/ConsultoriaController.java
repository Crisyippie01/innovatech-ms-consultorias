package com.innovatech.ms_consultorias.controller;

import com.innovatech.ms_consultorias.dto.request.ConsultoriaRequestDTO;
import com.innovatech.ms_consultorias.dto.response.ConsultoriaResponseDTO;
import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import com.innovatech.ms_consultorias.security.AuthenticatedUser;
import com.innovatech.ms_consultorias.security.SecurityUtils;
import com.innovatech.ms_consultorias.service.ConsultoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consultorias")
@RequiredArgsConstructor
@Validated
@Tag(name = "Consultorias", description = "API para gestion de consultorias")
public class ConsultoriaController {

    private final ConsultoriaService consultoriaService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Registrar nueva consultoria", description = "Crea una nueva solicitud de consultoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consultoria creada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validacion en los datos de entrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<ConsultoriaResponseDTO> registrarConsultoria(
            @Valid @RequestBody ConsultoriaRequestDTO requestDTO,
            Authentication authentication) {
        AuthenticatedUser currentUser = securityUtils.requireUser(authentication);
        ConsultoriaResponseDTO response = consultoriaService.registrarConsultoria(requestDTO, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener consultoria por ID", description = "Recupera los detalles de una consultoria especifica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consultoria encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consultoria no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultoriaResponseDTO> obtenerConsultoria(
            @Parameter(description = "ID de la consultoria")
            @PathVariable @Positive(message = "El id debe ser mayor a cero") Long id,
            Authentication authentication) {
        AuthenticatedUser currentUser = securityUtils.requireUser(authentication);
        ConsultoriaResponseDTO response = consultoriaService.obtenerConsultoriaPorId(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar consultorias por usuario", description = "Obtiene el historial de consultorias de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de consultorias recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarConsultoriasPorUsuario(
            @Parameter(description = "ID del usuario")
            @PathVariable @Positive(message = "El usuarioId debe ser mayor a cero") Long usuarioId,
            Authentication authentication) {
        AuthenticatedUser currentUser = securityUtils.requireUser(authentication);
        List<ConsultoriaResponseDTO> response = consultoriaService.listarConsultoriasPorUsuario(usuarioId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarMisConsultorias(Authentication authentication) {
        AuthenticatedUser currentUser = securityUtils.requireUser(authentication);
        return ResponseEntity.ok(consultoriaService.listarMisConsultorias(currentUser));
    }

    @GetMapping(params = "estado")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarConsultoriasPorEstado(@RequestParam EstadoConsultoria estado) {
        return ResponseEntity.ok(consultoriaService.listarConsultoriasPorEstado(estado));
    }

    @GetMapping("/usuario/{usuarioId}/historial")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarHistorialPorUsuario(
            @Parameter(description = "ID del usuario")
            @PathVariable @Positive(message = "El usuarioId debe ser mayor a cero") Long usuarioId,
            Authentication authentication) {
        return listarConsultoriasPorUsuario(usuarioId, authentication);
    }

    @Operation(summary = "Listar consultorias por usuario y estado", description = "Filtra consultorias de un usuario por estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de consultorias filtrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Estado invalido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    public ResponseEntity<List<ConsultoriaResponseDTO>> listarConsultoriasPorUsuarioYEstado(
            @Parameter(description = "ID del usuario")
            @PathVariable @Positive(message = "El usuarioId debe ser mayor a cero") Long usuarioId,
            @Parameter(description = "Estado de la consultoria")
            @PathVariable EstadoConsultoria estado,
            Authentication authentication) {
        AuthenticatedUser currentUser = securityUtils.requireUser(authentication);
        List<ConsultoriaResponseDTO> response = consultoriaService.listarConsultoriasPorUsuarioYEstado(usuarioId, estado, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar estado de consultoria", description = "Cambia el estado de una consultoria existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsultoriaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consultoria no encontrada"),
            @ApiResponse(responseCode = "400", description = "Estado invalido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/{id}/estado/{estado}")
    public ResponseEntity<ConsultoriaResponseDTO> actualizarEstado(
            @Parameter(description = "ID de la consultoria")
            @PathVariable @Positive(message = "El id debe ser mayor a cero") Long id,
            @Parameter(description = "Nuevo estado")
            @PathVariable EstadoConsultoria estado,
            Authentication authentication) {
        AuthenticatedUser currentUser = securityUtils.requireUser(authentication);
        ConsultoriaResponseDTO response = consultoriaService.actualizarEstado(id, estado, currentUser);
        return ResponseEntity.ok(response);
    }
}
