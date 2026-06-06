package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.CategoryDTO;
import com.mulehang.blog.entity.BlogCategory;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogCategoryMapper;
import com.mulehang.blog.service.CategoryService;
import com.mulehang.blog.vo.CategoryVO;
import com.mulehang.blog.vo.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 分类 Service。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final BlogCategoryMapper categoryMapper;
    private final BlogArticleMapper articleMapper;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>
     * 通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。
     * </p>
     */
    public CategoryServiceImpl(BlogCategoryMapper categoryMapper, BlogArticleMapper articleMapper) {
        this.categoryMapper = categoryMapper;
        this.articleMapper = articleMapper;
    }

    /**
     * 创建分类。
     *
     * @param dto 分类创建 DTO
     * @return 分类 ID
     * @throws IllegalArgumentException 当 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CategoryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        Long currentUserId = requireCurrentUserId();
        BlogCategory c = new BlogCategory();
        c.setParentId(dto.getParentId());
        c.setName(dto.getName());
        c.setSlug(dto.getSlug());
        c.setDescription(dto.getDescription());
        c.setSort(dto.getSort());
        c.setStatus(dto.getStatus());

        // 设置创建者ID
        c.setCreatorId(currentUserId);

        categoryMapper.insert(c);
        return c.getId();
    }

    /**
     * 更新分类。
     *
     * @param id  分类 ID
     * @param dto 分类更新 DTO
     * @throws IllegalArgumentException 当 id 或 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CategoryDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        BlogCategory existing = categoryMapper.selectById(id);
        assertCanOperate(existing);
        BlogCategory patch = new BlogCategory();
        patch.setId(id);
        if (dto.getParentId() != null)
            patch.setParentId(dto.getParentId());
        if (dto.getName() != null)
            patch.setName(dto.getName());
        if (dto.getSlug() != null)
            patch.setSlug(dto.getSlug());
        if (dto.getDescription() != null)
            patch.setDescription(dto.getDescription());
        if (dto.getSort() != null)
            patch.setSort(dto.getSort());
        if (dto.getStatus() != null)
            patch.setStatus(dto.getStatus());
        categoryMapper.updateById(patch);
    }

    /**
     * 删除分类。
     *
     * @param id 分类 ID
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        BlogCategory existing = categoryMapper.selectById(id);
        assertCanOperate(existing);
        categoryMapper.deleteById(id);
    }

    /**
     * 根据 ID 获取分类。
     *
     * @param id 分类 ID
     * @return 分类 VO
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    public CategoryVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        return toVO(categoryMapper.selectById(id));
    }

    /**
     * 获取所有分类。
     *
     * @return 分类列表
     */
    @Override
    public List<CategoryVO> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>()
                .orderByAsc(BlogCategory::getSort)
                .orderByDesc(BlogCategory::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 获取当前登录用户 ID（强制要求已登录）。
     *
     * @return 当前用户 ID
     * @throws IllegalStateException 当未登录时抛出
     */
    private Long requireCurrentUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("未登录或登录已过期");
        }
        return userId;
    }

    /**
     * 判断当前用户是否为管理员。
     *
     * @return true-管理员，false-非管理员
     */
    private boolean isAdmin() {
        UserInfoVO user = UserContext.getCurrentUser();
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().anyMatch(role -> ROLE_ADMIN.equalsIgnoreCase(role));
    }

    /**
     * 权限校验：仅创建者本人或管理员可操作分类。
     *
     * @param category 分类实体
     * @throws IllegalArgumentException 当无权限操作时抛出
     */
    private void assertCanOperate(BlogCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        if (isAdmin()) {
            return;
        }
        Long currentUserId = requireCurrentUserId();
        if (!Objects.equals(category.getCreatorId(), currentUserId)) {
            throw new IllegalArgumentException("无权限修改该分类");
        }
    }

    /**
     * 将分类实体转换为分类 VO。
     *
     * @param c 分类实体
     * @return 分类 VO
     */
    private CategoryVO toVO(BlogCategory c) {
        if (c == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        vo.setCreatorId(c.getCreatorId());

        // 统计文章数量
        int articleCount = articleMapper.countByCategoryId(c.getId());
        vo.setArticleCount(articleCount);

        return vo;
    }
}
