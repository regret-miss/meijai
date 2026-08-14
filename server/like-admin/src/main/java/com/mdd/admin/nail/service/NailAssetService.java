package com.mdd.admin.nail.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.stp.StpUtil;
import com.mdd.admin.LikeAdminThreadLocal;
import com.mdd.admin.nail.dto.NailAssetBatchDeleteRequest;
import com.mdd.admin.nail.dto.NailAssetSearchRequest;
import com.mdd.admin.nail.dto.NailAssetUpdateRequest;
import com.mdd.admin.nail.dto.NailAssetUploadMetadata;
import com.mdd.admin.nail.storage.NailAssetStorage;
import com.mdd.admin.nail.storage.StoredImage;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.nail.NailAsset;
import com.mdd.common.entity.nail.NailAssetAudit;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.nail.NailAssetAuditMapper;
import com.mdd.common.mapper.nail.NailAssetMapper;
import com.mdd.common.util.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NailAssetService {
    private static final int MAX_BATCH = 50;
    private static final Set<String> CATEGORIES = Set.of("INSPIRATION", "AI_WORK", "TREND", "COMMERCIAL", "CLIENT_REFERENCE");
    private static final Set<String> STYLES = Set.of("QUIET_LUXURY", "KOREAN_CLEAR", "RUNWAY", "FUTURISTIC", "ROMANTIC", "SWEET_COOL", "MINIMALIST", "Y2K", "COQUETTE", "OLD_MONEY", "DOPAMINE", "MORANDI");
    private static final Set<String> COLORS = Set.of("PINK", "RED", "NUDE", "WHITE", "BLACK", "BLUE", "PURPLE", "GREEN", "YELLOW", "METALLIC", "NEUTRAL", "MULTICOLOR");
    private static final Set<String> SHAPES = Set.of("SHORT_ALMOND", "SHORT_SQUOVAL", "ALMOND", "SQUARE", "COFFIN", "ROUND", "STILETTO", "LIPSTICK");
    private static final Set<String> CRAFTS = Set.of("VELVET_CAT_EYE", "JELLY", "CHROME", "MICRO_FRENCH", "AURA", "SCULPTED_GEL", "GLOSSY_GEL", "FRENCH_TIP", "MILK_BATH", "OMBRE", "GLITTER", "PEARL");
    private static final Set<String> SOURCES = Set.of("UPLOAD", "AI", "PUBLIC_REFERENCE");
    private static final Set<String> COPYRIGHTS = Set.of("ORIGINAL", "AUTHORIZED", "AI_GENERATED");

    @Resource private NailAssetMapper assetMapper;
    @Resource private NailAssetAuditMapper auditMapper;
    @Resource private NailAssetStorage storage;
    @Resource private NailMediaSigner mediaSigner;

    public PageResult<Map<String, Object>> list(PageValidate page, NailAssetSearchRequest search) {
        int size = Math.max(1, Math.min(page.getPageSize(), 50));
        boolean oldest = "OLDEST".equalsIgnoreCase(search.getSort());
        Integer cursor = decodeCursor(search.getCursor());
        QueryWrapper<NailAsset> query = applyMemberFilter(filters(search));
        if (cursor != null) {
            if (oldest) query.gt("id", cursor); else query.lt("id", cursor);
        }
        if (oldest) query.orderByAsc("id"); else query.orderByDesc("id");
        query.last("limit " + (size + 1));
        List<NailAsset> records = assetMapper.selectList(query);
        boolean hasMore = records.size() > size;
        if (hasMore) records = new ArrayList<>(records.subList(0, size));
        String nextCursor = hasMore && !records.isEmpty() ? encodeCursor(records.get(records.size() - 1).getId()) : "";
        boolean allowOriginal = canDownloadOriginal();
        List<Map<String, Object>> rows = records.stream().map(asset -> toView(asset, allowOriginal)).collect(Collectors.toList());
        long total = assetMapper.selectCount(applyMemberFilter(filters(search)));
        Map<String, Object> extend = new LinkedHashMap<>();
        extend.put("nextCursor", nextCursor); extend.put("hasMore", hasMore);
        return PageResult.iPageHandle(total, 1L, (long) size, rows, extend);
    }

    public Map<String, Object> options() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("categories", optionList(new String[][]{{"INSPIRATION","灵感参考"},{"AI_WORK","AI 作品"},{"TREND","趋势款式"},{"COMMERCIAL","商业成片"},{"CLIENT_REFERENCE","客户参考"}}));
        options.put("styles", optionList(new String[][]{{"QUIET_LUXURY","克制高级"},{"KOREAN_CLEAR","韩系清透"},{"RUNWAY","秀场前卫"},{"FUTURISTIC","未来机能"},{"ROMANTIC","细腻浪漫"},{"SWEET_COOL","甜酷混搭"},{"MINIMALIST","极简主义"},{"Y2K","千禧复古"},{"COQUETTE","甜心蝴蝶结"},{"OLD_MONEY","老钱风"},{"DOPAMINE","多巴胺"},{"MORANDI","莫兰迪"}}));
        options.put("colors", optionList(new String[][]{{"PINK","粉色系"},{"RED","红色系"},{"NUDE","裸色系"},{"WHITE","白色系"},{"BLACK","黑色系"},{"BLUE","蓝色系"},{"PURPLE","紫色系"},{"GREEN","绿色系"},{"YELLOW","黄色系"},{"METALLIC","金属色"},{"NEUTRAL","中性色"},{"MULTICOLOR","多色混搭"}}));
        options.put("shapes", optionList(new String[][]{{"SHORT_ALMOND","短杏仁"},{"SHORT_SQUOVAL","短方圆"},{"ALMOND","杏仁"},{"SQUARE","方形"},{"COFFIN","芭蕾"},{"ROUND","圆形"},{"STILETTO","尖形"},{"LIPSTICK","唇形"}}));
        options.put("crafts", optionList(new String[][]{{"VELVET_CAT_EYE","丝绒猫眼"},{"JELLY","果冻透色"},{"CHROME","镜面铬光"},{"MICRO_FRENCH","微法式"},{"AURA","晕染光圈"},{"SCULPTED_GEL","立体凝胶"},{"GLOSSY_GEL","高亮凝胶"},{"FRENCH_TIP","经典法式"},{"MILK_BATH","牛奶浴"},{"OMBRE","渐变"},{"GLITTER","满钻闪粉"},{"PEARL","珍珠"}}));
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        assetMapper.selectList(applyMemberFilter(new QueryWrapper<NailAsset>().select("tags_json").eq("is_delete", 0).eq("status", "ACTIVE").orderByDesc("id").last("limit 500")))
                .forEach(asset -> tags.addAll(parseTags(asset.getTagsJson())));
        options.put("tags", tags);
        return options;
    }

    public Map<String, Object> uploadBatch(List<MultipartFile> files, NailAssetUploadMetadata metadata, int creatorId) {
        if (files == null || files.isEmpty()) throw new OperateException("请选择图片");
        if (files.size() > MAX_BATCH) throw new OperateException("单次最多上传50张图片");
        List<Map<String, Object>> items = new ArrayList<>();
        int successCount = 0;
        for (MultipartFile file : files) {
            Map<String, Object> result = uploadTracked(file, metadata, creatorId);
            items.add(result);
            if (Boolean.TRUE.equals(result.get("success"))) successCount++;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items); response.put("successCount", successCount); response.put("failedCount", files.size() - successCount);
        return response;
    }

    private Map<String, Object> uploadTracked(MultipartFile file, NailAssetUploadMetadata metadata, int creatorId) {
        NailAsset asset = createProcessingAsset(file, metadata, creatorId, "UPLOAD");
        assetMapper.insert(asset);
        try {
            StoredImage image = storage.store(file, "assets");
            applyImage(asset, image);
            asset.setStatus("ACTIVE"); asset.setFailureReason(""); asset.setUpdateTime(TimeUtils.timestamp());
            assetMapper.updateById(asset);
            audit(asset.getId(), "UPLOAD", "上传完成：" + safeFileName(file.getOriginalFilename()), creatorId);
            Map<String, Object> item = toView(asset); item.put("success", true); return item;
        } catch (Exception error) {
            asset.setStatus("FAILED"); asset.setAiUsable(0); asset.setFailureReason(safeError(error)); asset.setUpdateTime(TimeUtils.timestamp());
            assetMapper.updateById(asset);
            audit(asset.getId(), "UPLOAD_FAILED", asset.getFailureReason(), creatorId);
            Map<String, Object> item = toView(asset); item.put("success", false); return item;
        }
    }

    @Transactional
    public Map<String, Object> upload(MultipartFile file, String name, String copyrightStatus, int creatorId, String source) {
        try {
            StoredImage image = storage.store(file, "assets");
            NailAssetUploadMetadata metadata = new NailAssetUploadMetadata();
            metadata.setName(name); metadata.setCopyrightStatus(copyrightStatus);
            metadata.setCategory("PUBLIC_REFERENCE".equals(source) ? "CLIENT_REFERENCE" : "INSPIRATION");
            NailAsset asset = createProcessingAsset(file, metadata, creatorId, allowed(SOURCES, source, "UPLOAD"));
            applyImage(asset, image); asset.setStatus("ACTIVE"); asset.setFailureReason("");
            assetMapper.insert(asset); audit(asset.getId(), "UPLOAD", "单图上传完成", creatorId);
            return toView(asset);
        } catch (IOException error) {
            throw new OperateException("图片保存失败：" + error.getMessage());
        }
    }

    public NailAsset requireUsable(Integer id) {
        NailAsset asset = assetMapper.selectOne(new QueryWrapper<NailAsset>()
                .eq("id", id).eq("is_delete", 0).eq("status", "ACTIVE").last("limit 1"));
        if (asset == null) throw new OperateException("参考资产不存在或不可用");
        if (!Integer.valueOf(1).equals(asset.getAiUsable()) || !isAuthorized(asset.getCopyrightStatus())) {
            throw new OperateException("该图片的版权状态不允许用于 AI 衍生");
        }
        return asset;
    }

    /**
     * 会员使用参考图：平台共享资产（UPLOAD/AI）对所有人开放；
     * 会员私有参考图（PUBLIC_REFERENCE）仅本人可用，避免不同会员的数据混用。
     */
    public NailAsset requireUsableForMember(Integer id, int memberId) {
        NailAsset asset = requireUsable(id);
        if ("PUBLIC_REFERENCE".equals(asset.getSource())
                && asset.getCreatorId() != null && asset.getCreatorId() != 0
                && asset.getCreatorId() != memberId) {
            throw new OperateException("只能使用自己的参考图或平台共享资产");
        }
        return asset;
    }

    @Transactional
    public Integer adopt(StoredImage image, String name, String prompt, int creatorId, Long sourceTaskId, Long sourceResultId) {
        NailAsset asset = new NailAsset();
        asset.setName(name); applyImage(asset, image);
        applyMetadata(asset, "AI_WORK", "QUIET_LUXURY", "NEUTRAL", "SHORT_ALMOND", "GLOSSY_GEL", List.of("AI采纳"));
        asset.setOriginalFilename(""); asset.setSource("AI"); asset.setCopyrightStatus("AI_GENERATED"); asset.setAiUsable(1);
        asset.setStatus("ACTIVE"); asset.setFailureReason(""); asset.setPrompt(prompt);
        asset.setSourceTaskId(sourceTaskId); asset.setSourceResultId(sourceResultId); asset.setCreatorId(creatorId); asset.setIsDelete(0);
        long now = TimeUtils.timestamp(); asset.setCreateTime(now); asset.setUpdateTime(now); asset.setDeleteTime(0L);
        assetMapper.insert(asset); audit(asset.getId(), "ADOPT_AI_RESULT", "从生成结果采纳为资产", creatorId);
        return asset.getId();
    }

    public long countActive() { return assetMapper.selectCount(new QueryWrapper<NailAsset>().eq("is_delete", 0).eq("status", "ACTIVE")); }

    /**
     * 分页查询某个会员拥有的资产（上传参考图 + 采纳的 AI 作品）。
     */
    public PageResult<Map<String, Object>> listByCreator(PageValidate page, int creatorId) {
        int size = Math.max(1, Math.min(page.getPageSize(), 50));
        QueryWrapper<NailAsset> query = new QueryWrapper<NailAsset>()
                .eq("is_delete", 0)
                .eq("creator_id", creatorId)
                .orderByDesc("id");
        IPage<NailAsset> result = assetMapper.selectPage(new Page<>(page.getPageNo(), size), query);
        List<Map<String, Object>> rows = result.getRecords().stream()
                .map(asset -> toView(asset, false))
                .collect(Collectors.toList());
        return PageResult.iPageHandle(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    public long countByCreator(int creatorId) {
        return assetMapper.selectCount(new QueryWrapper<NailAsset>().eq("is_delete", 0).eq("creator_id", creatorId));
    }

    @Transactional
    public void deleteMemberAsset(Integer id, int memberId) {
        NailAsset asset = requireExisting(id);
        if (asset.getCreatorId() == null || !Integer.valueOf(memberId).equals(asset.getCreatorId())) {
            throw new OperateException("无权删除该资产");
        }
        asset.setIsDelete(1);
        asset.setStatus("DELETED");
        asset.setDeleteTime(TimeUtils.timestamp());
        asset.setUpdateTime(TimeUtils.timestamp());
        assetMapper.updateById(asset);
        audit(asset.getId(), "SOFT_DELETE", "会员删除资产", memberId);
    }

    public Map<String, Object> findView(Integer id) {
        NailAsset asset = assetMapper.selectById(id);
        if (asset == null || Integer.valueOf(1).equals(asset.getIsDelete())) return null;
        return toView(asset, false);
    }

    public Map<String, Object> detail(Integer id) {
        NailAsset asset = assetMapper.selectById(id);
        if (asset == null || Integer.valueOf(1).equals(asset.getIsDelete())) throw new OperateException("资产不存在");
        ensureOwned(asset, LikeAdminThreadLocal.getAdminId());
        Map<String, Object> result = toView(asset, canDownloadOriginal());
        List<Map<String, Object>> audits = auditMapper.selectList(new QueryWrapper<NailAssetAudit>().eq("asset_id", id).orderByDesc("id").last("limit 20"))
                .stream().map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("action", item.getAction()); row.put("detail", item.getDetail()); row.put("operatorId", item.getOperatorId());
                    row.put("createTime", TimeUtils.timestampToDate(item.getCreateTime())); return row;
                }).collect(Collectors.toList());
        result.put("audits", audits);
        return result;
    }

    @Transactional
    public void update(NailAssetUpdateRequest request, int operatorId) {
        NailAsset asset = requireExisting(request.getId());
        ensureOwned(asset, operatorId);
        asset.setName(request.getName().trim()); asset.setCopyrightStatus(allowed(COPYRIGHTS, request.getCopyrightStatus(), "ORIGINAL"));
        asset.setAiUsable(Integer.valueOf(1).equals(request.getAiUsable()) && isAuthorized(asset.getCopyrightStatus()) ? 1 : 0);
        applyMetadata(asset, allowed(CATEGORIES, request.getCategory(), "INSPIRATION"), allowed(STYLES, request.getStyle(), "QUIET_LUXURY"),
                allowed(COLORS, request.getColorFamily(), "NEUTRAL"), allowed(SHAPES, request.getNailShape(), "SHORT_ALMOND"),
                allowed(CRAFTS, request.getCraft(), "GLOSSY_GEL"), normalizeTags(request.getTags()));
        asset.setUpdateTime(TimeUtils.timestamp()); assetMapper.updateById(asset);
        audit(asset.getId(), "UPDATE", "更新资产分类、版权与AI使用状态", operatorId);
    }

    @Transactional
    public void deleteBatch(NailAssetBatchDeleteRequest request, int operatorId) {
        for (Integer id : new LinkedHashSet<>(request.getIds())) deleteOne(id, operatorId);
    }

    private void deleteOne(Integer id, int operatorId) {
        NailAsset asset = requireExisting(id);
        ensureOwned(asset, operatorId);
        asset.setIsDelete(1); asset.setStatus("DELETED"); asset.setDeleteTime(TimeUtils.timestamp()); asset.setUpdateTime(TimeUtils.timestamp());
        assetMapper.updateById(asset); audit(asset.getId(), "SOFT_DELETE", "资产进入回收状态", operatorId);
    }

    /** 会员数据隔离：会员只能看到自己创建的资产 */
    private QueryWrapper<NailAsset> applyMemberFilter(QueryWrapper<NailAsset> query) {
        if (LikeAdminThreadLocal.isNailMember()) {
            query.eq("creator_id", LikeAdminThreadLocal.getAdminId());
        }
        return query;
    }

    /** 会员只能操作自己创建的资产（数据隔离） */
    private void ensureOwned(NailAsset asset, int operatorId) {
        if (LikeAdminThreadLocal.isNailMember() && (asset.getCreatorId() == null || asset.getCreatorId() != operatorId)) {
            throw new OperateException("无权操作该资产");
        }
    }

    /**
     * 关联删除：设计记录（任务/生成结果）被删除后，同步回收由它们采纳出来的派生资产。
     * <p>
     * 派生资产通过 source_task_id / source_result_id 与设计记录建立外键关联，
     * 因此删除记录前必须先软删资产并解除这些外键指针，否则会触发外键约束错误。
     * <p>
     * 注意：不能过滤 is_delete=0 —— 已被软删的资产行仍然保留外键指针，
     * 删除结果/任务时同样会触发外键约束，必须一并解除。
     */
    @Transactional
    public void removeDerivedBySource(Long sourceTaskId, List<Long> sourceResultIds, int operatorId) {
        boolean hasTask = sourceTaskId != null;
        boolean hasResults = sourceResultIds != null && !sourceResultIds.isEmpty();
        if (!hasTask && !hasResults) return;

        QueryWrapper<NailAsset> query = new QueryWrapper<NailAsset>();
        if (hasTask && hasResults) {
            query.and(w -> w.eq("source_task_id", sourceTaskId).or().in("source_result_id", sourceResultIds));
        } else if (hasTask) {
            query.eq("source_task_id", sourceTaskId);
        } else {
            query.in("source_result_id", sourceResultIds);
        }
        List<NailAsset> assets = assetMapper.selectList(query);
        if (assets.isEmpty()) return;

        long now = TimeUtils.timestamp();
        for (NailAsset asset : assets) {
            assetMapper.cascadeRemoveSource(asset.getId(), now, now);
            audit(asset.getId(), "CASCADE_DELETE", "关联设计记录已删除，派生资产同步回收", operatorId);
        }
    }

    public Map<String, Object> toView(NailAsset asset) {
        return toView(asset, canDownloadOriginal());
    }

    private Map<String, Object> toView(NailAsset asset, boolean allowOriginal) {
        Map<String, Object> row = new LinkedHashMap<>();
        boolean active = "ACTIVE".equals(asset.getStatus()) && Integer.valueOf(0).equals(asset.getIsDelete());
        row.put("id", asset.getId()); row.put("name", asset.getName());
        row.put("url", active ? mediaSigner.assetUrl(asset.getId(), "600", false) : "");
        row.put("smallUrl", active ? mediaSigner.assetUrl(asset.getId(), "200", false) : "");
        row.put("originalUrl", active && allowOriginal ? mediaSigner.assetUrl(asset.getId(), "original", false) : "");
        row.put("downloadUrl", active && allowOriginal ? mediaSigner.assetUrl(asset.getId(), "original", true) : "");
        row.put("mimeType", asset.getMimeType()); row.put("fileSize", asset.getFileSize()); row.put("width", asset.getWidth()); row.put("height", asset.getHeight());
        row.put("category", asset.getCategory()); row.put("style", asset.getStyle()); row.put("colorFamily", asset.getColorFamily());
        row.put("nailShape", asset.getNailShape()); row.put("craft", asset.getCraft()); row.put("tags", parseTags(asset.getTagsJson()));
        row.put("source", asset.getSource()); row.put("copyrightStatus", asset.getCopyrightStatus()); row.put("aiUsable", asset.getAiUsable());
        row.put("status", asset.getStatus()); row.put("failureReason", asset.getFailureReason()); row.put("sha256", asset.getSha256());
        row.put("originalFilename", asset.getOriginalFilename()); row.put("prompt", asset.getPrompt());
        row.put("createTime", asset.getCreateTime() == null || asset.getCreateTime() == 0 ? "" : TimeUtils.timestampToDate(asset.getCreateTime()));
        row.put("sourceTaskId", asset.getSourceTaskId()); row.put("sourceResultId", asset.getSourceResultId());
        return row;
    }

    private boolean canDownloadOriginal() {
        try {
            return LikeAdminThreadLocal.getAdminId() == 1 || StpUtil.hasPermission("nail:asset:download");
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private QueryWrapper<NailAsset> filters(NailAssetSearchRequest search) {
        QueryWrapper<NailAsset> query = new QueryWrapper<NailAsset>().eq("is_delete", 0);
        String status = StringUtils.hasText(search.getStatus()) ? search.getStatus() : "ACTIVE";
        query.eq(!"ALL".equals(status), "status", status);
        query.and(StringUtils.hasText(search.getKeyword()), wrapper -> wrapper.like("name", search.getKeyword()).or().like("original_filename", search.getKeyword()).or().like("prompt", search.getKeyword()));
        query.eq(StringUtils.hasText(search.getCategory()), "category", search.getCategory());
        query.eq(StringUtils.hasText(search.getStyle()), "style", search.getStyle());
        query.eq(StringUtils.hasText(search.getColorFamily()), "color_family", search.getColorFamily());
        query.eq(StringUtils.hasText(search.getNailShape()), "nail_shape", search.getNailShape());
        query.eq(StringUtils.hasText(search.getCraft()), "craft", search.getCraft());
        query.eq(StringUtils.hasText(search.getSource()), "source", search.getSource());
        query.eq(StringUtils.hasText(search.getCopyrightStatus()), "copyright_status", search.getCopyrightStatus());
        query.eq(search.getAiUsable() != null, "ai_usable", search.getAiUsable());
        query.ge(search.getCreatedStart() != null, "create_time", search.getCreatedStart());
        query.le(search.getCreatedEnd() != null, "create_time", search.getCreatedEnd());
        query.apply(StringUtils.hasText(search.getTag()), "JSON_CONTAINS(tags_json, JSON_QUOTE({0}))", search.getTag());
        return query;
    }

    private NailAsset createProcessingAsset(MultipartFile file, NailAssetUploadMetadata metadata, int creatorId, String source) {
        NailAsset asset = new NailAsset();
        String name = StringUtils.hasText(metadata.getName()) && file != null ? metadata.getName().trim() : safeFileName(file == null ? null : file.getOriginalFilename());
        asset.setName(name.length() > 160 ? name.substring(0, 160) : name);
        asset.setUri(""); asset.setMimeType(file == null || file.getContentType() == null ? "" : file.getContentType());
        asset.setFileSize(file == null ? 0L : file.getSize()); asset.setWidth(0); asset.setHeight(0);
        applyMetadata(asset, allowed(CATEGORIES, metadata.getCategory(), "INSPIRATION"), allowed(STYLES, metadata.getStyle(), "QUIET_LUXURY"),
                allowed(COLORS, metadata.getColorFamily(), "NEUTRAL"), allowed(SHAPES, metadata.getNailShape(), "SHORT_ALMOND"),
                allowed(CRAFTS, metadata.getCraft(), "GLOSSY_GEL"), normalizeTags(metadata.getTags()));
        asset.setOriginalFilename(safeFileName(file == null ? null : file.getOriginalFilename())); asset.setSha256("");
        asset.setThumb200Uri(""); asset.setThumb600Uri(""); asset.setSource(source);
        asset.setCopyrightStatus(allowed(COPYRIGHTS, metadata.getCopyrightStatus(), "ORIGINAL"));
        asset.setAiUsable(Integer.valueOf(1).equals(metadata.getAiUsable()) && isAuthorized(asset.getCopyrightStatus()) ? 1 : 0);
        asset.setStatus("PROCESSING"); asset.setFailureReason(""); asset.setPrompt(""); asset.setCreatorId(creatorId); asset.setIsDelete(0);
        long now = TimeUtils.timestamp(); asset.setCreateTime(now); asset.setUpdateTime(now); asset.setDeleteTime(0L);
        return asset;
    }

    private void applyImage(NailAsset asset, StoredImage image) {
        asset.setUri(image.uri()); asset.setMimeType(image.mimeType()); asset.setFileSize(image.fileSize()); asset.setWidth(image.width()); asset.setHeight(image.height());
        asset.setSha256(valueOr(image.sha256(), "")); asset.setThumb200Uri(valueOr(image.thumb200Uri(), "")); asset.setThumb600Uri(valueOr(image.thumb600Uri(), ""));
    }

    private void applyMetadata(NailAsset asset, String category, String style, String color, String shape, String craft, List<String> tags) {
        asset.setCategory(category); asset.setStyle(style); asset.setColorFamily(color); asset.setNailShape(shape); asset.setCraft(craft); asset.setTagsJson(JSON.toJSONString(tags));
    }

    private NailAsset requireExisting(Integer id) {
        NailAsset asset = assetMapper.selectById(id);
        if (asset == null || Integer.valueOf(1).equals(asset.getIsDelete())) throw new OperateException("资产不存在");
        return asset;
    }

    private void audit(Integer assetId, String action, String detail, int operatorId) {
        NailAssetAudit audit = new NailAssetAudit(); audit.setAssetId(assetId); audit.setAction(action); audit.setDetail(detail);
        audit.setOperatorId(operatorId); audit.setCreateTime(TimeUtils.timestamp()); auditMapper.insert(audit);
    }

    private List<String> normalizeTags(String raw) {
        if (!StringUtils.hasText(raw)) return List.of();
        return Arrays.stream(raw.split("[,，\\n]")).map(String::trim).filter(StringUtils::hasText)
                .map(value -> value.length() > 20 ? value.substring(0, 20) : value).distinct().limit(12).collect(Collectors.toList());
    }

    private List<String> parseTags(String json) {
        if (!StringUtils.hasText(json)) return new ArrayList<>();
        try { return JSON.parseArray(json, String.class); } catch (Exception ignored) { return new ArrayList<>(); }
    }

    private List<Map<String, String>> optionList(String[][] entries) {
        List<Map<String, String>> result = new ArrayList<>();
        for (String[] entry : entries) { Map<String, String> item = new LinkedHashMap<>(); item.put("value", entry[0]); item.put("label", entry[1]); result.add(item); }
        return result;
    }

    private String encodeCursor(Integer id) { return Base64.getUrlEncoder().withoutPadding().encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8)); }
    private Integer decodeCursor(String value) {
        if (!StringUtils.hasText(value)) return null;
        try { return Integer.valueOf(new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8)); }
        catch (Exception invalid) { throw new OperateException("分页游标无效，请重新筛选"); }
    }

    private boolean isAuthorized(String value) { return COPYRIGHTS.contains(value); }
    private String allowed(Set<String> values, String value, String fallback) { return values.contains(value) ? value : fallback; }
    private String valueOr(String value, String fallback) { return StringUtils.hasText(value) ? value : fallback; }
    private String safeFileName(String value) {
        if (!StringUtils.hasText(value)) return "未命名美甲图片";
        String clean = value.replaceAll("[\\r\\n]", "").trim(); return clean.substring(0, Math.min(clean.length(), 255));
    }
    private String safeError(Exception error) {
        String message = error.getMessage(); if (!StringUtils.hasText(message)) message = "图片处理失败";
        return message.substring(0, Math.min(message.length(), 500));
    }
}
