package com.innovatech.ms_consultorias.exception;

public class ConsultoriaNotFoundException extends RuntimeException {

    public ConsultoriaNotFoundException(String message) {
        super(message);
    }

    public ConsultoriaNotFoundException(Long id) {
        super("Consultoría no encontrada con ID: " + id);
    }
}
