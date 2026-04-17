package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SceneryRatingRequest {

    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0.5", message = "评分最小为0.5")
    @DecimalMax(value = "5.0", message = "评分最大为5.0")
    private BigDecimal score;

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
