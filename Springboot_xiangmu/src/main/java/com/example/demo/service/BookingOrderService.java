package com.example.demo.service;

import com.example.demo.dto.CartAddRequest;
import com.example.demo.dto.OrderReviewRequest;
import com.example.demo.entity.BookingOrder;
import com.example.demo.entity.Scenery;
import com.example.demo.entity.User;
import com.example.demo.repository.BookingOrderRepository;
import com.example.demo.repository.SceneryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class BookingOrderService {

    private final BookingOrderRepository bookingOrderRepository;
    private final UserRepository userRepository;
    private final SceneryRepository sceneryRepository;

    @Autowired
    public BookingOrderService(BookingOrderRepository bookingOrderRepository,
                               UserRepository userRepository,
                               SceneryRepository sceneryRepository) {
        this.bookingOrderRepository = bookingOrderRepository;
        this.userRepository = userRepository;
        this.sceneryRepository = sceneryRepository;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private Scenery getSceneryOrThrow(Long sceneryId) {
        return sceneryRepository.findById(sceneryId)
                .orElseThrow(() -> new IllegalArgumentException("景点不存在"));
    }

    private BookingOrder getOrderOrThrow(Long orderId) {
        return bookingOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }

    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        for (int i = 0; i < 5; i++) {
            String suffix = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
            String candidate = "BK" + LocalDateTime.now().format(formatter) + suffix;
            if (!bookingOrderRepository.existsByOrderNo(candidate)) {
                return candidate;
            }
        }
        return "BK" + System.currentTimeMillis();
    }

    private int normalizeTravelerCount(Integer travelerCount) {
        if (travelerCount == null || travelerCount < 1) {
            return 1;
        }
        return travelerCount;
    }

    private BigDecimal normalizePrice(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> toView(BookingOrder order, boolean includeUser) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("bookingDays", order.getBookingDays());
        data.put("travelerCount", order.getTravelerCount());
        data.put("travelDate", order.getTravelDate());
        data.put("note", order.getNote());
        data.put("status", order.getStatus());
        data.put("unitPrice", normalizePrice(order.getUnitPrice()));
        data.put("totalAmount", normalizePrice(order.getTotalAmount()));
        data.put("paidAt", order.getPaidAt());
        data.put("reviewedAt", order.getReviewedAt());
        data.put("rejectReason", order.getRejectReason());
        data.put("createdAt", order.getCreatedAt());
        data.put("updatedAt", order.getUpdatedAt());

        if (order.getScenery() != null) {
            Map<String, Object> scenery = new HashMap<>();
            scenery.put("id", order.getScenery().getId());
            scenery.put("name", order.getScenery().getName());
            scenery.put("coverImage", order.getScenery().getCoverImage());
            scenery.put("location", order.getScenery().getLocation());
            scenery.put("ticketPrice", normalizePrice(order.getScenery().getTicketPrice()));
            data.put("scenery", scenery);
        }

        if (includeUser && order.getUser() != null) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", order.getUser().getId());
            user.put("username", order.getUser().getUsername());
            user.put("nickname", order.getUser().getNickname());
            data.put("user", user);
        }

        if (order.getReviewedBy() != null) {
            Map<String, Object> reviewer = new HashMap<>();
            reviewer.put("id", order.getReviewedBy().getId());
            reviewer.put("username", order.getReviewedBy().getUsername());
            reviewer.put("nickname", order.getReviewedBy().getNickname());
            data.put("reviewedBy", reviewer);
        }

        return data;
    }

    public List<Map<String, Object>> listCart(Long userId) {
        return bookingOrderRepository.findByUserIdAndStatus(userId, BookingOrder.STATUS_CART)
                .stream()
                .map(order -> toView(order, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> addToCart(Long userId, CartAddRequest request) {
        User user = getUserOrThrow(userId);
        Scenery scenery = getSceneryOrThrow(request.getSceneryId());
        int travelerCount = normalizeTravelerCount(request.getTravelerCount());
        BigDecimal unitPrice = normalizePrice(scenery.getTicketPrice());
        BigDecimal totalAmount = unitPrice
                .multiply(BigDecimal.valueOf(request.getBookingDays()))
                .multiply(BigDecimal.valueOf(travelerCount))
                .setScale(2, RoundingMode.HALF_UP);

        BookingOrder order = new BookingOrder();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setScenery(scenery);
        order.setBookingDays(request.getBookingDays());
        order.setTravelerCount(travelerCount);
        order.setTravelDate(request.getTravelDate());
        order.setNote(request.getNote() == null ? null : request.getNote().trim());
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(totalAmount);
        order.setStatus(BookingOrder.STATUS_CART);

        BookingOrder saved = bookingOrderRepository.save(order);
        return toView(saved, false);
    }

    @Transactional
    public void removeCartItem(Long userId, Long orderId) {
        BookingOrder order = getOrderOrThrow(orderId);
        if (order.getUser() == null || !userId.equals(order.getUser().getId())) {
            throw new IllegalArgumentException("无权删除该购物车项");
        }
        if (!BookingOrder.STATUS_CART.equals(order.getStatus())) {
            throw new IllegalArgumentException("仅购物车中的订单可删除");
        }
        bookingOrderRepository.delete(order);
    }

    @Transactional
    public Map<String, Object> payCart(Long userId) {
        List<BookingOrder> cartOrders = bookingOrderRepository.findByUserIdAndStatus(userId, BookingOrder.STATUS_CART);
        if (cartOrders.isEmpty()) {
            throw new IllegalArgumentException("购物车为空，无需支付");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (BookingOrder order : cartOrders) {
            order.setStatus(BookingOrder.STATUS_PAID);
            order.setPaidAt(now);
            totalAmount = totalAmount.add(normalizePrice(order.getTotalAmount()));
        }
        bookingOrderRepository.saveAll(cartOrders);

        Map<String, Object> result = new HashMap<>();
        result.put("paidCount", cartOrders.size());
        result.put("totalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));
        result.put("paidAt", now);
        return result;
    }

    public Page<Map<String, Object>> listMyOrders(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String normalizedStatus = status == null ? null : status.trim().toUpperCase();

        Page<BookingOrder> dataPage;
        if (normalizedStatus == null || normalizedStatus.isEmpty()) {
            dataPage = bookingOrderRepository.findMyOrdersExcludingStatus(userId, BookingOrder.STATUS_CART, pageable);
        } else {
            dataPage = bookingOrderRepository.findMyOrders(userId, normalizedStatus, pageable);
        }
        return dataPage.map(order -> toView(order, false));
    }

    public Page<Map<String, Object>> listAdminOrders(String status, Long sceneryId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        String normalizedStatus = status == null ? null : status.trim().toUpperCase();
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
            normalizedKeyword = null;
        }
        if (normalizedStatus != null && normalizedStatus.isEmpty()) {
            normalizedStatus = null;
        }

        Page<BookingOrder> dataPage = bookingOrderRepository.searchForAdmin(
                normalizedStatus,
                sceneryId,
                normalizedKeyword,
                pageable
        );
        return dataPage.map(order -> toView(order, true));
    }

    @Transactional
    public Map<String, Object> reviewOrder(Long adminUserId, Long orderId, OrderReviewRequest request) {
        BookingOrder order = getOrderOrThrow(orderId);
        if (!BookingOrder.STATUS_PAID.equals(order.getStatus())) {
            throw new IllegalArgumentException("仅待审核订单可执行审核");
        }

        String action = request.getAction() == null ? "" : request.getAction().trim().toUpperCase();
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new IllegalArgumentException("action仅支持 APPROVE 或 REJECT");
        }

        User admin = getUserOrThrow(adminUserId);
        order.setReviewedBy(admin);
        order.setReviewedAt(LocalDateTime.now());

        if ("APPROVE".equals(action)) {
            order.setStatus(BookingOrder.STATUS_APPROVED);
            order.setRejectReason(null);
        } else {
            String reason = request.getReason() == null ? "" : request.getReason().trim();
            order.setStatus(BookingOrder.STATUS_REJECTED);
            order.setRejectReason(reason.isEmpty() ? "管理员驳回" : reason);
        }

        BookingOrder saved = bookingOrderRepository.save(order);
        return toView(saved, true);
    }
}
