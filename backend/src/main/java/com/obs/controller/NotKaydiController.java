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

/**
 * Not Kaydı Controller
 *
 * Yetki matrisi:
 *   GET  /api/notlar                    → ADMIN, OGRETIM_UYESI (tümü)
 *   GET  /api/notlar/{id}               → ADMIN, OGRETIM_UYESI
 *   GET  /api/notlar/ogrenci/{ogrenciId}→ ADMIN, OGRETIM_UYESI
 *   GET  /api/notlar/ders/{dersId}      → ADMIN, OGRETIM_UYESI
 *   GET  /api/notlar/benim              → KULLANICI (sadece JWT'deki kendi öğrenci ID'si — veri izolasyonu)
 *   POST /api/notlar                    → ADMIN, OGRETIM_UYESI
 *   PUT  /api/notlar/{id}               → ADMIN, OGRETIM_UYESI  (Tampering deneyi)
 *   DELETE /api/notlar/{id}             → ADMIN
 *
 * STRIDE — Information Disclosure (IDOR):
 *   /api/notlar/ogrenci/{ogrenciId} endpoint'i — Burp Suite / Postman ile
 *   URL parametresi değiştirilerek başka öğrencinin notlarına erişim test edilir.
 *   Örn: /api/notlar/ogrenci/1 → /api/notlar/ogrenci/2
 *   Bu endpoint YALNIZCA ADMIN ve OGRETIM_UYESI rollerine açıktır; KULLANICI rolü 403 alır.
 *
 * VERİ İZOLASYONU (DÜZELTME):
 *   Orijinal kodda /api/notlar/benim endpoint'i JWT'deki ogrenci_id'yi kullanıyordu
 *   ancak öğrenci bağlantısı olmayan hesaplarda tüm notlar görünüyordu.
 *   Düzeltme: ogrenci null ise 404 döner, başkasının ID'si verilemez.
 */
@RestController
@RequestMapping("/api/notlar")
@RequiredArgsConstructor
public class NotKaydiController {

    private final NotKaydiService notKaydiService;
    private final KullaniciRepository kullaniciRepository;
    private final SecurityLogService securityLogService;

    /** ADMIN + OGRETIM_UYESI — tüm notlar */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<NotKaydiDTO.Response>> getAll() {
        return ResponseEntity.ok(notKaydiService.getAll());
    }

    /** ADMIN + OGRETIM_UYESI — tekil not */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<NotKaydiDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notKaydiService.getById(id));
    }

    /**
     * ADMIN + OGRETIM_UYESI — öğrenciye göre (IDOR test yüzeyi).
     * URL parametresi değiştirilerek farklı öğrencilere erişim deneyi yapılabilir.
     * Her erişim SecurityLog'a kaydedilir.
     * KULLANICI rolü bu endpoint'e erişirse → 403 + log.
     */
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

    /** ADMIN + OGRETIM_UYESI — derse göre */
    @GetMapping("/ders/{dersId}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<NotKaydiDTO.Response>> getByDers(@PathVariable Long dersId) {
        return ResponseEntity.ok(notKaydiService.getByDers(dersId));
    }

    /**
     * KULLANICI — SADECE KENDİ NOTLARINI GÖRÜR (Veri İzolasyonu).
     *
     * JWT token'ındaki kullanıcı adından ogrenci_id bulunur.
     * Kullanıcı başka bir ogrenciId parametresi veremez; endpoint sabit olarak
     * kendi hesabına bağlı öğrenci ID'sini kullanır.
     *
     * KULLANICI rolündeki bir hesapta ogrenci bağlantısı yoksa → 404.
     *
     * STRIDE — Information Disclosure testi:
     *   Bu endpoint düzgün çalışıyor → başkasının verisine erişilemiyor.
     *   Karşılaştırma için /api/notlar/ogrenci/{id} (yukarıdaki) IDOR deneyi yüzeyidir.
     */
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

        // JWT'den alınan kendi ID'si — başkasının ID'si verilemez
        return ResponseEntity.ok(notKaydiService.getByOgrenci(ogrenciId));
    }

    /**
     * ADMIN + OGRETIM_UYESI — not ekle.
     * KULLANICI rolü → 403.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<NotKaydiDTO.Response> create(@RequestBody NotKaydiDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notKaydiService.create(request));
    }

    /**
     * ADMIN + OGRETIM_UYESI — Tampering deneyi:
     * Burp Suite ile bu isteği intercept edip vize/final notunu değiştirin.
     * KULLANICI rolü → 403 + log.
     */
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

    /** Sadece ADMIN silebilir */
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
