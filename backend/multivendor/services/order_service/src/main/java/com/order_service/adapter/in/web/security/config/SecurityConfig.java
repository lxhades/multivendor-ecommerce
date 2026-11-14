package com.order_service.adapter.in.web.security.config;


import com.order_service.adapter.in.web.security.handler.JwtAccessDeniedHandler;
import com.order_service.adapter.in.web.security.handler.JwtAuthenticationEntryPoint;
import com.order_service.adapter.in.web.security.jwt.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )


                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness").permitAll()

                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("BUYER")
                                .requestMatchers(HttpMethod.GET, "/api/reviews/products/*").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/checkouts").hasRole("BUYER")
                                .requestMatchers(HttpMethod.GET, "/api/checkouts/*").hasAnyRole("BUYER", "ADMIN")
                                .requestMatchers("/api/orders/admin", "/api/orders/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/orders/seller", "/api/orders/seller/**").hasRole("SELLER")
                                .requestMatchers("/api/orders", "/api/orders/**").hasRole("BUYER")
                                .anyRequest().denyAll()
                )


                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }
}
