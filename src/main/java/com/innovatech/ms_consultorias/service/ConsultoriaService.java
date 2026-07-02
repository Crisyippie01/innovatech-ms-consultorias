package com.innovatech.ms_consultorias.service;

import com.innovatech.ms_consultorias.dto.request.ConsultoriaRequestDTO;
import com.innovatech.ms_consultorias.dto.response.ConsultoriaEventDTO;
import com.innovatech.ms_consultorias.dto.response.ConsultoriaResponseDTO;
import com.innovatech.ms_consultorias.exception.ConsultoriaNotFoundException;
import com.innovatech.ms_consultorias.model.Consultoria;
import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import com.innovatech.ms_consultorias.repository.ConsultoriaRepository;
import com.innovatech.ms_consultorias.security.AuthenticatedUser;
import com.innovatech.ms_consultorias.security.SecurityUtils;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultoriaService {

    private final ConsultoriaRepository consultoriaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SecurityUtils securityUtils;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Transactional
    public ConsultoriaResponseDTO registrarConsultoria(ConsultoriaRequestDTO requestDTO, AuthenticatedUser currentUser) {
        Long resolvedUserId = resolveTargetUserId(requestDTO.getUsuarioId(), currentUser);
        log.info("Registrando consultoria para usuario autenticado: {} (request usuarioId={})", currentUser.userId(), requestDTO.getUsuarioId());

        Consultoria consultoria = Consultoria.builder()
                .usuarioId(resolvedUserId)
                .tema(requestDTO.getTema())
                .descripcion(requestDTO.getDescripcion())
                .fechaSugerida(requestDTO.getFechaSugerida())
                .estado(EstadoConsultoria.PENDIENTE)
                .build();

        Consultoria savedConsultoria = consultoriaRepository.save(consultoria);
        log.info("Consultoria guardada con ID: {}", savedConsultoria.getId());

        emitirEventoConsultoriaSolicitada(savedConsultoria);

        return mapToResponseDTO(savedConsultoria);
    }

    @Transactional(readOnly = true)
    public List<ConsultoriaResponseDTO> listarConsultoriasPorUsuario(Long usuarioId, AuthenticatedUser currentUser) {
        securityUtils.requireOwnerOrAdmin(currentUser, usuarioId);
        log.info("Listando consultorias para usuario: {}", usuarioId);
        return consultoriaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultoriaResponseDTO> listarConsultoriasPorUsuarioYEstado(Long usuarioId, EstadoConsultoria estado, AuthenticatedUser currentUser) {
        securityUtils.requireOwnerOrAdmin(currentUser, usuarioId);
        log.info("Listando consultorias para usuario: {} con estado: {}", usuarioId, estado);
        return consultoriaRepository.findByUsuarioIdAndEstado(usuarioId, estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultoriaResponseDTO> listarConsultoriasPorEstado(EstadoConsultoria estado) {
        log.info("Listando consultorias con estado: {}", estado);
        return consultoriaRepository.findByEstado(estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultoriaResponseDTO obtenerConsultoriaPorId(Long id, AuthenticatedUser currentUser) {
        log.info("Buscando consultoria con ID: {}", id);
        Consultoria consultoria = consultoriaRepository.findById(id)
                .orElseThrow(() -> new ConsultoriaNotFoundException(id));
        securityUtils.requireOwnerOrAdmin(currentUser, consultoria.getUsuarioId());
        return mapToResponseDTO(consultoria);
    }

    @Transactional
    public ConsultoriaResponseDTO actualizarEstado(Long id, EstadoConsultoria nuevoEstado, AuthenticatedUser currentUser) {
        log.info("Actualizando consultoria {} a estado: {}", id, nuevoEstado);

        Consultoria consultoria = consultoriaRepository.findById(id)
                .orElseThrow(() -> new ConsultoriaNotFoundException(id));

        if (!currentUser.hasRole("ADMIN")) {
            securityUtils.requireOwnerOrAdmin(currentUser, consultoria.getUsuarioId());
        }

        consultoria.setEstado(nuevoEstado);
        Consultoria updatedConsultoria = consultoriaRepository.save(consultoria);

        log.info("Consultoria {} actualizada a estado: {}", id, nuevoEstado);
        return mapToResponseDTO(updatedConsultoria);
    }

    public List<ConsultoriaResponseDTO> listarMisConsultorias(AuthenticatedUser currentUser) {
        return listarConsultoriasPorUsuario(currentUser.userId(), currentUser);
    }

    private Long resolveTargetUserId(Long requestUserId, AuthenticatedUser currentUser) {
        if (currentUser.hasRole("ADMIN")) {
            if (requestUserId == null) {
                throw new IllegalArgumentException("usuarioId es obligatorio para operaciones administrativas");
            }
            return requestUserId;
        }

        if (requestUserId != null && !Objects.equals(requestUserId, currentUser.userId())) {
            log.warn("Se ignoró usuarioId={} enviado por frontend; se usará el usuario autenticado={}", requestUserId, currentUser.userId());
        }

        return currentUser.userId();
    }

    private void emitirEventoConsultoriaSolicitada(Consultoria consultoria) {
        ConsultoriaEventDTO eventDTO = ConsultoriaEventDTO.builder()
                .id(consultoria.getId())
                .usuarioId(consultoria.getUsuarioId())
                .tema(consultoria.getTema())
                .descripcion(consultoria.getDescripcion())
                .fechaSolicitud(consultoria.getFechaSolicitud())
                .fechaSugerida(consultoria.getFechaSugerida())
                .estado(consultoria.getEstado())
                .eventType("Consultoria_Solicitada")
                .build();

        log.info("Emitiendo evento Consultoria_Solicitada para consultoria ID: {}", consultoria.getId());
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, eventDTO);
            log.info("Evento Consultoria_Solicitada emitido exitosamente");
        } catch (Exception e) {
            log.warn("No se pudo emitir el evento Consultoria_Solicitada. RabbitMQ no esta disponible: {}", e.getMessage());
            log.warn("La consultoria se guardo correctamente, pero el evento no fue propagado a otros servicios");
        }
    }

    private ConsultoriaResponseDTO mapToResponseDTO(Consultoria consultoria) {
        return ConsultoriaResponseDTO.builder()
                .id(consultoria.getId())
                .usuarioId(consultoria.getUsuarioId())
                .tema(consultoria.getTema())
                .descripcion(consultoria.getDescripcion())
                .fechaSolicitud(consultoria.getFechaSolicitud())
                .fechaSugerida(consultoria.getFechaSugerida())
                .estado(consultoria.getEstado())
                .build();
    }
}
