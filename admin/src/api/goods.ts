import request from '@/utils/request'

// 商品分类-全部
export function goodsCategoryAll(params?: any) {
    return request.get({ url: '/goods/category/all', params })
}
// 商品分类-列表
export function goodsCategoryList(params?: any) {
    return request.get({ url: '/goods/category/list', params })
}
// 商品分类-新增
export function goodsCategoryAdd(params: any) {
    return request.post({ url: '/goods/category/add', params })
}
// 商品分类-编辑
export function goodsCategoryEdit(params: any) {
    return request.post({ url: '/goods/category/edit', params })
}
// 商品分类-删除
export function goodsCategoryDelete(params: any) {
    return request.post({ url: '/goods/category/del', params })
}
// 商品分类-详情
export function goodsCategoryDetail(params: any) {
    return request.get({ url: '/goods/category/detail', params })
}
// 商品分类-状态
export function goodsCategoryStatus(params: any) {
    return request.post({ url: '/goods/category/change', params })
}

// 商品-列表
export function goodsList(params?: any) {
    return request.get({ url: '/goods/list', params })
}
// 商品-新增
export function goodsAdd(params: any) {
    return request.post({ url: '/goods/add', params })
}
// 商品-编辑
export function goodsEdit(params: any) {
    return request.post({ url: '/goods/edit', params })
}
// 商品-删除
export function goodsDelete(params: any) {
    return request.post({ url: '/goods/del', params })
}
// 商品-详情
export function goodsDetail(params: any) {
    return request.get({ url: '/goods/detail', params })
}
// 商品-上下架
export function goodsStatus(params: any) {
    return request.post({ url: '/goods/change', params })
}