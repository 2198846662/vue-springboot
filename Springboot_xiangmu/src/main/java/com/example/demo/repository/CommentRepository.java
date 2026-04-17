package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAll(Pageable pageable);
    Page<Comment> findBySceneryId(Long sceneryId, Pageable pageable);
    Page<Comment> findByUserId(Long userId, Pageable pageable);
    Page<Comment> findByContentContaining(String content, Pageable pageable);
    Page<Comment> findBySceneryIdAndContentContaining(Long sceneryId, String content, Pageable pageable);
    Long countBySceneryId(Long sceneryId);
}
