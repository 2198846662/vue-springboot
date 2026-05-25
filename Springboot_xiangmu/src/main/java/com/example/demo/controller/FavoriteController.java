package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenery/{sceneryId}/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public ApiResponse<String> toggle(
            @PathVariable Long sceneryId,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        boolean favorited;
        try {
            favorited = favoriteService.toggleFavorite(userId, sceneryId);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }

        String message = favorited ? "收藏成功" : "取消收藏成功";
        return ApiResponse.success(message, favorited ? "favorited" : "unfavorited");
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> check(
            @PathVariable Long sceneryId,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        return ApiResponse.success(favoriteService.isFavorited(userId, sceneryId));
    }

    @GetMapping("/count")
    public ApiResponse<Long> count(@PathVariable Long sceneryId) {
        return ApiResponse.success(favoriteService.countBySceneryId(sceneryId));
    }
}
