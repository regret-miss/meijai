<template>
    <popup
        ref="popupRef"
        :title="mode === 'add' ? '新增分类' : '编辑分类'"
        width="480px"
        @confirm="handleConfirm"
        @close="handleClose"
    >
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
            <el-form-item label="分类名称" prop="name">
                <el-input v-model="formData.name" placeholder="请输入分类名称" maxlength="60" />
            </el-form-item>
            <el-form-item label="排序" prop="sort">
                <el-input-number v-model="formData.sort" :min="0" :max="99999" />
            </el-form-item>
            <el-form-item label="状态" prop="isShow">
                <el-switch
                    v-model="formData.isShow"
                    :active-value="1"
                    :inactive-value="0"
                />
            </el-form-item>
        </el-form>
    </popup>
</template>

<script lang="ts" setup name="goodsCategoryEdit">
import { reactive, ref, shallowRef } from 'vue'
import Popup from '@/components/popup/index.vue'
import { goodsCategoryAdd, goodsCategoryEdit, goodsCategoryDetail } from '@/api/goods'
import feedback from '@/utils/feedback'

const emit = defineEmits(['refresh'])

const popupRef = shallowRef<InstanceType<typeof Popup>>()
const formRef = ref()

const mode = ref<'add' | 'edit'>('add')
const editId = ref<number>(0)

const formData = reactive({
    name: '',
    sort: 0,
    isShow: 1
})

const formRules = {
    name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
    sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

const open = (type: 'add' | 'edit') => {
    mode.value = type
    popupRef.value?.open()
}

const getDetail = async (id: number) => {
    editId.value = id
    const data = await goodsCategoryDetail({ id })
    formData.name = data.name
    formData.sort = data.sort
    formData.isShow = data.isShow
}

const handleConfirm = async () => {
    await formRef.value?.validate()
    try {
        if (mode.value === 'add') {
            await goodsCategoryAdd({ ...formData })
            feedback.msgSuccess('新增成功')
        } else {
            await goodsCategoryEdit({ id: editId.value, ...formData })
            feedback.msgSuccess('编辑成功')
        }
        popupRef.value?.close()
        emit('refresh')
    } catch (e: any) {
        feedback.msgError(e.message)
    }
}

const handleClose = () => {
    formRef.value?.resetFields()
    formData.name = ''
    formData.sort = 0
    formData.isShow = 1
    editId.value = 0
}

defineExpose({ open, getDetail })
</script>