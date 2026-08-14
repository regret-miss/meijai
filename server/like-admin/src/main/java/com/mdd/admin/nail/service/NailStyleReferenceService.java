package com.mdd.admin.nail.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.nail.dto.NailStyleReferenceSaveRequest;
import com.mdd.admin.nail.storage.NailAssetStorage;
import com.mdd.admin.nail.storage.StoredImage;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.nail.NailStyleReference;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.nail.NailStyleReferenceMapper;
import com.mdd.common.util.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NailStyleReferenceService {
    @Resource private NailStyleReferenceMapper styleMapper;
    @Resource private NailAssetStorage storage;
    @Resource private NailMediaSigner mediaSigner;

    @Transactional
    public Map<String, Object> upload(MultipartFile file, NailStyleReferenceSaveRequest meta) {
        if (file == null || file.isEmpty()) throw new OperateException("请上传风格母版图片");
        StoredImage stored;
        try {
            stored = storage.store(file, "styles");
        } catch (IOException error) {
            throw new OperateException("风格母版图片保存失败");
        }
        NailStyleReference ref = new NailStyleReference();
        ref.setName(meta.getName().trim());
        ref.setCategory(meta.getCategory().trim());
        ref.setUri(stored.uri());
        ref.setThumbUri(StringUtils.hasText(stored.thumb600Uri()) ? stored.thumb600Uri() : stored.thumb200Uri());
        ref.setMimeType(stored.mimeType());
        ref.setPromptEnhance(meta.getPromptEnhance() == null ? "" : meta.getPromptEnhance().trim());
        ref.setReferenceStrategy(meta.getReferenceStrategy());
        ref.setSort(meta.getSort() == null ? 0 : meta.getSort());
        ref.setStatus("ACTIVE");
        ref.setIsDelete(0);
        long now = TimeUtils.timestamp();
        ref.setCreateTime(now); ref.setUpdateTime(now); ref.setDeleteTime(0L);
        styleMapper.insert(ref);
        return toView(ref, true);
    }

    public PageResult<Map<String, Object>> list(PageValidate page, String status, String keyword) {
        QueryWrapper<NailStyleReference> query = new QueryWrapper<NailStyleReference>()
                .eq("is_delete", 0)
                .eq(StringUtils.hasText(status), "status", status)
                .and(StringUtils.hasText(keyword), w -> w.like("name", keyword).or().like("category", keyword))
                .orderByAsc("sort").orderByDesc("id");
        IPage<NailStyleReference> result = styleMapper.selectPage(new Page<>(page.getPageNo(), page.getPageSize()), query);
        List<Map<String, Object>> rows = new ArrayList<>();
        result.getRecords().forEach(ref -> rows.add(toView(ref, true)));
        return PageResult.iPageHandle(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Transactional
    public void update(NailStyleReferenceSaveRequest meta) {
        NailStyleReference ref = require(meta.getId());
        ref.setName(meta.getName().trim());
        ref.setCategory(meta.getCategory().trim());
        ref.setPromptEnhance(meta.getPromptEnhance() == null ? "" : meta.getPromptEnhance().trim());
        ref.setReferenceStrategy(meta.getReferenceStrategy());
        if (meta.getSort() != null) ref.setSort(meta.getSort());
        ref.setUpdateTime(TimeUtils.timestamp());
        styleMapper.updateById(ref);
    }

    @Transactional
    public void delete(Integer id) {
        NailStyleReference ref = require(id);
        ref.setIsDelete(1); ref.setDeleteTime(TimeUtils.timestamp()); ref.setUpdateTime(TimeUtils.timestamp());
        styleMapper.updateById(ref);
    }

    @Transactional
    public void changeStatus(Integer id, String status) {
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) throw new OperateException("不支持的上下架状态");
        NailStyleReference ref = require(id);
        ref.setStatus(status); ref.setUpdateTime(TimeUtils.timestamp());
        styleMapper.updateById(ref);
    }

    public List<Map<String, Object>> publicList() {
        List<NailStyleReference> list = styleMapper.selectList(new QueryWrapper<NailStyleReference>()
                .eq("is_delete", 0).eq("status", "ACTIVE").orderByAsc("sort").orderByAsc("id"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (NailStyleReference ref : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", ref.getId());
            row.put("name", ref.getName());
            row.put("category", ref.getCategory());
            row.put("thumbUrl", mediaSigner.styleUrl(ref.getId(), "600", false));
            row.put("promptEnhance", ref.getPromptEnhance());
            row.put("referenceStrategy", ref.getReferenceStrategy());
            rows.add(row);
        }
        return rows;
    }

    public NailStyleReference require(Integer id) {
        NailStyleReference ref = styleMapper.selectById(id);
        if (ref == null || Integer.valueOf(1).equals(ref.getIsDelete())) throw new OperateException("风格母版不存在");
        return ref;
    }

    private Map<String, Object> toView(NailStyleReference ref, boolean withOriginal) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", ref.getId());
        row.put("name", ref.getName());
        row.put("category", ref.getCategory());
        row.put("mimeType", ref.getMimeType());
        row.put("promptEnhance", ref.getPromptEnhance());
        row.put("referenceStrategy", ref.getReferenceStrategy());
        row.put("sort", ref.getSort());
        row.put("status", ref.getStatus());
        row.put("thumbUrl", mediaSigner.styleUrl(ref.getId(), "600", false));
        if (withOriginal) row.put("originalUrl", mediaSigner.styleUrl(ref.getId(), "original", false));
        row.put("createTime", TimeUtils.timestampToDate(ref.getCreateTime()));
        return row;
    }
}
