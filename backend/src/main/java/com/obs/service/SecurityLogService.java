package com.obs.service;

import com.obs.model.SecurityLog;
import com.obs.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tüm STRIDE tehdit kategorilerini kaydeden merkezi servis.
 */
@Service
@RequiredArgsConstructor
public class SecurityLogService {

    private final SecurityLogRepository repo;

    /** Genel log ekleme */
    public SecurityLog log(String ip, String kullaniciAdi, SecurityLog.OlayTuru tur,
                           String aciklama, String endpoint, String metod,
                           SecurityLog.OlaySonucu sonuc, int risk) {
        SecurityLog log = new SecurityLog();
        log.setIpAdresi(ip != null ? ip : "unknown");
        log.setKullaniciAdi(kullaniciAdi);
        log.setOlayTuru(tur);
        log.setAciklama(aciklama);
        log.setHedefEndpoint(endpoint);
        log.setHttpMetod(metod);
        log.setSonuc(sonuc);
        log.setRiskSeviyesi(risk);
        log.setOlusturmaZamani(LocalDateTime.now());
        return repo.save(log);
    }

    /** Bruteforce: son 5 dakikada kaç başarısız giriş? */
    public boolean isBruteForce(String ip) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        return repo.countRecentFailedLogins(ip, since) >= 5;
    }

    /** DoS tespiti: son 1 dakikada 100+ istek */
    public boolean isDosSuspect(String ip) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(1);
        return repo.countRecentRequests(ip, since) >= 100;
    }

    public List<SecurityLog> getAll() {
        return repo.findTop50ByOrderByOlusturmaZamaniDesc();
    }

    public List<SecurityLog> getByType(SecurityLog.OlayTuru tur) {
        return repo.findByOlayTuruOrderByOlusturmaZamaniDesc(tur);
    }

    public List<SecurityLog> getHighRisk() {
        return repo.findHighRiskEvents(60);
    }
}
