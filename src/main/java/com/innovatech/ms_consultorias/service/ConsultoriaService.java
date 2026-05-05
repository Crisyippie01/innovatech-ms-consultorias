package com.innovatech.ms_consultorias.service;

import com.innovatech.ms_consultorias.dto.request.ConsultoriaRequestDTO;
import com.innovatech.ms_consultorias.dto.response.ConsultoriaEventDTO;
import com.innovatech.ms_consultorias.dto.response.ConsultoriaResponseDTO;
import com.innovatech.ms_consultorias.exception.ConsultoriaNotFoundException;
import com.innovatech.ms_consultorias.model.Consultoria;
import com.innovatech.ms_consultorias.model.enums.EstadoConsultoria;
import com.innovatech.ms_consultorias.repository.ConsultoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultoriaService {

    private final ConsultoriaRepository consultoriaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.consultoria:consultoria.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey.consultoria:consultoria.routingkey}")
    private String routingKey;

    @Transactional
    public ConsultoriaResponseDTO registrarConsultoria(ConsultoriaRequestDTO requestDTO) {
        log.info("Registrando nueva consultoría para usuario: {}", requestDTO.getUsuarioId());

        Consultoria consultoria = Consultoria.builder()
                .usuarioId(requestDTO.getUsuarioId())
                .tema(requestDTO.getTema())
                .descripcion(requestDTO.getDescripcion())
                .fechaSugerida(requestDTO.getFechaSugerida())
                .estado(EstadoConsultoria.PENDIENTE)
                .build();

        Consultoria savedConsultoria = consultoriaRepository.save(consultoria);
        log.info("Consultoría guardada con ID: {}", savedConsultoria.getId());

        // Emitir evento Consultoria_Solicitada
        emitirEventoConsultoriaSolicitada(savedConsultoria);

        return mapToResponseDTO(savedConsultoria);
    }

    @Transactional(readOnly = true)
    public List<ConsultoriaResponseDTO> listarConsultoriasPorUsuario(Long usuarioId) {
        log.info("Listando consultorías para usuario: {}", usuarioId);
        return consultoriaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultoriaResponseDTO> listarConsultoriasPorUsuarioYEstado(Long usuarioId, EstadoConsultoria estado) {
        log.info("Listando consultorías para usuario: {} con estado: {}", usuarioId, estado);
        return consultoriaRepository.findByUsuarioIdAndEstado(usuarioId, estado)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultoriaResponseDTO obtenerConsultoriaPorId(Long id) {
        log.info("Buscando consultoría con ID: {}", id);
        Consultoria consultoria = consultoriaRepository.findById(id)
                .orElseThrow(() -> new ConsultoriaNotFoundException(id));
        return mapToResponseDTO(consultoria);
    }

    @Transactional
    public ConsultoriaResponseDTO actualizarEstado(Long id, EstadoConsultoria nuevoEstado) {
        log.info("Actualizando consultoría {} a estado: {}", id, nuevoEstado);

        Consultoria consultoria = consultoriaRepository.findById(id)
                .orElseThrow(() -> new ConsultoriaNotFoundException(id));

        consultoria.setEstado(nuevoEstado);
        Consultoria updatedConsultoria = consultoriaRepository.save(consultoria);

        log.info("Consultoría {} actualizada a estado: {}", id, nuevoEstado);
        return mapToResponseDTO(updatedConsultoria);
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

        log.info("Emitiendo evento Consultoria_Solicitada para consultoría ID: {}", consultoria.getId());
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, eventDTO);
            log.info("Evento Consultoria_Solicitada emitido exitosamente");
        } catch (Exception e) {
            log.warn("No se pudo emitir el evento Consultoria_Solicitada. RabbitMQ no está disponible: {}", e.getMessage());
            log.warn("La consultoría se guardó correctamente, pero el evento no fue propagado a otros servicios");
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
