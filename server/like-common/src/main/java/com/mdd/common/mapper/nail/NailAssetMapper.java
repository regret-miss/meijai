package com.mdd.common.mapper.nail;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.nail.NailAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NailAssetMapper extends IBaseMapper<NailAsset> {

    /**
     * 级联回收派生资产：软删并显式清空 source_task_id / source_result_id 外键指针。
     * <p>
     * 使用原生 SQL 是为了确保能把两列置为 NULL（MyBatis-Plus 的 updateById/UpdateWrapper
     * 对 null 字段的落库行为不可靠），从而解除与 la_nail_ai_task / la_nail_ai_result 的外键关联。
     */
    @Update("UPDATE la_nail_asset SET is_delete = 1, status = 'DELETED', delete_time = #{deleteTime}, "
            + "update_time = #{updateTime}, source_task_id = NULL, source_result_id = NULL WHERE id = #{id}")
    void cascadeRemoveSource(@Param("id") Integer id,
                             @Param("deleteTime") Long deleteTime,
                             @Param("updateTime") Long updateTime);
}
