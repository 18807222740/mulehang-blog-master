package com.mulehang.blog.service.article;

import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.exception.BusinessException;
import com.mulehang.blog.model.ResultCodeEnum;
import com.mulehang.blog.vo.UserInfoVO;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 文章权限校验辅助类。
 */
@Component
public class ArticlePermissionHelper {

    private static final String ROLE_ADMIN = "ADMIN";

    /**
     * 获取当前登录用户 ID（必须已登录）。
     *
     * @return 用户 ID
     */
    public Long requireCurrentUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }

    /**
     * 判断当前用户是否为管理员。
     *
     * @return true=管理员
     */
    public boolean isAdmin() {
        UserInfoVO user = UserContext.getCurrentUser();
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().anyMatch(ROLE_ADMIN::equalsIgnoreCase);
    }

    /**
     * 校验是否有权操作文章（作者本人或管理员）。
     *
     * @param article 文章实体
     */
    public void assertCanOperate(BlogArticle article) {
        if (article == null) {
            throw BusinessException.notFound("文章不存在");
        }
        if (isAdmin()) {
            return;
        }
        Long currentUserId = requireCurrentUserId();
        if (!Objects.equals(article.getAuthorId(), currentUserId)) {
            throw BusinessException.forbidden("无权限操作该文章");
        }
    }
}
