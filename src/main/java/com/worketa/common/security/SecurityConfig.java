package com.worketa.common.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Deprecated  // Replaced by SecurityConfigProd
// @Configuration  // Disabled - use SecurityConfigProd instead
@EnableConfigurationProperties(JwtProperties.class)
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        // NoOp authentication manager - we handle auth via JWT
        return authentication -> authentication;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
            .csrf(csrf -> csrf.disable())
            
            .httpBasic(basic -> basic.disable())
            
            .formLogin(form -> form.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                    // Public endpoints
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers("/api/organisations/register").permitAll()
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/refresh").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    // All other endpoints require authentication
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
                })
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}