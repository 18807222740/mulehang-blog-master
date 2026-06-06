package com.mulehang.blog.controller.api.v1;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.storage.StorageService;
import com.mulehang.blog.vo.UploadFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 API。
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileController {

    private final StorageService storageService;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * 上传文件。
     *
     * @param file 上传的文件
     * @return 文件上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @SentinelResource(value = "file-upload", blockHandler = "uploadBlockHandler")
    public Result<UploadFileVO> upload(@RequestParam("file") MultipartFile file) {
        FileInfo info = storageService.upload(file);

        UploadFileVO vo = new UploadFileVO();
        vo.setUrl(info.getUrl());
        vo.setPlatform(info.getPlatform());
        vo.setPath(info.getPath());
        vo.setFilename(info.getFilename());
        vo.setOriginalFilename(info.getOriginalFilename());
        vo.setExt(info.getExt());
        vo.setSize(info.getSize());

        return Result.ok(vo);
    }

    /**
     * 上传限流降级处理。
     */
    public Result<UploadFileVO> uploadBlockHandler(MultipartFile file, BlockException e) {
        return Result.fail("上传过于频繁，请稍后再试");
    }
}
