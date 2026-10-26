package dev.jppb.bddestudio.application.exception;

public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(Long usuarioId) {
        super("Usuário " + usuarioId + " não encontrado: informe um X-Usuario-Id válido");
    }
}
