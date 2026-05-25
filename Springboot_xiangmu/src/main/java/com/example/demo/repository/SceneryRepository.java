package com.example.demo.repository;

import com.example.demo.entity.Scenery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SceneryRepository extends JpaRepository<Scenery, Long> {

    Page<Scenery> findByNameContaining(String name, Pageable pageable);

    Page<Scenery> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT s FROM Scenery s JOIN s.category c WHERE c.name = :categoryName")
    Page<Scenery> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT s FROM Scenery s WHERE s.location LIKE %:location%")
    Page<Scenery> findByLocation(@Param("location") String location, Pageable pageable);

    @Query("SELECT s FROM Scenery s WHERE s.ticketPrice <= :maxPrice")
    Page<Scenery> findByTicketPriceLessThanEqual(@Param("maxPrice") java.math.BigDecimal maxPrice, Pageable pageable);
}
