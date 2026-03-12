package com.obs.config;

import com.obs.security.JwtAuthEntryPoint;
import com.obs.security.JwtAuthFilter;
import com.obs.security.KullaniciDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Güvenlik yapılandırması
 *
 * Rol hiyerarşisi (URL seviyesi):
 *   /api/auth/login,register         → herkese açık (Spoofing / EoP deneyi)
 *   /api/kullanicilar/**              → sadece ADMIN
 *   /api/auth/security-logs/**        → sadece ADMIN
 *   /api/notlar/benim/**              → sadece KULLANICI (öğrenci kendi notları)
 *   /api/dersler/benim/**             → sadece KULLANICI (öğrenci kendi dersleri)
 *   diğer /api/**                     → kimlik doğrulaması + @PreAuthorize ile ince kontrol
 *
 * Method-level yetki: @EnableMethodSecurity → controller'larda @PreAuthorize çalışır.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KullaniciDetailsService kullaniciDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(kullaniciDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Herkese açık ────────────────────────────────────────────
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()   // Kasıtlı açık — EoP deneyi

                // ── Sadece ADMIN ─────────────────────────────────────────────
                .requestMatchers("/api/kullanicilar/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/security-logs/**").hasRole("ADMIN")

                // ── Sadece KULLANICI (öğrenci) — kendi verisi ────────────────
                // (ek kontrol controller'da yapılır: gerçekten kendi ogrenciId'si mi?)
                .requestMatchers("/api/notlar/benim/**").hasRole("KULLANICI")
                .requestMatchers("/api/dersler/benim/**").hasRole("KULLANICI")

                // ── Kimliği doğrulanmış herkes — ince kontrol @PreAuthorize ile
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
