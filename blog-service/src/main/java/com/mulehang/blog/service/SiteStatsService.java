package com.mulehang.blog.service;

import com.mulehang.blog.vo.SiteStatsVO;

/**
 * 网站统计服务接口
 */
public interface SiteStatsService {

    /**
     * 记录页面访问（PV）。
     *
     * @param visitorId 访客唯一标识（IP 或 sessionId）
     */
    void recordPageView(String visitorId);

    /**
     * 获取网站统计数据。
     *
     * @return 统计数据
     */
    SiteStatsVO getSiteStats();

    /**
     * 每日重置统计数据（可选，根据业务需求）。
     */
    void resetDailyStats();
}
