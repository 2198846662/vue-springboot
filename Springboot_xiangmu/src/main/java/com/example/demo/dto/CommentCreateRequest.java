package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Integer rating;
    private Long replyToCommentId;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Long getReplyToCommentId() { return replyToCommentId; }
    public void setReplyToCommentId(Long replyToCommentId) { this.replyToCommentId = replyToCommentId; }
}
