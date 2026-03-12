package com.obs.service;

import com.obs.dto.BolumDTO;
import com.obs.model.Bolum;
import com.obs.repository.BolumRepository;
import com.obs.repository.DersRepository;
import com.obs.repository.OgrenciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BolumService {

    private final BolumRepository bolumRepository;
    private final OgrenciRepository ogrenciRepository;
    private final DersRepository dersRepository;

    public List<BolumDTO.Response> getAll() {
        return bolumRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BolumDTO.Response getById(Long id) {
        Bolum bolum = bolumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı: " + id));
        return toResponse(bolum);
    }

    public BolumDTO.Response create(BolumDTO.Request request) {
        if (bolumRepository.existsByBolumKodu(request.getBolumKodu())) {
            throw new RuntimeException("Bu bölüm kodu zaten kullanılıyor: " + request.getBolumKodu());
        }
        Bolum bolum = new Bolum();
        bolum.setBolumAdi(request.getBolumAdi());
        bolum.setBolumKodu(request.getBolumKodu().toUpperCase());
        bolum.setFakulte(request.getFakulte());
        return toResponse(bolumRepository.save(bolum));
    }

    public BolumDTO.Response update(Long id, BolumDTO.Request request) {
        Bolum bolum = bolumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bölüm bulunamadı: " + id));
        bolum.setBolumAdi(request.getBolumAdi());
        bolum.setFakulte(request.getFakulte());
        return toResponse(bolumRepository.save(bolum));
    }

    public void delete(Long id) {
        if (!bolumRepository.existsById(id)) {
            throw new RuntimeException("Bölüm bulunamadı: " + id);
        }
        bolumRepository.deleteById(id);
    }

    private BolumDTO.Response toResponse(Bolum bolum) {
        BolumDTO.Response resp = new BolumDTO.Response();
        resp.setId(bolum.getId());
        resp.setBolumAdi(bolum.getBolumAdi());
        resp.setBolumKodu(bolum.getBolumKodu());
        resp.setFakulte(bolum.getFakulte());
        resp.setOgrenciSayisi((int) ogrenciRepository.countByBolumId(bolum.getId()));
        resp.setDersSayisi((int) dersRepository.countByBolumId(bolum.getId()));
        return resp;
    }
}
