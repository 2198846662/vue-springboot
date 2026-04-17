package com.example.demo.repository;

import com.example.demo.entity.SceneryRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SceneryRatingRepository extends JpaRepository<SceneryRating, Long> {

    Optional<SceneryRating> findByUserIdAndSceneryId(Long userId, Long sceneryId);

    long countBySceneryId(Long sceneryId);

    @Query("SELECT AVG(sr.score) FROM SceneryRating sr WHERE sr.scenery.id = :sceneryId")
    BigDecimal findAverageScoreBySceneryId(@Param("sceneryId") Long sceneryId);
}
