// config/SecurityConfig.java
package id.sevenspeed.tracking.config;

import id.sevenspeed.tracking.security.JwtAuthFilter;
import id.sevenspeed.tracking.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers("/error").permitAll()

                // Swagger UI (dev only)
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()

                // Customer endpoints — satu-satunya jalur untuk role CUSTOMER
                .requestMatchers("/api/v1/me/orders/**").hasRole("CUSTOMER")

                // Operator endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/me/queue").hasAnyRole("OPERATOR", "ADMIN")

                // Orders subtree — ADMIN only. Mencakup list, detail, create,
                // update, delete, dan nested /orders/{id}/batches.
                // CUSTOMER & OPERATOR -> 403 (customer hanya boleh lewat /me/*).
                .requestMatchers("/api/v1/orders", "/api/v1/orders/**").hasRole("ADMIN")

                // Batches subtree — OPERATOR butuh baca batch & progress-event,
                // serta append progress-event untuk batch yang dikerjakannya.
                .requestMatchers(HttpMethod.POST, "/api/v1/batches/*/progress-events")
                    .hasAnyRole("OPERATOR", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/batches/**").hasAnyRole("OPERATOR", "ADMIN")
                // Sisa /batches/** (PATCH batch, tulis barcode) -> ADMIN only.
                .requestMatchers("/api/v1/batches/**").hasRole("ADMIN")

                // Barcodes — domain operator/admin (scanner). Bukan jalur customer.
                .requestMatchers(HttpMethod.GET, "/api/v1/barcodes/**").hasAnyRole("OPERATOR", "ADMIN")
                .requestMatchers("/api/v1/barcodes/**").hasRole("ADMIN")

                // Admin-only endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")

                // Semua endpoint lain butuh authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("*"); // Mengizinkan semua origin
        configuration.addAllowedMethod(CorsConfiguration.ALL); // Mengizinkan semua method
        configuration.addAllowedHeader(CorsConfiguration.ALL); // Mengizinkan semua headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}