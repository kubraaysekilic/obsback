package com.obs.repository;

import com.obs.model.Bolum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BolumRepository extends JpaRepository<Bolum, Long> {
    Optional<Bolum> findByBolumKodu(String bolumKodu);
    boolean existsByBolumKodu(String bolumKodu);
}
