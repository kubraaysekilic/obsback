package com.obs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTO {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        private String kullaniciAdi;
        private String sifre;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String tokenTipi = "Bearer";
        private String kullaniciAdi;
        private String adSoyad;
        private String email;
        private String rol;
        /** Bağlı öğrenci kaydının ID'si — sadece KULLANICI rolünde dolu gelir */
        private Long ogrenciId;
        /** Bağlı öğrenci numarası */
        private String ogrenciNo;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        private String kullaniciAdi;
        private String sifre;
        private String adSoyad;
        private String email;
        private String rol;
        /** Kayıt sırasında bağlanacak öğrenci ID'si (opsiyonel) */
        private Long ogrenciId;
    }
}
