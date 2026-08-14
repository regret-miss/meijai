<template>
    <div class="asset-library">
        <header class="library-head">
            <div class="title-block">
                <span>CURATED NAIL LIBRARY</span>
                <h1>美甲资产库</h1>
                <p>按风格、色系、甲形与工艺整理图片，所有预览与下载均使用短时签名地址。</p>
            </div>
            <div class="head-actions">
                <div class="library-stat"><b>{{ total }}</b><span>当前结果</span></div>
                <el-button v-perms="['nail:asset:upload']" type="primary" @click="uploadDialog = true"><icon name="el-icon-Upload" /> 上传资产</el-button>
            </div>
        </header>

        <section class="library-shell">
            <aside class="filter-panel">
                <header><div><span>FILTER</span><b>精细筛选</b></div><button type="button" @click="resetFilters">重置</button></header>
                <el-input v-model="query.keyword" clearable placeholder="名称、文件名或生成描述" @keyup.enter="applyFilters"><template #prefix><icon name="el-icon-Search" /></template></el-input>

                <div class="filter-section"><label>资产分类</label><el-select v-model="query.category" clearable placeholder="全部分类" @change="applyFilters"><el-option v-for="item in options.categories" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
                <div class="filter-section"><label>设计风格</label><div class="choice-grid"><button v-for="item in options.styles" :key="item.value" type="button" :class="{ active: query.style === item.value }" @click="toggleFilter('style', item.value)">{{ item.label }}</button></div></div>
                <div class="filter-section"><label>色系</label><div class="color-choices"><button v-for="item in options.colors" :key="item.value" type="button" :class="{ active: query.colorFamily === item.value }" :title="item.label" @click="toggleFilter('colorFamily', item.value)"><i :class="`color-${item.value.toLowerCase()}`"></i><span>{{ item.label }}</span></button></div></div>
                <div class="filter-section split"><label>甲形</label><el-select v-model="query.nailShape" clearable placeholder="全部甲形" @change="applyFilters"><el-option v-for="item in options.shapes" :key="item.value" :label="item.label" :value="item.value" /></el-select><label>工艺</label><el-select v-model="query.craft" clearable placeholder="全部工艺" @change="applyFilters"><el-option v-for="item in options.crafts" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
                <div class="filter-section split"><label>来源</label><el-select v-model="query.source" clearable placeholder="全部来源" @change="applyFilters"><el-option label="人工上传" value="UPLOAD" /><el-option label="AI 生成" value="AI" /><el-option label="访客参考" value="PUBLIC_REFERENCE" /></el-select><label>版权</label><el-select v-model="query.copyrightStatus" clearable placeholder="全部版权" @change="applyFilters"><el-option label="原创" value="ORIGINAL" /><el-option label="已授权" value="AUTHORIZED" /><el-option label="AI 生成" value="AI_GENERATED" /></el-select></div>
                <div class="filter-section"><label>AI 使用状态</label><div class="ai-state"><button v-for="item in aiOptions" :key="String(item.value)" type="button" :class="{ active: query.aiUsable === item.value }" @click="query.aiUsable = item.value; applyFilters()">{{ item.label }}</button></div></div>
                <div class="filter-section"><label>处理状态</label><el-select v-model="query.status" placeholder="资产状态" @change="applyFilters"><el-option label="可用资产" value="ACTIVE" /><el-option label="处理失败" value="FAILED" /><el-option label="全部状态" value="ALL" /></el-select></div>
                <div class="filter-section"><label>上传时间</label><el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="x" @change="applyFilters" /></div>
                <div v-if="options.tags.length" class="filter-section"><label>热门标签</label><div class="tag-cloud"><button v-for="tag in options.tags.slice(0, 12)" :key="tag" type="button" :class="{ active: query.tag === tag }" @click="toggleFilter('tag', tag)"># {{ tag }}</button></div></div>
                <el-button class="filter-submit" type="primary" @click="applyFilters"><icon name="el-icon-Filter" /> 应用筛选 <span v-if="activeFilterCount">{{ activeFilterCount }}</span></el-button>
            </aside>

            <main class="asset-content">
                <div class="content-toolbar">
                    <div><b>{{ total }} 件资产</b><span v-if="activeFilterCount">已启用 {{ activeFilterCount }} 个筛选条件</span><span v-else>展示全部可用资产</span></div>
                    <div class="toolbar-actions">
                        <el-select v-model="query.sort" @change="applyFilters"><el-option label="最新上传" value="NEWEST" /><el-option label="最早上传" value="OLDEST" /></el-select>
                        <button type="button" :class="{ active: selectionMode }" @click="toggleSelectionMode"><icon name="el-icon-Select" /> {{ selectionMode ? '退出选择' : '批量选择' }}</button>
                        <button type="button" aria-label="刷新资产" @click="applyFilters"><icon name="el-icon-Refresh" /></button>
                    </div>
                </div>

                <div v-if="selectedIds.length" class="batch-bar"><div><b>已选择 {{ selectedIds.length }} 项</b><span>批量操作会逐项校验权限并写入审计记录</span></div><div><button type="button" @click="selectAllVisible">选择当前页</button><button type="button" @click="selectedIds = []">取消选择</button><el-button v-perms="['nail:asset:batch-delete']" type="danger" :loading="deleting" @click="removeSelected">批量删除</el-button></div></div>

                <div v-loading="loading" class="asset-waterfall" :class="{ empty: !assets.length }">
                    <article v-for="asset in assets" :key="asset.id" class="asset-card" :class="{ selected: selectedIds.includes(asset.id), failed: asset.status === 'FAILED', locked: !canDetail }" @click="handleCardClick(asset)">
                        <div v-if="asset.status === 'ACTIVE'" class="asset-image">
                            <img :src="asset.url" :alt="asset.name" loading="lazy" />
                            <span class="source-pill">{{ sourceLabel(asset.source) }}</span>
                            <button v-if="selectionMode" type="button" class="select-control" :aria-label="selectedIds.includes(asset.id) ? '取消选择' : '选择资产'" @click.stop="toggleSelect(asset.id)"><i :class="{ checked: selectedIds.includes(asset.id) }"><icon v-if="selectedIds.includes(asset.id)" name="el-icon-Check" /></i></button>
                            <div class="image-actions"><button v-if="canDetail" type="button" @click.stop="openDetail(asset)"><icon name="el-icon-FullScreen" /> 预览</button><a v-if="canDownload && asset.downloadUrl" :href="asset.downloadUrl" download aria-label="下载原图" @click.stop><icon name="el-icon-Download" /></a></div>
                        </div>
                        <div v-else class="asset-failed"><icon name="el-icon-WarningFilled" /><b>图片处理失败</b><span>{{ asset.failureReason || '未获取到可用图片' }}</span></div>
                        <div class="asset-copy">
                            <header><div><h2>{{ asset.name }}</h2><p>{{ formatMeta(asset) }}</p></div><button v-perms="['nail:asset:update']" type="button" aria-label="编辑资产" @click.stop="openEdit(asset)"><icon name="el-icon-MoreFilled" /></button></header>
                            <div class="taxonomy"><span>{{ optionLabel(options.styles, asset.style) }}</span><span>{{ optionLabel(options.shapes, asset.nailShape) }}</span><span>{{ optionLabel(options.crafts, asset.craft) }}</span></div>
                            <div v-if="asset.tags?.length" class="asset-tags"><span v-for="tag in asset.tags.slice(0, 4)" :key="tag">#{{ tag }}</span></div>
                            <footer><span :class="`copyright-${asset.copyrightStatus.toLowerCase()}`">{{ copyrightLabel(asset.copyrightStatus) }}</span><span :class="{ disabled: !asset.aiUsable }"><i></i>{{ asset.aiUsable ? 'AI 可用' : '禁止 AI' }}</span><time>{{ asset.createTime?.slice(0, 10) }}</time></footer>
                        </div>
                    </article>
                    <el-empty v-if="!loading && !assets.length" description="没有符合条件的美甲资产"><el-button type="primary" @click="resetFilters">清除筛选条件</el-button></el-empty>
                </div>
                <div class="pagination-bar">
                    <el-pagination
                        v-model:current-page="currentPage"
                        v-model:page-size="pageSize"
                        :page-sizes="[8, 16, 32, 64]"
                        :total="total"
                        layout="total, sizes, prev, pager, next, jumper"
                        @current-change="handlePageChange"
                        @size-change="handleSizeChange"
                    />
                </div>
            </main>
        </section>

        <el-dialog v-model="uploadDialog" custom-class="asset-upload-dialog" title="上传美甲资产" width="680px" @closed="clearUpload">
            <el-upload ref="uploadRef" drag multiple :auto-upload="false" :limit="50" accept="image/png,image/jpeg" :on-change="onFileChange" :on-remove="onFileRemove">
                <icon name="el-icon-UploadFilled"/><div class="el-upload__text">拖入多张图片，或<em>点击选择</em></div><template #tip><div class="upload-tip"><span>PNG / JPG</span><span>单张不超过 20MB</span><span>单批最多 50 张</span><span>服务端校验真实格式与像素</span></div></template>
            </el-upload>
            <el-form label-position="top" class="upload-metadata">
                <el-form-item label="统一资产名称"><el-input v-model="uploadForm.name" maxlength="150" placeholder="留空则使用每张图片的文件名" /></el-form-item>
                <el-form-item label="资产分类"><el-select v-model="uploadForm.category"><el-option v-for="item in options.categories" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="设计风格"><el-select v-model="uploadForm.style"><el-option v-for="item in options.styles" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="色系"><el-select v-model="uploadForm.colorFamily"><el-option v-for="item in options.colors" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="甲形"><el-select v-model="uploadForm.nailShape"><el-option v-for="item in options.shapes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="工艺"><el-select v-model="uploadForm.craft"><el-option v-for="item in options.crafts" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="版权状态"><el-select v-model="uploadForm.copyrightStatus"><el-option label="原创" value="ORIGINAL" /><el-option label="已获衍生授权" value="AUTHORIZED" /></el-select></el-form-item>
                <el-form-item label="标签"><el-input v-model="uploadForm.tags" maxlength="500" placeholder="用逗号分隔，例如：通勤、秋冬、显白" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="uploadDialog = false">取消</el-button><el-button type="primary" :loading="uploading" :disabled="!uploadFiles.length" @click="submitUpload">上传 {{ uploadFiles.length || '' }} 张图片</el-button></template>
        </el-dialog>

        <el-dialog v-model="previewDialog" class="image-preview-dialog" :show-close="true" destroy-on-close>
            <img v-if="previewUrl" :src="previewUrl" :alt="currentAsset?.name" class="preview-image" />
        </el-dialog>

        <el-dialog v-model="editDialog" title="编辑资产信息" width="600px">
            <el-form label-position="top" class="edit-form">
                <el-form-item label="资产名称" class="field-wide"><el-input v-model="editForm.name" maxlength="160" /></el-form-item>
                <el-form-item label="分类"><el-select v-model="editForm.category"><el-option v-for="item in options.categories" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="风格"><el-select v-model="editForm.style"><el-option v-for="item in options.styles" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="色系"><el-select v-model="editForm.colorFamily"><el-option v-for="item in options.colors" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="甲形"><el-select v-model="editForm.nailShape"><el-option v-for="item in options.shapes" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="工艺"><el-select v-model="editForm.craft"><el-option v-for="item in options.crafts" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
                <el-form-item label="版权状态"><el-select v-model="editForm.copyrightStatus"><el-option label="原创" value="ORIGINAL" /><el-option label="已授权" value="AUTHORIZED" /><el-option label="AI 生成" value="AI_GENERATED" /></el-select></el-form-item>
                <el-form-item label="标签" class="field-wide"><el-input v-model="editForm.tags" maxlength="500" placeholder="用逗号分隔" /></el-form-item>
                <el-form-item label="AI 使用权限" class="field-wide"><el-switch v-model="editForm.aiUsable" :active-value="1" :inactive-value="0" active-text="允许作为生成参考" /></el-form-item>
            </el-form>
            <template #footer><el-button @click="editDialog = false">取消</el-button><el-button type="primary" :disabled="!editForm.name.trim()" @click="saveEdit">保存修改</el-button></template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup name="nailAssets">
import type { UploadInstance, UploadFile } from 'element-plus'
import {
    nailAssetBatchDelete, nailAssetDetail, nailAssetList, nailAssetOptions, nailAssetUpdate, nailAssetUpload,
    type NailAsset, type NailAssetOption, type NailAssetOptions
} from '@/api/nail'
import feedback from '@/utils/feedback'
import useUserStore from '@/stores/modules/user'

const emptyOptions = (): NailAssetOptions => ({ categories: [], styles: [], colors: [], shapes: [], crafts: [], tags: [] })
const options = reactive<NailAssetOptions>(emptyOptions())
const query = reactive<any>({ keyword: '', category: '', style: '', colorFamily: '', nailShape: '', craft: '', tag: '', source: '', copyrightStatus: '', aiUsable: '', status: 'ACTIVE', sort: 'NEWEST' })
const aiOptions = [{ label: '全部', value: '' }, { label: 'AI 可用', value: 1 }, { label: '禁止 AI', value: 0 }]
const dateRange = ref<[string, string] | undefined>()
const assets = ref<NailAsset[]>([])
const total = ref(0)
const loading = ref(false)
const userStore = useUserStore()
const hasPermission = (permission: string) => userStore.perms.includes('*') || userStore.perms.includes(permission)
const canDetail = computed(() => hasPermission('nail:asset:detail'))
const canDownload = computed(() => hasPermission('nail:asset:download'))
const canUpdate = computed(() => hasPermission('nail:asset:update'))
const selectionMode = ref(false)
const selectedIds = ref<number[]>([])
const deleting = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const requestParams = () => ({ ...query, pageNo: currentPage.value, pageSize: pageSize.value, createdStart: dateRange.value?.[0] ? Math.floor(Number(dateRange.value[0]) / 1000) : undefined, createdEnd: dateRange.value?.[1] ? Math.floor(Number(dateRange.value[1]) / 1000) + 86399 : undefined })
const fetchAssets = async () => {
    loading.value = true
    try {
        const response = await nailAssetList(requestParams())
        assets.value = response.lists || []
        total.value = response.count || 0
        selectedIds.value = selectedIds.value.filter(id => assets.value.some(asset => asset.id === id))
    } finally { loading.value = false }
}
const applyFilters = () => { currentPage.value = 1; fetchAssets() }
const handlePageChange = (page: number) => { currentPage.value = page; fetchAssets() }
const handleSizeChange = (size: number) => { pageSize.value = size; currentPage.value = 1; fetchAssets() }
const toggleFilter = (key: string, value: string) => { query[key] = query[key] === value ? '' : value; applyFilters() }
const resetFilters = () => { Object.assign(query, { keyword: '', category: '', style: '', colorFamily: '', nailShape: '', craft: '', tag: '', source: '', copyrightStatus: '', aiUsable: '', status: 'ACTIVE', sort: 'NEWEST' }); dateRange.value = undefined; applyFilters() }
const activeFilterCount = computed(() => ['keyword','category','style','colorFamily','nailShape','craft','tag','source','copyrightStatus'].filter(key => query[key]).length + (query.aiUsable !== '' ? 1 : 0) + (dateRange.value?.length ? 1 : 0) + (query.status !== 'ACTIVE' ? 1 : 0))

const toggleSelectionMode = () => { selectionMode.value = !selectionMode.value; if (!selectionMode.value) selectedIds.value = [] }
const toggleSelect = (id: number) => { selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter(item => item !== id) : [...selectedIds.value, id] }
const selectAllVisible = () => { selectedIds.value = Array.from(new Set([...selectedIds.value, ...assets.value.map(asset => asset.id)])) }
const handleCardClick = (asset: NailAsset) => { if (selectionMode.value) toggleSelect(asset.id); else if (canDetail.value) openDetail(asset) }
const removeSelected = async () => { await feedback.confirm(`确定将选中的 ${selectedIds.value.length} 项资产移入回收状态？`); deleting.value = true; try { await nailAssetBatchDelete({ ids: selectedIds.value }); feedback.msgSuccess('批量删除完成'); selectedIds.value = []; selectionMode.value = false; applyFilters() } finally { deleting.value = false } }

const uploadDialog = ref(false)
const uploading = ref(false)
const uploadRef = shallowRef<UploadInstance>()
const uploadFiles = ref<File[]>([])
const uploadForm = reactive({ name: '', category: 'INSPIRATION', style: 'QUIET_LUXURY', colorFamily: 'NEUTRAL', nailShape: 'SHORT_ALMOND', craft: 'GLOSSY_GEL', copyrightStatus: 'ORIGINAL', tags: '', aiUsable: 1 })
const onFileChange = (file: UploadFile, files: UploadFile[]) => { uploadFiles.value = files.map(item => item.raw).filter(Boolean) as File[] }
const onFileRemove = (_file: UploadFile, files: UploadFile[]) => { uploadFiles.value = files.map(item => item.raw).filter(Boolean) as File[] }
const clearUpload = () => { uploadRef.value?.clearFiles(); uploadFiles.value = []; Object.assign(uploadForm, { name: '', category: 'INSPIRATION', style: 'QUIET_LUXURY', colorFamily: 'NEUTRAL', nailShape: 'SHORT_ALMOND', craft: 'GLOSSY_GEL', copyrightStatus: 'ORIGINAL', tags: '', aiUsable: 1 }) }
const submitUpload = async () => { if (!uploadFiles.value.length) return; uploading.value = true; try { const data = new FormData(); uploadFiles.value.forEach(file => data.append('files', file)); Object.entries(uploadForm).forEach(([key,value]) => data.append(key, String(value))); const result = await nailAssetUpload(data); result.failedCount ? feedback.msgWarning(`${result.successCount} 张上传成功，${result.failedCount} 张处理失败`) : feedback.msgSuccess(`${result.successCount} 张图片已安全入库`); uploadDialog.value = false; await Promise.all([loadOptions(), applyFilters()]) } finally { uploading.value = false } }

const previewDialog = ref(false)
const previewUrl = ref('')
const currentAsset = ref<NailAsset & { audits?: any[] }>()
const openDetail = async (asset: NailAsset) => { currentAsset.value = asset; previewUrl.value = asset.url; previewDialog.value = true }

const editDialog = ref(false)
const editForm = reactive({ id: 0, name: '', copyrightStatus: 'ORIGINAL', aiUsable: 1, category: 'INSPIRATION', style: 'QUIET_LUXURY', colorFamily: 'NEUTRAL', nailShape: 'SHORT_ALMOND', craft: 'GLOSSY_GEL', tags: '' })
const openEdit = (asset: NailAsset) => { Object.assign(editForm, { id: asset.id, name: asset.name, copyrightStatus: asset.copyrightStatus, aiUsable: asset.aiUsable, category: asset.category, style: asset.style, colorFamily: asset.colorFamily, nailShape: asset.nailShape, craft: asset.craft, tags: asset.tags?.join('，') || '' }); editDialog.value = true }
const openCurrentEdit = () => { if (currentAsset.value) openEdit(currentAsset.value) }
const saveEdit = async () => { await nailAssetUpdate({ ...editForm, name: editForm.name.trim() }); feedback.msgSuccess('资产信息已更新'); editDialog.value = false; await Promise.all([loadOptions(), applyFilters()]) }

const loadOptions = async () => Object.assign(options, await nailAssetOptions())
const optionLabel = (items: NailAssetOption[], value: string) => items.find(item => item.value === value)?.label || value || '未分类'
const sourceLabel = (value: string) => ({ UPLOAD: '人工上传', AI: 'AI 生成', PUBLIC_REFERENCE: '访客参考' } as Record<string,string>)[value] || value
const copyrightLabel = (value: string) => ({ ORIGINAL: '原创', AUTHORIZED: '已授权', AI_GENERATED: 'AI 生成' } as Record<string,string>)[value] || value
const auditLabel = (value: string) => ({ UPLOAD: '上传入库', UPDATE: '信息更新', DOWNLOAD: '原图下载', SOFT_DELETE: '移入回收', ADOPT_AI_RESULT: '采纳 AI 结果', UPLOAD_FAILED: '处理失败' } as Record<string,string>)[value] || value
const formatSize = (bytes: number) => bytes ? bytes >= 1024 * 1024 ? `${(bytes / 1024 / 1024).toFixed(1)} MB` : `${Math.round(bytes / 1024)} KB` : '未知大小'
const formatMeta = (asset: NailAsset) => `${asset.width || '—'} × ${asset.height || '—'} · ${formatSize(asset.fileSize)}`
const imageRatio = (asset: NailAsset) => ({ aspectRatio: asset.width && asset.height ? `${asset.width}/${asset.height}` : '1/1' })

onMounted(async () => { await loadOptions(); await fetchAssets() })
</script>

<style lang="scss" scoped>
.asset-library{min-height:calc(100vh - 94px);color:#272823}.library-head{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;padding:8px 2px 16px}.title-block>span,.drawer-title>span{color:#a4545c;font-size:9px;letter-spacing:.2em}.title-block h1{margin:4px 0 3px;font-family:'Noto Serif SC','Songti SC',serif;font-size:28px;font-weight:580}.title-block p{margin:0;color:#6b6f69;font-size:12px}.head-actions{display:flex;align-items:center;gap:16px}.library-stat{display:grid;text-align:right}.library-stat b{font-family:Georgia,serif;font-size:20px;font-weight:500}.library-stat span{color:#858983;font-size:9px}
.library-shell{display:grid;grid-template-columns:250px minmax(0,1fr);min-height:calc(100vh - 200px);border:1px solid #dedfd9;background:#fff}.filter-panel{padding:14px 16px;border-right:1px solid #dedfd9;background:#f7f6f2}.filter-panel>header{display:flex;align-items:center;justify-content:space-between;margin-bottom:10px}.filter-panel>header div{display:grid}.filter-panel>header span{color:#a4545c;font-size:8px;letter-spacing:.16em}.filter-panel>header b{font-family:'Noto Serif SC','Songti SC',serif;font-size:17px}.filter-panel>header button{border:0;background:transparent;color:#8c5b62;font-size:10px}.filter-panel :deep(.el-input__wrapper),.filter-panel :deep(.el-select .el-input__wrapper){border-radius:5px;box-shadow:0 0 0 1px #dcddd7 inset}.filter-section{display:grid;gap:5px;margin-top:12px}.filter-section>label{color:#60635e;font-size:10px;font-weight:620}.filter-section.split{grid-template-columns:1fr 1fr}.filter-section.split>label{grid-column:auto}.choice-grid{display:grid;grid-template-columns:1fr 1fr;gap:5px}.choice-grid button,.tag-cloud button{padding:9px 8px;border:1px solid #dedfd9;border-radius:4px;background:#fff;color:#60635e;font-size:11px;text-align:left}.choice-grid button.active,.tag-cloud button.active{border-color:#b56872;background:#f7e9eb;color:#8f3843}.color-choices{display:grid;grid-template-columns:repeat(4,1fr);gap:6px}.color-choices button{display:grid;place-items:center;gap:3px;padding:7px 4px;border:1px solid transparent;border-radius:5px;background:transparent;color:#686b65;font-size:10px}.color-choices button.active{border-color:#b56872;background:#fff}.color-choices i{display:block;width:21px;height:13px;border:1px solid rgba(40,40,35,.12);border-radius:3px}.color-pink{background:#d59ba7}.color-red{background:#a84849}.color-nude{background:#c9aa94}.color-white{background:#fff}.color-black{background:#30312f}.color-blue{background:#7597a9}.color-purple{background:#887ba0}.color-green{background:#7e9a86}.color-yellow{background:#d3b85f}.color-metallic{background:linear-gradient(90deg,#a8aaac,#d7c48e)}.color-neutral{background:#aaa9a1}.color-multicolor{background:linear-gradient(90deg,#bc7d8e,#d1b564,#719a9c,#847aa1)}.tag-cloud{display:flex;flex-wrap:wrap;gap:5px}.tag-cloud button{padding:5px 6px}.ai-state{display:grid;grid-template-columns:repeat(3,1fr);padding:3px;border-radius:6px;background:#e9e9e5}.ai-state button{height:32px;border:0;border-radius:4px;background:transparent;color:#6b6e68;font-size:11px}.ai-state button.active{background:#fff;color:#8f3843;box-shadow:0 2px 7px rgba(40,37,33,.07)}.filter-submit{width:100%;margin-top:20px}.filter-submit span{display:inline-grid;min-width:17px;height:17px;margin-left:5px;place-items:center;border-radius:50%;background:rgba(255,255,255,.2);font-size:8px}.filter-panel :deep(.el-date-editor){width:100%;padding:0 7px}
.asset-content{min-width:0;padding:12px 18px 16px;background:#fbfbf9;display:flex;flex-direction:column;flex:1}.content-toolbar{display:flex;align-items:center;justify-content:space-between;gap:16px;padding-bottom:10px;border-bottom:1px solid #e4e5df;flex-shrink:0}.content-toolbar>div:first-child{display:grid;gap:2px}.content-toolbar b{font-size:12px}.content-toolbar span{color:#858983;font-size:9px}.toolbar-actions{display:flex;align-items:center;gap:7px}.toolbar-actions :deep(.el-select){width:118px}.toolbar-actions>button{height:32px;padding:0 10px;border:1px solid #dfe0da;border-radius:5px;background:#fff;color:#60635e;font-size:10px}.toolbar-actions>button.active{border-color:#ae626b;color:#943e47}.batch-bar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:12px;padding:10px 12px;border:1px solid #d7b1b6;background:#fbf2f3;flex-shrink:0}.batch-bar>div{display:flex;align-items:center;gap:10px}.batch-bar>div:first-child{display:grid;gap:2px}.batch-bar b{font-size:11px}.batch-bar span{color:#7c6568;font-size:9px}.batch-bar button{padding:6px 14px;border:1px solid #d7b1b6;border-radius:4px;background:#fff;color:#90444d;font-size:11px}.asset-waterfall{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:10px;align-items:start;flex:1;margin-top:12px}.asset-waterfall.empty{display:flex;place-items:center;justify-content:center}.asset-card{min-width:0;margin:0;overflow:hidden;border:1px solid #dfe0da;background:#fff;cursor:pointer;transition:border-color .18s ease,transform .18s ease,box-shadow .18s ease}.asset-card:hover{border-color:#c99da3;transform:translateY(-2px);box-shadow:0 8px 24px rgba(45,42,37,.1)}.asset-card:active{transform:translateY(-1px);box-shadow:0 4px 12px rgba(45,42,37,.08)}.asset-card.selected{border-color:#a94e58;box-shadow:0 0 0 2px rgba(169,78,88,.15)}.asset-image{position:relative;aspect-ratio:1/1;overflow:hidden;background:#ecece7}.asset-image>img{display:block;width:100%;height:100%;object-fit:cover}.source-pill{position:absolute;top:6px;left:6px;padding:3px 5px;border-radius:3px;background:rgba(31,32,29,.76);color:#fff;font-size:7px;backdrop-filter:blur(8px)}.select-control{position:absolute;top:6px;right:6px;display:grid;width:22px;height:22px;place-items:center;border:1px solid rgba(255,255,255,.85);border-radius:50%;background:rgba(255,255,255,.92);cursor:pointer}.select-control i{display:grid;width:14px;height:14px;place-items:center;border-radius:50%;background:#fff;color:#a94e58;font-size:9px}.select-control i.checked{background:#a94e58;color:#fff}.image-actions{position:absolute;bottom:0;left:0;right:0;display:flex;align-items:center;justify-content:space-between;gap:6px;padding:6px 8px;background:linear-gradient(0deg,rgba(0,0,0,.55) 0%,transparent 100%)}.image-actions button,.image-actions a{display:flex;align-items:center;gap:4px;padding:4px 7px;border:0;border-radius:4px;background:rgba(255,255,255,.88);color:#4d525a;font-size:9px;text-decoration:none;cursor:pointer}.image-actions button:hover,.image-actions a:hover{background:#fff;color:#202328}.asset-copy{padding:8px 10px 10px}.asset-copy>header{display:flex;align-items:flex-start;justify-content:space-between;gap:6px}.asset-copy h2{margin:0;overflow:hidden;font-size:11px;font-weight:600;line-height:1.4;text-overflow:ellipsis;white-space:nowrap;max-width:calc(100% - 22px)}.asset-copy header p{margin:2px 0 0;color:#9ca1aa;font-size:9px}.asset-copy header button{flex:none;display:grid;width:20px;height:20px;place-items:center;border:0;border-radius:4px;background:transparent;color:#b0b5bd;cursor:pointer}.asset-copy header button:hover{background:#f0f1f3;color:#6d727b}.taxonomy{display:flex;flex-wrap:wrap;gap:4px;margin-top:6px}.taxonomy>span{padding:2px 5px;border-radius:3px;background:#f4f5f7;color:#7d838c;font-size:8px}.asset-tags{display:flex;flex-wrap:wrap;gap:3px;margin-top:5px}.asset-tags>span{color:#9a6d77;font-size:8px}.asset-copy footer{display:flex;align-items:center;justify-content:space-between;gap:4px;margin-top:4px;padding-top:4px;border-top:1px solid #f0f1f3}.asset-copy footer>span:first-child{padding:2px 5px;border-radius:3px;font-size:7px}.asset-copy footer .copyright-original{background:#eaf3ed;color:#3e7458}.asset-copy footer .copyright-authorized{background:#eef3fb;color:#3e6a9e}.asset-copy footer .copyright-ai_generated{background:#f3eefb;color:#6b4fa0}.asset-copy footer>span:nth-child(2){display:flex;align-items:center;gap:3px;color:#7d838c;font-size:8px}.asset-copy footer>span:nth-child(2) i{width:5px;height:5px;border-radius:50%;background:#3e7458}.asset-copy footer>span:nth-child(2).disabled i{background:#c0c4cc}.asset-copy footer time{color:#b0b5bd;font-size:8px}.asset-failed{display:grid;place-items:center;gap:6px;padding:40px 16px;color:#c0c4cc;text-align:center}.asset-failed icon{font-size:22px}.asset-failed b{font-size:11px;color:#909399}.asset-failed span{font-size:9px;color:#c0c4cc}.select-control{position:absolute;top:6px;right:6px;display:grid;width:28px;height:28px;place-items:center;border:0;border-radius:50%;background:rgba(255,255,255,.9)}.select-control i{display:grid;width:12px;height:12px;place-items:center;border:1px solid #aaa9a3;border-radius:4px;color:#fff;font-size:9px}.select-control i.checked{border-color:#a74651;background:#a74651}.image-actions{position:absolute;right:8px;bottom:8px;left:8px;display:flex;justify-content:space-between;opacity:0;transform:translateY(5px);transition:.18s ease}.asset-card:hover .image-actions{opacity:1;transform:none}.image-actions button,.image-actions a{display:flex;align-items:center;gap:5px;height:29px;padding:0 8px;border:0;border-radius:4px;background:rgba(255,255,255,.9);color:#353632;font-size:9px;text-decoration:none}.asset-failed{display:grid;min-height:190px;place-content:center;place-items:center;padding:20px;background:#f6efef;color:#944650;text-align:center}.asset-failed svg{font-size:24px}.asset-failed b{margin-top:9px;font-size:11px}.asset-failed span{max-width:24ch;margin-top:5px;color:#80686b;font-size:9px;line-height:1.5}.asset-copy{padding:10px}.asset-copy>header{display:grid;grid-template-columns:minmax(0,1fr) 24px;gap:6px}.asset-copy h2{overflow:hidden;margin:0;font-size:11px;font-weight:650;text-overflow:ellipsis;white-space:nowrap}.asset-copy p{margin:2px 0 0;color:#7c807a;font-size:9px}.asset-copy>header button{display:grid;width:24px;height:24px;place-items:center;border:0;background:transparent;color:#73766f}.taxonomy,.asset-tags{display:flex;flex-wrap:wrap;gap:4px;margin-top:9px}.taxonomy span{padding:2px 4px;background:#f0f0ec;color:#5f625d;font-size:8px}.asset-tags span{color:#9a5a62;font-size:8px}.asset-copy>footer{display:flex;align-items:center;gap:5px;margin-top:5px;padding-top:5px;border-top:1px solid #ecece7;font-size:8px}.asset-copy>footer>span{color:#397158}.asset-copy>footer>span:first-child{padding:3px 5px;background:#edf4ef}.asset-copy>footer>span.disabled{color:#8b8e87}.asset-copy>footer>span:nth-child(2){display:flex;align-items:center;gap:3px}.asset-copy>footer i{width:5px;height:5px;border-radius:50%;background:currentColor}.asset-copy time{margin-left:auto;color:#8b8e87}.asset-waterfall>:deep(.el-empty){width:auto}.load-more{display:grid;place-items:center;gap:6px;margin-top:18px}.load-more span{color:#8a8e87;font-size:8px}
.upload-tip{display:flex;flex-wrap:wrap;gap:6px;margin-top:8px}.upload-tip span{padding:4px 6px;border:1px solid #e0e1dc;border-radius:4px;color:#72766f;font-size:8px}.upload-metadata,.edit-form{display:grid;grid-template-columns:1fr 1fr;gap:0 12px;margin-top:18px}.upload-metadata :deep(.el-select),.edit-form :deep(.el-select){width:100%}.field-wide{grid-column:1/-1}.drawer-title h2{margin:4px 0 0;font-family:'Noto Serif SC','Songti SC',serif;font-size:20px}.asset-detail{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(260px,.85fr);gap:20px}.detail-visual{position:sticky;top:0;align-self:start}.detail-visual>img{display:block;width:100%;max-height:74vh;object-fit:contain;background:#f0f0ec}.detail-visual-actions{display:flex;align-items:center;gap:8px;margin-top:8px}.detail-visual-actions a,.detail-visual-actions button{display:flex;align-items:center;justify-content:center;gap:6px;flex:1;height:36px;border:1px solid #dedfd9;background:#fff;color:#565954;font-size:10px;text-decoration:none}.download-locked{display:flex;align-items:center;height:36px;padding:0 12px;border:1px solid #e2e0da;background:#f4f3ef;color:#777a74;font-size:9px}.detail-metadata{display:grid;align-content:start;gap:20px}.detail-metadata section{padding-bottom:18px;border-bottom:1px solid #e2e3dd}.detail-metadata h3{margin:0 0 11px;font-size:11px}.detail-metadata dl{display:grid;grid-template-columns:1fr 1fr;gap:11px;margin:0}.detail-metadata dl div{display:grid;gap:3px}.detail-metadata dt{color:#8a8d87;font-size:8px}.detail-metadata dd{margin:0;font-size:10px}.detail-rights,.detail-tags{display:flex;flex-wrap:wrap;gap:6px}.detail-rights span,.detail-tags span{padding:5px 7px;background:#eaf3ed;color:#356f53;font-size:9px}.detail-rights span.off{background:#f0f0ed;color:#777a74}.detail-metadata section>p{color:#936069;font-size:9px}.audit-list{display:grid;gap:10px}.audit-list>div{display:grid;grid-template-columns:8px minmax(0,1fr) auto;align-items:start;gap:8px}.audit-list i{width:7px;height:7px;margin-top:4px;border-radius:50%;background:#a6505a}.audit-list span{display:grid;gap:2px}.audit-list b{font-size:9px}.audit-list small,.audit-list time{color:#838780;font-size:8px}.audit-list time{white-space:nowrap}
button:focus-visible,a:focus-visible{outline:2px solid #b65d68;outline-offset:2px}@media(max-width:1280px){.asset-waterfall{grid-template-columns:repeat(4,minmax(0,1fr))}}@media(max-width:960px){.library-shell{grid-template-columns:1fr}.filter-panel{border-right:0;border-bottom:1px solid #dedfd9}.filter-section{margin-top:12px}.asset-waterfall{grid-template-columns:repeat(3,minmax(0,1fr))}.asset-detail{grid-template-columns:1fr}}@media(max-width:620px){.library-head,.content-toolbar,.batch-bar{align-items:flex-start;flex-direction:column}.library-shell{border-right:0;border-left:0}.asset-content{padding:12px}.asset-waterfall{grid-template-columns:repeat(2,minmax(0,1fr))}.head-actions{width:100%;justify-content:space-between}.toolbar-actions{width:100%;flex-wrap:wrap}.upload-metadata,.edit-form{grid-template-columns:1fr}.asset-detail{display:block}.detail-metadata{margin-top:18px}}@media(prefers-reduced-motion:reduce){.asset-card,.image-actions{transition:none}}
.pagination-bar{display:flex;justify-content:flex-end;padding:12px 0 6px}
.pagination-bar :deep(.el-pagination){font-size:12px}
.pagination-bar :deep(.el-pagination .el-pager li){border-radius:6px}
.pagination-bar :deep(.el-pagination .el-pager li.is-active){background:#a66d78;color:#fff}
.image-preview-dialog :deep(.el-dialog){background:transparent;box-shadow:none;max-width:95vw;max-height:95vh}
.image-preview-dialog :deep(.el-dialog__body){padding:0}
.image-preview-dialog :deep(.el-dialog__header){display:none}
.preview-image{display:block;max-width:95vw;max-height:95vh;object-fit:contain}
</style>
