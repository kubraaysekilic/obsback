package com.obs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DersDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private String dersAdi;
        private String dersKodu;
        private Integer kredi;
        private Long bolumId;
        private String ogretimUyesi;
        private String donem;
        private boolean aktif = true;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String dersAdi;
        private String dersKodu;
        private Integer kredi;
        private Long bolumId;
        private String bolumAdi;
        private String ogretimUyesi;
        private String donem;
        private boolean aktif;
    }
}
