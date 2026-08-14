package com.mdd.admin.nail.service;

import com.mdd.admin.nail.config.NailAiProperties;
import com.mdd.admin.nail.provider.GeneratedImage;
import com.mdd.admin.nail.provider.NailGenerationCommand;
import com.mdd.admin.nail.provider.NailImageProvider;
import com.mdd.admin.nail.storage.NailAssetStorage;
import com.mdd.admin.nail.storage.StoredImage;
import com.mdd.common.entity.nail.NailAiResult;
import com.mdd.common.entity.nail.NailAiTask;
import com.mdd.common.entity.nail.NailAsset;
import com.mdd.common.entity.nail.NailStyleReference;
import com.mdd.common.exception.BaseException;
import com.mdd.common.mapper.nail.NailAiResultMapper;
import com.mdd.common.mapper.nail.NailAiTaskMapper;
import com.mdd.common.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class NailAiWorker {
    private static final Logger log = LoggerFactory.getLogger(NailAiWorker.class);
    @Resource private NailAiTaskMapper taskMapper;
    @Resource private NailAiResultMapper resultMapper;
    @Resource private NailAssetService assetService;
    @Resource private NailAssetStorage storage;
    @Resource private NailImageProvider provider;
    @Resource private NailStyleReferenceService styleReferenceService;
    @Resource private NailImagePostProcessor postProcessor;
    @Resource private NailAestheticClient aestheticClient;
    @Resource private NailHeuristicScorer heuristicScorer;
    @Resource private NailAiProperties properties;

    @Async("nailAiExecutor")
    public void generate(Long taskId) {
        NailAiTask task = taskMapper.selectById(taskId);
        if (task == null || !"QUEUED".equals(task.getStatus())) return;
        long now = TimeUtils.timestamp();
        task.setStatus("RUNNING"); task.setStartedTime(now); task.setUpdateTime(now);
        if (task.getModelCode() == null || task.getModelCode().isBlank()) {
            task.setModelCode(provider.modelCode());
        }
        taskMapper.updateById(task);
        int succeeded = 0;
        String lastError = "";
        try {
            List<byte[]> referenceImages = new ArrayList<>();
            List<String> referenceMimeTypes = new ArrayList<>();
            if (task.getReferenceAssetId() != null) {
                NailAsset reference = assetService.requireUsable(task.getReferenceAssetId());
                referenceImages.add(storage.read(reference.getUri()));
                referenceMimeTypes.add(reference.getMimeType());
            } else if (task.getReferenceResultId() != null) {
                NailAiResult refResult = resultMapper.selectById(task.getReferenceResultId());
                if (refResult != null) {
                    referenceImages.add(storage.read(refResult.getUri()));
                    referenceMimeTypes.add(refResult.getMimeType());
                }
            }
            if (task.getStyleReferenceId() != null) {
                NailStyleReference style = styleReferenceService.require(task.getStyleReferenceId());
                referenceImages.add(storage.read(style.getUri()));
                referenceMimeTypes.add(style.getMimeType());
            }
            for (int index = 0; index < task.getOutputCount(); index++) {
                try {
                    long seedBase = task.getSeed() == null ? 0L : task.getSeed();
                    GeneratedImage generated = provider.generate(new NailGenerationCommand(
                            task.getPromptCompiled(), task.getModelCode(), task.getAspectRatio(), task.getResolution(),
                            seedBase > 0 ? seedBase + index : 0L,
                            referenceImages.isEmpty() ? null : referenceImages,
                            referenceMimeTypes.isEmpty() ? null : referenceMimeTypes));
                    byte[] enhanced = postProcessor.sharpen(generated.bytes(), generated.mimeType(),
                            properties.isPostprocessEnabled() ? properties.getSharpenAmount() : 0);
                    StoredImage stored = storage.store(enhanced, generated.mimeType(), "results");
                    double score = heuristicScorer.score(enhanced);
                    if (properties.getAesthetic().isEnabled()) {
                        // 云美学（智谱免费 / 火山豆包）配置了 Key 时优先使用，失败回退启发式
                        double cloud = aestheticClient.score(enhanced, generated.mimeType());
                        if (cloud > 0) score = cloud;
                    }
                    NailAiResult result = new NailAiResult(); result.setTaskId(taskId); result.setUri(stored.uri());
                    result.setMimeType(stored.mimeType()); result.setWidth(stored.width()); result.setHeight(stored.height());
                    result.setReviewStatus("PENDING"); result.setReviewNote(""); result.setReviewerId(0); result.setReviewTime(0L);
                    result.setSort(index); result.setScore(score > 0 ? score : null);
                    result.setCreateTime(TimeUtils.timestamp());
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
