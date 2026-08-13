package com.mdd.common.entity.nail;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class NailAiTaskReference {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Integer assetId;
    private String uriSnapshot;
    private String copyrightStatusSnapshot;
    private String referenceStrategy;
    private Integer sort;
    private Long createTime;
}
