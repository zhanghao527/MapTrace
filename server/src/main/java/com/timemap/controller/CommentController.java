package com.timemap.controller;

import com.timemap.common.ErrorCode;
import com.timemap.common.Result;
import com.timemap.common.ThrowUtils;
import com.timemap.model.dto.AddCommentRequest;
import com.timemap.model.vo.CommentPageVO;
import com.timemap.model.vo.CommentVO;
import com.timemap.service.CommentService;
import com.timemap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @GetMapping("/list")
    public Result<CommentPageVO> list(
            @RequestParam("photoId") Long photoId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return Result.success(commentService.getComments(photoId, page, size, userId));
    }

    @GetMapping("/replies")
    public Result<CommentPageVO> replies(
            @RequestParam("commentId") Long commentId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return Result.success(commentService.getReplies(commentId, page, size, userId));
    }

    @PostMapping("/add")
    public Result<CommentVO> add(
            @RequestBody AddCommentRequest req,
            @RequestAttribute("userId") Long userId) {
        userService.checkMuted(userId);
        ThrowUtils.throwIf(req.getContent() == null || req.getContent().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        ThrowUtils.throwIf(req.getContent().length() > 500,
                ErrorCode.PARAMS_ERROR, "评论内容不能超过500字");
        return Result.success(commentService.addComment(req, userId));
    }

    @PostMapping("/delete")
    public Result<Void> delete(
            @RequestParam("commentId") Long commentId,
            @RequestAttribute("userId") Long userId) {
        commentService.deleteComment(commentId, userId);
        return Result.success();
    }

    @PostMapping("/like")
    public Result<Map<String, Object>> like(
            @RequestParam("commentId") Long commentId,
            @RequestAttribute("userId") Long userId) {
        return Result.success(commentService.toggleLike(commentId, userId));
    }
}
