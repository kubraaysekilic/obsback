package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "ders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dersAdi;

    @Column(nullable = false, unique = true, length = 15)
    private String dersKodu;

    @Column(nullable = false)
    private Integer kredi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bolum_id")
    private Bolum bolum;

    private String ogretimUyesi;

    private String donem;

    @Column(nullable = false)
    private boolean aktif = true;

    @OneToMany(mappedBy = "ders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotKaydi> notlar;
}
