package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.CategoryDTO;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.CategoryService;
import com.mulehang.blog.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类 REST API。
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "分类管理", description = "分类相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 创建分类。
     *
     * @param dto 分类创建 DTO
     * @return 分类 ID
     */
    @PostMapping
    @Operation(summary = "创建分类")
    public Result<Long> create(@RequestBody CategoryDTO dto) {
        return Result.ok(categoryService.create(dto));
    }

    /**
     * 更新分类。
     *
     * @param id 分类 ID
     * @param dto 分类更新 DTO
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.ok();
    }

    /**
     * 删除分类。
     *
     * @param id 分类 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }

    /**
     * 获取分类详情。
     *
     * @param id 分类 ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public Result<CategoryVO> getById(@PathVariable Long id) {
        return Result.ok(categoryService.getById(id));
    }

    /**
     * 分类列表。
     *
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "分类列表")
    public Result<List<CategoryVO>> listAll() {
        return Result.ok(categoryService.listAll());
    }
}
