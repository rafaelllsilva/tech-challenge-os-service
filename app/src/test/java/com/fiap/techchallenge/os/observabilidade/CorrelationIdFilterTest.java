package com.fiap.techchallenge.os.observabilidade;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filtro = new CorrelationIdFilter();

    private String executar(String uri, String headerRecebido, MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (headerRecebido != null) {
            request.addHeader(CorrelationIdFilter.HEADER, headerRecebido);
        }
        AtomicReference<String> noMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> noMdc.set(MDC.get(CorrelationIdFilter.MDC_KEY));
        filtro.doFilter(request, response, chain);
        return noMdc.get();
    }

    @Test
    void usaOOsIdNasRotasDaOrdemDeServico() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String noMdc = executar("/api/ordemDeServico/42/aprovacao", "outro-id", response);

        assertThat(noMdc).isEqualTo("42");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("42");
    }

    @Test
    void reaproveitaOHeaderRecebidoNasDemaisRotas() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String noMdc = executar("/api/clientes", "abc-123", response);

        assertThat(noMdc).isEqualTo("abc-123");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("abc-123");
    }

    @Test
    void geraUmIdQuandoNaoHaHeaderOuQuandoEleEhInvalido() throws Exception {
        MockHttpServletResponse semHeader = new MockHttpServletResponse();
        MockHttpServletResponse invalido = new MockHttpServletResponse();

        String gerado = executar("/api/clientes", null, semHeader);
        String descartado = executar("/api/clientes", "x y\nz", invalido);

        assertThat(gerado).isNotBlank().isEqualTo(semHeader.getHeader(CorrelationIdFilter.HEADER));
        assertThat(descartado).isNotEqualTo("x y\nz").isEqualTo(invalido.getHeader(CorrelationIdFilter.HEADER));
    }

    @Test
    void limpaOMdcAoFinal() throws Exception {
        executar("/api/ordemDeServico/7", null, new MockHttpServletResponse());

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
