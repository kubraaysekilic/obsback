package com.obs.repository;

import com.obs.model.Ogrenci;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OgrenciRepository extends JpaRepository<Ogrenci, Long> {

    Optional<Ogrenci> findByOgrenciNo(String ogrenciNo);

    Optional<Ogrenci> findByEmail(String email);

    boolean existsByOgrenciNo(String ogrenciNo);

    boolean existsByEmail(String email);

    List<Ogrenci> findByAktif(boolean aktif);

    List<Ogrenci> findByBolumId(Long bolumId);

    @Query("SELECT o FROM Ogrenci o WHERE " +
           "LOWER(o.ad) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.soyad) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.ogrenciNo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ogrenci> searchOgrenci(@Param("keyword") String keyword);

    long countByBolumId(Long bolumId);

    long countByAktif(boolean aktif);
}
