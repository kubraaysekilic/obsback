package com.obs.repository;

import com.obs.model.NotKaydi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotKaydiRepository extends JpaRepository<NotKaydi, Long> {

    List<NotKaydi> findByOgrenciId(Long ogrenciId);

    List<NotKaydi> findByDersId(Long dersId);

    Optional<NotKaydi> findByOgrenciIdAndDersIdAndYilAndDonem(
            Long ogrenciId, Long dersId, Integer yil, String donem);

    @Query("SELECT AVG((n.vizeNotu * 0.4) + (n.finalNotu * 0.6)) FROM NotKaydi n WHERE n.ders.id = :dersId")
    Double findAverageByDersId(@Param("dersId") Long dersId);

    @Query("SELECT AVG((n.vizeNotu * 0.4) + (n.finalNotu * 0.6)) FROM NotKaydi n WHERE n.ogrenci.id = :ogrenciId")
    Double findAverageByOgrenciId(@Param("ogrenciId") Long ogrenciId);
}
