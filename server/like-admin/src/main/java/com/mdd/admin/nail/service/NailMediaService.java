package com.mdd.admin.nail.service;

import com.mdd.admin.nail.storage.NailAssetStorage;
import com.mdd.common.entity.nail.NailAiResult;
import com.mdd.common.entity.nail.NailAsset;
import com.mdd.common.entity.nail.NailAssetAudit;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.nail.NailAiResultMapper;
import com.mdd.common.mapper.nail.NailAssetAuditMapper;
import com.mdd.common.mapper.nail.NailAssetMapper;
import com.mdd.common.util.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class NailMediaService {
    @Resource private NailMediaSigner signer;
    @Resource private NailAssetStorage storage;
    @Resource private NailAssetMapper assetMapper;
    @Resource private NailAiResultMapper resultMapper;
    @Resource private NailAssetAuditMapper auditMapper;

    public MediaFile read(String type, long id, String variant, boolean download, long expires, String signature) {
        String safeVariant = normalizeVariant(variant);
        signer.verify(type, id, safeVariant, download, expires, signature);
        try {
            if ("asset".equals(type)) return readAsset(Math.toIntExact(id), safeVariant, download);
            if ("result".equals(type)) return readResult(id, download);
            throw new OperateException("不支持的媒体类型");
        } catch (IOException error) {
            throw new OperateException("图片文件读取失败");
        }
    }

    private MediaFile readAsset(Integer id, String variant, boolean download) throws IOException {
        NailAsset asset = assetMapper.selectById(id);
        if (asset == null || Integer.valueOf(1).equals(asset.getIsDelete()) || !"ACTIVE".equals(asset.getStatus())) {
            throw new OperateException("资产不存在或不可预览");
        }
        String uri = asset.getUri();
        if (!download && "200".equals(variant) && StringUtils.hasText(asset.getThumb200Uri())) uri = asset.getThumb200Uri();
        if (!download && "600".equals(variant) && StringUtils.hasText(asset.getThumb600Uri())) uri = asset.getThumb600Uri();
        if (download) audit(asset.getId(), "DOWNLOAD", "下载原始资产", 0);
        return new MediaFile(storage.read(uri), asset.getMimeType(), safeFileName(asset.getName(), asset.getMimeType()), download);
    }

    private MediaFile readResult(Long id, boolean download) throws IOException {
        NailAiResult result = resultMapper.selectById(id);
        if (result == null) throw new OperateException("生成结果不存在");
        return new MediaFile(storage.read(result.getUri()), result.getMimeType(), "nail-ai-result-" + id + extension(result.getMimeType()), download);
    }

    private void audit(Integer assetId, String action, String detail, int operatorId) {
        NailAssetAudit audit = new NailAssetAudit();
        audit.setAssetId(assetId); audit.setAction(action); audit.setDetail(detail);
        audit.setOperatorId(operatorId); audit.setCreateTime(TimeUtils.timestamp());
        auditMapper.insert(audit);
    }

    private String normalizeVariant(String variant) {
        return switch (variant == null ? "600" : variant) {
            case "200" -> "200";
            case "original" -> "original";
            default -> "600";
        };
    }

    private String safeFileName(String value, String mime) {
        String name = StringUtils.hasText(value) ? value.replaceAll("[\\r\\n\\\\/\"]", "_") : "nail-asset";
        return name + extension(mime);
    }

    private String extension(String mime) { return "image/png".equals(mime) ? ".png" : ".jpg"; }

    public record MediaFile(byte[] bytes, String mimeType, String fileName, boolean download) {}
}
