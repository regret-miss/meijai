package com.mdd.common.mapper.goods;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.goods.GoodsCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类
 */
@Mapper
public interface GoodsCategoryMapper extends IBaseMapper<GoodsCategory> {
}