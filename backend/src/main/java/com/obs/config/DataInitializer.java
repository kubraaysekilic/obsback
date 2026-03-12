package com.obs.config;

import com.obs.model.*;
import com.obs.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final KullaniciRepository kullaniciRepository;
    private final OgrenciRepository ogrenciRepository;
    private final BolumRepository bolumRepository;
    private final DersRepository dersRepository;
    private final NotKaydiRepository notKaydiRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (kullaniciRepository.count() > 0) {
            log.info("Veriler zaten mevcut, DataInitializer atlanıyor.");
            return;
        }

        log.info("=== Test verileri oluşturuluyor... ===");

        Bolum bilgisayar = createBolum("Bilgisayar Mühendisliği", "CENG", "Mühendislik Fakültesi");
        Bolum elektrik   = createBolum("Elektrik-Elektronik Mühendisliği", "EEE", "Mühendislik Fakültesi");
        Bolum endustri   = createBolum("Endüstri Mühendisliği", "IE", "Mühendislik Fakültesi");
        log.info("✅ Bölümler oluşturuldu: {}, {}, {}", bilgisayar.getBolumAdi(), elektrik.getBolumAdi(), endustri.getBolumAdi());

        Ogrenci ali    = createOgrenci("Ali",    "Veli",    "20210001", "ali.veli@obs.edu.tr",     "05301234567", bilgisayar, 4, LocalDate.of(2000,  3, 15));
        Ogrenci fatma  = createOgrenci("Fatma",  "Şahin",   "20210002", "fatma.sahin@obs.edu.tr",  "05311234568", bilgisayar, 4, LocalDate.of(2001,  7, 22));
        Ogrenci hasan  = createOgrenci("Hasan",  "Çelik",   "20220001", "hasan.celik@obs.edu.tr",  "05321234569", elektrik,   3, LocalDate.of(2001, 11,  5));
        Ogrenci zeynep = createOgrenci("Zeynep", "Arslan",  "20220002", "zeynep.arslan@obs.edu.tr","05331234570", elektrik,   3, LocalDate.of(2002,  2, 18));
        Ogrenci emre   = createOgrenci("Emre",   "Demir",   "20220003", "emre.demir@obs.edu.tr",   "05341234571", bilgisayar, 3, LocalDate.of(2002,  5, 30));
        Ogrenci selin  = createOgrenci("Selin",  "Kurt",    "20220004", "selin.kurt@obs.edu.tr",   "05351234572", endustri,   3, LocalDate.of(2002,  9, 12));
        Ogrenci murat  = createOgrenci("Murat",  "Yılmaz",  "20230001", "murat.yilmaz@obs.edu.tr", "05361234573", bilgisayar, 2, LocalDate.of(2003,  1,  8));
        Ogrenci ayse   = createOgrenci("Ayşe",   "Kara",    "20230002", "ayse.kara@obs.edu.tr",    "05371234574", endustri,   2, LocalDate.of(2003,  6, 25));
        log.info("✅ {} öğrenci oluşturuldu", ogrenciRepository.count());

        Ders veri     = createDers("Veri Yapıları ve Algoritmalar", "CENG201", 4, bilgisayar, "Dr. Mehmet Yıldız",  "2024-Güz");
        Ders ag       = createDers("Bilgisayar Ağları",              "CENG301", 3, bilgisayar, "Prof. Ayşe Kaya",    "2024-Güz");
        Ders db       = createDers("Veritabanı Yönetim Sistemleri",  "CENG302", 4, bilgisayar, "Dr. Ahmet Albayrak", "2024-Güz");
        Ders guvenlik = createDers("Bilgi Güvenliği",                "CENG401", 3, bilgisayar, "Dr. Mehmet Yıldız",  "2024-Güz");
        Ders devre    = createDers("Devre Analizi",                  "EEE201",  4, elektrik,   "Prof. Ayşe Kaya",    "2024-Güz");
        Ders sinyal   = createDers("Sinyal ve Sistemler",            "EEE301",  3, elektrik,   "Dr. Ahmet Albayrak", "2024-Güz");
        Ders yazilim  = createDers("Yazılım Mühendisliği",           "IE401",   3, endustri,   "Dr. Mehmet Yıldız",  "2024-Güz");
        log.info("✅ {} ders oluşturuldu", dersRepository.count());

        createKullanici("admin",    "Admin123.",   "Sistem Yöneticisi",  "admin@obs.edu.tr",      Kullanici.Rol.ADMIN,         null);
        createKullanici("ogretim1", "Ogretim123.", "Dr. Mehmet Yıldız",  "myildiz@obs.edu.tr",    Kullanici.Rol.OGRETIM_UYESI, null);
        createKullanici("ogretim2", "Ogretim123.", "Prof. Ayşe Kaya",    "akaya@obs.edu.tr",      Kullanici.Rol.OGRETIM_UYESI, null);
        createKullanici("ogretim3", "Ogretim123.", "Dr. Ahmet Albayrak", "aalbayrak@obs.edu.tr",  Kullanici.Rol.OGRETIM_UYESI, null);
        createKullanici("ogrenci1", "Ogrenci123.", ali.getAd()    + " " + ali.getSoyad(),    "ali.veli.kullanici@obs.edu.tr",    Kullanici.Rol.KULLANICI, ali);
        createKullanici("ogrenci2", "Ogrenci123.", fatma.getAd()  + " " + fatma.getSoyad(),  "fatma.sahin.kullanici@obs.edu.tr", Kullanici.Rol.KULLANICI, fatma);
        createKullanici("ogrenci3", "Ogrenci123.", hasan.getAd()  + " " + hasan.getSoyad(),  "hasan.celik.kullanici@obs.edu.tr", Kullanici.Rol.KULLANICI, hasan);
        createKullanici("ogrenci4", "Ogrenci123.", zeynep.getAd() + " " + zeynep.getSoyad(), "zeynep.arslan.kullanici@obs.edu.tr",Kullanici.Rol.KULLANICI, zeynep);
        createKullanici("ogrenci5", "Ogrenci123.", emre.getAd()   + " " + emre.getSoyad(),   "emre.demir.kullanici@obs.edu.tr",  Kullanici.Rol.KULLANICI, emre);
        createKullanici("ogrenci6", "Ogrenci123.", selin.getAd()  + " " + selin.getSoyad(),  "selin.kurt.kullanici@obs.edu.tr",  Kullanici.Rol.KULLANICI, selin);
        createKullanici("demo",     "Demo123.",    "Demo Kullanıcı",     "demo@obs.edu.tr",       Kullanici.Rol.KULLANICI, null);
        log.info("✅ {} kullanıcı oluşturuldu", kullaniciRepository.count());

        createNot(ali,    veri,     78.0, 82.0, 2024, "Güz");
        createNot(ali,    ag,       65.0, 71.0, 2024, "Güz");
        createNot(ali,    db,       88.0, 91.0, 2024, "Güz");
        createNot(ali,    guvenlik, 72.0, 68.0, 2024, "Güz");

        createNot(fatma,  veri,     90.0, 95.0, 2024, "Güz");
        createNot(fatma,  ag,       85.0, 88.0, 2024, "Güz");
        createNot(fatma,  db,       77.0, 80.0, 2024, "Güz");
        createNot(fatma,  guvenlik, 92.0, 94.0, 2024, "Güz");

        createNot(hasan,  devre,    55.0, 60.0, 2024, "Güz");
        createNot(hasan,  sinyal,   48.0, 52.0, 2024, "Güz");

        createNot(zeynep, devre,    83.0, 87.0, 2024, "Güz");
        createNot(zeynep, sinyal,   79.0, 81.0, 2024, "Güz");

        createNot(emre,   veri,     62.0, 58.0, 2024, "Güz");
        createNot(emre,   db,       71.0, 75.0, 2024, "Güz");
        createNot(emre,   guvenlik, 80.0, 85.0, 2024, "Güz");

        createNot(selin,  yazilim,  88.0, 92.0, 2024, "Güz");
        createNot(selin,  veri,     75.0, 70.0, 2024, "Güz");

        createNot(murat,  veri,     45.0, 40.0, 2024, "Güz");

        createNot(ayse,   yazilim,  91.0, 96.0, 2024, "Güz");

        log.info("✅ {} not kaydı oluşturuldu", notKaydiRepository.count());

        log.info("┌──────────────────────────────────────────────────────────┐");
        log.info("│               DENEY HESAPLARI — OBS                     │");
        log.info("├────────────────┬──────────────┬──────────────────────────┤");
        log.info("│ Kullanıcı Adı  │ Şifre        │ Rol / Öğrenci No        │");
        log.info("├────────────────┼──────────────┼──────────────────────────┤");
        log.info("│ admin          │ Admin123.    │ ADMIN                   │");
        log.info("│ ogretim1       │ Ogretim123.  │ OGRETIM_UYESI           │");
        log.info("│ ogretim2       │ Ogretim123.  │ OGRETIM_UYESI           │");
        log.info("│ ogretim3       │ Ogretim123.  │ OGRETIM_UYESI           │");
        log.info("│ ogrenci1       │ Ogrenci123.  │ KULLANICI / 20210001    │");
        log.info("│ ogrenci2       │ Ogrenci123.  │ KULLANICI / 20210002    │");
        log.info("│ ogrenci3       │ Ogrenci123.  │ KULLANICI / 20220001    │");
        log.info("│ ogrenci4       │ Ogrenci123.  │ KULLANICI / 20220002    │");
        log.info("│ ogrenci5       │ Ogrenci123.  │ KULLANICI / 20220003    │");
        log.info("│ ogrenci6       │ Ogrenci123.  │ KULLANICI / 20220004    │");
        log.info("│ demo           │ Demo123.     │ KULLANICI (öğrencisiz)  │");
        log.info("└────────────────┴──────────────┴──────────────────────────┘");
    }


    private Bolum createBolum(String ad, String kod, String fakulte) {
        Bolum b = new Bolum();
        b.setBolumAdi(ad);
        b.setBolumKodu(kod);
        b.setFakulte(fakulte);
        return bolumRepository.save(b);
    }

    private Ogrenci createOgrenci(String ad, String soyad, String no, String email,
                                   String tel, Bolum bolum, int sinif, LocalDate dogum) {
        Ogrenci o = new Ogrenci();
        o.setAd(ad);
        o.setSoyad(soyad);
        o.setOgrenciNo(no);
        o.setEmail(email);
        o.setTelefon(tel);
        o.setDogumTarihi(dogum);
        o.setBolum(bolum);
        o.setSinif(sinif);
        o.setKayitTarihi(LocalDate.of(2021, 9, 1));
        o.setAktif(true);
        return ogrenciRepository.save(o);
    }

    private Ders createDers(String ad, String kod, int kredi, Bolum bolum,
                             String ogretimUyesi, String donem) {
        Ders d = new Ders();
        d.setDersAdi(ad);
        d.setDersKodu(kod);
        d.setKredi(kredi);
        d.setBolum(bolum);
        d.setOgretimUyesi(ogretimUyesi);
        d.setDonem(donem);
        d.setAktif(true);
        return dersRepository.save(d);
    }

    private void createKullanici(String kullaniciAdi, String sifre, String adSoyad,
                                  String email, Kullanici.Rol rol, Ogrenci ogrenci) {
        if (kullaniciRepository.existsByKullaniciAdi(kullaniciAdi)) return;
        Kullanici k = new Kullanici();
        k.setKullaniciAdi(kullaniciAdi);
        k.setSifre(passwordEncoder.encode(sifre));
        k.setAdSoyad(adSoyad);
        k.setEmail(email);
        k.setRol(rol);
        k.setAktif(true);
        k.setOgrenci(ogrenci);
        kullaniciRepository.save(k);
    }

    private void createNot(Ogrenci ogrenci, Ders ders, double vize, double fin,
                            int yil, String donem) {
        NotKaydi n = new NotKaydi();
        n.setOgrenci(ogrenci);
        n.setDers(ders);
        n.setVizeNotu(vize);
        n.setFinalNotu(fin);
        n.setYil(yil);
        n.setDonem(donem);
        notKaydiRepository.save(n);
    }
}
