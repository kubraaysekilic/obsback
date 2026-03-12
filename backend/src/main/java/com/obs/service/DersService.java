package com.obs.service;

import com.obs.dto.DersDTO;
import com.obs.model.Bolum;
import com.obs.model.Ders;
import com.obs.repository.BolumRepository;
import com.obs.repository.DersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DersService {

    private final DersRepository dersRepository;
    private final BolumRepository bolumRepository;

    public List<DersDTO.Response> getAll() {
        return dersRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DersDTO.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public List<DersDTO.Response> getByBolum(Long bolumId) {
        return dersRepository.findByBolumId(bolumId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DersDTO.Response create(DersDTO.Request request) {
        if (dersRepository.existsByDersKodu(request.getDersKodu())) {
            throw new RuntimeException("Bu ders kodu zaten kullanılıyor: " + request.getDersKodu());
        }
        Ders ders = toEntity(new Ders(), request);
        return toResponse(dersRepository.save(ders));
    }

    public DersDTO.Response update(Long id, DersDTO.Request request) {
        Ders ders = findById(id);
        toEntity(ders, request);
        return toResponse(dersRepository.save(ders));
    }

    public void delete(Long id) {
        if (!dersRepository.existsById(id)) {
            throw new RuntimeException("Ders bulunamadı: " + id);
        }
        dersRepository.deleteById(id);
    }

    private Ders findById(Long id) {
        return dersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı: " + id));
    }

    private Ders toEntity(Ders ders, DersDTO.Request req) {
        ders.setDersAdi(req.getDersAdi());
        ders.setDersKodu(req.getDersKodu());
        ders.setKredi(req.getKredi());
        ders.setOgretimUyesi(req.getOgretimUyesi());
        ders.setDonem(req.getDonem());
        ders.setAktif(req.isAktif());
        if (req.getBolumId() != null) {
            Bolum bolum = bolumRepository.findById(req.getBolumId())
                    .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı: " + req.getBolumId()));
            ders.setBolum(bolum);
        }
        return ders;
    }

    public DersDTO.Response toResponse(Ders d) {
        DersDTO.Response resp = new DersDTO.Response();
        resp.setId(d.getId());
        resp.setDersAdi(d.getDersAdi());
        resp.setDersKodu(d.getDersKodu());
        resp.setKredi(d.getKredi());
        resp.setOgretimUyesi(d.getOgretimUyesi());
        resp.setDonem(d.getDonem());
        resp.setAktif(d.isAktif());
        if (d.getBolum() != null) {
            resp.setBolumId(d.getBolum().getId());
            resp.setBolumAdi(d.getBolum().getBolumAdi());
        }
        return resp;
    }
}
