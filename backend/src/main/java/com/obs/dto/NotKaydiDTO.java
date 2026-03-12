package com.obs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NotKaydiDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private Long ogrenciId;
        private Long dersId;
        private Double vizeNotu;
        private Double finalNotu;
        private Integer yil;
        private String donem;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long ogrenciId;
        private String ogrenciAd;
        private String ogrenciSoyad;
        private String ogrenciNo;
        private Long dersId;
        private String dersAdi;
        private String dersKodu;
        private Double vizeNotu;
        private Double finalNotu;
        private Double ortalama;
        private String harfNotu;
        private boolean gecti;
        private Integer yil;
        private String donem;
    }
}
