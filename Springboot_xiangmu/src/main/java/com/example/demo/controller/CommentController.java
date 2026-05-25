package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Scenery;
import com.example.demo.entity.User;
import com.example.demo.repository.SceneryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scenery/{sceneryId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final SceneryRepository sceneryRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentController(CommentService commentService, SceneryRepository sceneryRepository, UserRepository userRepository) {
        this.commentService = commentService;
        this.sceneryRepository = sceneryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<Page<Comment>> list(
            @PathVariable Long sceneryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.success(commentService.getBySceneryId(sceneryId, pageable));
    }

    private Long extractUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JwtUtil.validateToken(token)) {
                return JwtUtil.getUserId(token);
            }
        }
        return null;
    }

    @PostMapping
    public ApiResponse<Comment> create(
            @PathVariable Long sceneryId,
            @Valid @RequestBody CommentCreateRequest request,
            HttpServletRequest httpRequest) {

        Long userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        Scenery scenery = sceneryRepository.findById(sceneryId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (scenery == null) {
            return ApiResponse.error(400, "景点不存在");
        }
        if (user == null) {
            return ApiResponse.error(400, "用户不存在");
        }

        String finalContent = request.getContent();
        if (request.getReplyToCommentId() != null) {
            Comment targetComment = commentService.getById(request.getReplyToCommentId()).orElse(null);
            if (targetComment == null) {
                return ApiResponse.error(400, "回复目标评论不存在");
            }
            if (targetComment.getScenery() == null || !sceneryId.equals(targetComment.getScenery().getId())) {
                return ApiResponse.error(400, "回复目标不属于当前景点");
            }

            String targetUserName = targetComment.getUser() != null
                    ? (targetComment.getUser().getNickname() != null ? targetComment.getUser().getNickname() : targetComment.getUser().getUsername())
                    : "用户";
        // 结构化回复元信息，便于前端识别“谁回复谁”并实现折叠
        finalContent = "[RT:" + targetComment.getId() + ":" + targetUserName + "]" + request.getContent();
        }

        Comment comment = new Comment();
        comment.setContent(finalContent);
        comment.setScenery(scenery);
        comment.setUser(user);
        comment.setRating(request.getRating());

        Comment saved = commentService.create(comment);
        return ApiResponse.success("评论成功", saved);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long sceneryId, @PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 获取评论并验证权限（只能删除自己的评论或需要管理员权限）
        Comment comment = commentService.getById(id).orElse(null);
        if (comment == null) {
            return ApiResponse.error(404, "评论不存在");
        }

        if (comment.getScenery() == null || !sceneryId.equals(comment.getScenery().getId())) {
            return ApiResponse.error(404, "评论不存在");
        }

        // 检查是否是评论所有者
        boolean isOwner = comment.getUser() != null &&
                comment.getUser().getId().equals(userId);

        // 检查是否是管理员
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        if (!isOwner && !isAdmin) {
            return ApiResponse.error(403, "无权限删除此评论");
        }

        commentService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> toggleLike(
            @PathVariable Long sceneryId,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        Comment comment = commentService.getById(id).orElse(null);
        if (comment == null || comment.getScenery() == null || !sceneryId.equals(comment.getScenery().getId())) {
            return ApiResponse.error(404, "评论不存在");
        }

        CommentService.LikeToggleResult result;
        try {
            result = commentService.toggleLike(id, userId);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("liked", result.liked());
        data.put("likeCount", result.likeCount());
        return ApiResponse.success(result.liked() ? "点赞成功" : "取消点赞成功", data);
    }

    @GetMapping("/{id}/like/check")
    public ApiResponse<Boolean> checkLike(
            @PathVariable Long sceneryId,
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        Comment comment = commentService.getById(id).orElse(null);
        if (comment == null || comment.getScenery() == null || !sceneryId.equals(comment.getScenery().getId())) {
            return ApiResponse.error(404, "评论不存在");
        }

        return ApiResponse.success(commentService.isLikedByUser(id, userId));
    }
}
