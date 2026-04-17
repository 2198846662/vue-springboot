package com.example.demo.service;

import com.example.demo.entity.BookingOrder;
import com.example.demo.entity.Scenery;
import com.example.demo.entity.SceneryRating;
import com.example.demo.entity.User;
import com.example.demo.repository.BookingOrderRepository;
import com.example.demo.repository.SceneryRatingRepository;
import com.example.demo.repository.SceneryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SceneryRatingService {

    private final SceneryRatingRepository sceneryRatingRepository;
    private final SceneryRepository sceneryRepository;
    private final UserRepository userRepository;
    private final BookingOrderRepository bookingOrderRepository;

    @Autowired
    public SceneryRatingService(SceneryRatingRepository sceneryRatingRepository,
                                SceneryRepository sceneryRepository,
                                UserRepository userRepository,
                                BookingOrderRepository bookingOrderRepository) {
        this.sceneryRatingRepository = sceneryRatingRepository;
        this.sceneryRepository = sceneryRepository;
        this.userRepository = userRepository;
        this.bookingOrderRepository = bookingOrderRepository;
    }

    public record RatingSummary(BigDecimal averageScore, long ratingCount) {}

    public record RatingPermission(boolean canRate, boolean alreadyRated, boolean hasPurchased, String reason) {}

    private void validateScoreStep(BigDecimal score) {
        if (score == null) {
            throw new IllegalArgumentException("评分不能为空");
        }

        BigDecimal normalized = score.stripTrailingZeros();
        BigDecimal doubled = normalized.multiply(BigDecimal.valueOf(2));
        if (doubled.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("评分仅支持 0.5 分步进");
        }
    }

    private Scenery getSceneryOrThrow(Long sceneryId) {
        return sceneryRepository.findById(sceneryId)
                .orElseThrow(() -> new IllegalArgumentException("景点不存在"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private boolean hasApprovedPurchase(Long userId, Long sceneryId) {
        return bookingOrderRepository.existsByUserIdAndSceneryIdAndStatus(
                userId,
                sceneryId,
                BookingOrder.STATUS_APPROVED
        );
    }

    public RatingPermission getRatingPermission(Long userId, Long sceneryId) {
        getSceneryOrThrow(sceneryId);

        boolean alreadyRated = sceneryRatingRepository.findByUserIdAndSceneryId(userId, sceneryId).isPresent();
        if (alreadyRated) {
            return new RatingPermission(false, true, true, "你已评分，不能修改");
        }

        boolean hasPurchased = hasApprovedPurchase(userId, sceneryId);
        if (!hasPurchased) {
            return new RatingPermission(false, false, false, "仅购买过该景点并审核通过后才能评分");
        }

        return new RatingPermission(true, false, true, "");
    }

    @Transactional
    public RatingSummary rateScenery(Long userId, Long sceneryId, BigDecimal score) {
        validateScoreStep(score);

        User user = getUserOrThrow(userId);
        Scenery scenery = getSceneryOrThrow(sceneryId);

        RatingPermission permission = getRatingPermission(userId, sceneryId);
        if (!permission.canRate()) {
            throw new IllegalArgumentException(permission.reason());
        }

        BigDecimal normalized = score.setScale(1, RoundingMode.HALF_UP);
        SceneryRating rating = new SceneryRating();
        rating.setUser(user);
        rating.setScenery(scenery);
        rating.setScore(normalized);
        sceneryRatingRepository.save(rating);

        return refreshAndGetSummary(scenery);
    }

    public BigDecimal getMyRating(Long userId, Long sceneryId) {
        getSceneryOrThrow(sceneryId);
        return sceneryRatingRepository.findByUserIdAndSceneryId(userId, sceneryId)
                .map(SceneryRating::getScore)
                .orElse(null);
    }

    @Transactional
    public RatingSummary refreshAndGetSummary(Long sceneryId) {
        Scenery scenery = getSceneryOrThrow(sceneryId);
        return refreshAndGetSummary(scenery);
    }

    @Transactional
    protected RatingSummary refreshAndGetSummary(Scenery scenery) {
        BigDecimal avg = sceneryRatingRepository.findAverageScoreBySceneryId(scenery.getId());
        long count = sceneryRatingRepository.countBySceneryId(scenery.getId());

        BigDecimal normalizedAvg = (avg == null ? BigDecimal.ZERO : avg).setScale(1, RoundingMode.HALF_UP);
        scenery.setRating(normalizedAvg);
        sceneryRepository.save(scenery);

        return new RatingSummary(normalizedAvg, count);
    }
}
