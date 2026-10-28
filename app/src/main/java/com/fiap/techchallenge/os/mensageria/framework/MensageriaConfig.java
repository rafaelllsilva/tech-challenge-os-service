package com.fiap.techchallenge.os.mensageria.framework;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({MensageriaProperties.class, OutboxRelayProperties.class})
public class MensageriaConfig {
}
