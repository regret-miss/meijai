<template>
    <div class="nail-studio-page">
        <header class="studio-titlebar">
            <div>
                <span>AI NAIL DESIGN STUDIO</span>
                <h1>让灵感成为一套可落地的美甲设计</h1>
                <p>围绕甲型、材质、配色与陈列方式生成专业提案，每次创作都会形成可追踪的设计档案。</p>
            </div>
            <router-link class="library-link" to="/nail/assets">
                <icon name="el-icon-FolderOpened" />
                进入资产库
            </router-link>
        </header>

        <div class="studio-shell">
            <TaskRail
                :tasks="tasks"
                :loading="taskLoading"
                @refresh="loadTasks"
                @create="resetComposer"
                @select="openTask"
            />

            <main class="creation-canvas">
                <section class="canvas-intro">
                    <div class="edition-mark"><span>NAIL</span><b>ATELIER</b><small>2026</small></div>
                    <div>
                        <p>从一个清晰的设计意图开始</p>
                        <h2>今天想做什么样的指尖作品？</h2>
                    </div>
                </section>

                <section class="mode-row" aria-label="作品呈现方式">
                    <button
                        v-for="mode in creativeModes"
                        :key="mode.value"
                        type="button"
                        :class="{ active: form.creativeMode === mode.value }"
                        @click="form.creativeMode = mode.value"
                    >
                        <span><icon :name="mode.icon" /></span>
                        <span><b>{{ mode.label }}</b><small>{{ mode.note }}</small></span>
                    </button>
                </section>

                <section class="brief-card">
                    <div class="brief-label">
                        <span>DESIGN BRIEF</span>
                        <small>{{ form.prompt.length }} / 1000</small>
                    </div>
                    <textarea
                        v-model="form.prompt"
                        maxlength="1000"
                        aria-label="美甲设计需求"
                        placeholder="描述场景、气质和想保留的细节。例如：适合初秋约会的克制高级款，烟粉猫眼为主，重点甲加入极细金边，不要堆钻。"
                    ></textarea>

                    <button v-if="!reference" class="reference-entry" type="button" @click="assetDialog = true">
                        <span><icon name="el-icon-Plus" /></span>
                        <span><b>加入参考资产</b><small>用于保留配色、材质或构图语言</small></span>
                        <icon name="el-icon-ArrowRight" />
                    </button>
                    <div v-else class="reference-picked">
                        <img :src="reference.url" :alt="reference.name" />
                        <span><b>{{ reference.name }}</b><small>已授权参考 · {{ reference.width }} × {{ reference.height }}</small></span>
                        <el-button link @click="assetDialog = true">更换</el-button>
                        <el-button link type="danger" @click="removeReference">移除</el-button>
                    </div>
                    <p class="generation-mode-note">同一个创作入口：直接描述即为文生图，加入参考资产后自动进入图生图。</p>
                </section>

                <section class="trend-section">
                    <div class="section-heading">
                        <div><span>CURATED DIRECTIONS</span><h3>当季设计方向</h3></div>
                        <small>选择后仍可继续调整细节</small>
                    </div>
                    <div class="trend-grid">
                        <button
                            type="button"
                            :class="{ active: form.trendPreset === 'CUSTOM' }"
                            @click="form.trendPreset = 'CUSTOM'"
                        >
                            <span class="swatches"><i style="background:#e0e0e0"></i><i style="background:#f0f0f0"></i><i style="background:#ffffff"></i></span>
                            <b>自由发挥</b>
                            <small>不选趋势，自由描述</small>
                        </button>
                        <button
                            v-for="trend in trends"
                            :key="trend.value"
                            type="button"
                            :class="{ active: form.trendPreset === trend.value }"
                            @click="applyTrend(trend)"
                        >
                            <span class="swatches"><i v-for="color in trend.colors" :key="color" :style="{ background: color }"></i></span>
                            <b>{{ trend.label }}</b>
                            <small>{{ trend.note }}</small>
                        </button>
                    </div>
                </section>

                <section class="style-reference-section" v-if="styleReferences.length">
                    <div class="section-heading">
                        <div><span>STYLE REFERENCES</span><h3>风格母版</h3></div>
                        <small>选择风格母版锚定质感、光影与构图</small>
                    </div>
                    <div class="style-reference-grid">
                        <button
                            type="button"
                            :class="{ active: !form.styleReferenceId }"
                            @click="form.styleReferenceId = undefined"
                        >
                            <span class="style-thumb empty"><icon name="el-icon-Close" /></span>
                            <b>不使用</b>
                        </button>
                        <button
                            v-for="ref in styleReferences"
                            :key="ref.id"
                            type="button"
                            :class="{ active: form.styleReferenceId === ref.id }"
                            @click="form.styleReferenceId = ref.id"
                        >
                            <img class="style-thumb" :src="ref.thumbUrl" :alt="ref.name" loading="lazy" />
                            <b>{{ ref.name }}</b>
                            <small>{{ ref.category }}</small>
                        </button>
                    </div>
                </section>

                <footer class="composer-actions">
                    <button class="settings-button" type="button" @click="settingsOpen = true">
                        <icon name="el-icon-Setting" />
                        <span><b>专业参数</b><small>{{ selectedSummary }}</small></span>
                    </button>
                    <button class="generate-button" type="button" :disabled="!canSubmit || submitting" @click="submit">
                        <span><small>{{ form.outputCount }} 张 · {{ form.resolution }}</small><b>{{ submitting ? '正在建立设计档案' : '生成设计提案' }}</b></span>
                        <icon name="el-icon-Right" />
                    </button>
                </footer>
            </main>
        </div>

        <el-dialog
            v-model="settingsOpen"
            width="720px"
            align-center
            append-to-body
            destroy-on-close
            :show-close="false"
        >
            <template #header="{ close }">
                <div class="settings-dialog-head">
                    <div>
                        <b>专业设计参数</b>
                        <span>调整造型、材质与输出规格</span>
                    </div>
                    <button type="button" aria-label="关闭专业设计参数" @click="close"><icon name="el-icon-Close" /></button>
                </div>
            </template>

            <div class="settings-sheet">
                <div class="settings-engine">
                    <span><icon name="el-icon-MagicStick" /></span>
                    <div><b>Seedream 专业美甲工作流</b><small>商业设计 · 真实材质 · 高质感成片</small></div>
                    <em>同源工作流</em>
                </div>

                <section class="settings-section model-section">
                    <div class="settings-group wide">
                        <label>模型选择</label>
                        <el-select v-model="form.model" placeholder="选择生成模型">
                            <el-option label="Seedream 5.0 Pro（推荐）" value="ep-20260814004823-6kd24" />
                            <el-option label="Seedream 5.0 Lite" value="ep-20260814005153-f6fcg" />
                            <el-option label="Seedream 4.5" value="ep-20260814005244-8dwk6" />
                            <el-option label="Seedream 4.0" value="ep-20260814005309-bnl9h" />
                        </el-select>
                    </div>
                </section>

                <section class="settings-section">
                    <div class="settings-group">
                        <label>甲型</label>
                        <el-radio-group v-model="form.nailShape">
                            <el-radio-button v-for="item in nailShapes" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
                        </el-radio-group>
                    </div>
                    <div class="settings-group">
                        <label>材质与工艺</label>
                        <el-radio-group v-model="form.finish">
                            <el-radio-button v-for="item in finishes" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
                        </el-radio-group>
                    </div>
                </section>

                <section class="settings-form-grid">
                    <div><label>设计气质</label><el-select v-model="form.designStyle"><el-option v-for="item in designStyles" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
                    <div><label>甲面布局</label><el-select v-model="form.layoutStyle"><el-option v-for="item in layouts" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
                    <div class="wide"><label>配色描述</label><el-input v-model="form.colorPalette" maxlength="120" placeholder="例如：烟粉、冷银、奶白" /></div>
                    <div v-if="reference" class="wide"><label>参考图使用方式</label><el-select v-model="form.referenceStrategy"><el-option v-for="item in referenceStrategies" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
                </section>

                <section class="output-settings">
                    <div class="output-setting ratio-setting">
                        <label>画面比例</label>
                        <el-radio-group v-model="form.aspectRatio">
                            <el-radio-button v-for="item in ratios" :key="item" :label="item"><i :style="ratioStyle(item)"></i><span>{{ item }}</span></el-radio-button>
                        </el-radio-group>
                    </div>
                    <div class="output-setting">
                        <label>清晰度</label>
                        <el-radio-group v-model="form.resolution"><el-radio-button label="2K">高清 2K</el-radio-button></el-radio-group>
                    </div>
                    <div class="output-setting output-count-setting">
                        <label>方案数量</label>
                        <el-radio-group v-model="form.outputCount"><el-radio-button v-for="count in 4" :key="count" :label="count">{{ count }}</el-radio-button></el-radio-group>
                    </div>
                </section>
            </div>

            <template #footer>
                <div class="settings-dialog-foot">
                    <span>{{ selectedSummary }} · {{ form.resolution }} · {{ form.outputCount }} 张</span>
                    <div><el-button @click="settingsOpen = false">关闭</el-button><el-button type="primary" @click="settingsOpen = false">应用参数</el-button></div>
                </div>
            </template>
        </el-dialog>

        <el-dialog v-model="assetDialog" title="选择参考资产" width="780px">
            <div class="inline-reference-upload">
                <span><icon name="el-icon-UploadFilled" /></span>
                <div><b>直接上传参考图</b><small>PNG 或 JPG，10MB 以内。上传即确认图片为原创或已获得使用授权。</small></div>
                <el-upload :auto-upload="false" :show-file-list="false" accept="image/png,image/jpeg" :on-change="uploadReference">
                    <el-button :loading="referenceUploading">选择图片</el-button>
                </el-upload>
            </div>
            <div v-loading="assetLoading" class="asset-picker">
                <button v-for="asset in assets" :key="asset.id" type="button" :class="{ active: reference?.id === asset.id }" @click="selectReference(asset)">
                    <img :src="asset.url" :alt="asset.name" loading="lazy" />
                    <span><b>{{ asset.name }}</b><small>{{ copyrightLabel(asset.copyrightStatus) }}</small></span>
                </button>
                <el-empty v-if="!assetLoading && !assets.length" description="资产库为空，请先上传有权使用的图片" />
            </div>
            <template #footer><router-link to="/nail/assets"><el-button>管理资产</el-button></router-link><el-button type="primary" :disabled="!reference" @click="assetDialog = false">确认参考</el-button></template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup name="nailAi">
import { computed, onMounted, reactive, ref } from 'vue'
import type { UploadFile } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import TaskRail from './components/TaskRail.vue'
import {
    nailAssetList,
    nailAssetUpload,
    nailStyleReferencePublicList,
    nailTaskCreate,
    nailTaskList,
    type NailAsset,
    type NailGeneratePayload,
    type NailTaskSummary
} from '@/api/nail'
import feedback from '@/utils/feedback'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const tasks = ref<NailTaskSummary[]>([])
const assets = ref<NailAsset[]>([])
const styleReferences = ref<any[]>([])
const reference = ref<NailAsset>()
const taskLoading = ref(false)
const assetLoading = ref(false)
const submitting = ref(false)
const settingsOpen = ref(false)
const assetDialog = ref(false)
const referenceUploading = ref(false)

const defaults: NailGeneratePayload = {
    taskType: 'TEXT_TO_IMAGE', prompt: '', creativeMode: 'ON_HAND', nailShape: 'SHORT_ALMOND',
    finish: 'VELVET_CAT_EYE', designStyle: 'QUIET_LUXURY', layoutStyle: 'TWO_ACCENTS',
    trendPreset: 'CUSTOM', referenceStrategy: 'REINTERPRET', colorPalette: '烟粉、冷银、奶白',
    aspectRatio: '1:1', resolution: '2K', outputCount: 2,
    model: 'ep-20260814004823-6kd24', styleReferenceId: undefined
}
const form = reactive<NailGeneratePayload>({ ...defaults })

const creativeModes = [
    { value: 'ON_HAND' as const, label: '真人上手效果', note: '强调手型、肤色与真实佩戴质感', icon: 'el-icon-Avatar' },
    { value: 'DESIGN_BOARD' as const, label: '明档设计稿', note: '完整展示十枚甲片与工艺细节', icon: 'el-icon-Collection' }
]
const trends = [
    { value: 'ROSE_VELVET' as const, label: '玫瑰丝绒', note: '烟灰玫瑰 · 香槟细线', colors: ['#a96f79', '#dfcdd0', '#c4bfb9'], palette: '烟粉、冷银、奶白', finish: 'VELVET_CAT_EYE' as const },
    { value: 'SEA_GLASS' as const, label: '海盐玻璃', note: '雾蓝透色 · 冰玻璃光', colors: ['#86aeb1', '#dce6e5', '#c4d0d2'], palette: '雾蓝、海盐白、透明玻璃', finish: 'JELLY' as const },
    { value: 'BUTTER_MICRO_FRENCH' as const, label: '奶油微法式', note: '柔奶黄 · 极细法式边', colors: ['#ddc46f', '#f1eddf', '#d7c39e'], palette: '柔奶黄、象牙白、浅沙色', finish: 'MICRO_FRENCH' as const },
    { value: 'MIXED_METAL' as const, label: '混合金属', note: '冷银交错 · 镜面细节', colors: ['#b4b8ba', '#9d8858', '#303236'], palette: '冷银、钛灰、少量香槟金', finish: 'CHROME' as const },
    { value: 'AURORA_MAGNETIC' as const, label: '极光磁场', note: '冷紫偏光 · 深层磁吸', colors: ['#756c9f', '#839fb1', '#d6d2e1'], palette: '雾紫、冰蓝、珍珠白', finish: 'VELVET_CAT_EYE' as const },
    { value: 'JADE_CAT_EYE' as const, label: '翡翠猫眼', note: '翡翠绿 · 玻璃珠光', colors: ['#3f7d6a', '#a8d0c0', '#e6efe8'], palette: '翡翠绿、水晶透、奶白', finish: 'VELVET_CAT_EYE' as const },
    { value: 'MINT_FRENCH' as const, label: '薄荷法式', note: '薄荷绿 · 细法式边', colors: ['#9fd8c4', '#eaf6f0', '#c9d2d6'], palette: '薄荷绿、透白、银灰', finish: 'MICRO_FRENCH' as const },
    { value: 'LACE_NAILS' as const, label: '蕾丝美甲', note: '奶白蕾丝 · 浪漫细纹', colors: ['#f2ece4', '#e8c8c9', '#faf6f0'], palette: '奶白、裸粉、米白', finish: 'SCULPTED_GEL' as const },
    { value: 'REVERSE_FRENCH' as const, label: '半月法式', note: '反向法式 · 根部半月', colors: ['#c58b83', '#e5c9bf', '#f5efe8'], palette: '豆沙、裸色、奶白', finish: 'FRENCH_TIP' as const },
    { value: 'LEOPARD_PRINT' as const, label: '豹纹', note: '焦糖豹纹 · 克制野性', colors: ['#b07a4a', '#e8d5b7', '#5a3d2b'], palette: '焦糖、奶油、深棕', finish: 'GLOSSY_GEL' as const },
    { value: 'METALLIC_FRENCH' as const, label: '金属法式', note: '冷银 · 金属法式边', colors: ['#aeb2b5', '#8d7b8f', '#2c2e32'], palette: '冷银、灰紫、金属', finish: 'CHROME' as const },
    { value: 'MILKY_WHITE' as const, label: '白月光奶白', note: '奶白 · 高光珍珠', colors: ['#f5efe6', '#fbf8f3', '#e8dfd3'], palette: '奶白、珍珠白、乳白', finish: 'GLOSSY_GEL' as const },
    { value: 'SUNSET_OMBRE' as const, label: '落日渐变', note: '珊瑚到薰衣草 · 渐变', colors: ['#e89a7a', '#f2c6a8', '#b9a7c9'], palette: '珊瑚橘、蜜桃、薰衣草', finish: 'OMBRE' as const }
]
const nailShapes = [
    { value: 'SHORT_ALMOND', label: '短杏仁' }, { value: 'SHORT_SQUOVAL', label: '短方圆' },
    { value: 'ALMOND', label: '杏仁' }, { value: 'SQUARE', label: '方形' }, { value: 'COFFIN', label: '芭蕾' },
    { value: 'ROUND', label: '圆形' }, { value: 'STILETTO', label: '尖形' }, { value: 'LIPSTICK', label: '唇形' }
]
const finishes = [
    { value: 'VELVET_CAT_EYE', label: '丝绒猫眼' }, { value: 'JELLY', label: '果冻透色' },
    { value: 'CHROME', label: '镜面铬光' }, { value: 'MICRO_FRENCH', label: '微法式' },
    { value: 'AURA', label: '晕染光圈' }, { value: 'SCULPTED_GEL', label: '立体凝胶' }, { value: 'GLOSSY_GEL', label: '高亮凝胶' },
    { value: 'FRENCH_TIP', label: '经典法式' }, { value: 'MILK_BATH', label: '牛奶浴' },
    { value: 'OMBRE', label: '渐变' }, { value: 'GLITTER', label: '满钻闪粉' }, { value: 'PEARL', label: '珍珠' }
]
const designStyles = [
    { value: 'QUIET_LUXURY', label: '克制高级' }, { value: 'KOREAN_CLEAR', label: '韩系清透' },
    { value: 'RUNWAY', label: '秀场前卫' }, { value: 'FUTURISTIC', label: '未来机能' },
    { value: 'ROMANTIC', label: '细腻浪漫' }, { value: 'SWEET_COOL', label: '甜酷混搭' },
    { value: 'MINIMALIST', label: '极简主义' }, { value: 'Y2K', label: '千禧复古' },
    { value: 'COQUETTE', label: '甜心蝴蝶结' }, { value: 'OLD_MONEY', label: '老钱风' },
    { value: 'DOPAMINE', label: '多巴胺' }, { value: 'MORANDI', label: '莫兰迪' }
]
const layouts = [
    { value: 'UNIFIED', label: '十指统一' }, { value: 'TWO_ACCENTS', label: '两枚重点甲' },
    { value: 'MICRO_FRENCH_LAYOUT', label: '微法式节奏' }, { value: 'MISMATCHED', label: '错落混搭' }
]
const referenceStrategies = [
    { value: 'REINTERPRET', label: '提取语言重新设计' }, { value: 'KEEP_PALETTE', label: '保留配色' },
    { value: 'KEEP_LAYOUT', label: '保留布局' }, { value: 'KEEP_TEXTURE', label: '保留材质' }
]
const ratios = ['1:1', '16:9', '9:16', '4:3', '3:4', '3:2', '2:3', '21:9']
const ratioStyle = (ratio: string) => {
    const [width, height] = ratio.split(':').map(Number)
    const max = Math.max(width, height)
    return {
        '--ratio-width': `${Math.max(10, Math.round((width / max) * 22))}px`,
        '--ratio-height': `${Math.max(10, Math.round((height / max) * 22))}px`
    }
}

const canSubmit = computed(() => form.prompt.trim().length >= 2)
const selectedSummary = computed(() => {
    const shape = nailShapes.find((item) => item.value === form.nailShape)?.label
    const finish = finishes.find((item) => item.value === form.finish)?.label
    return `${shape} · ${finish} · ${form.aspectRatio}`
})

const applyTrend = (trend: typeof trends[number]) => {
    form.trendPreset = trend.value
    form.colorPalette = trend.palette
    form.finish = trend.finish
}
const loadTasks = async () => {
    taskLoading.value = true
    try { tasks.value = (await nailTaskList({ pageNo: 1, pageSize: 30 })).lists || [] } finally { taskLoading.value = false }
}
const loadAssets = async () => {
    assetLoading.value = true
    try { assets.value = (await nailAssetList({ pageNo: 1, pageSize: 60 })).lists || [] } finally { assetLoading.value = false }
}
const openTask = (id: number) => router.push({ path: '/nail/ai/detail', query: { id } })
const resetComposer = () => { Object.assign(form, defaults); delete form.referenceAssetId; reference.value = undefined; window.scrollTo({ top: 0, behavior: 'smooth' }) }
const selectReference = (asset: NailAsset) => { reference.value = asset; form.referenceAssetId = asset.id; form.taskType = 'IMAGE_TO_IMAGE' }
const removeReference = () => { reference.value = undefined; delete form.referenceAssetId; form.taskType = 'TEXT_TO_IMAGE' }
const uploadReference = async (upload: UploadFile) => {
    const file = upload.raw
    if (!file) return
    if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 10 * 1024 * 1024) {
        feedback.msgError('请选择 10MB 以内的 PNG 或 JPG 图片')
        return
    }
    referenceUploading.value = true
    try {
        const data = new FormData()
        data.append('file', file)
        data.append('name', file.name.replace(/\.[^.]+$/, ''))
        data.append('copyrightStatus', 'AUTHORIZED')
        const asset = await nailAssetUpload(data) as NailAsset
        assets.value = [asset, ...assets.value.filter((item) => item.id !== asset.id)]
        selectReference(asset)
        assetDialog.value = false
        feedback.msgSuccess('参考图已加入，生成时将自动使用图生图')
    } finally { referenceUploading.value = false }
}
const copyrightLabel = (value: string) => ({ ORIGINAL: '原创', AUTHORIZED: '已授权', AI_GENERATED: 'AI 生成' } as Record<string, string>)[value] || value
const submit = async () => {
    if (!canSubmit.value) return
    submitting.value = true
    try {
        const id = await nailTaskCreate({ ...form, prompt: form.prompt.trim() })
        feedback.msgSuccess('设计任务已创建')
        await router.push({ path: '/nail/ai/detail', query: { id } })
    } finally { submitting.value = false }
}

const restoreCreationIntent = async () => {
    const ticket = route.query.creation_ticket
    const prompt = route.query.prompt
    if (typeof prompt === 'string' && prompt.trim()) {
        form.prompt = prompt.trim()
        return
    }
    if (typeof ticket === 'string' && ticket) {
        try {
            const data = await request.get({ url: '/nail/creation-bridge/consume?ticket=' + encodeURIComponent(ticket) }, { withToken: false })
            if (data?.prompt) form.prompt = data.prompt
        } catch {}
    }
}

onMounted(() => { loadTasks(); loadAssets(); loadStyleReferences(); restoreCreationIntent() })
const loadStyleReferences = async () => {
    try { styleReferences.value = await nailStyleReferencePublicList() } catch {}
}
</script>

<style lang="scss" scoped>
.nail-studio-page { color: #202328; }
.studio-titlebar { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding: 8px 4px 22px; }
.studio-titlebar > div { max-width: 760px; }
.studio-titlebar span { color: #9a6d77; font-size: 10px; letter-spacing: .2em; }
.studio-titlebar h1 { margin: 7px 0 5px; color: #17191d; font-family: 'Noto Serif SC', 'Songti SC', serif; font-size: 28px; font-weight: 560; letter-spacing: -.02em; }
.studio-titlebar p { margin: 0; color: #7d838c; font-size: 13px; }
.library-link { display: flex; align-items: center; gap: 7px; padding: 10px 13px; border: 1px solid #d9dde3; border-radius: 10px; background: #fff; color: #4d525a; }
.studio-shell { display: grid; grid-template-columns: 270px minmax(0, 1fr); align-items: stretch; gap: 14px; }
.creation-canvas { align-self: start; min-width: 0; overflow: hidden; border: 1px solid #dfe2e7; border-radius: 16px; background: #fff; }
.canvas-intro { display: flex; align-items: center; gap: 24px; padding: 30px 34px; border-bottom: 1px solid #eaecf0; background: #f7f8fa; }
.edition-mark { display: grid; width: 94px; height: 94px; place-content: center; border: 1px solid #25282d; border-radius: 50%; color: #25282d; text-align: center; }
.edition-mark span { font-size: 9px; letter-spacing: .3em; text-indent: .3em; }
.edition-mark b { margin: 2px 0; font-family: Georgia, serif; font-size: 18px; font-weight: 500; }
.edition-mark small { color: #9b7079; font-size: 9px; }
.canvas-intro p { margin: 0 0 6px; color: #9b7079; font-size: 11px; letter-spacing: .08em; }
.canvas-intro h2 { margin: 0; color: #202328; font-family: 'Noto Serif SC', 'Songti SC', serif; font-size: 24px; font-weight: 540; }
.mode-row { display: grid; grid-template-columns: 1fr 1fr; border-bottom: 1px solid #eaecf0; }
.mode-row button { display: flex; align-items: center; gap: 12px; padding: 17px 24px; border: 0; border-right: 1px solid #eaecf0; background: #fff; color: #6d727b; text-align: left; }
.mode-row button:last-child { border-right: 0; }
.mode-row button > span:first-child { display: grid; width: 35px; height: 35px; place-items: center; border: 1px solid #d9dde3; border-radius: 50%; }
.mode-row button > span:last-child { display: grid; gap: 3px; }
.mode-row b { color: #34383e; font-size: 13px; }
.mode-row small { font-size: 10px; }
.mode-row button.active { box-shadow: inset 0 -2px #b47683; color: #8f5e69; }
.mode-row button.active > span:first-child { border-color: #c795a0; background: #f7edef; color: #9c6672; }
.brief-card { padding: 20px 34px 16px; border-bottom: 1px solid #eaecf0; }
.brief-label { display: flex; justify-content: space-between; margin-bottom: 8px; }
.brief-label span { color: #8e626c; font-size: 10px; letter-spacing: .14em; }
.brief-label small { color: #a1a5ad; font-size: 10px; }
.brief-card textarea { width: 100%; min-height: 72px; resize: vertical; border: 0; outline: 0; color: #282b30; font-family: inherit; font-size: 15px; line-height: 1.9; }
.brief-card textarea::placeholder { color: #b1b5bc; }
.reference-entry { display: grid; grid-template-columns: 38px minmax(0,1fr) auto; align-items: center; gap: 12px; width: 100%; margin-top: 10px; padding: 12px; border: 1px dashed #d3d7dd; border-radius: 11px; background: #f8f8f9; color: #60656d; text-align: left; }
.reference-entry > span:first-child { display: grid; width: 38px; height: 38px; place-items: center; border-radius: 9px; background: #fff; color: #9d6b76; }
.reference-entry > span:nth-child(2) { display: grid; gap: 2px; }
.reference-entry b { color: #33373d; font-size: 12px; }
.reference-entry small { color: #959aa3; font-size: 10px; }
.reference-picked { display: grid; grid-template-columns: 54px minmax(0,1fr) auto auto; align-items: center; gap: 10px; margin-top: 10px; padding: 8px; border: 1px solid #e0d3d6; border-radius: 11px; background: #faf6f7; }
.reference-picked img { width: 54px; height: 54px; border-radius: 8px; object-fit: cover; }
.reference-picked > span { display: grid; gap: 4px; min-width: 0; }
.reference-picked b { overflow: hidden; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.reference-picked small { color: #8d929a; font-size: 10px; }
.generation-mode-note { display: flex; align-items: center; gap: 8px; margin: 10px 2px 0; color: #777d82; font-size: 11px; line-height: 1.5; }
.generation-mode-note::before { width: 18px; height: 1px; flex: none; background: rgba(157, 104, 115, .6); content: ''; }
.inline-reference-upload { display: grid; grid-template-columns: 38px minmax(0,1fr) auto; align-items: center; gap: 12px; margin-bottom: 14px; padding: 13px; border: 1px solid #e0d4d6; background: #faf6f7; }
.inline-reference-upload > span { display: grid; width: 38px; height: 38px; place-items: center; background: #fff; color: #9d6873; }
.inline-reference-upload > div { display: grid; gap: 3px; }
.inline-reference-upload b { font-size: 13px; }
.inline-reference-upload small { color: #7d8288; font-size: 10px; }
.trend-section { padding: 18px 34px 20px; }
.section-heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 13px; }
.section-heading span { color: #9a6d77; font-size: 9px; letter-spacing: .16em; }
.section-heading h3 { margin: 3px 0 0; font-size: 15px; font-weight: 620; }
.section-heading small { color: #9ca1aa; font-size: 10px; }
.trend-grid { display: grid; grid-template-columns: repeat(5, minmax(0,1fr)); gap: 8px; }
.trend-grid button { min-width: 0; padding: 10px; border: 1px solid #e0e3e8; border-radius: 10px; background: #fff; text-align: left; }
.trend-grid button.active { border-color: #b97987; box-shadow: inset 0 0 0 1px #b97987; }
.swatches { display: flex; height: 7px; margin-bottom: 9px; overflow: hidden; border-radius: 3px; }
.swatches i { flex: 1; }
.trend-grid b, .trend-grid small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.trend-grid b { color: #34373d; font-size: 11px; }
.trend-grid small { margin-top: 3px; color: #979ca4; font-size: 9px; }
.style-reference-section { padding: 18px 34px 20px; border-top: 1px solid #eaecf0; }
.style-reference-grid { display: grid; grid-template-columns: repeat(5, minmax(0,1fr)); gap: 8px; }
.style-reference-grid button { min-width: 0; padding: 10px; border: 1px solid #e0e3e8; border-radius: 10px; background: #fff; text-align: left; }
.style-reference-grid button.active { border-color: #b97987; box-shadow: inset 0 0 0 1px #b97987; }
.style-thumb { display: block; width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 7px; margin-bottom: 9px; background: #f1f3f5; }
.style-thumb.empty { display: grid; place-items: center; color: #c0c4cc; font-size: 18px; }
.style-reference-grid b, .style-reference-grid small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.style-reference-grid b { color: #34373d; font-size: 11px; }
.style-reference-grid small { margin-top: 3px; color: #979ca4; font-size: 9px; }
.composer-actions { display: grid; grid-template-columns: minmax(0,1fr) 260px; gap: 10px; padding: 14px 18px; border-top: 1px solid #e2e5e9; background: #f7f8fa; }
.settings-button, .generate-button { display: flex; align-items: center; border-radius: 11px; text-align: left; }
.settings-button { gap: 11px; padding: 11px 14px; border: 1px solid #dfe2e7; background: #fff; color: #555b64; }
.settings-button span { display: grid; gap: 2px; }
.settings-button b { color: #30343a; font-size: 11px; }
.settings-button small { color: #9297a0; font-size: 10px; }
.generate-button { justify-content: space-between; padding: 11px 16px; border: 1px solid #24272c; background: #24272c; color: #fff; }
.generate-button:disabled { cursor: not-allowed; opacity: .45; }
.generate-button span { display: grid; gap: 1px; }
.generate-button small { color: #c6c9ce; font-size: 9px; }
.generate-button b { font-size: 13px; font-weight: 600; }
.model-section { padding: 14px; border: 1px solid #ece7e5; border-radius: 12px; background: #faf8f7; }
.model-section :deep(.el-select) { width: 100%; }
.model-section :deep(.el-input__wrapper) { min-height: 40px; border: 1px solid #e3dfdc; border-radius: 9px; background: #fff; box-shadow: none!important; }
.model-section :deep(.el-input__wrapper:hover) { border-color: #c9a5ad; }
.model-section :deep(.el-input__wrapper.is-focus) { border-color: #a66d78; box-shadow: 0 0 0 3px rgba(166,109,120,.08)!important; }
.settings-sheet { display: grid; gap: 20px; }
.settings-engine { display: grid; grid-template-columns: 44px minmax(0,1fr) auto; align-items: center; gap: 12px; padding: 12px 14px; border: 1px solid #ece7e5; border-radius: 12px; background: #f8f6f5; }
.settings-engine > span { display: grid; width: 44px; height: 44px; place-items: center; border: 1px solid #eadcdf; border-radius: 10px; background: #fff; color: #a66d78; font-size: 18px; }
.settings-engine > div { display: grid; gap: 3px; }.settings-engine b { color: #2c2928; font-size: 13px; }.settings-engine small { color: #8d8784; font-size: 10px; }
.settings-engine em { padding: 5px 8px; border-radius: 7px; background: #eaf3ee; color: #3e7458; font-size: 9px; font-style: normal; }
.settings-section { display: grid; gap: 17px; }.settings-group { display: grid; gap: 8px; }
.settings-group > label, .settings-form-grid label, .output-setting > label { color: #4d4947; font-size: 11px; font-weight: 620; }
.settings-sheet :deep(.el-radio-group) { display: flex; flex-wrap: wrap; gap: 7px; }
.settings-sheet :deep(.el-radio-button__inner) { min-height: 34px; padding: 8px 13px; border: 1px solid #e2dedc!important; border-radius: 8px!important; background: #fff; color: #5c5754; font-size: 11px; line-height: 16px; box-shadow: none!important; transition: border-color .18s ease, background-color .18s ease, color .18s ease, box-shadow .18s ease; }
.settings-sheet :deep(.el-radio-button__inner:hover) { border-color: #c9a5ad!important; color: #8f5965; }
.settings-sheet :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { border-color: #a66d78!important; background: #a66d78; color: #fff; box-shadow: 0 5px 14px rgba(118,72,83,.15)!important; }
.settings-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; padding: 18px 0; border-top: 1px solid #ebe7e4; border-bottom: 1px solid #ebe7e4; }
.settings-form-grid > div { display: grid; gap: 7px; }.settings-form-grid .wide { grid-column: 1 / -1; }
.settings-form-grid :deep(.el-select), .settings-form-grid :deep(.el-input) { width: 100%; }
.settings-form-grid :deep(.el-input__wrapper), .settings-form-grid :deep(.el-select .el-input__wrapper) { min-height: 40px; border: 1px solid #e3dfdc; border-radius: 9px; background: #fbfaf9; box-shadow: none!important; }
.settings-form-grid :deep(.el-input__wrapper:hover), .settings-form-grid :deep(.el-select .el-input__wrapper:hover) { border-color: #c9a5ad; }
.settings-form-grid :deep(.el-input__wrapper.is-focus), .settings-form-grid :deep(.el-select .el-input.is-focus .el-input__wrapper) { border-color: #a66d78; box-shadow: 0 0 0 3px rgba(166,109,120,.08)!important; }
.output-settings { display: grid; gap: 16px; }.output-setting { display: grid; gap: 8px; }
.output-setting :deep(.el-radio-group) { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 0; padding: 4px; border-radius: 11px; background: #f1efed; }
.output-setting :deep(.el-radio-button__inner) { width: 100%; min-height: 38px; display: grid; place-items: center; border: 0!important; background: transparent; color: #575350; }
.output-setting :deep(.el-radio-button__inner:hover) { background: rgba(255,255,255,.62); }
.output-setting :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) { background: #fff; color: #2b2928; box-shadow: 0 3px 12px rgba(42,35,32,.08)!important; }
.output-count-setting :deep(.el-radio-group) { grid-template-columns: repeat(4,minmax(0,1fr)); }
.ratio-setting :deep(.el-radio-group) { grid-template-columns: repeat(8,minmax(0,1fr)); }
.ratio-setting :deep(.el-radio-button__inner) { display: grid; min-width: 0; gap: 4px; padding: 6px 2px; }
.ratio-setting i { display: block; width: var(--ratio-width); height: var(--ratio-height); border: 1.5px solid currentColor; border-radius: 3px; }.ratio-setting span { font-size: 9px; }
.asset-picker { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; min-height: 220px; max-height: 520px; overflow-y: auto; }
.asset-picker button { overflow: hidden; padding: 0; border: 1px solid #e0e3e8; border-radius: 10px; background: #fff; text-align: left; }
.asset-picker button.active { border-color: #b97987; box-shadow: inset 0 0 0 1px #b97987; }
.asset-picker img { display: block; width: 100%; aspect-ratio: 1; object-fit: cover; }
.asset-picker button span { display: grid; gap: 3px; padding: 9px; }
.asset-picker b, .asset-picker small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.asset-picker b { font-size: 11px; }.asset-picker small { color: #9297a0; font-size: 9px; }
.asset-picker > :deep(.el-empty) { grid-column: 1 / -1; }
:global(.el-overlay:has(.settings-sheet)) { background: rgba(31, 29, 28, .42); backdrop-filter: blur(5px); }
:global(.el-dialog:has(.settings-sheet)) { --el-color-primary: #9f6874; overflow: hidden; margin: 0!important; border: 1px solid rgba(92, 78, 75, .16); border-radius: 16px!important; background: #fffefd; box-shadow: 0 30px 90px rgba(38, 31, 29, .22), 0 4px 18px rgba(38,31,29,.08); }
:global(.el-dialog:has(.settings-sheet) .el-dialog__header) { margin: 0; padding: 20px 22px 16px; border-bottom: 1px solid #ebe7e4; }
:global(.el-dialog:has(.settings-sheet) .el-dialog__body) { max-height: min(510px, calc(100vh - 220px)); overflow-y: auto; padding: 18px 22px; scrollbar-width: thin; scrollbar-color: rgba(166,109,120,.28) transparent; }
:global(.el-dialog:has(.settings-sheet) .el-dialog__footer) { padding: 13px 22px; border-top: 1px solid #ebe7e4; background: #faf8f7; }
.settings-dialog-head { display: flex; align-items: center; justify-content: space-between; gap: 18px; }
.settings-dialog-head > div { display: grid; gap: 4px; }.settings-dialog-head b { color: #282524; font-size: 18px; font-weight: 650; letter-spacing: -.02em; }.settings-dialog-head span { color: #8d8784; font-size: 11px; }
.settings-dialog-head > button { display: grid; width: 36px; height: 36px; place-items: center; border: 1px solid #e6e1de; border-radius: 10px; background: #faf8f7; color: #716b68; }
.settings-dialog-head > button:hover { border-color: #cdaab1; background: #fff; color: #95616c; }
.settings-dialog-foot { display: flex; align-items: center; justify-content: space-between; gap: 18px; }.settings-dialog-foot > span { overflow: hidden; color: #817b78; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.settings-dialog-foot > div { display: flex; gap: 8px; }.settings-dialog-foot :deep(.el-button) { min-width: 88px; height: 38px; margin: 0; border-radius: 9px; }.settings-dialog-foot :deep(.el-button--primary) { border-color: #332e2b; background: #332e2b; }.settings-dialog-foot :deep(.el-button--primary:hover) { border-color: #49413d; background: #49413d; }
button, a { transition: border-color .18s ease, background-color .18s ease, opacity .18s ease; }
button:focus-visible, a:focus-visible, textarea:focus-visible { outline: 2px solid #b97987; outline-offset: 2px; }
@media (max-width: 1280px) { .trend-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1100px) { .studio-shell { grid-template-columns: 1fr; }.trend-grid { grid-template-columns: repeat(5, minmax(140px,1fr)); overflow-x: auto; } }
@media (max-width: 760px) { .studio-titlebar { align-items: flex-start; flex-direction: column; }.canvas-intro { padding: 22px; }.edition-mark { display: none; }.mode-row { grid-template-columns: 1fr; }.mode-row button { border-right: 0; border-bottom: 1px solid #eaecf0; }.brief-card, .trend-section { padding: 22px; }.composer-actions { grid-template-columns: 1fr; }.asset-picker { grid-template-columns: repeat(2,1fr); }.settings-form-grid { grid-template-columns: 1fr; }.settings-form-grid .wide { grid-column: auto; }.ratio-setting :deep(.el-radio-group) { grid-template-columns: repeat(4,minmax(0,1fr)); }.settings-engine em { display: none; }.settings-dialog-foot { align-items: stretch; flex-direction: column; }.settings-dialog-foot > div { display: grid; grid-template-columns: 1fr 1fr; }.settings-dialog-foot :deep(.el-button) { width: 100%; } }
@media (max-width: 760px) { :global(.el-dialog:has(.settings-sheet)) { position: fixed; right: 8px; bottom: 8px; left: 8px; width: auto!important; border-radius: 16px; }.settings-dialog-head b { font-size: 16px; } }
@media (prefers-reduced-motion: reduce) { button, a { transition: none; } }

/* 美甲工作室操作台：与工作台共享同一套暖白、细线与玫瑰焦点语言。 */
.nail-studio-page { --studio-line: #e7e1dc; --studio-rose: #a66d78; --studio-muted: #8f8984; max-width: 1240px; margin: 0 auto; }
.studio-titlebar { padding: 20px 2px 18px; }
.studio-titlebar span { color: #aa858c; font-family: Georgia, serif; font-size: 9px; }
.studio-titlebar h1 { margin-top: 5px; font-family: inherit; font-size: 27px; font-weight: 640; letter-spacing: -.035em; }
.studio-titlebar p { color: var(--studio-muted); font-size: 12px; }
.library-link { border-color: var(--studio-line); border-radius: 1px; color: #726b67; }
.studio-shell { grid-template-columns: 258px minmax(0, 1fr); gap: 10px; }
.creation-canvas { border-color: var(--studio-line); border-radius: 1px; }
.canvas-intro { min-height: 116px; padding: 24px 30px; border-color: var(--studio-line); background: #faf7f5; }
.edition-mark { width: 72px; height: 72px; border-color: #bdaeb0; border-radius: 50%; }
.edition-mark b { font-size: 14px; }
.canvas-intro p { color: #aa7882; font-family: Georgia, serif; font-size: 9px; letter-spacing: .14em; }
.canvas-intro h2 { font-family: inherit; font-size: 20px; font-weight: 640; }
.mode-row { border-color: var(--studio-line); }
.mode-row button { min-height: 74px; border-color: var(--studio-line); }
.mode-row button > span:first-child { border-radius: 50%; }
.mode-row button.active { box-shadow: inset 0 -2px var(--studio-rose); }
.brief-card { border-color: var(--studio-line); }
.brief-label span, .section-heading span { color: #a66d78; font-family: Georgia, serif; }
.reference-entry, .reference-picked { border-radius: 1px; }
.trend-grid button { border-color: var(--studio-line); border-radius: 1px; }
.trend-grid button.active { border-color: var(--studio-rose); box-shadow: inset 0 0 0 1px var(--studio-rose); background: #fcf7f7; }
.swatches { border-radius: 0; }
.composer-actions { border-color: var(--studio-line); background: #f7f3f0; }
.settings-button, .generate-button { border-radius: 1px; }
.generate-button { border-color: #332e2b; background: #332e2b; }
.asset-picker button, .asset-picker img { border-radius: 1px; }
.nail-studio-page :deep(.el-drawer), .nail-studio-page :deep(.el-dialog) { --el-border-radius-small: 2px; --el-border-radius-base: 2px; }
@media (max-width: 1100px) { .studio-shell { grid-template-columns: 1fr; }.trend-grid { grid-template-columns: repeat(5, minmax(140px,1fr)); overflow-x: auto; } }
@media (max-width: 760px) { .studio-titlebar { align-items: flex-start; flex-direction: column; }.edition-mark { display: none; }.mode-row { grid-template-columns: 1fr; }.composer-actions { grid-template-columns: 1fr; } }
</style>
