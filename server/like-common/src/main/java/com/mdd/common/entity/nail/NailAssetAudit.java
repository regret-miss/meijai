package com.mdd.common.entity.nail;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class NailAssetAudit {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer assetId;
    private String action;
    private String detail;
    private Integer operatorId;
    private Long createTime;
}
