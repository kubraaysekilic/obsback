package com.obs.repository;

import com.obs.model.Ders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DersRepository extends JpaRepository<Ders, Long> {
    Optional<Ders> findByDersKodu(String dersKodu);
    boolean existsByDersKodu(String dersKodu);
    List<Ders> findByBolumId(Long bolumId);
    List<Ders> findByAktif(boolean aktif);
    long countByBolumId(Long bolumId);
}
