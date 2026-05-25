package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CartAddRequest {

    @NotNull(message = "sceneryId不能为空")
    private Long sceneryId;

    @NotNull(message = "bookingDays不能为空")
    @Min(value = 1, message = "bookingDays最小为1")
    @Max(value = 365, message = "bookingDays最大为365")
    private Integer bookingDays;

    @Min(value = 1, message = "travelerCount最小为1")
    @Max(value = 20, message = "travelerCount最大为20")
    private Integer travelerCount = 1;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate travelDate;

    @Size(max = 500, message = "note长度不能超过500")
    private String note;

    public Long getSceneryId() {
        return sceneryId;
    }

    public void setSceneryId(Long sceneryId) {
        this.sceneryId = sceneryId;
    }

    public Integer getBookingDays() {
        return bookingDays;
    }

    public void setBookingDays(Integer bookingDays) {
        this.bookingDays = bookingDays;
    }

    public Integer getTravelerCount() {
        return travelerCount;
    }

    public void setTravelerCount(Integer travelerCount) {
        this.travelerCount = travelerCount;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
