package com.obs.service;

import com.obs.dto.AuthDTO;
import com.obs.model.Kullanici;
import com.obs.model.Ogrenci;
import com.obs.repository.KullaniciRepository;
import com.obs.repository.OgrenciRepository;
import com.obs.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final KullaniciRepository kullaniciRepository;
    private final OgrenciRepository ogrenciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getKullaniciAdi(), request.getSifre())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtUtils.generateToken(auth);

        Kullanici kullanici = kullaniciRepository
                .findByKullaniciAdi(request.getKullaniciAdi())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Long ogrenciId  = kullanici.getOgrenci() != null ? kullanici.getOgrenci().getId()        : null;
        String ogrenciNo = kullanici.getOgrenci() != null ? kullanici.getOgrenci().getOgrenciNo() : null;

        return new AuthDTO.LoginResponse(
                token,
                "Bearer",
                kullanici.getKullaniciAdi(),
                kullanici.getAdSoyad(),
                kullanici.getEmail(),
                kullanici.getRol().name(),
                ogrenciId,
                ogrenciNo
        );
    }

    public String register(AuthDTO.RegisterRequest request) {
        if (kullaniciRepository.existsByKullaniciAdi(request.getKullaniciAdi())) {
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor");
        }
        if (kullaniciRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta zaten kayıtlı");
        }

        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi(request.getKullaniciAdi());
        kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
        kullanici.setAdSoyad(request.getAdSoyad());
        kullanici.setEmail(request.getEmail());
        kullanici.setRol(request.getRol() != null
                ? Kullanici.Rol.valueOf(request.getRol().toUpperCase())
                : Kullanici.Rol.KULLANICI);
        kullanici.setAktif(true);

        // Öğrenci FK bağlantısı — ogrenciId verilmişse bağla
        if (request.getOgrenciId() != null) {
            if (kullaniciRepository.existsByOgrenciId(request.getOgrenciId())) {
                throw new RuntimeException("Bu öğrenci zaten bir hesaba bağlı");
            }
            Ogrenci ogrenci = ogrenciRepository.findById(request.getOgrenciId())
                    .orElseThrow(() -> new RuntimeException(
                            "Öğrenci bulunamadı: " + request.getOgrenciId()));
            kullanici.setOgrenci(ogrenci);
        }

        kullaniciRepository.save(kullanici);
        return "Kullanıcı başarıyla oluşturuldu";
    }
}
