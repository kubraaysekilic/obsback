package com.obs.dto;

import com.obs.model.Ogrenci;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class OgrenciDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private String ad;
        private String soyad;
        private String ogrenciNo;
        private String email;
        private String telefon;
        private LocalDate dogumTarihi;
        private Ogrenci.Cinsiyet cinsiyet;
        private String adres;
        private Long bolumId;
        private Integer sinif;
        private LocalDate kayitTarihi;
        private boolean aktif = true;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String ad;
        private String soyad;
        private String ogrenciNo;
        private String email;
        private String telefon;
        private LocalDate dogumTarihi;
        private String cinsiyet;
        private String adres;
        private Long bolumId;
        private String bolumAdi;
        private String fakulte;
        private Integer sinif;
        private LocalDate kayitTarihi;
        private boolean aktif;
        /** Bağlı sistem kullanıcısının ID'si (varsa) */
        private Long kullaniciId;
        /** Bağlı sistem kullanıcısının adı (varsa) */
        private String kullaniciAdi;
    }
}
