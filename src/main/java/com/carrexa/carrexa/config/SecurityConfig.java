package com.carrexa.carrexa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import java.util.*;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import java.time.Duration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${keycloak.client-id}")
    private String clientId; // Imports "carrexa.ai" from application.yml

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8090/realms/Carrexa/protocol/openid-connect/certs")
                .jwsAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                // Give Keycloak 30 seconds to respond (Fixes the timeout)
                .restOperations(new org.springframework.web.client.RestTemplate() {{
                    setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                        setConnectTimeout(30000); // 30 seconds
                        setReadTimeout(30000);    // 30 seconds
                    }});
                }})
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // 1. Get the "resource_access" claim (where client roles live)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            // 2. Check if our specific Client ID exists in the token
            if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);

                // 3. Extract the roles list
                if (clientAccess != null && clientAccess.containsKey("roles")) {
                    List<String> roles = (List<String>) clientAccess.get("roles");

                    // 4. Convert them to Authorities (ROLE_RECRUITER, ROLE_USER)
                    roles.forEach(role ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    );
                }
            }
            return authorities;
        });
        return converter;
    }
}