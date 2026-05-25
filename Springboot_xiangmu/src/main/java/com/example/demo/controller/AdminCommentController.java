package com.example.demo.controller;

import com.example.demo.annotation.RequireAdmin;
import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @Autowired
    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @RequireAdmin
    public ApiResponse<Page<Comment>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sceneryId,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        boolean hasKeyword = trimmedKeyword != null && !trimmedKeyword.isEmpty();

        Page<Comment> result;
        if (sceneryId != null && hasKeyword) {
            result = commentService.searchBySceneryAndContent(sceneryId, trimmedKeyword, pageable);
        } else if (sceneryId != null) {
            result = commentService.getBySceneryId(sceneryId, pageable);
        } else if (hasKeyword) {
            result = commentService.searchByContent(trimmedKeyword, pageable);
        } else {
            result = commentService.getCommentPage(pageable);
        }

        return ApiResponse.success(result);
    }
}
