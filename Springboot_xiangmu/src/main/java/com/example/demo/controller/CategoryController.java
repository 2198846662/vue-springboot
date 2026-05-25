package com.example.demo.controller;

import com.example.demo.annotation.RequireAdmin;
import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Category;
import com.example.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Category> detail(@PathVariable Long id) {
        return categoryService.getById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "分类不存在"));
    }

    @PostMapping
    @RequireAdmin
    public ApiResponse<Category> create(@RequestBody Category category) {
        Category saved = categoryService.create(category);
        return ApiResponse.success("创建成功", saved);
    }

    @PutMapping("/{id}")
    @RequireAdmin
    public ApiResponse<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.update(id, category)
                .map(c -> ApiResponse.success("更新成功", c))
                .orElse(ApiResponse.error(404, "分类不存在"));
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (categoryService.delete(id)) {
            return ApiResponse.success("删除成功", null);
        }
        return ApiResponse.error(404, "分类不存在");
    }
}
