package com.fiap.techchallenge.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret, accessTokenExpiration);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil(), userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrfCustomizer -> csrfCustomizer.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Público
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Criação de usuário requer GERENTE
                        .requestMatchers(HttpMethod.POST, "/api/auth/usuario").hasRole("GERENTE")

                        // CLIENTE
                        .requestMatchers(HttpMethod.GET,
                                "/api/ordemDeServico/cliente/**")
                        .hasAnyRole("CLIENTE", "ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/veiculo/**")
                        .hasAnyRole("CLIENTE", "ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/cliente/**")
                        .hasAnyRole("CLIENTE", "ATENDENTE", "GERENTE")

                        // Webhooks (deve vir antes das regras genéricas de ordemDeServico)
                        .requestMatchers(HttpMethod.POST,
                                "/api/ordemDeServico/transicao/paraDiagnostico/**",
                                "/api/ordemDeServico/transicao/paraAprovacao/**",
                                "/api/ordemDeServico/transicao/paraAprovacaoOuRejeicaoCliente/**",
                                "/api/ordemDeServico/transicao/paraEmExecucao/**",
                                "/api/ordemDeServico/transicao/paraFinalizada/**",
                                "/api/ordemDeServico/transicao/paraEntregue/**").permitAll()

                        // MECANICO
                        .requestMatchers(HttpMethod.PUT,
                                "/api/ordemDeServico/adicionarServicos/**",
                                "/api/ordemDeServico/adicionarPecas/**")
                        .hasAnyRole("MECANICO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/ordemDeServico/removerServicos/**",
                                "/api/ordemDeServico/removerPecas/**")
                        .hasAnyRole("MECANICO", "GERENTE")

                        // ATENDENTE
                        .requestMatchers("/api/cliente/**").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers("/api/veiculo/**").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers("/api/ordemDeServico/**").hasAnyRole("ATENDENTE", "GERENTE")

                        // GERENTE
                        .requestMatchers("/api/peca/**").hasRole("GERENTE")
                        .requestMatchers("/api/tipoPeca/**").hasRole("GERENTE")
                        .requestMatchers("/api/servico/**").hasRole("GERENTE")
                        .requestMatchers("/api/tipoServico/**").hasRole("GERENTE")
                        .requestMatchers("/api/usuario/**").hasRole("GERENTE")

                        .anyRequest().authenticated()
                )
                .headers(
                        headers -> headers.crossOriginResourcePolicy(
                        corp -> corp.policy(CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy.SAME_ORIGIN)
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
