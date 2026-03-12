package com.obs.controller;

import com.obs.dto.AuthDTO;
import com.obs.model.SecurityLog;
import com.obs.service.AuthService;
import com.obs.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityLogService securityLogService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        if (securityLogService.isBruteForce(ip)) {
            securityLogService.log(ip, request.getKullaniciAdi(),
                    SecurityLog.OlayTuru.LOGIN_FAILED,
                    "Bruteforce engellendi — 5+ başarısız deneme (Hydra/Burp Suite tespiti)",
                    "/api/auth/login", "POST",
                    SecurityLog.OlaySonucu.ENGELLENDI, 85);
            return ResponseEntity.status(429)
                    .body(Map.of("hata", "Çok fazla başarısız giriş denemesi. Lütfen bekleyin.",
                                 "stride", "Spoofing — Bruteforce koruması devrede"));
        }

        try {
            AuthDTO.LoginResponse response = authService.login(request);
            securityLogService.log(ip, request.getKullaniciAdi(),
                    SecurityLog.OlayTuru.ADMIN_ACTION,
                    "Başarılı giriş",
                    "/api/auth/login", "POST",
                    SecurityLog.OlaySonucu.BASARILI, 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            securityLogService.log(ip, request.getKullaniciAdi(),
                    SecurityLog.OlayTuru.LOGIN_FAILED,
                    "Yanlış kullanıcı adı veya şifre: " + e.getMessage(),
                    "/api/auth/login", "POST",
                    SecurityLog.OlaySonucu.ENGELLENDI, 70);
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthDTO.RegisterRequest request,
                                                         HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String mesaj = authService.register(request);

        int risk = "ADMIN".equalsIgnoreCase(request.getRol()) ? 90 : 10;
        SecurityLog.OlayTuru tur = "ADMIN".equalsIgnoreCase(request.getRol())
                ? SecurityLog.OlayTuru.ELEVATION_OF_PRIVILEGE
                : SecurityLog.OlayTuru.ADMIN_ACTION;

        securityLogService.log(ip, request.getKullaniciAdi(), tur,
                "Yeni kullanıcı kaydı — Rol: " + request.getRol(),
                "/api/auth/register", "POST",
                SecurityLog.OlaySonucu.BASARILI, risk);

        return ResponseEntity.ok(Map.of("mesaj", mesaj));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "kullaniciAdi", auth.getName(),
                "roller", auth.getAuthorities().toString()
        ));
    }

    @GetMapping("/security-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityLog>> getSecurityLogs() {
        return ResponseEntity.ok(securityLogService.getAll());
    }

    @GetMapping("/security-logs/high-risk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityLog>> getHighRiskLogs() {
        return ResponseEntity.ok(securityLogService.getHighRisk());
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader != null ? xfHeader.split(",")[0].trim() : request.getRemoteAddr();
    }
}
