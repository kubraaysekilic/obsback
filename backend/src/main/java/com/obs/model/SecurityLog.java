package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bilgi Güvenliği Deneyi — STRIDE tehdit kayıt tablosu.
 * Her güvenlik olayı (başarısız giriş, yetkisiz erişim, vb.) burada loglanır.
 * DoS / Elevation of Privilege / Repudiation deneyleri için kritik varlık.
 */
@Entity
@Table(name = "security_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Olayı tetikleyen IP adresi */
    @Column(nullable = false, length = 50)
    private String ipAdresi;

    /** Kullanılan kullanıcı adı (varsa) */
    @Column(length = 100)
    private String kullaniciAdi;

    /** Olay türü: LOGIN_FAILED, UNAUTHORIZED_ACCESS, ADMIN_ACTION, DOS_DETECTED, IDOR_ATTEMPT */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OlayTuru olayTuru;

    /** Ayrıntılı açıklama */
    @Column(length = 1000)
    private String aciklama;

    /** Hangi endpoint hedef alındı */
    @Column(length = 255)
    private String hedefEndpoint;

    /** HTTP metodu: GET, POST, PUT, DELETE */
    @Column(length = 10)
    private String httpMetod;

    /** Sonuç: BASARILI, ENGELLENDI, DEVAM_EDIYOR */
    @Enumerated(EnumType.STRING)
    private OlaySonucu sonuc;

    /** Risk seviyesi: DUSUK (0-29), ORTA (30-59), YUKSEK (60-100) */
    @Column(nullable = false)
    private Integer riskSeviyesi = 0;

    @Column(nullable = false)
    private LocalDateTime olusturmaZamani = LocalDateTime.now();

    public enum OlayTuru {
        LOGIN_FAILED,           // Spoofing — Hydra / Burp Suite bruteforce
        INFORMATION_DISCLOSURE, // Kişisel veri sızıntısı — IDOR (Burp/Postman)
        TAMPERING,              // Veri bütünlüğü bozulması — Burp Suite intercept
        DOS_DETECTED,           // DoS — Apache Benchmark / Slowloris / LOIC
        REPUDIATION,            // İnkar edilebilirlik — log manipülasyon girişimi
        ELEVATION_OF_PRIVILEGE, // Yetki yükseltme — KULLANICI → ADMIN
        UNAUTHORIZED_ACCESS,    // JWT olmadan korumalı endpoint
        ADMIN_ACTION            // Admin kullanıcı işlemleri izleme
    }

    public enum OlaySonucu {
        BASARILI,
        ENGELLENDI,
        DEVAM_EDIYOR
    }
}
