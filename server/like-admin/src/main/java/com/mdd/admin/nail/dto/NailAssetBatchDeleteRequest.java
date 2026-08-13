package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class NailAssetBatchDeleteRequest {
    @NotEmpty(message = "请选择要删除的资产")
    @Size(max = 50, message = "单次最多删除50个资产")
    private List<Integer> ids;
}
