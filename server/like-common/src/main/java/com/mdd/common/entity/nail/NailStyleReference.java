package com.mdd.common.entity.nail;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class NailStyleReference {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String category;
    private String uri;
    private String thumbUri;
    private String mimeType;
    private String promptEnhance;
    private String referenceStrategy;
    private Integer sort;
    private String status;
    private Integer isDelete;
    private Long createTime;
    private Long updateTime;
    private Long deleteTime;
}
