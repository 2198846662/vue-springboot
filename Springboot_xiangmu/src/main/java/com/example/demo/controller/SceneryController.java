package com.example.demo.controller;

import com.example.demo.annotation.RequireAdmin;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SceneryCreateRequest;
import com.example.demo.entity.Category;
import com.example.demo.entity.Scenery;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.SceneryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenery")
public class SceneryController {

    private final SceneryService sceneryService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public SceneryController(SceneryService sceneryService, CategoryRepository categoryRepository) {
        this.sceneryService = sceneryService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ApiResponse<Page<Scenery>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Scenery> result;
        if (name != null && !name.isEmpty()) {
            result = sceneryService.searchByName(name, pageable);
        } else if (categoryId != null) {
            result = sceneryService.searchByCategory(categoryId, pageable);
        } else if (category != null && !category.isEmpty()) {
            // Search by category name
            result = sceneryService.searchByCategoryName(category, pageable);
        } else if (location != null && !location.isEmpty()) {
            result = sceneryService.searchByLocation(location, pageable);
        } else if (maxPrice != null) {
            result = sceneryService.searchByPrice(maxPrice, pageable);
        } else {
            result = sceneryService.getSceneryPage(pageable);
        }

        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Optional<Scenery> sceneryOpt = sceneryService.getById(id);
        if (sceneryOpt.isEmpty()) {
            return ApiResponse.error(404, "景点不存在");
        }

        sceneryService.incrementViewCount(id);
        Scenery scenery = sceneryOpt.get();

        Map<String, Object> data = new HashMap<>();
        data.put("scenery", scenery);
        data.put("category", scenery.getCategory());

        return ApiResponse.success(data);
    }

    @PostMapping
    @RequireAdmin
    public ApiResponse<Scenery> create(@Valid @RequestBody SceneryCreateRequest request) {
        Scenery scenery = new Scenery();
        scenery.setName(request.getName());
        scenery.setDescription(request.getDescription());
        scenery.setCoverImage(request.getCoverImage());
        scenery.setLocation(request.getLocation());
        scenery.setTicketPrice(request.getTicketPrice());
        scenery.setOpenTime(request.getOpenTime());
    scenery.setLatitude(request.getLatitude());
    scenery.setLongitude(request.getLongitude());
        scenery.setRating(request.getRating());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .ifPresent(scenery::setCategory);
        }

        Scenery saved = sceneryService.create(scenery);
        return ApiResponse.success("创建成功", saved);
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ApiResponse<Scenery> update(@PathVariable Long id, @Valid @RequestBody SceneryCreateRequest request) {
        Scenery scenery = new Scenery();
        scenery.setName(request.getName());
        scenery.setDescription(request.getDescription());
        scenery.setCoverImage(request.getCoverImage());
        scenery.setLocation(request.getLocation());
        scenery.setTicketPrice(request.getTicketPrice());
        scenery.setOpenTime(request.getOpenTime());
    scenery.setLatitude(request.getLatitude());
    scenery.setLongitude(request.getLongitude());
        scenery.setRating(request.getRating());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .ifPresent(scenery::setCategory);
        }

        Optional<Scenery> updated = sceneryService.update(id, scenery);
        return updated
                .map(s -> ApiResponse.success("更新成功", s))
                .orElse(ApiResponse.error(404, "景点不存在"));
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (sceneryService.delete(id)) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error(404, "景点不存在");
    }
}
