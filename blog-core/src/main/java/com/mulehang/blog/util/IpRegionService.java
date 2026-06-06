package com.mulehang.blog.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * IP 归属地解析服务
 */
@Slf4j
@Component
public class IpRegionService {

    private Searcher searcher;

    /**
     * 初始化 IP 数据库
     */
    @PostConstruct
    public void init() throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource("ip2region_v4.xdb");
            byte[] buff = FileCopyUtils.copyToByteArray(resource.getInputStream());
            this.searcher = Searcher.newWithBuffer(buff);
            log.info("IP 归属地数据库加载成功");
        } catch (Exception e) {
            log.warn("IP 归属地数据库加载失败，IP 解析功能将不可用: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 解析 IP 归属地
     *
     * @param ip IP 地址
     * @return 格式：国家|区域|省份|城市|ISP
     */
    public String getRegion(String ip) {
        try {
            return searcher.search(ip);
        } catch (Exception e) {
            log.debug("IP 解析失败: ip={}, error={}", ip, e.getMessage());
            return "未知";
        }
    }

    /**
     * 获取简短归属地（省份 + 城市）
     *
     * @param ip IP 地址
     * @return 简短归属地，如 "江苏省 南京市" 或 "美国"
     */
    public String getShortRegion(String ip) {
        String region = getRegion(ip);
        String[] parts = region.split("\\|");
        if (parts.length >= 4) {
            String province = parts[2];
            String city = parts[3];
            if ("0".equals(city)) {
                return province;
            }
            return province + " " + city;
        }
        return region;
    }
}
