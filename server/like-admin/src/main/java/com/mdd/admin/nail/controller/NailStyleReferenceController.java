package com.mdd.admin.nail.controller;

import com.mdd.admin.nail.dto.NailStyleReferenceSaveRequest;
import com.mdd.admin.nail.service.NailStyleReferenceService;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("api/nail/style-reference")
public class NailStyleReferenceController {
    @Resource private NailStyleReferenceService styleService;

    @PostMapping("/upload")
    public AjaxResult<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam String name,
                                                   @RequestParam String category,
                                                   @RequestParam(defaultValue = "") String promptEnhance,
                                                   @RequestParam(defaultValue = "REINTERPRET") String referenceStrategy,
                                                   @RequestParam(defaultValue = "0") Integer sort) {
        NailStyleReferenceSaveRequest meta = new NailStyleReferenceSaveRequest();
        meta.setName(name); meta.setCategory(category); meta.setPromptEnhance(promptEnhance);
        meta.setReferenceStrategy(referenceStrategy); meta.setSort(sort);
        return AjaxResult.success(styleService.upload(file, meta));
    }

    @GetMapping("/list")
    public AjaxResult<PageResult<Map<String, Object>>> list(@Validated PageValidate page, String status, String keyword) {
        return AjaxResult.success(styleService.list(page, status, keyword));
    }

    @PostMapping("/update")
    public AjaxResult<Object> update(@Validated @RequestBody NailStyleReferenceSaveRequest meta) {
        styleService.update(meta);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    public AjaxResult<Object> delete(@RequestBody Map<String, Integer> request) {
        styleService.delete(request.get("id"));
        return AjaxResult.success();
    }

    @PostMapping("/status")
    public AjaxResult<Object> status(@RequestBody Map<String, String> request) {
        styleService.changeStatus(Integer.valueOf(request.get("id")), request.get("status"));
        return AjaxResult.success();
    }
}
