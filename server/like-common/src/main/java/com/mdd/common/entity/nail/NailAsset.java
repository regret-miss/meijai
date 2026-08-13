package com.mdd.common.entity.nail;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class NailAsset {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String uri;
    private String mimeType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String category;
    private String style;
    private String colorFamily;
    private String nailShape;
    private String craft;
    private String tagsJson;
    private String originalFilename;
    private String sha256;
    @TableField("thumb_200_uri")
    private String thumb200Uri;
    @TableField("thumb_600_uri")
    private String thumb600Uri;
    private String source;
    private String copyrightStatus;
    private Integer aiUsable;
    private String status;
    private String failureReason;
    private String prompt;
    private Long sourceTaskId;
    private Long sourceResultId;
    private Integer creatorId;
    private Integer isDelete;
    private Long createTime;
    private Long updateTime;
    private Long deleteTime;
}
