package com.obs.dto;

import com.obs.model.Ogrenci;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

public class BolumDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        private String bolumAdi;
        private String bolumKodu;
        private String fakulte;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private String bolumAdi;
        private String bolumKodu;
        private String fakulte;
        private int ogrenciSayisi;
        private int dersSayisi;
    }
}
