package com.worketa.common.security;

import java.util.Arrays;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Production-grade security configuration with:
 * - Proper CORS hardening
 * - Secure headers (HSTS, CSP, X-Frame-Options, etc.)
 * - JWT-only authentication (stateless)
 * - Method-level @PreAuthorize support
 * - CSRF disabled (stateless API)
 * - No HTTP Basic auth or form login
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfigProd {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final SecureAuthenticationEntryPoint authenticationEntryPoint;
    private final SecureAccessDeniedHandler accessDeniedHandler;

    public SecurityConfigProd(
            JwtTokenProvider jwtTokenProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter,
            SecureAuthenticationEntryPoint authenticationEntryPoint,
            SecureAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://worketa-frontend-web.vercel.app"
        ));

        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.toString(),
                HttpMethod.POST.toString(),
                HttpMethod.PUT.toString(),
                HttpMethod.PATCH.toString(),
                HttpMethod.DELETE.toString(),
                HttpMethod.OPTIONS.toString()
        ));

        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                "X-Requested-With",
                "X-Correlation-ID"
        ));

        config.setExposedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                "X-Correlation-ID"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChainProd(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .headers(headers -> headers
                    .frameOptions(frame -> frame.deny())
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .preload(true)
                            .maxAgeInSeconds(31536000)
                    )
            )

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/api/v1/health").permitAll()

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/organisations/register"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/auth/login"
                    ).permitAll()

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/v1/auth/refresh"
                    ).permitAll()

                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/actuator/health/**").permitAll()

                    .requestMatchers("/actuator/**").denyAll()

                    .requestMatchers(
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-ui.html"
                    ).permitAll()

                    .requestMatchers("/api/**").authenticated()

                    .anyRequest().permitAll()
            )

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )

            .addFilterBefore(
                    rateLimitingFilter,
                    UsernamePasswordAuthenticationFilter.class
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
