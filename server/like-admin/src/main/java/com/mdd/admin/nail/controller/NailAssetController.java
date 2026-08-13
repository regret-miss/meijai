package com.mdd.admin.nail.controller;

import com.mdd.admin.LikeAdminThreadLocal;
import com.mdd.admin.nail.dto.NailAssetBatchDeleteRequest;
import com.mdd.admin.nail.dto.NailAssetSearchRequest;
import com.mdd.admin.nail.dto.NailAssetUpdateRequest;
import com.mdd.admin.nail.dto.NailAssetUploadMetadata;
import com.mdd.admin.nail.service.NailAssetService;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/nail/asset")
public class NailAssetController {
    @Resource private NailAssetService assetService;

    @GetMapping("/list")
    public AjaxResult<PageResult<Map<String, Object>>> list(@Validated PageValidate page,
                                                            @ModelAttribute NailAssetSearchRequest search) {
        return AjaxResult.success(assetService.list(page, search));
    }

    @NotPower
    @GetMapping("/options")
    public AjaxResult<Map<String, Object>> options() {
        return AjaxResult.success(assetService.options());
    }

    @GetMapping("/detail")
    public AjaxResult<Map<String, Object>> detail(@RequestParam Integer id) {
        return AjaxResult.success(assetService.detail(id));
    }

    @PostMapping("/upload")
    public AjaxResult<Map<String, Object>> upload(@RequestParam("files") List<MultipartFile> files,
                                                   @ModelAttribute NailAssetUploadMetadata metadata) {
        return AjaxResult.success(assetService.uploadBatch(files, metadata, LikeAdminThreadLocal.getAdminId()));
    }

    @PostMapping("/batch-delete")
    public AjaxResult<Object> batchDelete(@Validated @RequestBody NailAssetBatchDeleteRequest request) {
        assetService.deleteBatch(request, LikeAdminThreadLocal.getAdminId());
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    public AjaxResult<Object> delete(@Validated @RequestBody NailAssetBatchDeleteRequest request) {
        assetService.deleteBatch(request, LikeAdminThreadLocal.getAdminId());
        return AjaxResult.success();
    }

    @PostMapping("/update")
    public AjaxResult<Object> update(@Validated @RequestBody NailAssetUpdateRequest request) {
        assetService.update(request, LikeAdminThreadLocal.getAdminId());
        return AjaxResult.success();
    }
}
