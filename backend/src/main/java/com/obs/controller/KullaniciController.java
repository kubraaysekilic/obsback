package com.obs.controller;

import com.obs.model.Kullanici;
import com.obs.model.Ogrenci;
import com.obs.model.SecurityLog;
import com.obs.repository.KullaniciRepository;
import com.obs.repository.OgrenciRepository;
import com.obs.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kullanicilar")
@RequiredArgsConstructor
public class KullaniciController {

    private final KullaniciRepository kullaniciRepository;
    private final OgrenciRepository ogrenciRepository;
    private final SecurityLogService securityLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Map<String, Object>> liste = kullaniciRepository.findAll().stream()
                .map(this::toMap)
                .toList();
        return ResponseEntity.ok(liste);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Kullanici k = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return ResponseEntity.ok(toMap(k));
    }

    @PutMapping("/{id}/rol-degistir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> rolDegistir(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            org.springframework.security.core.Authentication auth) {

        String yeniRolStr = body.get("rol");
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String eskiRol = kullanici.getRol().name();
        kullanici.setRol(Kullanici.Rol.valueOf(yeniRolStr.toUpperCase()));
        kullaniciRepository.save(kullanici);

        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.ELEVATION_OF_PRIVILEGE,
                String.format("Rol değiştirildi: kullanıcı=%s, %s → %s",
                        kullanici.getKullaniciAdi(), eskiRol, yeniRolStr),
                "/api/kullanicilar/" + id + "/rol-degistir", "PUT",
                SecurityLog.OlaySonucu.BASARILI,
                "ADMIN".equalsIgnoreCase(yeniRolStr) ? 80 : 40);

        return ResponseEntity.ok(Map.of(
                "mesaj", "Rol güncellendi",
                "kullaniciAdi", kullanici.getKullaniciAdi(),
                "eskiRol", eskiRol,
                "yeniRol", yeniRolStr.toUpperCase()
        ));
    }

    @PutMapping("/{id}/ogrenci-bagla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> ogrenciBagla(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            HttpServletRequest request,
            org.springframework.security.core.Authentication auth) {

        Long ogrenciId = body.get("ogrenciId");
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (ogrenciId == null) {
            kullanici.setOgrenci(null);
        } else {
            if (kullaniciRepository.existsByOgrenciId(ogrenciId)) {
                throw new RuntimeException("Bu öğrenci zaten başka bir hesaba bağlı");
            }
            Ogrenci ogrenci = ogrenciRepository.findById(ogrenciId)
                    .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı: " + ogrenciId));
            kullanici.setOgrenci(ogrenci);
        }
        kullaniciRepository.save(kullanici);

        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.ADMIN_ACTION,
                "Öğrenci bağlantısı güncellendi: kullanıcı=" + kullanici.getKullaniciAdi()
                        + ", ogrenciId=" + ogrenciId,
                "/api/kullanicilar/" + id + "/ogrenci-bagla", "PUT",
                SecurityLog.OlaySonucu.BASARILI, 10);

        return ResponseEntity.ok(toMap(kullanici));
    }

    @PatchMapping("/{id}/toggle-aktif")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleAktif(
            @PathVariable Long id,
            HttpServletRequest request,
            org.springframework.security.core.Authentication auth) {

        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        kullanici.setAktif(!kullanici.isAktif());
        kullaniciRepository.save(kullanici);

        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.ADMIN_ACTION,
                "Kullanıcı durumu değiştirildi: " + kullanici.getKullaniciAdi()
                        + " → aktif=" + kullanici.isAktif(),
                "/api/kullanicilar/" + id + "/toggle-aktif", "PATCH",
                SecurityLog.OlaySonucu.BASARILI, 15);

        return ResponseEntity.ok(toMap(kullanici));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest request,
            org.springframework.security.core.Authentication auth) {

        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.ADMIN_ACTION,
                "Kullanıcı silindi: " + kullanici.getKullaniciAdi(),
                "/api/kullanicilar/" + id, "DELETE",
                SecurityLog.OlaySonucu.BASARILI, 20);

        kullaniciRepository.delete(kullanici);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/security-logs/temizle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> temizleLog(
            HttpServletRequest request,
            org.springframework.security.core.Authentication auth) {

        securityLogService.log(getClientIp(request), auth.getName(),
                SecurityLog.OlayTuru.REPUDIATION,
                "Log temizleme girişimi — Repudiation deneyi, silme engellendi",
                "/api/kullanicilar/security-logs/temizle", "DELETE",
                SecurityLog.OlaySonucu.ENGELLENDI, 95);

        return ResponseEntity.ok(Map.of(
                "mesaj", "Log temizleme engellendi",
                "stride", "Repudiation — inkâr edilemezlik koruması aktif"
        ));
    }


    private Map<String, Object> toMap(Kullanici k) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",           k.getId());
        map.put("kullaniciAdi", k.getKullaniciAdi());
        map.put("adSoyad",      k.getAdSoyad());
        map.put("email",        k.getEmail());
        map.put("rol",          k.getRol().name());
        map.put("aktif",        k.isAktif());
        map.put("ogrenciId",    k.getOgrenci() != null ? k.getOgrenci().getId()        : null);
        map.put("ogrenciNo",    k.getOgrenci() != null ? k.getOgrenci().getOgrenciNo() : null);
        map.put("ogrenciAd",    k.getOgrenci() != null
                ? k.getOgrenci().getAd() + " " + k.getOgrenci().getSoyad() : null);
        return map;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader != null ? xfHeader.split(",")[0].trim() : request.getRemoteAddr();
    }
}
