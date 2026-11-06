package dev.jppb.bddestudio;

import dev.jppb.bddestudio.application.exception.RegraNegocioException;
import dev.jppb.bddestudio.application.service.DadosPasso;
import dev.jppb.bddestudio.application.service.ValidadorPassos;
import dev.jppb.bddestudio.domain.model.TipoPasso;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static dev.jppb.bddestudio.domain.model.TipoPasso.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidadorPassosTest {

    private static void validar(TipoPasso... tipos) {
        ValidadorPassos.validar(Arrays.stream(tipos).map(t -> new DadosPasso(t, "texto")).toList());
    }

    @Test
    void cenarioCompleto_aceito() {
        assertThatCode(() -> validar(DADO, QUANDO, ENTAO, E, MAS)).doesNotThrowAnyException();
    }

    @Test
    void semDado_aceito() {
        assertThatCode(() -> validar(QUANDO, ENTAO)).doesNotThrowAnyException();
    }

    @Test
    void eMasContinuamOTipoAnterior_aceito() {
        assertThatCode(() -> validar(DADO, E, QUANDO, MAS, ENTAO, E)).doesNotThrowAnyException();
    }

    @Test
    void semEntao_rejeitado() {
        assertThatThrownBy(() -> validar(DADO, QUANDO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Então");
    }

    @Test
    void semQuando_rejeitado() {
        assertThatThrownBy(() -> validar(DADO, ENTAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Quando");
    }

    @Test
    void listaVazia_rejeitada() {
        assertThatThrownBy(() -> validar())
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void foraDeOrdem_rejeitado() {
        assertThatThrownBy(() -> validar(QUANDO, DADO, ENTAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ordem");
    }

    @Test
    void quandoDepoisDeEntao_rejeitado() {
        assertThatThrownBy(() -> validar(QUANDO, ENTAO, E, QUANDO))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void iniciadoPorE_rejeitado() {
        assertThatThrownBy(() -> validar(E, QUANDO, ENTAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pode começar");
    }

    @Test
    void iniciadoPorMas_rejeitado() {
        assertThatThrownBy(() -> validar(MAS, QUANDO, ENTAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pode começar");
    }
}
