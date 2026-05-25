package com.example.demo.repository;

import com.example.demo.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.scenery.id = :sceneryId")
    Long countBySceneryId(@Param("sceneryId") Long sceneryId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.id = :userId AND f.scenery.id = :sceneryId")
    boolean existsByUserIdAndSceneryId(@Param("userId") Long userId, @Param("sceneryId") Long sceneryId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.scenery.id = :sceneryId")
    void deleteByUserIdAndSceneryId(@Param("userId") Long userId, @Param("sceneryId") Long sceneryId);
}
