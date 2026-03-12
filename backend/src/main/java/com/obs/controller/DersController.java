package com.obs.controller;

import com.obs.dto.DersDTO;
import com.obs.model.Kullanici;
import com.obs.model.NotKaydi;
import com.obs.repository.KullaniciRepository;
import com.obs.repository.NotKaydiRepository;
import com.obs.service.DersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ders Controller
 *
 * Yetki matrisi:
 *   GET  /api/dersler            → ADMIN, OGRETIM_UYESI (tüm dersler)
 *   GET  /api/dersler/{id}       → ADMIN, OGRETIM_UYESI
 *   GET  /api/dersler/bolum/{id} → ADMIN, OGRETIM_UYESI
 *   GET  /api/dersler/benim      → KULLANICI (not kaydı olan dersleri)
 *   POST/PUT/DELETE              → ADMIN, OGRETIM_UYESI
 */
@RestController
@RequestMapping("/api/dersler")
@RequiredArgsConstructor
public class DersController {

    private final DersService dersService;
    private final KullaniciRepository kullaniciRepository;
    private final NotKaydiRepository notKaydiRepository;

    /** ADMIN + OGRETIM_UYESI — tüm dersler */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<DersDTO.Response>> getAll() {
        return ResponseEntity.ok(dersService.getAll());
    }

    /** ADMIN + OGRETIM_UYESI — tekil ders */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<DersDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dersService.getById(id));
    }

    /** ADMIN + OGRETIM_UYESI — bölüme göre */
    @GetMapping("/bolum/{bolumId}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<DersDTO.Response>> getByBolum(@PathVariable Long bolumId) {
        return ResponseEntity.ok(dersService.getByBolum(bolumId));
    }

    /**
     * KULLANICI — sadece kendi not kaydı olan dersleri görür.
     * JWT'den kullanıcı adı alınır → ogrenci_id → o öğrencinin notlarındaki dersler.
     */
    @GetMapping("/benim")
    @PreAuthorize("hasRole('KULLANICI')")
    public ResponseEntity<?> getBenim(Authentication auth) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElse(null);

        if (kullanici == null || kullanici.getOgrenci() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Hesabınıza bağlı öğrenci kaydı bulunamadı.");
        }

        Long ogrenciId = kullanici.getOgrenci().getId();
        // Not kayıtlarından ders ID'lerini çıkar, tekrarsız ders listesi döndür
        List<DersDTO.Response> dersler = notKaydiRepository.findByOgrenciId(ogrenciId)
                .stream()
                .map(NotKaydi::getDers)
                .distinct()
                .map(dersService::toResponse)
                .toList();

        return ResponseEntity.ok(dersler);
    }

    /** ADMIN + OGRETIM_UYESI */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<DersDTO.Response> create(@RequestBody DersDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dersService.create(request));
    }

    /** ADMIN + OGRETIM_UYESI */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<DersDTO.Response> update(@PathVariable Long id,
                                                    @RequestBody DersDTO.Request request) {
        return ResponseEntity.ok(dersService.update(id, request));
    }

    /** Sadece ADMIN */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dersService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
