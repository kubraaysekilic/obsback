package com.obs.service;

import com.obs.dto.OgrenciDTO;
import com.obs.model.Bolum;
import com.obs.model.Ogrenci;
import com.obs.repository.BolumRepository;
import com.obs.repository.KullaniciRepository;
import com.obs.repository.OgrenciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OgrenciService {

    private final OgrenciRepository ogrenciRepository;
    private final BolumRepository bolumRepository;
    private final KullaniciRepository kullaniciRepository;

    public List<OgrenciDTO.Response> getAll() {
        return ogrenciRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OgrenciDTO.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public List<OgrenciDTO.Response> search(String keyword) {
        return ogrenciRepository.searchOgrenci(keyword).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OgrenciDTO.Response> getByBolum(Long bolumId) {
        return ogrenciRepository.findByBolumId(bolumId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OgrenciDTO.Response create(OgrenciDTO.Request request) {
        if (ogrenciRepository.existsByOgrenciNo(request.getOgrenciNo())) {
            throw new RuntimeException("Bu öğrenci numarası zaten kayıtlı: " + request.getOgrenciNo());
        }
        if (ogrenciRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta zaten kayıtlı: " + request.getEmail());
        }
        Ogrenci ogrenci = toEntity(new Ogrenci(), request);
        return toResponse(ogrenciRepository.save(ogrenci));
    }

    public OgrenciDTO.Response update(Long id, OgrenciDTO.Request request) {
        Ogrenci ogrenci = findById(id);
        toEntity(ogrenci, request);
        return toResponse(ogrenciRepository.save(ogrenci));
    }

    public void delete(Long id) {
        if (!ogrenciRepository.existsById(id)) {
            throw new RuntimeException("Öğrenci bulunamadı: " + id);
        }
        ogrenciRepository.deleteById(id);
    }

    public OgrenciDTO.Response toggleAktif(Long id) {
        Ogrenci ogrenci = findById(id);
        ogrenci.setAktif(!ogrenci.isAktif());
        return toResponse(ogrenciRepository.save(ogrenci));
    }

    public long getToplamOgrenciSayisi() {
        return ogrenciRepository.count();
    }

    public long getAktifOgrenciSayisi() {
        return ogrenciRepository.countByAktif(true);
    }

    private Ogrenci findById(Long id) {
        return ogrenciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı: " + id));
    }

    private Ogrenci toEntity(Ogrenci ogrenci, OgrenciDTO.Request req) {
        ogrenci.setAd(req.getAd());
        ogrenci.setSoyad(req.getSoyad());
        ogrenci.setOgrenciNo(req.getOgrenciNo());
        ogrenci.setEmail(req.getEmail());
        ogrenci.setTelefon(req.getTelefon());
        ogrenci.setDogumTarihi(req.getDogumTarihi());
        ogrenci.setCinsiyet(req.getCinsiyet());
        ogrenci.setAdres(req.getAdres());
        ogrenci.setSinif(req.getSinif());
        ogrenci.setKayitTarihi(req.getKayitTarihi());
        ogrenci.setAktif(req.isAktif());
        if (req.getBolumId() != null) {
            Bolum bolum = bolumRepository.findById(req.getBolumId())
                    .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı: " + req.getBolumId()));
            ogrenci.setBolum(bolum);
        }
        return ogrenci;
    }

    public OgrenciDTO.Response toResponse(Ogrenci o) {
        OgrenciDTO.Response resp = new OgrenciDTO.Response();
        resp.setId(o.getId());
        resp.setAd(o.getAd());
        resp.setSoyad(o.getSoyad());
        resp.setOgrenciNo(o.getOgrenciNo());
        resp.setEmail(o.getEmail());
        resp.setTelefon(o.getTelefon());
        resp.setDogumTarihi(o.getDogumTarihi());
        resp.setCinsiyet(o.getCinsiyet() != null ? o.getCinsiyet().name() : null);
        resp.setAdres(o.getAdres());
        resp.setSinif(o.getSinif());
        resp.setKayitTarihi(o.getKayitTarihi());
        resp.setAktif(o.isAktif());
        if (o.getBolum() != null) {
            resp.setBolumId(o.getBolum().getId());
            resp.setBolumAdi(o.getBolum().getBolumAdi());
            resp.setFakulte(o.getBolum().getFakulte());
        }
        kullaniciRepository.findByOgrenciId(o.getId()).ifPresent(k -> {
            resp.setKullaniciId(k.getId());
            resp.setKullaniciAdi(k.getKullaniciAdi());
        });
        return resp;
    }
}
