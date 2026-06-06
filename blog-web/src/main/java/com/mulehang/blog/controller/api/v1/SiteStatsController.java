package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.SiteStatsService;
import com.mulehang.blog.vo.SiteStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 网站统计 Controller
 */
@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "网站统计", description = "网站 PV/UV 统计相关接口")
@RequiredArgsConstructor
public class SiteStatsController {

    private final SiteStatsService siteStatsService;

    /**
     * 记录页面访问（PV/UV）。
     * <p>
     * 前端每次访问页面时调用此接口，系统会根据访客 IP 或 sessionId 进行统计。
     * </p>
     */
    @PostMapping("/pv")
    @Operation(summary = "记录页面访问")
    public Result<Void> recordPageView(HttpServletRequest request) {
        // 获取访客唯一标识（优先使用 IP，也可以使用 sessionId）
        String visitorId = getVisitorId(request);
        siteStatsService.recordPageView(visitorId);
        return Result.ok();
    }

    /**
     * 获取网站统计数据。
     */
    @GetMapping
    @Operation(summary = "获取网站统计数据")
    public Result<SiteStatsVO> getSiteStats() {
        return Result.ok(siteStatsService.getSiteStats());
    }

    /**
     * 获取访客唯一标识。
     * <p>
     * 优先使用 X-Forwarded-For 获取真实 IP，若无则使用 RemoteAddr。
     * </p>
     *
     * @param request HTTP 请求
     * @return 访客唯一标识
     */
    private String getVisitorId(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个 IP（经过多层代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}
