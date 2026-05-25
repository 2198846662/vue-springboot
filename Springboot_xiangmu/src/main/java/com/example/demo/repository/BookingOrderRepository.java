package com.example.demo.repository;

import com.example.demo.entity.BookingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {

    boolean existsByOrderNo(String orderNo);
    boolean existsByUserIdAndSceneryIdAndStatus(Long userId, Long sceneryId, String status);

    @Query("SELECT o FROM BookingOrder o " +
            "WHERE o.user.id = :userId AND o.status = :status " +
            "ORDER BY o.createdAt DESC")
    List<BookingOrder> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT o FROM BookingOrder o " +
            "WHERE o.user.id = :userId AND (:status IS NULL OR o.status = :status) " +
            "ORDER BY o.createdAt DESC")
    Page<BookingOrder> findMyOrders(
            @Param("userId") Long userId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT o FROM BookingOrder o " +
            "WHERE o.user.id = :userId AND o.status <> :excludedStatus " +
            "ORDER BY o.createdAt DESC")
    Page<BookingOrder> findMyOrdersExcludingStatus(
            @Param("userId") Long userId,
            @Param("excludedStatus") String excludedStatus,
            Pageable pageable
    );

    @Query("SELECT o FROM BookingOrder o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:sceneryId IS NULL OR o.scenery.id = :sceneryId) " +
            "AND (:keyword IS NULL OR o.orderNo LIKE CONCAT('%', :keyword, '%') " +
            "OR o.scenery.name LIKE CONCAT('%', :keyword, '%') " +
            "OR o.user.username LIKE CONCAT('%', :keyword, '%') " +
            "OR o.user.nickname LIKE CONCAT('%', :keyword, '%') " +
            "OR o.note LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY o.createdAt DESC")
    Page<BookingOrder> searchForAdmin(
            @Param("status") String status,
            @Param("sceneryId") Long sceneryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
