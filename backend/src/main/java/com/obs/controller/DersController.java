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

@RestController
@RequestMapping("/api/dersler")
@RequiredArgsConstructor
public class DersController {

    private final DersService dersService;
    private final KullaniciRepository kullaniciRepository;
    private final NotKaydiRepository notKaydiRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<DersDTO.Response>> getAll() {
        return ResponseEntity.ok(dersService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<DersDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dersService.getById(id));
    }

    @GetMapping("/bolum/{bolumId}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<List<DersDTO.Response>> getByBolum(@PathVariable Long bolumId) {
        return ResponseEntity.ok(dersService.getByBolum(bolumId));
    }

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
        List<DersDTO.Response> dersler = notKaydiRepository.findByOgrenciId(ogrenciId)
                .stream()
                .map(NotKaydi::getDers)
                .distinct()
                .map(dersService::toResponse)
                .toList();

        return ResponseEntity.ok(dersler);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<DersDTO.Response> create(@RequestBody DersDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dersService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OGRETIM_UYESI')")
    public ResponseEntity<DersDTO.Response> update(@PathVariable Long id,
                                                    @RequestBody DersDTO.Request request) {
        return ResponseEntity.ok(dersService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dersService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
