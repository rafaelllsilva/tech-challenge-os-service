package com.fiap.techchallenge.os.observabilidade;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Coloca o correlationId no MDC e o devolve no header X-Correlation-Id. Nas rotas
 * /api/ordemDeServico/{id}/..., o osId é o correlationId (o mesmo usado nos eventos da saga);
 * nas demais, vale o header recebido ou um UUID gerado.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private static final Pattern ROTA_DA_OS = Pattern.compile("^/api/ordemDeServico/(\\d+)(/.*)?$");
    private static final int TAMANHO_MAXIMO = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = resolver(request);
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String resolver(HttpServletRequest request) {
        Matcher rota = ROTA_DA_OS.matcher(request.getRequestURI());
        if (rota.matches()) {
            return rota.group(1);
        }
        String recebido = request.getHeader(HEADER);
        if (recebido != null && !recebido.isBlank() && recebido.length() <= TAMANHO_MAXIMO
                && recebido.matches("[A-Za-z0-9._-]+")) {
            return recebido;
        }
        return UUID.randomUUID().toString();
    }
}
