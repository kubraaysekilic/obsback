package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ogrenci_id", unique = true)
    private Ogrenci ogrenci;

    public enum Rol {
        ADMIN, KULLANICI, OGRETIM_UYESI
    }
}
