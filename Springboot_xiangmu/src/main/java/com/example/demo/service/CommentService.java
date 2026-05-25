package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.CommentLike;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.CommentLikeRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.userRepository = userRepository;
    }

    public Page<Comment> getBySceneryId(Long sceneryId, Pageable pageable) {
        return commentRepository.findBySceneryId(sceneryId, pageable);
    }

    public Page<Comment> getCommentPage(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    public Page<Comment> getByUserId(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    public Page<Comment> searchByContent(String keyword, Pageable pageable) {
        return commentRepository.findByContentContaining(keyword, pageable);
    }

    public Page<Comment> searchBySceneryAndContent(Long sceneryId, String keyword, Pageable pageable) {
        return commentRepository.findBySceneryIdAndContentContaining(sceneryId, keyword, pageable);
    }

    public Optional<Comment> getById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional
    public Comment create(Comment comment) {
        return commentRepository.save(comment);
    }

    @Transactional
    public boolean delete(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Long countBySceneryId(Long sceneryId) {
        return commentRepository.countBySceneryId(sceneryId);
    }

    public boolean isLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }

    @Transactional
    public LikeToggleResult toggleLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        boolean liked;
        int likeCount = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
            liked = false;
            likeCount = Math.max(0, likeCount - 1);
        } else {
            CommentLike commentLike = new CommentLike();
            commentLike.setComment(comment);
            commentLike.setUser(userOpt.get());
            commentLikeRepository.save(commentLike);
            liked = true;
            likeCount += 1;
        }

        comment.setLikeCount(likeCount);
        commentRepository.save(comment);
        return new LikeToggleResult(liked, likeCount);
    }

    public record LikeToggleResult(boolean liked, int likeCount) {}
}
