package com.example.demo.service;

import com.example.demo.entity.Favorite;
import com.example.demo.entity.Scenery;
import com.example.demo.entity.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.SceneryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final SceneryRepository sceneryRepository;

    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, SceneryRepository sceneryRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.sceneryRepository = sceneryRepository;
    }

    public Page<Favorite> getByUserId(Long userId, Pageable pageable) {
        return favoriteRepository.findByUserId(userId, pageable);
    }

    public boolean isFavorited(Long userId, Long sceneryId) {
        return favoriteRepository.existsByUserIdAndSceneryId(userId, sceneryId);
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long sceneryId) {
        if (favoriteRepository.existsByUserIdAndSceneryId(userId, sceneryId)) {
            favoriteRepository.deleteByUserIdAndSceneryId(userId, sceneryId);
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Scenery> sceneryOpt = sceneryRepository.findById(sceneryId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (sceneryOpt.isEmpty()) {
            throw new IllegalArgumentException("景点不存在");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(userOpt.get());
        favorite.setScenery(sceneryOpt.get());
        favoriteRepository.save(favorite);
        return true;
    }

    public Long countBySceneryId(Long sceneryId) {
        return favoriteRepository.countBySceneryId(sceneryId);
    }
}
