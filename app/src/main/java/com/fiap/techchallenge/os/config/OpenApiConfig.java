package com.fiap.techchallenge.os.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OS Service — Tech Challenge Fase 4")
                        .description("Serviço core da oficina: clientes, veículos, autenticação e ordens de serviço. "
                                + "Participa da saga coreografada de orçamento, pagamento e execução trocando eventos "
                                + "com o billing-service e o execution-service via SNS/SQS (padrão Outbox, consumidores "
                                + "idempotentes e correlationId = id da OS). Consulte GET /api/ordemDeServico/{id}/historico "
                                + "para acompanhar a saga de uma OS.")
                        .version("2.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Informe o access token JWT obtido em POST /api/auth/login")));
    }

}
