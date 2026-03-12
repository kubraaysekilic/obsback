package com.obs.security;

import com.obs.model.Kullanici;
import com.obs.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KullaniciDetailsService implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String kullaniciAdi) throws UsernameNotFoundException {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(kullaniciAdi)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + kullaniciAdi));

        return User.builder()
                .username(kullanici.getKullaniciAdi())
                .password(kullanici.getSifre())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + kullanici.getRol().name())))
                .accountExpired(false)
                .accountLocked(!kullanici.isAktif())
                .credentialsExpired(false)
                .disabled(!kullanici.isAktif())
                .build();
    }
}
