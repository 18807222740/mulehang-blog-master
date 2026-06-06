package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.NotificationDTO;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.WebSocketNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * WebSocket 测试接口
 * 提供 WebSocket 连接测试和消息推送测试功能
 *
 * @author mulehang
 * @date 2026-01-21
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ws")
@Tag(name = "WebSocket 测试", description = "WebSocket 实时通知测试接口")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketNotificationService wsNotificationService;

    /**
     * 发送测试通知给指定用户
     *
     * @param userId 用户ID
     * @param message 消息内容
     * @return 操作结果
     */
    @PostMapping("/send")
    @Operation(summary = "发送测试通知", description = "向指定用户发送 WebSocket 测试通知")
    public Result<Void> sendNotification(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "消息内容", required = true)
            @RequestParam String message) {
        
        NotificationDTO notification = NotificationDTO.builder()
                .type("SYSTEM")
                .title("系统测试通知")
                .content(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();
        
        wsNotificationService.sendToUser(userId, notification);
        log.info("已发送测试通知: userId={}, message={}", userId, message);
        
        return Result.ok();
    }

    /**
     * 广播测试通知给所有在线用户
     *
     * @param message 消息内容
     * @return 操作结果
     */
    @PostMapping("/broadcast")
    @Operation(summary = "广播测试通知", description = "向所有在线用户广播 WebSocket 测试通知")
    public Result<Void> broadcastNotification(
            @Parameter(description = "消息内容", required = true)
            @RequestParam String message) {
        
        NotificationDTO notification = NotificationDTO.builder()
                .type("SYSTEM")
                .title("系统广播通知")
                .content(message)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();
        
        wsNotificationService.broadcast(notification);
        log.info("已广播测试通知: message={}", message);
        
        return Result.ok();
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数
     */
    @GetMapping("/online-count")
    @Operation(summary = "获取在线用户数", description = "获取当前 WebSocket 在线用户数量")
    public Result<Integer> getOnlineCount() {
        int count = wsNotificationService.getOnlineUserCount();
        return Result.ok(count);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    @GetMapping("/is-online")
    @Operation(summary = "检查用户是否在线", description = "检查指定用户是否在线")
    public Result<Boolean> isUserOnline(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {
        
        boolean online = wsNotificationService.isUserOnline(userId);
        return Result.ok(online);
    }
}
