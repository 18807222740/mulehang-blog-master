package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.TagDTO;
import com.mulehang.blog.entity.BlogTag;
import com.mulehang.blog.mapper.BlogArticleTagMapper;
import com.mulehang.blog.mapper.BlogTagMapper;
import com.mulehang.blog.mapper.dto.TagArticleCountDTO;
import com.mulehang.blog.service.TagService;
import com.mulehang.blog.vo.TagVO;
import com.mulehang.blog.vo.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 标签 Service。
 */
@Service
public class TagServiceImpl implements TagService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final BlogTagMapper tagMapper;

    private final BlogArticleTagMapper articleTagMapper;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>
     * 通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。
     * </p>
     */
    public TagServiceImpl(BlogTagMapper tagMapper, BlogArticleTagMapper articleTagMapper) {
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
    }

    /**
     * 创建标签。
     *
     * @param dto 标签创建 DTO
     * @return 标签 ID
     * @throws IllegalArgumentException 当 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TagDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        Long currentUserId = requireCurrentUserId();
        BlogTag t = new BlogTag();
        t.setName(dto.getName());
        t.setSlug(dto.getSlug());
        t.setColor(dto.getColor());
        t.setDescription(dto.getDescription());

        // 设置创建者ID
        t.setCreatorId(currentUserId);

        tagMapper.insert(t);
        return t.getId();
    }

    /**
     * 更新标签。
     *
     * @param id  标签 ID
     * @param dto 标签更新 DTO
     * @throws IllegalArgumentException 当 id 或 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TagDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        BlogTag existing = tagMapper.selectById(id);
        assertCanOperate(existing);
        BlogTag patch = new BlogTag();
        patch.setId(id);
        if (dto.getName() != null)
            patch.setName(dto.getName());
        if (dto.getSlug() != null)
            patch.setSlug(dto.getSlug());
        if (dto.getColor() != null)
            patch.setColor(dto.getColor());
        if (dto.getDescription() != null)
            patch.setDescription(dto.getDescription());
        tagMapper.updateById(patch);
    }

    /**
     * 删除标签。
     *
     * @param id 标签 ID
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        BlogTag existing = tagMapper.selectById(id);
        assertCanOperate(existing);
        tagMapper.deleteById(id);
    }

    /**
     * 根据 ID 获取标签。
     *
     * @param id 标签 ID
     * @return 标签 VO
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    public TagVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        TagVO vo = toVO(tagMapper.selectById(id));
        if (vo != null) {
            Long count = articleTagMapper.countByTagId(id);
            vo.setArticleCount(count == null ? 0L : count);
        }
        return vo;
    }

    /**
     * 获取所有标签。
     *
     * @return 标签列表
     */
    @Override
    public List<TagVO> listAll() {
        List<TagVO> tags = tagMapper.selectList(new LambdaQueryWrapper<BlogTag>()
                .orderByAsc(BlogTag::getId))
                .stream()
                .map(this::toVO)
                .toList();
        if (tags.isEmpty()) {
            return tags;
        }

        List<TagArticleCountDTO> countList = articleTagMapper.selectTagArticleCounts();
        if (countList == null || countList.isEmpty()) {
            tags.forEach(tag -> tag.setArticleCount(0L));
            return tags;
        }

        Map<Long, Long> countMap = new HashMap<>();
        for (TagArticleCountDTO dto : countList) {
            if (dto == null || dto.getTagId() == null) {
                continue;
            }
            countMap.put(dto.getTagId(), dto.getArticleCount() == null ? 0L : dto.getArticleCount());
        }

        for (TagVO tag : tags) {
            if (tag == null) {
                continue;
            }
            Long count = countMap.getOrDefault(tag.getId(), 0L);
            tag.setArticleCount(count);
        }
        return tags;
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
     * 权限校验：仅创建者本人或管理员可操作标签。
     *
     * @param tag 标签实体
     * @throws IllegalArgumentException 当无权限操作时抛出
     */
    private void assertCanOperate(BlogTag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("标签不存在");
        }
        if (isAdmin()) {
            return;
        }
        Long currentUserId = requireCurrentUserId();
        if (!Objects.equals(tag.getCreatorId(), currentUserId)) {
            throw new IllegalArgumentException("无权限修改或删除该标签");
        }
    }

    /**
     * 将标签实体转换为标签 VO。
     *
     * @param t 标签实体
     * @return 标签 VO
     */
    private TagVO toVO(BlogTag t) {
        if (t == null) {
            return null;
        }
        TagVO vo = new TagVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setSlug(t.getSlug());
        vo.setColor(t.getColor());
        vo.setDescription(t.getDescription());
        vo.setCreatorId(t.getCreatorId());
        return vo;
    }
}
