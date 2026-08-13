package com.mdd.common.mapper.goods;

import com.mdd.common.core.basics.IBaseMapper;
import com.mdd.common.entity.goods.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品
 */
@Mapper
public interface GoodsMapper extends IBaseMapper<Goods> {
}