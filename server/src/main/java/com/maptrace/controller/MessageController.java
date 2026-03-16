package com.maptrace.controller;

import com.maptrace.common.ErrorCode;
import com.maptrace.common.Result;
import com.maptrace.common.ThrowUtils;
import com.maptrace.model.vo.ConversationVO;
import com.maptrace.model.vo.MessageVO;
import com.maptrace.model.dto.SendMessageRequest;
import com.maptrace.service.MessageService;
import com.maptrace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations(
            @RequestAttribute("userId") Long userId) {
        return Result.success(messageService.getConversations(userId));
    }

    @GetMapping("/history")
    public Result<List<MessageVO>> history(
            @RequestAttribute("userId") Long userId,
            @RequestParam("otherUserId") Long otherUserId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "30") int size) {
        return Result.success(messageService.getChatHistory(userId, otherUserId, page, size));
    }

    @PostMapping("/send")
    public Result<MessageVO> send(
            @RequestBody SendMessageRequest req,
            @RequestAttribute("userId") Long userId) {
        ThrowUtils.throwIf(req.getToUserId() == null, ErrorCode.PARAMS_ERROR, "接收者不能为空");
        ThrowUtils.throwIf(req.getToUserId().equals(userId), ErrorCode.PARAMS_ERROR, "不能给自己发私信");
        ThrowUtils.throwIf(req.getContent() == null || req.getContent().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        userService.checkMuted(userId);
        userService.checkBanned(userId);
        return Result.success(messageService.sendMessage(req, userId));
    }

    @PostMapping("/read")
    public Result<Void> read(
            @RequestParam("fromUserId") Long fromUserId,
            @RequestAttribute("userId") Long userId) {
        messageService.markAsRead(fromUserId, userId);
        return Result.success();
    }

    @GetMapping("/unread")
    public Result<Map<String, Integer>> unread(
            @RequestAttribute("userId") Long userId) {
        return Result.success(Map.of("count", messageService.getUnreadCount(userId)));
    }
}
