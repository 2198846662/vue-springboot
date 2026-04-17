package com.example.demo.controller;

import com.example.demo.annotation.RequireAdmin;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OrderReviewRequest;
import com.example.demo.service.BookingOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final BookingOrderService bookingOrderService;

    @Autowired
    public AdminOrderController(BookingOrderService bookingOrderService) {
        this.bookingOrderService = bookingOrderService;
    }

    @GetMapping
    @RequireAdmin
    public ApiResponse<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long sceneryId,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(bookingOrderService.listAdminOrders(status, sceneryId, keyword, page, size));
    }

    @PostMapping("/{orderId}/review")
    @RequireAdmin
    public ApiResponse<Map<String, Object>> review(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderReviewRequest requestBody,
            HttpServletRequest request
    ) {
        Long adminUserId = (Long) request.getAttribute("userId");
        if (adminUserId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        try {
            return ApiResponse.success(
                    "审核完成",
                    bookingOrderService.reviewOrder(adminUserId, orderId, requestBody)
            );
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
