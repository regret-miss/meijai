<template>
    <div class="goods-edit">
        <el-card shadow="never">
            <div class="mb-4">
                <router-link :to="'/goods/lists'">
                    <el-button type="primary">返回列表</el-button>
                </router-link>
            </div>

            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-row :gutter="24">
                    <el-col :span="12">
                        <el-form-item label="商品名称" prop="name">
                            <el-input v-model="formData.name" placeholder="请输入商品名称" maxlength="120" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="商品分类" prop="categoryId">
                            <el-select v-model="formData.categoryId" placeholder="请选择分类" class="w-full">
                                <el-option
                                    v-for="item in categoryOptions"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id"
                                />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-form-item label="商品主图">
                    <material-picker v-model="formData.image" :limit="1" />
                </el-form-item>

                <el-form-item label="商品轮播图">
                    <material-picker v-model="formData.images" :limit="5" />
                </el-form-item>

                <el-form-item label="商品详情">
                    <editor v-model="formData.content" :height="400" />
                </el-form-item>

                <el-row :gutter="24">
                    <el-col :span="12">
                        <el-form-item label="排序" prop="sort">
                            <el-input-number v-model="formData.sort" :min="0" :max="99999" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="虚拟销量">
                            <el-input-number v-model="formData.salesVirtual" :min="0" :max="999999" />
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row :gutter="24">
                    <el-col :span="12">
                        <el-form-item label="上架状态" prop="isShow">
                            <el-switch
                                v-model="formData.isShow"
                                :active-value="1"
                                :inactive-value="0"
                            />
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- SKU 编辑区域 -->
                <el-divider content-position="left">商品规格(SKU)</el-divider>
                <div class="mb-4">
                    <el-button type="primary" @click="addSku">添加规格</el-button>
                </div>
                <el-table :data="formData.skuList" border size="medium">
                    <el-table-column label="规格名称" min-width="160">
                        <template #default="{ row, $index }">
                            <el-input v-model="row.skuName" placeholder="如: 红色-L" maxlength="120" />
                        </template>
                    </el-table-column>
                    <el-table-column label="图片" width="120">
                        <template #default="{ row }">
                            <material-picker v-model="row.image" :limit="1" size="small" />
                        </template>
                    </el-table-column>
                    <el-table-column label="售价(元)" width="140">
                        <template #default="{ row }">
                            <el-input-number
                                v-model="row.price"
                                :min="0"
                                :precision="2"
                                :controls="false"
                                style="width: 100%"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="成本价(元)" width="140">
                        <template #default="{ row }">
                            <el-input-number
                                v-model="row.costPrice"
                                :min="0"
                                :precision="2"
                                :controls="false"
                                style="width: 100%"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="库存" width="120">
                        <template #default="{ row }">
                            <el-input-number
                                v-model="row.stock"
                                :min="0"
                                :controls="true"
                                style="width: 100%"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80" fixed="right">
                        <template #default="{ $index }">
                            <el-button type="danger" link @click="removeSku($index)">
                                删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-form>
        </el-card>

        <div class="fixed-buttons">
            <el-button size="large" type="primary" @click="handleSave">保存</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup name="goodsEdit">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { goodsAdd, goodsEdit, goodsDetail, goodsCategoryAll } from '@/api/goods'
import feedback from '@/utils/feedback'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const editId = ref<number>(0)

const formData = reactive({
    categoryId: 0,
    name: '',
    image: '',
    images: '',
    content: '',
    sort: 100,
    isShow: 1,
    salesVirtual: 0,
    skuList: [] as any[]
})

const formRules = {
    name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
    sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
    isShow: [{ required: true, message: '请选择上架状态', trigger: 'change' }]
}

const categoryOptions = ref<any[]>([])

const getCategoryOptions = async () => {
    try {
        const data = await goodsCategoryAll({})
        categoryOptions.value = data
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

const getDetail = async (id: number) => {
    try {
        const data = await goodsDetail({ id })
        formData.categoryId = data.categoryId
        formData.name = data.name
        formData.image = data.image
        formData.images = data.images
        formData.content = data.content
        formData.sort = data.sort
        formData.isShow = data.isShow
        formData.salesVirtual = data.salesVirtual
        formData.skuList = data.skuList || []
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

const addSku = () => {
    formData.skuList.push({
        skuName: '',
        image: '',
        price: 0,
        costPrice: 0,
        stock: 0,
        skuData: ''
    })
}

const removeSku = (index: number) => {
    formData.skuList.splice(index, 1)
}

const handleSave = async () => {
    await formRef.value?.validate()
    try {
        if (editId.value) {
            await goodsEdit({ id: editId.value, ...formData })
            feedback.msgSuccess('编辑成功')
        } else {
            await goodsAdd({ ...formData })
            feedback.msgSuccess('新增成功')
        }
        router.push('/goods/lists')
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

getCategoryOptions()

const queryId = route.query.id
if (queryId) {
    editId.value = Number(queryId)
    getDetail(editId.value)
}
</script>

<style lang="scss" scoped>
.goods-edit {
    padding-bottom: 80px;
}

.fixed-buttons {
    position: fixed;
    bottom: 0;
    right: 0;
    left: 0;
    z-index: 99;
    background: white;
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: center;
}
</style>