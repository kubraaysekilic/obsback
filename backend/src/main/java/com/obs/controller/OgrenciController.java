package com.obs.controller;

import com.obs.dto.OgrenciDTO;
import com.obs.model.SecurityLog;
import com.obs.service.OgrenciService;
import com.obs.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Öğrenci Controller
 *
 * Yetki matrisi:
 *   GET  → ADMIN, OGRETIM_UYESI
 *   POST → ADMIN, OGRETIM_UYESI
 *   PUT  → ADMIN, OGRETIM_UYESI  (Tampering deneyi)
 *   DELETE / toggle-aktif → ADMIN
 *
 * KULLANICI rolü (öğrenci) bu endpoint'lere hiç erişemez → 403.
 * Öğrenci kendi bilgilerine /api/notlar/benim ve /api/dersler/benim ile erişir.
 *
 * STRIDE — Information Disclosure (IDOR):
 *   Yetkili kullanıcılar GET /{id} ile ardışık id değiştirerek tüm öğrenci
 *   kayıtlarına erişebilir. Her erişim SecurityLog'a kaydedilir.
 */
@RestController
@RequestMapping("/api/ogrenciler")
@RequiredArgsConstructor
public class OgrenciController {

    private final OgrenciService ogrenciService;
    private final SecurityLogService securityLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<OgrenciDTO.Response>> getAll() {
        return ResponseEntity.ok(ogrenciService.getAll());
    }

    /**
     * IDOR deneyi — ADMIN/OGRETIM_UYESI ardışık id ile erişir.
     * KULLANICI rolü bu endpoint'e erişemez → 403 + log.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<OgrenciDTO.Response> getById(@PathVariable Long id,
                                                        HttpServletRequest request,
                                                        Authentication auth) {
        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.INFORMATION_DISCLOSURE,
                "Öğrenci kaydına ID ile erişim — ID: " + id + " (IDOR deneyi)",
                "/api/ogrenciler/" + id, "GET",
                SecurityLog.OlaySonucu.BASARILI, 60);
        return ResponseEntity.ok(ogrenciService.getById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<OgrenciDTO.Response>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ogrenciService.search(keyword));
    }

    @GetMapping("/bolum/{bolumId}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<OgrenciDTO.Response>> getByBolum(@PathVariable Long bolumId) {
        return ResponseEntity.ok(ogrenciService.getByBolum(bolumId));
    }

    @GetMapping("/istatistik")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<Map<String, Long>> getIstatistik() {
        return ResponseEntity.ok(Map.of(
                "toplam", ogrenciService.getToplamOgrenciSayisi(),
                "aktif",  ogrenciService.getAktifOgrenciSayisi()
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<OgrenciDTO.Response> create(@RequestBody OgrenciDTO.Request request,
                                                       HttpServletRequest httpRequest,
                                                       Authentication auth) {
        securityLogService.log(getClientIp(httpRequest), auth.getName(),
                SecurityLog.OlayTuru.ADMIN_ACTION,
                "Yeni öğrenci oluşturuldu: " + request.getAd() + " " + request.getSoyad(),
                "/api/ogrenciler", "POST", SecurityLog.OlaySonucu.BASARILI, 5);
        return ResponseEntity.status(HttpStatus.CREATED).body(ogrenciService.create(request));
    }

    /**
     * Tampering deneyi — KULLANICI rolü bu endpoint'e erişemez → 403.
     * Burp Suite: OGRETIM_UYESI tokenıyla PUT isteğini intercept et, notu manipüle et.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<OgrenciDTO.Response> update(@PathVariable Long id,
                                                       @RequestBody OgrenciDTO.Request request,
                                                       HttpServletRequest httpRequest,
                                                       Authentication auth) {
        securityLogService.log(getClientIp(httpRequest), auth.getName(),
                SecurityLog.OlayTuru.TAMPERING,
                "Öğrenci kaydı güncellendi — ID: " + id + " (Tampering deneyi)",
                "/api/ogrenciler/" + id, "PUT", SecurityLog.OlaySonucu.BASARILI, 50);
        return ResponseEntity.ok(ogrenciService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        HttpServletRequest httpRequest,
                                        Authentication auth) {
        securityLogService.log(getClientIp(httpRequest), auth.getName(),
                SecurityLog.OlayTuru.ADMIN_ACTION,
                "Öğrenci silindi — ID: " + id,
                "/api/ogrenciler/" + id, "DELETE", SecurityLog.OlaySonucu.BASARILI, 10);
        ogrenciService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-aktif")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OgrenciDTO.Response> toggleAktif(@PathVariable Long id) {
        return ResponseEntity.ok(ogrenciService.toggleAktif(id));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader != null ? xfHeader.split(",")[0].trim() : request.getRemoteAddr();
    }
}
