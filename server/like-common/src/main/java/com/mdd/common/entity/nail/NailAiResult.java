package com.mdd.common.entity.nail;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class NailAiResult {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String uri;
    private String mimeType;
    private Integer width;
    private Integer height;
    private String reviewStatus;
    private String reviewNote;
    private Integer reviewerId;
    private Long reviewTime;
    private Integer adoptedAssetId;
    private Integer sort;
    private Double score;
    private Long createTime;
}
