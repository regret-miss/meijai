package com.mdd.admin.nail.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.nail.config.NailAiProperties;
import com.mdd.admin.nail.dto.NailGenerateRequest;
import com.mdd.admin.nail.provider.NailImageProvider;
import com.mdd.admin.nail.storage.StoredImage;
import com.mdd.admin.nail.storage.NailAssetStorage;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.nail.NailAiResult;
import com.mdd.common.entity.nail.NailAiTask;
import com.mdd.common.entity.nail.NailAiTaskReference;
import com.mdd.common.entity.nail.NailAsset;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.nail.NailAiResultMapper;
import com.mdd.common.mapper.nail.NailAiTaskMapper;
import com.mdd.common.mapper.nail.NailAiTaskReferenceMapper;
import com.mdd.common.util.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;

@Service
public class NailAiTaskService {
    @Resource private NailAiTaskMapper taskMapper;
    @Resource private NailAiResultMapper resultMapper;
    @Resource private NailAiTaskReferenceMapper referenceMapper;
    @Resource private NailAssetService assetService;
    @Resource private NailPromptCompiler promptCompiler;
    @Resource private NailMediaSigner mediaSigner;
    @Resource private NailAiProperties properties;
    @Resource private NailImageProvider imageProvider;
    @Resource private NailAiWorker worker;
    @Resource private NailAssetStorage storage;

    @Transactional
    public Long create(NailGenerateRequest request, int creatorId) {
        return createInternal(request, creatorId, null);
    }

    @Transactional
    public Map<String, Object> createPublic(NailGenerateRequest request) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Long id = createInternal(request, 0, token);
        Map<String, Object> access = new LinkedHashMap<>();
        access.put("id", id);
        access.put("accessToken", token);
        return access;
    }

    private Long createInternal(NailGenerateRequest request, int creatorId, String publicToken) {
        imageProvider.validateConfiguration();
        NailAsset reference = null;
        boolean hasReference = request.getReferenceAssetId() != null;
        if ("IMAGE_TO_IMAGE".equals(request.getTaskType()) && !hasReference) {
            throw new OperateException("参考图生成必须选择一张参考图");
        }
        if (hasReference) {
            reference = assetService.requireUsable(request.getReferenceAssetId());
        }

        NailAiTask task = new NailAiTask();
        task.setTaskType(hasReference ? "IMAGE_TO_IMAGE" : "TEXT_TO_IMAGE");
        task.setTitle(buildTitle(request.getPrompt()));
        task.setStatus("QUEUED");
        task.setProvider("VOLCENGINE");
        task.setModelCode(properties.getVolcengine().getModel());
        task.setPromptRaw(request.getPrompt().trim());
        task.setPromptCompiled(promptCompiler.compile(request, reference != null));
        task.setNegativePrompt(promptCompiler.negativePrompt());
        task.setAspectRatio(request.getAspectRatio());
        task.setResolution(request.getResolution());
        task.setOutputCount(request.getOutputCount());
        task.setReferenceAssetId(reference == null ? null : reference.getId());
        task.setCreativeMode(request.getCreativeMode());
        task.setNailShape(request.getNailShape());
        task.setFinish(request.getFinish());
        task.setDesignStyle(request.getDesignStyle());
        task.setLayoutStyle(request.getLayoutStyle());
        task.setTrendPreset(request.getTrendPreset());
        task.setReferenceStrategy(request.getReferenceStrategy());
        task.setColorPalette(request.getColorPalette());
        task.setTemplateVersion(properties.getPromptTemplateVersion());
        task.setErrorMessage("");
        task.setCreatorId(creatorId);
        task.setPublicToken(publicToken);
        long now = TimeUtils.timestamp();
        task.setStartedTime(0L);
        task.setFinishedTime(0L);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        taskMapper.insert(task);

        if (reference != null) {
            NailAiTaskReference snapshot = new NailAiTaskReference();
            snapshot.setTaskId(task.getId());
            snapshot.setAssetId(reference.getId());
            snapshot.setUriSnapshot(reference.getUri());
            snapshot.setCopyrightStatusSnapshot(reference.getCopyrightStatus());
            snapshot.setReferenceStrategy(request.getReferenceStrategy());
            snapshot.setSort(0);
            snapshot.setCreateTime(now);
            referenceMapper.insert(snapshot);
        }

        Long taskId = task.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                worker.generate(taskId);
            }
        });
        return task.getId();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverQueuedTasks() {
        long now = TimeUtils.timestamp();
        List<NailAiTask> interrupted = taskMapper.selectList(new QueryWrapper<NailAiTask>()
                .eq("status", "RUNNING").orderByAsc("id").last("limit 20"));
        for (NailAiTask task : interrupted) {
            task.setStatus("QUEUED");
            task.setStartedTime(0L);
            task.setFinishedTime(0L);
            task.setErrorMessage("服务重启后已自动恢复任务");
            task.setUpdateTime(now);
            taskMapper.updateById(task);
        }
        List<NailAiTask> queued = taskMapper.selectList(new QueryWrapper<NailAiTask>()
                .eq("status", "QUEUED").orderByAsc("id").last("limit 20"));
        queued.forEach(task -> worker.generate(task.getId()));
    }

    public PageResult<Map<String, Object>> list(PageValidate page, String status, String keyword, String taskType) {
        QueryWrapper<NailAiTask> query = new QueryWrapper<NailAiTask>()
                .eq(StringUtils.hasText(status), "status", status)
                .eq(StringUtils.hasText(taskType), "task_type", taskType)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper.like("title", keyword).or().like("prompt_raw", keyword))
                .orderByDesc("id");
        IPage<NailAiTask> result = taskMapper.selectPage(new Page<>(page.getPageNo(), page.getPageSize()), query);
        List<Map<String, Object>> rows = buildListViews(result.getRecords());
        return PageResult.iPageHandle(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    public Map<String, Object> detail(Long id, boolean publicOnly) {
        NailAiTask task = requireTask(id);
        if (publicOnly && !Integer.valueOf(0).equals(task.getCreatorId())) {
            throw new OperateException("任务不存在");
        }
        return toDetailView(task);
    }

    public Map<String, Object> publicDetail(Long id, String accessToken) {
        NailAiTask task = requireTask(id);
        if (!Integer.valueOf(0).equals(task.getCreatorId())) throw new OperateException("任务不存在");
        if (StringUtils.hasText(task.getPublicToken()) && !task.getPublicToken().equals(accessToken)) {
            throw new OperateException("无权查看该设计记录");
        }
        return toDetailView(task);
    }

    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTasks", taskMapper.selectCount(new QueryWrapper<>()));
        stats.put("runningTasks", taskMapper.selectCount(new QueryWrapper<NailAiTask>().in("status", "QUEUED", "RUNNING")));
        stats.put("successfulTasks", taskMapper.selectCount(new QueryWrapper<NailAiTask>().in("status", "SUCCEEDED", "PARTIAL_SUCCEEDED")));
        stats.put("pendingResults", resultMapper.selectCount(new QueryWrapper<NailAiResult>().eq("review_status", "PENDING")));
        stats.put("adoptedResults", resultMapper.selectCount(new QueryWrapper<NailAiResult>().eq("review_status", "ADOPTED")));
        stats.put("activeAssets", assetService.countActive());
        return stats;
    }

    @Transactional
    public Integer adopt(Long resultId, int creatorId) {
        NailAiResult result = requireResult(resultId);
        if ("ADOPTED".equals(result.getReviewStatus())) return result.getAdoptedAssetId();
        NailAiTask task = requireTask(result.getTaskId());
        long fileSize = 0;
        try {
            fileSize = storage.read(result.getUri()).length;
        } catch (IOException ignored) {
            // The result metadata remains usable even if the storage backend cannot expose byte length.
        }
        Integer assetId = assetService.adopt(
                new StoredImage(result.getUri(), result.getMimeType(), fileSize, result.getWidth(), result.getHeight()),
                task.getTitle() + " · 方案 " + (result.getSort() + 1),
                task.getPromptRaw(), creatorId, task.getId(), result.getId());
        result.setReviewStatus("ADOPTED");
        result.setReviewNote("已采纳为正式资产");
        result.setReviewerId(creatorId);
        result.setReviewTime(TimeUtils.timestamp());
        result.setAdoptedAssetId(assetId);
        resultMapper.updateById(result);
        return assetId;
    }

    @Transactional
    public void reject(Long resultId, String note, int reviewerId) {
        NailAiResult result = requireResult(resultId);
        if ("ADOPTED".equals(result.getReviewStatus())) {
            throw new OperateException("已采纳的结果不能驳回，请先在资产库处理对应资产");
        }
        result.setReviewStatus("REJECTED");
        result.setReviewNote(StringUtils.hasText(note) ? note.trim() : "未达到设计要求");
        result.setReviewerId(reviewerId);
        result.setReviewTime(TimeUtils.timestamp());
        resultMapper.updateById(result);
    }

    @Transactional
    public void rename(Long taskId, String title) {
        NailAiTask task = requireTask(taskId);
        task.setTitle(title.trim());
        task.setUpdateTime(TimeUtils.timestamp());
        taskMapper.updateById(task);
    }

    private List<Map<String, Object>> buildListViews(List<NailAiTask> tasks) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (tasks.isEmpty()) return rows;
        List<Long> taskIds = tasks.stream().map(NailAiTask::getId).toList();
        List<NailAiResult> results = resultMapper.selectList(new QueryWrapper<NailAiResult>()
                .in("task_id", taskIds).orderByAsc("task_id", "sort"));
        Map<Long, Integer> counts = new HashMap<>();
        Map<Long, Integer> adoptedCounts = new HashMap<>();
        Map<Long, NailAiResult> covers = new HashMap<>();
        for (NailAiResult result : results) {
            counts.merge(result.getTaskId(), 1, Integer::sum);
            if ("ADOPTED".equals(result.getReviewStatus())) adoptedCounts.merge(result.getTaskId(), 1, Integer::sum);
            covers.putIfAbsent(result.getTaskId(), result);
        }
        for (NailAiTask task : tasks) {
            Map<String, Object> row = baseView(task);
            row.put("resultCount", counts.getOrDefault(task.getId(), 0));
            row.put("adoptedCount", adoptedCounts.getOrDefault(task.getId(), 0));
            NailAiResult cover = covers.get(task.getId());
            row.put("coverUrl", cover == null ? "" : mediaSigner.resultUrl(cover.getId(), false));
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> toDetailView(NailAiTask task) {
        Map<String, Object> row = baseView(task);
        row.put("provider", task.getProvider());
        row.put("templateVersion", task.getTemplateVersion());
        row.put("startedTime", formatTime(task.getStartedTime()));
        row.put("finishedTime", formatTime(task.getFinishedTime()));

        Map<String, Object> designSpec = new LinkedHashMap<>();
        designSpec.put("creativeMode", creativeMode(task));
        designSpec.put("nailShape", valueOr(task.getNailShape(), "SHORT_ALMOND"));
        designSpec.put("finish", valueOr(task.getFinish(), "VELVET_CAT_EYE"));
        designSpec.put("designStyle", valueOr(task.getDesignStyle(), "QUIET_LUXURY"));
        designSpec.put("layoutStyle", valueOr(task.getLayoutStyle(), "TWO_ACCENTS"));
        designSpec.put("trendPreset", valueOr(task.getTrendPreset(), "CUSTOM"));
        designSpec.put("referenceStrategy", valueOr(task.getReferenceStrategy(), "REINTERPRET"));
        designSpec.put("colorPalette", valueOr(task.getColorPalette(), ""));
        row.put("designSpec", designSpec);

        if (task.getReferenceAssetId() != null) {
            row.put("referenceAsset", assetService.findView(task.getReferenceAssetId()));
        } else {
            row.put("referenceAsset", null);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (NailAiResult result : resultMapper.selectList(new QueryWrapper<NailAiResult>()
                .eq("task_id", task.getId()).orderByAsc("sort"))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", result.getId());
            item.put("url", mediaSigner.resultUrl(result.getId(), false));
            item.put("width", result.getWidth());
            item.put("height", result.getHeight());
            item.put("reviewStatus", result.getReviewStatus());
            item.put("reviewNote", result.getReviewNote());
            item.put("reviewTime", formatTime(result.getReviewTime()));
            item.put("adoptedAssetId", result.getAdoptedAssetId());
            item.put("sort", result.getSort());
            item.put("createTime", formatTime(result.getCreateTime()));
            results.add(item);
        }
        row.put("resultCount", results.size());
        row.put("coverUrl", results.isEmpty() ? "" : results.get(0).get("url"));
        row.put("results", results);
        return row;
    }

    private Map<String, Object> baseView(NailAiTask task) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", task.getId());
        row.put("taskType", task.getTaskType());
        row.put("title", valueOr(task.getTitle(), buildTitle(task.getPromptRaw())));
        row.put("status", task.getStatus());
        row.put("prompt", task.getPromptRaw());
        row.put("aspectRatio", task.getAspectRatio());
        row.put("resolution", task.getResolution());
        row.put("outputCount", task.getOutputCount());
        row.put("referenceAssetId", task.getReferenceAssetId());
        row.put("creativeMode", creativeMode(task));
        row.put("modelCode", task.getModelCode());
        row.put("errorMessage", task.getErrorMessage());
        row.put("createTime", formatTime(task.getCreateTime()));
        return row;
    }

    private NailAiTask requireTask(Long id) {
        NailAiTask task = taskMapper.selectById(id);
        if (task == null) throw new OperateException("任务不存在");
        return task;
    }

    private NailAiResult requireResult(Long id) {
        NailAiResult result = resultMapper.selectById(id);
        if (result == null) throw new OperateException("生成结果不存在");
        return result;
    }

    private String creativeMode(NailAiTask task) {
        if (StringUtils.hasText(task.getCreativeMode())) return task.getCreativeMode();
        return task.getPromptCompiled() != null && task.getPromptCompiled().startsWith("Professional nail artist design board")
                ? "DESIGN_BOARD" : "ON_HAND";
    }

    private String buildTitle(String prompt) {
        String clean = prompt == null ? "未命名美甲设计" : prompt.trim().replaceAll("[\\r\\n]+", " ");
        if (clean.isEmpty()) return "未命名美甲设计";
        return clean.length() > 30 ? clean.substring(0, 30) + "…" : clean;
    }

    private String valueOr(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String formatTime(Long timestamp) {
        return timestamp == null || timestamp == 0 ? "" : TimeUtils.timestampToDate(timestamp);
    }
}
