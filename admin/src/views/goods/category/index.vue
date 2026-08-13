<template>
    <div class="article-column">
        <el-card class="search-card" shadow="never">
            <el-form ref="formRef" :inline="true" :model="formData">
                <el-form-item label="分类名称">
                    <el-input class="w-[180px]" clearable v-model="formData.name" placeholder="分类名称" />
                </el-form-item>
                <el-form-item label="是否显示">
                    <el-select class="w-[180px]" clearable v-model="formData.isShow" placeholder="请选择">
                        <el-option label="全部" :value="undefined" />
                        <el-option label="显示" :value="1" />
                        <el-option label="隐藏" :value="0" />
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
                    <el-button v-perms="['goods:category:add']" type="primary" @click="showEdit('add')">
                        新增分类
                    </el-button>
                </div>
            </div>
            <div class="mt-4">
                <el-table :data="pager.lists" v-loading="pager.loading" size="large">
                    <el-table-column label="ID" prop="id" width="80" />
                    <el-table-column label="分类名称" prop="name" min-width="120" />
                    <el-table-column label="商品数量" min-width="100">
                        <template #default="{ row }">
                            <span>{{ row.number ?? 0 }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="状态" width="100">
                        <template #default="{ row }">
                            <el-switch
                                v-perms="['goods:category:change']"
                                :model-value="row.isShow"
                                :active-value="1"
                                :inactive-value="0"
                                @change="changeStatus(row)"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="排序" prop="sort" width="100" />
                    <el-table-column label="创建时间" prop="createTime" min-width="160" />
                    <el-table-column label="操作" width="180" fixed="right">
                        <template #default="{ row }">
                            <el-button
                                v-perms="['goods:category:edit']"
                                type="primary"
                                link
                                @click="showEdit('edit', row)"
                            >
                                编辑
                            </el-button>
                            <el-button
                                v-perms="['goods:category:del']"
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

        <edit ref="editRef" @refresh="getLists" />
    </div>
</template>

<script lang="ts" setup name="goodsCategory">
import { reactive, ref, shallowRef } from 'vue'
import { goodsCategoryList, goodsCategoryStatus, goodsCategoryDelete } from '@/api/goods'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'
import Edit from './edit.vue'

const editRef = shallowRef<InstanceType<typeof Edit>>()

const formRef = ref()
const formData = reactive({
    name: '',
    isShow: undefined as number | undefined
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: goodsCategoryList,
    params: formData
})

const resetForm = () => {
    formRef.value?.resetFields()
    resetPage()
}

const changeStatus = async (data: any) => {
    try {
        await goodsCategoryStatus({ id: data.id })
        getLists()
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

const handleDelete = async (id: number) => {
    try {
        await feedback.confirm('确认删除该分类?')
        await goodsCategoryDelete({ id })
        feedback.msgSuccess('删除成功')
        getLists()
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

const showEdit = (mode: 'add' | 'edit', data?: any) => {
    editRef.value?.open(mode)
    if (mode === 'edit' && data) {
        editRef.value?.getDetail(data.id)
    }
}
</script>