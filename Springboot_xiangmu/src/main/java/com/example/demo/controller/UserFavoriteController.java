package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Favorite;
import com.example.demo.entity.Scenery;
import com.example.demo.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/favorites")
public class UserFavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public UserFavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<Page<Scenery>> myFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Favorite> favorites = favoriteService.getByUserId(userId, pageable);
        Page<Scenery> sceneryPage = favorites.map(Favorite::getScenery);
        return ApiResponse.success(sceneryPage);
    }
}
