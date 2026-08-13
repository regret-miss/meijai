package com.mdd.admin.nail.service;

import com.mdd.admin.nail.provider.GeneratedImage;
import com.mdd.admin.nail.provider.NailGenerationCommand;
import com.mdd.admin.nail.provider.NailImageProvider;
import com.mdd.admin.nail.storage.NailAssetStorage;
import com.mdd.admin.nail.storage.StoredImage;
import com.mdd.common.entity.nail.NailAiResult;
import com.mdd.common.entity.nail.NailAiTask;
import com.mdd.common.entity.nail.NailAsset;
import com.mdd.common.exception.BaseException;
import com.mdd.common.mapper.nail.NailAiResultMapper;
import com.mdd.common.mapper.nail.NailAiTaskMapper;
import com.mdd.common.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NailAiWorker {
    private static final Logger log = LoggerFactory.getLogger(NailAiWorker.class);
    @Resource private NailAiTaskMapper taskMapper;
    @Resource private NailAiResultMapper resultMapper;
    @Resource private NailAssetService assetService;
    @Resource private NailAssetStorage storage;
    @Resource private NailImageProvider provider;

    @Async("nailAiExecutor")
    public void generate(Long taskId) {
        NailAiTask task = taskMapper.selectById(taskId);
        if (task == null || !"QUEUED".equals(task.getStatus())) return;
        long now = TimeUtils.timestamp();
        task.setStatus("RUNNING"); task.setStartedTime(now); task.setUpdateTime(now); task.setModelCode(provider.modelCode());
        taskMapper.updateById(task);
        int succeeded = 0;
        String lastError = "";
        try {
            byte[] referenceBytes = null;
            String referenceMime = null;
            if (task.getReferenceAssetId() != null) {
                NailAsset reference = assetService.requireUsable(task.getReferenceAssetId());
                referenceBytes = storage.read(reference.getUri());
                referenceMime = reference.getMimeType();
            }
            for (int index = 0; index < task.getOutputCount(); index++) {
                try {
                    GeneratedImage generated = provider.generate(new NailGenerationCommand(
                            task.getPromptCompiled(), task.getAspectRatio(), task.getResolution(), referenceBytes, referenceMime));
                    StoredImage stored = storage.store(generated.bytes(), generated.mimeType(), "results");
                    NailAiResult result = new NailAiResult(); result.setTaskId(taskId); result.setUri(stored.uri());
                    result.setMimeType(stored.mimeType()); result.setWidth(stored.width()); result.setHeight(stored.height());
                    result.setReviewStatus("PENDING"); result.setReviewNote(""); result.setReviewerId(0); result.setReviewTime(0L);
                    result.setSort(index); result.setCreateTime(TimeUtils.timestamp());
                    resultMapper.insert(result); succeeded++;
                } catch (Exception itemError) {
                    lastError = safeMessage(itemError);
                    log.warn("Nail AI task {} item {} failed: {}", taskId, index, lastError);
                }
            }
            task.setStatus(succeeded == task.getOutputCount() ? "SUCCEEDED" : succeeded > 0 ? "PARTIAL_SUCCEEDED" : "FAILED");
            task.setErrorMessage(succeeded == task.getOutputCount() ? "" : lastError);
        } catch (Exception | LinkageError e) {
            task.setStatus("FAILED"); task.setErrorMessage(safeMessage(e));
            log.error("Nail AI task {} failed", taskId, e);
        } finally {
            task.setFinishedTime(TimeUtils.timestamp()); task.setUpdateTime(TimeUtils.timestamp()); taskMapper.updateById(task);
        }
    }

    private String safeMessage(Throwable error) {
        String message = error instanceof BaseException ? ((BaseException) error).getMsg() : error.getMessage();
        if (message == null || message.isBlank()) message = "未知错误";
        return message.length() > 900 ? message.substring(0, 900) : message;
    }
}
