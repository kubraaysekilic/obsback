package com.obs.service;

import com.obs.dto.NotKaydiDTO;
import com.obs.model.Ders;
import com.obs.model.NotKaydi;
import com.obs.model.Ogrenci;
import com.obs.repository.DersRepository;
import com.obs.repository.NotKaydiRepository;
import com.obs.repository.OgrenciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotKaydiService {

    private final NotKaydiRepository notKaydiRepository;
    private final OgrenciRepository ogrenciRepository;
    private final DersRepository dersRepository;

    public List<NotKaydiDTO.Response> getAll() {
        return notKaydiRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public NotKaydiDTO.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public List<NotKaydiDTO.Response> getByOgrenci(Long ogrenciId) {
        return notKaydiRepository.findByOgrenciId(ogrenciId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotKaydiDTO.Response> getByDers(Long dersId) {
        return notKaydiRepository.findByDersId(dersId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public NotKaydiDTO.Response create(NotKaydiDTO.Request request) {
        Ogrenci ogrenci = ogrenciRepository.findById(request.getOgrenciId())
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı"));
        Ders ders = dersRepository.findById(request.getDersId())
                .orElseThrow(() -> new RuntimeException("Ders bulunamadı"));

        NotKaydi notKaydi = new NotKaydi();
        notKaydi.setOgrenci(ogrenci);
        notKaydi.setDers(ders);
        notKaydi.setVizeNotu(request.getVizeNotu());
        notKaydi.setFinalNotu(request.getFinalNotu());
        notKaydi.setYil(request.getYil());
        notKaydi.setDonem(request.getDonem());

        return toResponse(notKaydiRepository.save(notKaydi));
    }

    public NotKaydiDTO.Response update(Long id, NotKaydiDTO.Request request) {
        NotKaydi notKaydi = findById(id);
        notKaydi.setVizeNotu(request.getVizeNotu());
        notKaydi.setFinalNotu(request.getFinalNotu());
        notKaydi.setYil(request.getYil());
        notKaydi.setDonem(request.getDonem());
        return toResponse(notKaydiRepository.save(notKaydi));
    }

    public void delete(Long id) {
        if (!notKaydiRepository.existsById(id)) {
            throw new RuntimeException("Not kaydı bulunamadı: " + id);
        }
        notKaydiRepository.deleteById(id);
    }

    private NotKaydi findById(Long id) {
        return notKaydiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not kaydı bulunamadı: " + id));
    }

    private NotKaydiDTO.Response toResponse(NotKaydi n) {
        NotKaydiDTO.Response resp = new NotKaydiDTO.Response();
        resp.setId(n.getId());
        resp.setVizeNotu(n.getVizeNotu());
        resp.setFinalNotu(n.getFinalNotu());
        resp.setOrtalama(n.getOrtalama());
        resp.setHarfNotu(n.getHarfNotu());
        resp.setGecti(n.isGecti());
        resp.setYil(n.getYil());
        resp.setDonem(n.getDonem());
        if (n.getOgrenci() != null) {
            resp.setOgrenciId(n.getOgrenci().getId());
            resp.setOgrenciAd(n.getOgrenci().getAd());
            resp.setOgrenciSoyad(n.getOgrenci().getSoyad());
            resp.setOgrenciNo(n.getOgrenci().getOgrenciNo());
        }
        if (n.getDers() != null) {
            resp.setDersId(n.getDers().getId());
            resp.setDersAdi(n.getDers().getDersAdi());
            resp.setDersKodu(n.getDers().getDersKodu());
        }
        return resp;
    }
}
