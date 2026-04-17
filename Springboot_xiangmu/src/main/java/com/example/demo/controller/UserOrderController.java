package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.BookingOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class UserOrderController {

    private final BookingOrderService bookingOrderService;

    @Autowired
    public UserOrderController(BookingOrderService bookingOrderService) {
        this.bookingOrderService = bookingOrderService;
    }

    @GetMapping("/my")
    public ApiResponse<Page<Map<String, Object>>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }

        return ApiResponse.success(bookingOrderService.listMyOrders(userId, status, page, size));
    }
}
