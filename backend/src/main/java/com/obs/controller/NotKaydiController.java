package com.obs.controller;

import com.obs.dto.NotKaydiDTO;
import com.obs.model.Kullanici;
import com.obs.model.SecurityLog;
import com.obs.repository.KullaniciRepository;
import com.obs.service.NotKaydiService;
import com.obs.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notlar")
@RequiredArgsConstructor
public class NotKaydiController {

    private final NotKaydiService notKaydiService;
    private final KullaniciRepository kullaniciRepository;
    private final SecurityLogService securityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<NotKaydiDTO.Response>> getAll() {
        return ResponseEntity.ok(notKaydiService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<NotKaydiDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notKaydiService.getById(id));
    }

    @GetMapping("/ogrenci/{ogrenciId}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<NotKaydiDTO.Response>> getByOgrenci(
            @PathVariable Long ogrenciId,
            HttpServletRequest request,
            Authentication auth) {
        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.INFORMATION_DISCLOSURE,
                "Öğrenci notlarına ID ile erişim — ogrenciId=" + ogrenciId +
                " (IDOR deneyi: URL parametresi değiştirilerek test edilir)",
                "/api/notlar/ogrenci/" + ogrenciId, "GET",
                SecurityLog.OlaySonucu.BASARILI, 60);
        return ResponseEntity.ok(notKaydiService.getByOgrenci(ogrenciId));
    }

    @GetMapping("/ders/{dersId}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<NotKaydiDTO.Response>> getByDers(@PathVariable Long dersId) {
        return ResponseEntity.ok(notKaydiService.getByDers(dersId));
    }

    @GetMapping("/benim")
    @PreAuthorize("hasRole('KULLANICI')")
    public ResponseEntity<?> getBenim(Authentication auth,
                                       HttpServletRequest request) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElse(null);

        if (kullanici == null || kullanici.getOgrenci() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Hesabınıza bağlı öğrenci kaydı bulunamadı. " +
                          "Admin ile iletişime geçin: /api/kullanicilar/{id}/ogrenci-bagla");
        }

        Long ogrenciId = kullanici.getOgrenci().getId();
        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.INFORMATION_DISCLOSURE,
                "Öğrenci kendi notlarını görüntüledi (izole) — ogrenciId=" + ogrenciId,
                "/api/notlar/benim", "GET",
                SecurityLog.OlaySonucu.BASARILI, 10);

        return ResponseEntity.ok(notKaydiService.getByOgrenci(ogrenciId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<NotKaydiDTO.Response> create(@RequestBody NotKaydiDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notKaydiService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<NotKaydiDTO.Response> update(@PathVariable Long id,
                                                        @RequestBody NotKaydiDTO.Request request,
                                                        Authentication auth,
                                                        HttpServletRequest httpRequest) {
        securityLogService.log(getClientIp(httpRequest), auth.getName(),
                SecurityLog.OlayTuru.TAMPERING,
                "Not kaydı güncellendi — ID: " + id +
                " (Tampering deneyi: Burp Suite ile intercept ederek not manipülasyonu)",
                "/api/notlar/" + id, "PUT",
                SecurityLog.OlaySonucu.BASARILI, 75);
        return ResponseEntity.ok(notKaydiService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notKaydiService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader != null ? xfHeader.split(",")[0].trim() : request.getRemoteAddr();
    }
}
