package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ogrenci")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ogrenci {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String ad;

    @Column(nullable = false, length = 50)
    private String soyad;

    @Column(nullable = false, unique = true, length = 20)
    private String ogrenciNo;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 15)
    private String telefon;

    private LocalDate dogumTarihi;

    @Enumerated(EnumType.STRING)
    private Cinsiyet cinsiyet;

    @Column(length = 500)
    private String adres;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bolum_id")
    private Bolum bolum;

    private Integer sinif;

    private LocalDate kayitTarihi;

    @Column(nullable = false)
    private boolean aktif = true;

    @OneToMany(mappedBy = "ogrenci", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotKaydi> notlar;

    public enum Cinsiyet {
        ERKEK, KADIN, DIGER
    }
}
