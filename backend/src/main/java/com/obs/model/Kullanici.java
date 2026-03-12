package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sistem kullanıcısı — giriş, yetki ve oturum yönetimi.
 *
 * ADMIN        : Tüm yetkiler
 * OGRETIM_UYESI: Ders/not ekleme-güncelleme
 * KULLANICI    : Sadece okuma (öğrenci hesabı)
 *
 * Öğrenci hesapları için ogrenci alanı FK ile bağlıdır.
 * Rol KULLANICI ise ogrenci != null olmalıdır.
 * ADMIN / OGRETIM_UYESI rolleri için ogrenci null olabilir.
 */
@Entity
@Table(name = "kullanici")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String kullaniciAdi;

    @Column(nullable = false)
    private String sifre;

    @Column(nullable = false)
    private String adSoyad;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.KULLANICI;

    @Column(nullable = false)
    private boolean aktif = true;

    /**
     * Bu kullanıcıya karşılık gelen öğrenci kaydı.
     * KULLANICI rolündeki hesaplar için zorunlu,
     * ADMIN / OGRETIM_UYESI için null bırakılabilir.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ogrenci_id", unique = true)
    private Ogrenci ogrenci;

    public enum Rol {
        ADMIN, KULLANICI, OGRETIM_UYESI
    }
}
