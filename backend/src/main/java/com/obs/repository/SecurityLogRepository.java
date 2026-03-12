package com.obs.repository;

import com.obs.model.SecurityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Long> {

    List<SecurityLog> findByOlayTuruOrderByOlusturmaZamaniDesc(SecurityLog.OlayTuru olayTuru);

    List<SecurityLog> findByIpAdresiOrderByOlusturmaZamaniDesc(String ipAdresi);

    List<SecurityLog> findByKullaniciAdiOrderByOlusturmaZamaniDesc(String kullaniciAdi);

    @Query("SELECT COUNT(s) FROM SecurityLog s WHERE s.ipAdresi = :ip " +
           "AND s.olayTuru = 'LOGIN_FAILED' AND s.olusturmaZamani > :since")
    long countRecentFailedLogins(@Param("ip") String ip, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM SecurityLog s WHERE s.ipAdresi = :ip " +
           "AND s.olusturmaZamani > :since")
    long countRecentRequests(@Param("ip") String ip, @Param("since") LocalDateTime since);

    List<SecurityLog> findTop50ByOrderByOlusturmaZamaniDesc();

    @Query("SELECT s FROM SecurityLog s WHERE s.riskSeviyesi >= :minRisk ORDER BY s.olusturmaZamani DESC")
    List<SecurityLog> findHighRiskEvents(@Param("minRisk") int minRisk);
}
