package com.example.demo.service;

import com.example.demo.entity.Scenery;
import com.example.demo.repository.SceneryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class SceneryService {

    private final SceneryRepository sceneryRepository;

    @Autowired
    public SceneryService(SceneryRepository sceneryRepository) {
        this.sceneryRepository = sceneryRepository;
    }

    public Page<Scenery> getSceneryPage(Pageable pageable) {
        return sceneryRepository.findAll(pageable);
    }

    public Page<Scenery> searchByName(String name, Pageable pageable) {
        return sceneryRepository.findByNameContaining(name, pageable);
    }

    public Page<Scenery> searchByCategory(Long categoryId, Pageable pageable) {
        return sceneryRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Scenery> searchByCategoryName(String categoryName, Pageable pageable) {
        return sceneryRepository.findByCategoryName(categoryName, pageable);
    }

    public Page<Scenery> searchByLocation(String location, Pageable pageable) {
        return sceneryRepository.findByLocation(location, pageable);
    }

    public Page<Scenery> searchByPrice(BigDecimal maxPrice, Pageable pageable) {
        return sceneryRepository.findByTicketPriceLessThanEqual(maxPrice, pageable);
    }

    public Optional<Scenery> getById(Long id) {
        return sceneryRepository.findById(id);
    }

    @Transactional
    public Scenery create(Scenery scenery) {
        return sceneryRepository.save(scenery);
    }

    @Transactional
    public Optional<Scenery> update(Long id, Scenery scenery) {
        return sceneryRepository.findById(id).map(existing -> {
            existing.setName(scenery.getName());
            existing.setDescription(scenery.getDescription());
            existing.setCoverImage(scenery.getCoverImage());
            existing.setLocation(scenery.getLocation());
            existing.setTicketPrice(scenery.getTicketPrice());
            existing.setOpenTime(scenery.getOpenTime());
            existing.setLatitude(scenery.getLatitude());
            existing.setLongitude(scenery.getLongitude());
            existing.setCategory(scenery.getCategory());
            existing.setRating(scenery.getRating());
            return sceneryRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (sceneryRepository.existsById(id)) {
            sceneryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public void incrementViewCount(Long id) {
        sceneryRepository.findById(id).ifPresent(scenery -> {
            scenery.setViewCount(scenery.getViewCount() + 1);
            sceneryRepository.save(scenery);
        });
    }
}
