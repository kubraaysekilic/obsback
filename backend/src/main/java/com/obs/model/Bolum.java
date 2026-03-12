package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "bolum")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bolum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bolumAdi;

    @Column(nullable = false, unique = true, length = 10)
    private String bolumKodu;

    @Column(nullable = false)
    private String fakulte;

    @OneToMany(mappedBy = "bolum", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ogrenci> ogrenciler;

    @OneToMany(mappedBy = "bolum", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ders> dersler;
}
