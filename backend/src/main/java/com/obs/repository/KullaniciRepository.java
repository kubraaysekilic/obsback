package com.obs.repository;

import com.obs.model.Kullanici;
import com.obs.model.Ogrenci;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    Optional<Kullanici> findByKullaniciAdi(String kullaniciAdi);

    Optional<Kullanici> findByOgrenci(Ogrenci ogrenci);

    Optional<Kullanici> findByOgrenciId(Long ogrenciId);

    boolean existsByKullaniciAdi(String kullaniciAdi);

    boolean existsByEmail(String email);

    boolean existsByOgrenciId(Long ogrenciId);
}
