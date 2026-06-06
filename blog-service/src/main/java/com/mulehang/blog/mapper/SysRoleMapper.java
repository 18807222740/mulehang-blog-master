package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询角色 ID。
     *
     * @param code 角色编码
     * @return 角色 ID，不存在时返回 null
     */
    @Select("SELECT id FROM sys_role WHERE code = #{code} AND is_deleted = 0 LIMIT 1")
    Long selectIdByCode(@Param("code") String code);
}

