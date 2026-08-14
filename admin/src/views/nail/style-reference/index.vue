<template>
  <div class="style-reference">
    <header class="library-head">
      <div class="title-block">
        <span>STYLE REFERENCES</span>
        <h1>风格母版</h1>
        <p>上传风格母版图，前台 AI 创作时用它锚定质感、光影与构图。</p>
      </div>
      <div class="head-actions">
        <div class="library-stat"><b>{{ list.length }}</b><span>当前母版</span></div>
        <el-button type="primary" @click="uploadDialog = true"><icon name="el-icon-Upload" /> 上传母版</el-button>
      </div>
    </header>

    <section class="library-shell">
      <aside class="filter-panel">
        <header><div><span>FILTER</span><b>精细筛选</b></div><button type="button" @click="resetFilters">重置</button></header>
        <el-input v-model="query.keyword" clearable placeholder="名称或分类" @keyup.enter="loadList"><template #prefix><icon name="el-icon-Search" /></template></el-input>
        <div class="filter-section"><label>状态</label><div class="state-group"><button v-for="item in statusOptions" :key="item.value" type="button" :class="{ active: query.status === item.value }" @click="query.status = item.value; loadList()">{{ item.label }}</button></div></div>
        <el-button class="filter-submit" type="primary" @click="loadList"><icon name="el-icon-Filter" /> 应用筛选</el-button>
      </aside>

      <main class="style-content">
        <div class="content-toolbar"><div><b>{{ list.length }} 个母版</b><span>前台按排序展示已上架的母版</span></div></div>
        <div v-loading="loading" class="style-grid" :class="{ empty: !list.length }">
          <article v-for="item in list" :key="item.id" class="style-card">
            <div class="style-image"><img :src="item.thumbUrl" :alt="item.name" loading="lazy" /><span class="status-pill" :class="{ off: item.status !== 'ACTIVE' }">{{ item.status === 'ACTIVE' ? '已上架' : '已下架' }}</span></div>
            <div class="style-copy">
              <header><div><h2>{{ item.name }}</h2><p>{{ item.category }}</p></div><button type="button" aria-label="编辑母版" @click="openEdit(item)"><icon name="el-icon-MoreFilled" /></button></header>
              <div class="style-meta"><span>{{ item.promptEnhance || '无提示词增强' }}</span></div>
              <footer><time>{{ item.createTime?.slice(0, 10) }}</time><div class="card-actions"><button type="button" @click="toggleStatus(item)">{{ item.status === 'ACTIVE' ? '下架' : '上架' }}</button><button type="button" class="danger" @click="handleDelete(item)">删除</button></div></footer>
            </div>
          </article>
          <el-empty v-if="!loading && !list.length" description="还没有风格母版，点击右上角上传" />
        </div>
      </main>
    </section>

    <el-dialog v-model="uploadDialog" title="上传风格母版" width="620px" @closed="clearForm">
      <el-upload ref="uploadRef" drag :auto-upload="false" :limit="1" accept="image/png,image/jpeg" :on-change="onFileChange" :on-remove="onFileRemove">
        <icon name="el-icon-UploadFilled" /><div class="el-upload__text">拖入一张母版图，或<em>点击选择</em></div>
        <template #tip><div class="upload-tip"><span>PNG / JPG</span><span>建议 1:1 或 3:4</span><span>单张不超过 10MB</span></div></template>
      </el-upload>
      <el-form label-position="top" class="upload-form">
        <el-form-item label="母版名称"><el-input v-model="form.name" maxlength="80" placeholder="如：钻饰猫眼" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" maxlength="32" placeholder="如：钻饰 / 韩系 / 法式" /></el-form-item>
        <el-form-item label="提示词增强（可选）"><el-input v-model="form.promptEnhance" maxlength="500" type="textarea" :rows="2" placeholder="补充质感描述，例如：奢华钻饰光带，高级质感" /></el-form-item>
        <el-form-item label="参考策略"><el-select v-model="form.referenceStrategy"><el-option label="提取语言重新设计" value="REINTERPRET" /><el-option label="保留配色" value="KEEP_PALETTE" /><el-option label="保留布局" value="KEEP_LAYOUT" /><el-option label="保留材质" value="KEEP_TEXTURE" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="uploadDialog = false">取消</el-button><el-button type="primary" :loading="uploading" :disabled="!fileList.length" @click="handleUpload">上传</el-button></template>
    </el-dialog>

    <el-dialog v-model="editDialog" title="编辑风格母版" width="560px">
      <el-form label-position="top">
        <el-form-item label="名称"><el-input v-model="form.name" maxlength="80" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" maxlength="32" /></el-form-item>
        <el-form-item label="提示词增强"><el-input v-model="form.promptEnhance" maxlength="500" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="参考策略"><el-select v-model="form.referenceStrategy"><el-option label="提取语言重新设计" value="REINTERPRET" /><el-option label="保留配色" value="KEEP_PALETTE" /><el-option label="保留布局" value="KEEP_LAYOUT" /><el-option label="保留材质" value="KEEP_TEXTURE" /></el-select></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDialog = false">取消</el-button><el-button type="primary" @click="saveEdit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup name="nailStyleReference">
import { onMounted, reactive, ref } from 'vue'
import type { UploadInstance, UploadUserFile } from 'element-plus'
import feedback from '@/utils/feedback'
import { nailStyleReferenceList, nailStyleReferenceUpload, nailStyleReferenceUpdate, nailStyleReferenceDelete, nailStyleReferenceStatus } from '@/api/nail'

const loading = ref(false)
const uploading = ref(false)
const list = ref<any[]>([])
const query = reactive({ keyword: '', status: 'ALL' })
const statusOptions = [{ label: '全部状态', value: 'ALL' }, { label: '已上架', value: 'ACTIVE' }, { label: '已下架', value: 'DISABLED' }]

const loadList = async () => {
  loading.value = true
  try {
    const params: any = { pageNo: 1, pageSize: 50 }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== 'ALL') params.status = query.status
    list.value = (await nailStyleReferenceList(params)).lists || []
  } finally { loading.value = false }
}

const resetFilters = () => { query.keyword = ''; query.status = 'ALL'; loadList() }

const uploadDialog = ref(false)
const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadUserFile[]>([])
const form = reactive({ id: 0, name: '', category: '', promptEnhance: '', referenceStrategy: 'REINTERPRET', sort: 0 })
const onFileChange = (file: UploadUserFile) => { fileList.value = [file] }
const onFileRemove = () => { fileList.value = [] }
const clearForm = () => { uploadRef.value?.clearFiles(); fileList.value = []; Object.assign(form, { id: 0, name: '', category: '', promptEnhance: '', referenceStrategy: 'REINTERPRET', sort: 0 }) }

const handleUpload = async () => {
  const file = fileList.value[0]?.raw
  if (!file) return feedback.msgError('请先选择图片')
  if (!form.name.trim() || !form.category.trim()) return feedback.msgError('请填写名称和分类')
  uploading.value = true
  try {
    const data = new FormData()
    data.append('file', file)
    data.append('name', form.name)
    data.append('category', form.category)
    data.append('promptEnhance', form.promptEnhance)
    data.append('referenceStrategy', form.referenceStrategy)
    await nailStyleReferenceUpload(data)
    feedback.msgSuccess('上传成功')
    uploadDialog.value = false
    clearForm()
    loadList()
  } catch (e: any) { feedback.msgError(e?.message || '上传失败') } finally { uploading.value = false }
}

const editDialog = ref(false)
const openEdit = (item: any) => { Object.assign(form, { id: item.id, name: item.name, category: item.category, promptEnhance: item.promptEnhance, referenceStrategy: item.referenceStrategy, sort: item.sort }); editDialog.value = true }
const saveEdit = async () => {
  await nailStyleReferenceUpdate({ id: form.id, name: form.name.trim(), category: form.category.trim(), promptEnhance: form.promptEnhance, referenceStrategy: form.referenceStrategy, sort: form.sort })
  feedback.msgSuccess('已更新')
  editDialog.value = false
  loadList()
}

const toggleStatus = async (item: any) => {
  await nailStyleReferenceStatus({ id: item.id, status: item.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' })
  feedback.msgSuccess('操作成功')
  loadList()
}

const handleDelete = async (item: any) => {
  await feedback.confirm(`确定删除风格母版「${item.name}」？`)
  await nailStyleReferenceDelete({ id: item.id })
  feedback.msgSuccess('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.style-reference{min-height:calc(100vh - 94px);color:#272823}
.library-head{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;padding:8px 2px 24px}
.title-block>span{color:#a4545c;font-size:9px;letter-spacing:.2em}
.title-block h1{margin:6px 0 5px;font-family:'Noto Serif SC','Songti SC',serif;font-size:28px;font-weight:580}
.title-block p{margin:0;color:#6b6f69;font-size:12px}
.head-actions{display:flex;align-items:center;gap:16px}
.library-stat{display:grid;text-align:right}
.library-stat b{font-family:Georgia,serif;font-size:20px;font-weight:500}
.library-stat span{color:#858983;font-size:9px}
.library-shell{display:grid;grid-template-columns:230px minmax(0,1fr);min-height:680px;border:1px solid #dedfd9;background:#fff}
.filter-panel{padding:18px 16px;border-right:1px solid #dedfd9;background:#f7f6f2}
.filter-panel>header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}
.filter-panel>header div{display:grid}
.filter-panel>header span{color:#a4545c;font-size:8px;letter-spacing:.16em}
.filter-panel>header b{font-family:'Noto Serif SC','Songti SC',serif;font-size:17px}
.filter-panel>header button{border:0;background:transparent;color:#8c5b62;font-size:10px}
.filter-panel :deep(.el-input__wrapper){border-radius:5px;box-shadow:0 0 0 1px #dcddd7 inset}
.filter-section{display:grid;gap:7px;margin-top:17px}
.filter-section>label{color:#60635e;font-size:10px;font-weight:620}
.state-group{display:grid;gap:5px}
.state-group button{padding:9px 10px;border:1px solid #dedfd9;border-radius:4px;background:#fff;color:#60635e;font-size:11px;text-align:left}
.state-group button.active{border-color:#b56872;background:#f7e9eb;color:#8f3843}
.state-group button.active{border-color:#b56872;background:#f7e9eb;color:#8f3843}
.filter-submit{width:100%;margin-top:18px}
.style-content{min-width:0;padding:16px 18px 24px;background:#fbfbf9}
.content-toolbar{display:flex;align-items:center;justify-content:space-between;padding-bottom:14px;border-bottom:1px solid #e4e5df}
.content-toolbar>div{display:grid;gap:2px}
.content-toolbar b{font-size:12px}
.content-toolbar span{color:#858983;font-size:9px}
.style-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;min-height:300px;margin-top:14px;align-items:start}
.style-grid.empty{display:flex;align-items:center;justify-content:center;min-height:400px}
.style-card{min-width:0;overflow:hidden;border:1px solid #dfe0da;background:#fff;transition:border-color .18s ease,transform .18s ease,box-shadow .18s ease}
.style-card:hover{border-color:#c99da3;transform:translateY(-2px);box-shadow:0 14px 32px rgba(45,42,37,.08)}
.style-image{position:relative;aspect-ratio:1/1;overflow:hidden;background:#ecece7}
.style-image>img{display:block;width:100%;height:100%;object-fit:cover}
.status-pill{position:absolute;top:9px;left:9px;padding:5px 7px;border-radius:3px;background:#3f6f54;color:#fff;font-size:8px}
.status-pill.off{background:#8a8d87}
.style-copy{padding:12px}
.style-copy>header{display:flex;align-items:flex-start;justify-content:space-between;gap:8px}
.style-copy h2{margin:0;font-size:12px;font-weight:600}
.style-copy header p{margin:3px 0 0;color:#8a8d87;font-size:9px}
.style-copy header button{border:0;background:transparent;color:#9a9d97;cursor:pointer}
.style-meta{margin-top:10px;min-height:30px;color:#60635e;font-size:9px;line-height:1.5}
.style-copy footer{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-top:10px;padding-top:10px;border-top:1px solid #e7e8e2}
.style-copy footer time{color:#9a9d97;font-size:8px}
.card-actions{display:flex;gap:6px}
.card-actions button{padding:6px 14px;border:1px solid #dfe0da;border-radius:4px;background:#fff;color:#60635e;font-size:11px;cursor:pointer}
.card-actions button:hover{border-color:#b56872;color:#8f3843}
.card-actions button.danger:hover{border-color:#c65a5a;color:#a84444}
.upload-tip{display:flex;flex-wrap:wrap;gap:6px;margin-top:8px}
.upload-tip span{padding:4px 6px;border:1px solid #e0e1dc;border-radius:4px;color:#72766f;font-size:8px}
.upload-form{margin-top:18px}
.upload-form :deep(.el-select){width:100%}
@media(max-width:1280px){.style-grid{grid-template-columns:repeat(3,minmax(0,1fr))}}
@media(max-width:960px){.library-shell{grid-template-columns:1fr}.filter-panel{border-right:0;border-bottom:1px solid #dedfd9}.style-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}
@media(max-width:620px){.library-head{align-items:flex-start;flex-direction:column}.style-grid{grid-template-columns:1fr}.style-content{padding:12px}}
</style>
