package com.obs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "not_kaydi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotKaydi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ogrenci_id", nullable = false)
    private Ogrenci ogrenci;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ders_id", nullable = false)
    private Ders ders;

    private Double vizeNotu;

    private Double finalNotu;

    private Integer yil;

    private String donem;

    public Double getOrtalama() {
        if (vizeNotu == null && finalNotu == null) return null;
        double vize = vizeNotu != null ? vizeNotu : 0;
        double fin = finalNotu != null ? finalNotu : 0;
        return (vize * 0.4) + (fin * 0.6);
    }

    public String getHarfNotu() {
        Double ort = getOrtalama();
        if (ort == null) return "-";
        if (ort >= 90) return "AA";
        if (ort >= 85) return "BA";
        if (ort >= 80) return "BB";
        if (ort >= 75) return "CB";
        if (ort >= 70) return "CC";
        if (ort >= 65) return "DC";
        if (ort >= 60) return "DD";
        if (ort >= 50) return "FD";
        return "FF";
    }

    public boolean isGecti() {
        Double ort = getOrtalama();
        return ort != null && ort >= 60;
    }
}
