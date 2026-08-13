<template>
    <div class="goods-lists">
        <el-card class="search-card" shadow="never">
            <el-form ref="formRef" :inline="true" :model="formData">
                <el-form-item label="商品名称">
                    <el-input class="w-[180px]" clearable v-model="formData.name" placeholder="商品名称" />
                </el-form-item>
                <el-form-item label="商品分类">
                    <el-select class="w-[180px]" clearable v-model="formData.categoryId" placeholder="请选择">
                        <el-option label="全部" :value="undefined" />
                        <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="上架状态">
                    <el-select class="w-[180px]" clearable v-model="formData.isShow" placeholder="请选择">
                        <el-option label="全部" :value="undefined" />
                        <el-option label="上架" :value="1" />
                        <el-option label="下架" :value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetParams">查询</el-button>
                    <el-button @click="resetForm">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card class="mt-4" shadow="never">
            <div class="flex justify-between">
                <div></div>
                <div class="flex">
                    <el-button v-perms="['goods:add']" type="primary" @click="handleAdd">
                        新增商品
                    </el-button>
                </div>
            </div>
            <div class="mt-4">
                <el-table :data="pager.lists" v-loading="pager.loading" size="large">
                    <el-table-column label="ID" prop="id" width="80" />
                    <el-table-column label="主图" width="100">
                        <template #default="{ row }">
                            <el-image
                                v-if="row.image"
                                :src="row.image"
                                :preview-src-list="[row.image]"
                                style="width: 55px; height: 55px"
                                fit="cover"
                                preview-teleported
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="商品名称" prop="name" min-width="140" />
                    <el-table-column label="分类" prop="category" min-width="100" />
                    <el-table-column label="最低价格" min-width="100">
                        <template #default="{ row }">
                            <span>¥{{ row.priceMin }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="最高价格" min-width="100">
                        <template #default="{ row }">
                            <span>¥{{ row.priceMax }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="库存" prop="stockTotal" width="80" />
                    <el-table-column label="销量" prop="salesTotal" width="80" />
                    <el-table-column label="状态" width="100">
                        <template #default="{ row }">
                            <el-switch
                                v-perms="['goods:change']"
                                :model-value="row.isShow"
                                :active-value="1"
                                :inactive-value="0"
                                @change="changeStatus(row)"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="排序" prop="sort" width="80" />
                    <el-table-column label="创建时间" prop="createTime" min-width="160" />
                    <el-table-column label="操作" width="180" fixed="right">
                        <template #default="{ row }">
                            <el-button
                                v-perms="['goods:edit']"
                                type="primary"
                                link
                                @click="handleEdit(row.id)"
                            >
                                编辑
                            </el-button>
                            <el-button
                                v-perms="['goods:del']"
                                type="danger"
                                link
                                @click="handleDelete(row.id)"
                            >
                                删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>

<script lang="ts" setup name="goodsList">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { goodsList, goodsStatus, goodsDelete, goodsCategoryAll } from '@/api/goods'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'

const router = useRouter()

const formRef = ref()
const formData = reactive({
    name: '',
    categoryId: undefined as number | undefined,
    isShow: undefined as number | undefined
})

const categoryOptions = ref<any[]>([])

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: goodsList,
    params: formData
})

const getCategoryOptions = async () => {
    try {
        const data = await goodsCategoryAll({})
        categoryOptions.value = data
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

getCategoryOptions()

const resetForm = () => {
    formRef.value?.resetFields()
    resetPage()
}

const changeStatus = async (data: any) => {
    try {
        await goodsStatus({ id: data.id })
        getLists()
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

const handleAdd = () => {
    router.push('/goods/lists/edit')
}

const handleEdit = (id: number) => {
    router.push('/goods/lists/edit?id=' + id)
}

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm('确认删除该商品?')
        await goodsDelete({ id })
        feedback.msgSuccess('删除成功')
        getLists()
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}
</script>