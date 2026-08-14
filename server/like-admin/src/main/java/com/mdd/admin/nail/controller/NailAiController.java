package com.mdd.admin.nail.controller;

import com.mdd.admin.LikeAdminThreadLocal;
import com.mdd.admin.nail.dto.NailGenerateRequest;
import com.mdd.admin.nail.dto.NailResultDeleteRequest;
import com.mdd.admin.nail.dto.NailResultReviewRequest;
import com.mdd.admin.nail.dto.NailTaskDeleteRequest;
import com.mdd.admin.nail.dto.NailTaskRenameRequest;
import com.mdd.admin.nail.service.NailAiTaskService;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("api/nail/ai")
public class NailAiController {
    @Resource private NailAiTaskService taskService;

    @PostMapping("/task/create")
    public AjaxResult<Long> create(@Validated @RequestBody NailGenerateRequest request) {
        return AjaxResult.success(taskService.create(request, LikeAdminThreadLocal.getAdminId()));
    }

    @GetMapping("/task/list")
    public AjaxResult<PageResult<Map<String, Object>>> list(@Validated PageValidate page, String status, String keyword, String taskType) {
        Integer creatorId = LikeAdminThreadLocal.isNailMember() ? LikeAdminThreadLocal.getAdminId() : null;
        return AjaxResult.success(taskService.list(page, status, keyword, taskType, creatorId));
    }

    @GetMapping("/task/detail")
    public AjaxResult<Map<String, Object>> detail(@RequestParam Long id) { return AjaxResult.success(taskService.detail(id, false)); }

    @PostMapping("/result/adopt")
    public AjaxResult<Integer> adopt(@RequestBody Map<String, Long> request) {
        return AjaxResult.success("采纳成功", taskService.adopt(request.get("id"), LikeAdminThreadLocal.getAdminId()));
    }

    @PostMapping("/result/reject")
    public AjaxResult<Object> reject(@Validated @RequestBody NailResultReviewRequest request) {
        taskService.reject(request.getId(), request.getNote(), LikeAdminThreadLocal.getAdminId());
        return AjaxResult.success();
    }

    @PostMapping("/result/delete")
    public AjaxResult<Object> deleteResult(@Validated @RequestBody NailResultDeleteRequest request) {
        taskService.deleteResult(request.getId(), LikeAdminThreadLocal.getAdminId());
        return AjaxResult.success();
    }

    @PostMapping("/task/delete")
    public AjaxResult<Object> deleteTask(@Validated @RequestBody NailTaskDeleteRequest request) {
        taskService.deleteTask(request.getId(), LikeAdminThreadLocal.getAdminId());
        return AjaxResult.success();
    }

    @PostMapping("/task/rename")
    public AjaxResult<Object> rename(@Validated @RequestBody NailTaskRenameRequest request) {
        taskService.rename(request.getId(), request.getTitle());
        return AjaxResult.success();
    }

    @GetMapping("/stats")
    public AjaxResult<Map<String, Object>> stats() {
        return AjaxResult.success(taskService.stats());
    }
}
