package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.ColumnDTO;
import com.mulehang.blog.entity.BlogColumn;
import com.mulehang.blog.mapper.BlogColumnMapper;
import com.mulehang.blog.service.ColumnService;
import com.mulehang.blog.vo.ColumnVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 专栏 Service。
 */
@Service
public class ColumnServiceImpl implements ColumnService {

    private final BlogColumnMapper columnMapper;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public ColumnServiceImpl(BlogColumnMapper columnMapper) {
        this.columnMapper = columnMapper;
    }

    /**
     * 创建专栏。
     *
     * @param dto 专栏创建 DTO
     * @return 专栏 ID
     * @throws IllegalArgumentException 当 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ColumnDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        BlogColumn c = new BlogColumn();
        c.setName(dto.getName());
        c.setSlug(dto.getSlug());
        c.setCoverUrl(dto.getCoverUrl());
        c.setDescription(dto.getDescription());
        c.setSort(dto.getSort());
        c.setStatus(dto.getStatus());
        columnMapper.insert(c);
        return c.getId();
    }

    /**
     * 更新专栏。
     *
     * @param id 专栏 ID
     * @param dto 专栏更新 DTO
     * @throws IllegalArgumentException 当 id 或 dto 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ColumnDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("参数 dto 不能为空");
        }
        BlogColumn patch = new BlogColumn();
        patch.setId(id);
        if (dto.getName() != null) patch.setName(dto.getName());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getCoverUrl() != null) patch.setCoverUrl(dto.getCoverUrl());
        if (dto.getDescription() != null) patch.setDescription(dto.getDescription());
        if (dto.getSort() != null) patch.setSort(dto.getSort());
        if (dto.getStatus() != null) patch.setStatus(dto.getStatus());
        columnMapper.updateById(patch);
    }

    /**
     * 删除专栏。
     *
     * @param id 专栏 ID
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        columnMapper.deleteById(id);
    }

    /**
     * 根据 ID 获取专栏。
     *
     * @param id 专栏 ID
     * @return 专栏 VO
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    public ColumnVO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("参数 id 不能为空");
        }
        return toVO(columnMapper.selectById(id));
    }

    /**
     * 获取所有专栏。
     *
     * @return 专栏列表
     */
    @Override
    public List<ColumnVO> listAll() {
        return columnMapper.selectList(new LambdaQueryWrapper<BlogColumn>()
                        .orderByAsc(BlogColumn::getSort)
                        .orderByDesc(BlogColumn::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 将专栏实体转换为专栏 VO。
     *
     * @param c 专栏实体
     * @return 专栏 VO
     */
    private ColumnVO toVO(BlogColumn c) {
        if (c == null) {
            return null;
        }
        ColumnVO vo = new ColumnVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setCoverUrl(c.getCoverUrl());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }
}
