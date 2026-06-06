package com.mulehang.blog.security;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 敏感词过滤服务
 */
@Component
public class SensitiveWordService {

    private final SensitiveWordBs sensitiveWordBs;

    /**
     * 初始化敏感词库
     */
    public SensitiveWordService() {
        this.sensitiveWordBs = SensitiveWordBs.newInstance()
                .ignoreCase(true)           // 忽略大小写
                .ignoreWidth(true)          // 忽略全半角
                .ignoreNumStyle(true)       // 忽略数字样式
                .ignoreChineseStyle(true)   // 忽略中文样式
                .ignoreEnglishStyle(true)   // 忽略英文样式
                .ignoreRepeat(true)         // 忽略重复字符
                .enableWordCheck(true)      // 启用词校验
                .init();
    }

    /**
     * 检查是否包含敏感词
     *
     * @param text 待检查文本
     * @return true-包含敏感词，false-不包含
     */
    public boolean contains(String text) {
        return sensitiveWordBs.contains(text);
    }

    /**
     * 获取所有敏感词
     *
     * @param text 待检查文本
     * @return 敏感词列表
     */
    public List<String> findAll(String text) {
        return sensitiveWordBs.findAll(text);
    }

    /**
     * 替换敏感词为 ***
     *
     * @param text 待处理文本
     * @return 替换后的文本
     */
    public String replace(String text) {
        return sensitiveWordBs.replace(text);
    }
}
