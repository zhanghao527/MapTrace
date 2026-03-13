package com.timemap.service;

import com.timemap.model.dto.AddCommentRequest;
import com.timemap.model.vo.CommentPageVO;
import com.timemap.model.vo.CommentVO;

import java.util.Map;

public interface CommentService {
    CommentPageVO getComments(Long photoId, int page, int size, Long currentUserId);
    CommentPageVO getReplies(Long commentId, int page, int size, Long currentUserId);
    CommentVO addComment(AddCommentRequest req, Long userId);
    void deleteComment(Long commentId, Long userId);
    void deleteCommentByAdmin(Long commentId);
    Map<String, Object> toggleLike(Long commentId, Long userId);
}
