package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SceneryRatingRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SceneryRatingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scenery/{sceneryId}/rating")
public class SceneryRatingController {

    private final SceneryRatingService sceneryRatingService;
    private final UserRepository userRepository;

    @Autowired
    public SceneryRatingController(SceneryRatingService sceneryRatingService, UserRepository userRepository) {
        this.sceneryRatingService = sceneryRatingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@PathVariable Long sceneryId) {
        var summary = sceneryRatingService.refreshAndGetSummary(sceneryId);
        Map<String, Object> data = new HashMap<>();
        data.put("averageScore", summary.averageScore());
        data.put("ratingCount", summary.ratingCount());
        return ApiResponse.success(data);
    }

    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> myRating(@PathVariable Long sceneryId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        BigDecimal score = sceneryRatingService.getMyRating(userId, sceneryId);
        var permission = sceneryRatingService.getRatingPermission(userId, sceneryId);

        Map<String, Object> data = new HashMap<>();
        data.put("score", score);
        data.put("canRate", permission.canRate());
        data.put("alreadyRated", permission.alreadyRated());
        data.put("hasPurchased", permission.hasPurchased());
        data.put("reason", permission.reason());
        return ApiResponse.success(data);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> rate(
            @PathVariable Long sceneryId,
            @Valid @RequestBody SceneryRatingRequest requestBody,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ApiResponse.error(400, "用户不存在");
        }
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            return ApiResponse.error(403, "仅普通用户可参与评分");
        }

        try {
            var summary = sceneryRatingService.rateScenery(userId, sceneryId, requestBody.getScore());
            Map<String, Object> data = new HashMap<>();
            data.put("score", requestBody.getScore().setScale(1, java.math.RoundingMode.HALF_UP));
            data.put("averageScore", summary.averageScore());
            data.put("ratingCount", summary.ratingCount());
            return ApiResponse.success("评分成功", data);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
