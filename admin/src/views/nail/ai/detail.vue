<template>
    <div class="design-detail-page">
        <header class="detail-titlebar">
            <div>
                <span>AI NAIL DESIGN STUDIO</span>
                <h1>设计档案</h1>
                <p>查看生成结果、设计参数与后续采纳状态。</p>
            </div>
            <router-link class="library-link" to="/nail/assets"><icon name="el-icon-FolderOpened" />进入资产库</router-link>
        </header>
        <div class="detail-shell">
            <TaskRail
                :tasks="tasks"
                :loading="taskLoading"
                :active-id="task?.id"
                @refresh="loadTasks"
                @create="router.push('/nail/ai')"
                @select="openTask"
            />

            <main v-loading="detailLoading" class="detail-stage" :data-date="task ? formatTaskDate(task.createTime) : ''">
                <template v-if="task">
                    <header class="detail-header">
                        <div class="header-copy">
                            <button type="button" @click="router.push('/nail/ai')"><icon name="el-icon-ArrowLeft" /> 返回创作台</button>
                            <div class="title-line">
                                <div><span>DESIGN ARCHIVE · {{ task.id }}</span><h1>{{ task.title }}</h1></div>
                                <button class="rename-button" type="button" aria-label="重命名设计记录" @click="openRename"><icon name="el-icon-EditPen" /></button>
                            </div>
                            <div class="meta-row">
                                <span>{{ creativeModeLabel(task.creativeMode) }}</span>
                                <i></i><span>{{ task.aspectRatio }}</span><i></i><span>{{ task.resolution }}</span><i></i><span>{{ task.modelCode }}</span>
                            </div>
                        </div>
                        <span :class="['status-pill', statusClass(task.status)]">{{ statusLabel(task.status) }}</span>
                    </header>

                    <section class="prompt-panel">
                        <img
                            v-if="task.coverUrl || task.results[0]?.url"
                            class="task-preview"
                            :src="task.coverUrl || task.results[0]?.url"
                            :alt="`${task.title} 的生成预览`"
                            loading="eager"
                            @error="hideBrokenPreview"
                        />
                        <span v-else class="task-preview task-preview-placeholder" aria-hidden="true"><icon name="el-icon-MagicStick" /></span>
                        <div><span>CREATIVE BRIEF</span><small>{{ task.createTime }}</small></div>
                        <p>{{ task.prompt }}</p>
                        <div class="spec-tags">
                            <span>{{ specLabel('shape', task.designSpec.nailShape) }}</span>
                            <span>{{ specLabel('finish', task.designSpec.finish) }}</span>
                            <span>{{ specLabel('style', task.designSpec.designStyle) }}</span>
                            <span>{{ task.designSpec.colorPalette || '自定义配色' }}</span>
                        </div>
                    </section>

                    <section v-if="task.referenceAsset" class="reference-panel">
                        <img :src="task.referenceAsset.url" :alt="task.referenceAsset.name" />
                        <div><span>REFERENCE ASSET</span><b>{{ task.referenceAsset.name }}</b><small>{{ referenceStrategyLabel(task.designSpec.referenceStrategy) }} · {{ task.referenceAsset.copyrightStatus }}</small></div>
                        <router-link to="/nail/assets">查看资产</router-link>
                    </section>

                    <section class="result-area">
                        <header><div><span>GENERATED PROPOSALS</span><h2>本次设计提案</h2></div><small>{{ task.resultCount }} 张结果</small></header>

                        <div v-if="isPending(task.status)" class="generating-state">
                            <div class="loading-nails"><i v-for="item in 5" :key="item"></i></div>
                            <div><b>正在研色与模拟材质</b><p>生成通常需要几十秒，离开页面不会中断任务。</p></div>
                        </div>
                        <div v-else-if="task.results.length" :class="['result-grid', { single: task.results.length === 1 }]">
                            <article v-for="result in task.results" :key="result.id" class="result-card">
                                <el-image :src="result.url" :preview-src-list="task.results.map((item) => item.url)" fit="cover" preview-teleported />
                                <footer>
                                    <div><span>方案 {{ result.sort + 1 }}</span><small>{{ result.width }} × {{ result.height }} · {{ reviewLabel(result.reviewStatus) }}</small></div>
                                    <div class="result-actions">
                                        <el-button v-if="result.reviewStatus !== 'ADOPTED'" link @click="openReject(result)">驳回</el-button>
                                        <el-button v-if="result.reviewStatus !== 'ADOPTED'" type="primary" plain @click="adopt(result)">采纳到资产库</el-button>
                                        <router-link v-else to="/nail/assets"><el-button type="success" plain>已形成资产</el-button></router-link>
                                    </div>
                                </footer>
                                <p v-if="result.reviewNote" class="review-note">审阅：{{ result.reviewNote }}</p>
                            </article>
                        </div>
                        <div v-else class="failed-state">
                            <icon name="el-icon-Warning" />
                            <b>本次生成没有得到可用结果</b>
                            <p>{{ task.errorMessage || '请调整设计意图后重新生成。' }}</p>
                        </div>
                    </section>

                    <section class="iteration-composer">
                        <button type="button" aria-label="返回新创作" @click="router.push('/nail/ai')"><icon name="el-icon-Plus" /></button>
                        <textarea v-model="iterationPrompt" maxlength="1000" aria-label="继续修改当前方案" placeholder="继续修改，例如：保留配色，把重点甲改为极细法式边，减少金属元素。"></textarea>
                        <button class="iterate-button" type="button" :disabled="iterationPrompt.trim().length < 2 || submitting" @click="iterate">
                            <span>{{ submitting ? '创建中' : '再次生成' }}</span><icon name="el-icon-Top" />
                        </button>
                    </section>
                </template>

                <div v-else-if="!detailLoading" class="missing-state"><icon name="el-icon-DocumentDelete" /><h2>设计记录不存在</h2><el-button type="primary" @click="router.push('/nail/ai')">返回创作台</el-button></div>
            </main>
        </div>

        <el-dialog v-model="renameOpen" title="重命名设计记录" width="420px">
            <el-input v-model="renameTitle" maxlength="80" show-word-limit @keyup.enter="saveRename" />
            <template #footer><el-button @click="renameOpen = false">取消</el-button><el-button type="primary" :disabled="!renameTitle.trim()" @click="saveRename">保存</el-button></template>
        </el-dialog>
        <el-dialog v-model="rejectOpen" title="驳回生成结果" width="460px">
            <el-input v-model="rejectNote" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="填写未达到要求的原因，方便后续追踪。" />
            <template #footer><el-button @click="rejectOpen = false">取消</el-button><el-button type="danger" @click="confirmReject">确认驳回</el-button></template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup name="nailAiDetail">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TaskRail from './components/TaskRail.vue'
import {
    nailResultAdopt,
    nailResultReject,
    nailTaskCreate,
    nailTaskDetail,
    nailTaskList,
    nailTaskRename,
    type NailGeneratePayload,
    type NailResult,
    type NailTaskDetail,
    type NailTaskSummary
} from '@/api/nail'
import feedback from '@/utils/feedback'

const route = useRoute()
const router = useRouter()
const tasks = ref<NailTaskSummary[]>([])
const task = ref<NailTaskDetail>()
const taskLoading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const iterationPrompt = ref('')
const renameOpen = ref(false)
const renameTitle = ref('')
const rejectOpen = ref(false)
const rejectNote = ref('')
const rejectingResult = ref<NailResult>()
let pollTimer: number | undefined

const isPending = (status: string) => ['QUEUED', 'RUNNING'].includes(status)
const statusClass = (status: string) => status.toLowerCase().replace('_', '-')
const hideBrokenPreview = (event: Event) => {
    const image = event.target as HTMLImageElement
    image.style.display = 'none'
}
const statusLabel = (status: string) => ({ QUEUED: '等待生成', RUNNING: '正在生成', SUCCEEDED: '生成完成', PARTIAL_SUCCEEDED: '部分完成', FAILED: '生成失败' } as Record<string, string>)[status] || status
const reviewLabel = (status: string) => ({ PENDING: '待审阅', ADOPTED: '已采纳', REJECTED: '已驳回' } as Record<string, string>)[status] || status
const creativeModeLabel = (value: string) => value === 'DESIGN_BOARD' ? '明档设计稿' : '真人上手效果'
const formatTaskDate = (value: string) => {
    const parsed = new Date(value.replace(/-/g, '/'))
    return Number.isNaN(parsed.getTime()) ? '创作记录' : `${parsed.getMonth() + 1}月${parsed.getDate()}日`
}
const referenceStrategyLabel = (value: string) => ({ REINTERPRET: '提取设计语言', KEEP_PALETTE: '保留配色', KEEP_LAYOUT: '保留布局', KEEP_TEXTURE: '保留材质' } as Record<string, string>)[value] || value
const specLabel = (group: string, value: string) => {
    const labels: Record<string, Record<string, string>> = {
        shape: { SHORT_ALMOND: '短杏仁', SHORT_SQUOVAL: '短方圆', ALMOND: '杏仁', SQUARE: '方形', COFFIN: '芭蕾' },
        finish: { VELVET_CAT_EYE: '丝绒猫眼', JELLY: '果冻透色', CHROME: '镜面铬光', MICRO_FRENCH: '微法式', AURA: '晕染光圈', SCULPTED_GEL: '立体凝胶', GLOSSY_GEL: '高亮凝胶' },
        style: { QUIET_LUXURY: '克制高级', KOREAN_CLEAR: '韩系清透', RUNWAY: '秀场前卫', FUTURISTIC: '未来机能', ROMANTIC: '细腻浪漫', SWEET_COOL: '甜酷混搭' }
    }
    return labels[group]?.[value] || value
}
const loadTasks = async () => {
    taskLoading.value = true
    try { tasks.value = (await nailTaskList({ pageNo: 1, pageSize: 30 })).lists || [] } finally { taskLoading.value = false }
}
const loadDetail = async (silent = false) => {
    const id = Number(route.query.id)
    if (!id) { task.value = undefined; return }
    if (!silent) detailLoading.value = true
    try {
        task.value = await nailTaskDetail({ id })
        if (!iterationPrompt.value) iterationPrompt.value = task.value?.prompt || ''
        schedulePoll()
    } finally { detailLoading.value = false }
}
const schedulePoll = () => {
    if (pollTimer) window.clearInterval(pollTimer)
    if (task.value && isPending(task.value.status)) {
        pollTimer = window.setInterval(async () => {
            await loadDetail(true)
            if (task.value && !isPending(task.value.status)) { window.clearInterval(pollTimer); pollTimer = undefined; loadTasks() }
        }, 2500)
    }
}
const openTask = (id: number) => router.push({ path: '/nail/ai/detail', query: { id } })
const openRename = () => { if (!task.value) return; renameTitle.value = task.value.title; renameOpen.value = true }
const saveRename = async () => {
    if (!task.value || !renameTitle.value.trim()) return
    await nailTaskRename({ id: task.value.id, title: renameTitle.value.trim() })
    renameOpen.value = false
    feedback.msgSuccess('记录名称已更新')
    await Promise.all([loadDetail(true), loadTasks()])
}
const adopt = async (result: NailResult) => { await nailResultAdopt({ id: result.id }); feedback.msgSuccess('已采纳为正式资产'); await Promise.all([loadDetail(true), loadTasks()]) }
const openReject = (result: NailResult) => { rejectingResult.value = result; rejectNote.value = result.reviewNote || ''; rejectOpen.value = true }
const confirmReject = async () => {
    if (!rejectingResult.value) return
    await nailResultReject({ id: rejectingResult.value.id, note: rejectNote.value })
    rejectOpen.value = false
    feedback.msgSuccess('结果已驳回')
    await loadDetail(true)
}
const iterate = async () => {
    if (!task.value || iterationPrompt.value.trim().length < 2) return
    submitting.value = true
    const spec = task.value.designSpec
    const payload: NailGeneratePayload = {
        taskType: task.value.referenceAssetId ? 'IMAGE_TO_IMAGE' : 'TEXT_TO_IMAGE',
        prompt: iterationPrompt.value.trim(), creativeMode: spec.creativeMode, nailShape: spec.nailShape,
        finish: spec.finish, designStyle: spec.designStyle, layoutStyle: spec.layoutStyle,
        trendPreset: spec.trendPreset, referenceStrategy: spec.referenceStrategy, colorPalette: spec.colorPalette,
        aspectRatio: task.value.aspectRatio as NailGeneratePayload['aspectRatio'],
        resolution: task.value.resolution as NailGeneratePayload['resolution'], outputCount: task.value.outputCount,
        referenceAssetId: task.value.referenceAssetId
    }
    try {
        const id = await nailTaskCreate(payload)
        iterationPrompt.value = ''
        await router.push({ path: '/nail/ai/detail', query: { id } })
    } finally { submitting.value = false }
}

watch(() => route.query.id, () => { iterationPrompt.value = ''; loadDetail(); loadTasks() })
onMounted(() => { loadTasks(); loadDetail() })
onBeforeUnmount(() => { if (pollTimer) window.clearInterval(pollTimer) })
</script>

<style lang="scss" scoped>
.design-detail-page { min-height: calc(100vh - 94px); color: #24272c; }
.detail-shell { display: grid; grid-template-columns: 270px minmax(0,1fr); gap: 14px; }
.detail-stage { position: relative; min-width: 0; min-height: calc(100vh - 132px); padding-bottom: 108px; overflow: hidden; border: 1px solid #dfe2e7; border-radius: 16px; background: #f7f8fa; }
.detail-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; padding: 28px 34px 22px; border-bottom: 1px solid #e1e4e8; background: #fff; }
.header-copy > button { display: flex; align-items: center; gap: 5px; padding: 0; border: 0; background: none; color: #868b94; font-size: 11px; }
.title-line { display: flex; align-items: center; gap: 10px; margin-top: 18px; }
.title-line > div { min-width: 0; }
.title-line span { color: #a06d78; font-size: 9px; letter-spacing: .16em; }
.title-line h1 { margin: 4px 0 0; font-family: 'Noto Serif SC', 'Songti SC', serif; font-size: 25px; font-weight: 560; }
.rename-button { display: grid; width: 30px; height: 30px; place-items: center; border: 1px solid #dfe2e7; border-radius: 8px; background: #fff; color: #727780; }
.meta-row { display: flex; align-items: center; gap: 8px; margin-top: 10px; color: #8d929a; font-size: 10px; }
.meta-row i { width: 3px; height: 3px; border-radius: 50%; background: #b5b9c0; }
.status-pill { padding: 6px 10px; border-radius: 6px; background: #edf0f2; color: #66707a; font-size: 10px; }
.status-pill.succeeded { background: #e8f3ed; color: #397c5b; }.status-pill.running,.status-pill.queued,.status-pill.partial-succeeded { background: #f6eee0; color: #936d35; }.status-pill.failed { background: #f8e9ea; color: #a44f55; }
.prompt-panel { padding: 24px 34px; border-bottom: 1px solid #e1e4e8; background: #fff; }
.prompt-panel > div:first-child { display: flex; justify-content: space-between; }
.prompt-panel > div:first-child span { color: #9d6874; font-size: 9px; letter-spacing: .15em; }.prompt-panel > div:first-child small { color: #9da2aa; font-size: 10px; }
.prompt-panel p { max-width: 1100px; margin: 13px 0 16px; color: #34383e; font-size: 14px; line-height: 1.8; }
.spec-tags { display: flex; flex-wrap: wrap; gap: 6px; }.spec-tags span { padding: 5px 8px; border: 1px solid #e1e4e8; border-radius: 6px; background: #f8f8f9; color: #6f747d; font-size: 10px; }
.reference-panel { display: grid; grid-template-columns: 58px minmax(0,1fr) auto; align-items: center; gap: 12px; margin: 14px 34px 0; padding: 10px; border: 1px solid #e0d2d6; border-radius: 12px; background: #faf6f7; }
.reference-panel img { width: 58px; height: 58px; border-radius: 9px; object-fit: cover; }.reference-panel div { display: grid; min-width: 0; gap: 3px; }.reference-panel span { color: #8f5e69; font-size: 9px; letter-spacing: .12em; }.reference-panel b { overflow: hidden; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }.reference-panel small { color: #777d86; font-size: 10px; }.reference-panel a { color: #8f5e69; font-size: 11px; }
.result-area { padding: 26px 34px 34px; }.result-area > header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 14px; }.result-area > header span { color: #9d6874; font-size: 9px; letter-spacing: .15em; }.result-area h2 { margin: 4px 0 0; font-size: 17px; font-weight: 620; }.result-area > header small { color: #9ca1a9; font-size: 10px; }
.result-grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 14px; }.result-grid.single { grid-template-columns: minmax(0, 860px); justify-content: center; }
.result-card { overflow: hidden; border: 1px solid #dfe2e7; border-radius: 14px; background: #fff; }.result-card :deep(.el-image) { display: block; width: 100%; aspect-ratio: 1; background: #eceef1; }.result-card :deep(.el-image__inner) { width: 100%; height: 100%; }
.result-card footer { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 12px 14px; }.result-card footer > div:first-child { display: grid; gap: 3px; }.result-card footer span { font-size: 12px; font-weight: 600; }.result-card footer small { color: #969ba4; font-size: 9px; }.result-actions { display: flex; align-items: center; gap: 4px; }
.review-note { margin: 0; padding: 0 14px 12px; color: #8a6970; font-size: 10px; }
.generating-state { display: flex; align-items: center; justify-content: center; gap: 30px; min-height: 420px; border: 1px solid #e0e3e8; border-radius: 14px; background: #fff; }.generating-state b { font-size: 15px; }.generating-state p { margin: 6px 0 0; color: #8e939c; font-size: 11px; }
.loading-nails { display: flex; align-items: flex-end; gap: 5px; height: 58px; }.loading-nails i { width: 15px; height: 42px; border: 1px solid #c995a0; border-radius: 9px 9px 6px 6px; background: #f2e4e7; animation: craft 1.25s ease-in-out infinite alternate; }.loading-nails i:nth-child(2),.loading-nails i:nth-child(4){height:52px;animation-delay:.15s}.loading-nails i:nth-child(3){height:57px;animation-delay:.3s}
@keyframes craft { to { background: #d4a5af; transform: translateY(-5px); } }
.failed-state,.missing-state { display: grid; min-height: 360px; place-items: center; place-content: center; color: #9499a1; text-align: center; }.failed-state :deep(.icon),.missing-state :deep(.icon){font-size:32px}.failed-state b { margin-top: 12px; color: #4b5058; }.failed-state p { max-width: 520px; margin: 6px 0 0; font-size: 11px; }.missing-state h2 { margin: 12px 0; color: #4b5058; font-size: 18px; }
.iteration-composer { position: absolute; right: 28px; bottom: 20px; left: 304px; display: grid; grid-template-columns: 36px minmax(0,1fr) auto; align-items: end; gap: 10px; padding: 10px; border: 1px solid #d9dde3; border-radius: 15px; background: #fff; box-shadow: 0 16px 48px rgba(37,41,48,.12); }.iteration-composer > button:first-child { display: grid; width: 36px; height: 36px; place-items: center; border: 1px solid #e0e3e8; border-radius: 9px; background: #f7f8fa; color: #7c818a; }.iteration-composer textarea { min-height: 40px; max-height: 100px; resize: none; border: 0; outline: 0; color: #34383e; font-family: inherit; font-size: 12px; line-height: 1.6; }.iterate-button { display: flex; align-items: center; gap: 7px; min-height: 38px; padding: 0 13px; border: 0; border-radius: 10px; background: #24272c; color: #fff; }.iterate-button:disabled { opacity: .4; }
button:focus-visible,a:focus-visible,textarea:focus-visible { outline: 2px solid #b97987; outline-offset: 2px; }
@media (max-width:1100px){.detail-shell{grid-template-columns:1fr}.iteration-composer{left:28px}.detail-stage{min-height:900px}}
@media (max-width:760px){.detail-header,.prompt-panel,.result-area{padding-right:20px;padding-left:20px}.result-grid,.result-grid.single{grid-template-columns:1fr}.result-card footer{align-items:flex-start;flex-direction:column}.reference-panel{margin-right:20px;margin-left:20px}.iteration-composer{right:14px;left:14px}.meta-row{flex-wrap:wrap}}
@media (prefers-reduced-motion:reduce){.loading-nails i{animation:none}}

.design-detail-page { --studio-line: #e7e1dc; --studio-rose: #a66d78; max-width: 1240px; margin: 0 auto; }
.detail-shell { grid-template-columns: 258px minmax(0,1fr); gap: 10px; }
.detail-stage { border-color: var(--studio-line); border-radius: 1px; background: #f7f3f0; }
.detail-header, .prompt-panel { border-color: var(--studio-line); }
.header-copy > button { color: #827a75; }
.title-line span, .prompt-panel > div:first-child span, .result-area > header span { color: var(--studio-rose); font-family: Georgia, serif; }
.title-line h1 { font-family: inherit; font-size: 24px; font-weight: 650; letter-spacing: -.025em; }
.rename-button, .status-pill, .spec-tags span, .reference-panel, .result-card, .generating-state, .iteration-composer, .iteration-composer > button:first-child, .iterate-button { border-radius: 1px; }
.reference-panel { border-color: #e4d4d7; background: #fbf6f6; }
.reference-panel img { border-radius: 1px; }
.result-card { border-color: var(--studio-line); }
.result-card :deep(.el-image) { background: #eeeae7; }
.iteration-composer { border-color: #d9d0ca; box-shadow: 0 18px 42px rgba(72, 56, 49, .11); }
.iterate-button { background: #332e2b; }
@media (max-width:1100px){.detail-shell{grid-template-columns:1fr}.iteration-composer{left:28px}.detail-stage{min-height:900px}}
@media (max-width:760px){.result-grid,.result-grid.single{grid-template-columns:1fr}.iteration-composer{right:14px;left:14px}}

/* Detail uses the same studio desk as the creation surface. */
.design-detail-page { max-width: 1240px; margin: 0 auto; padding: 20px 0; background: transparent; }
.detail-titlebar { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding: 0 2px 18px; }
.detail-titlebar span { color: #aa858c; font-family: Georgia, serif; font-size: 9px; letter-spacing: .16em; }
.detail-titlebar h1 { margin: 5px 0 0; color: #24211f; font-size: 27px; font-weight: 640; letter-spacing: -.035em; }
.detail-titlebar p { margin: 6px 0 0; color: #8f8984; font-size: 12px; }
.library-link { display: inline-flex; align-items: center; gap: 7px; padding: 10px 13px; border: 1px solid #e7e1dc; border-radius: 1px; background: #fff; color: #726b67; font-size: 12px; }
.detail-shell { min-height: 622px; grid-template-columns: 258px minmax(0, 1fr); gap: 10px; }
.detail-stage { min-height: 622px; padding: 0 0 20px; overflow: hidden; border: 1px solid #e7e1dc; border-radius: 1px; background: #fff; }
.detail-stage::before { display: none; }
.detail-header { position: static; min-height: 116px; padding: 24px 30px; border-bottom: 1px solid #e7e1dc; background: #faf7f5; }
.header-copy { display: block; }.header-copy > button { display: flex; color: #827a75; }
.title-line { display: flex; margin-top: 14px; }.title-line span { color: #a66d78; font-family: Georgia, serif; }.title-line h1 { font-size: 20px; }
.meta-row { display: flex; }.status-pill { margin-left: auto; border-radius: 1px; }
.prompt-panel { width: auto; min-height: 0; margin: 0; padding: 22px 30px; border-bottom: 1px solid #e7e1dc; background: #fff; }
.task-preview { position: static; float: left; width: 58px; height: 58px; margin: 0 14px 7px 0; border-radius: 1px; background: #eeeae7; object-fit: cover; }
.task-preview-placeholder { display: grid; place-items: center; color: #7c818a; }.task-preview-placeholder :deep(.icon) { font-size: 20px; }
.prompt-panel > div:first-child { display: flex; }.prompt-panel p { max-width: none; margin: 12px 0; font-size: 13px; line-height: 1.7; }
.spec-tags { gap: 6px; }.spec-tags span { border-radius: 1px; }
.reference-panel { margin: 14px 30px 0; border-radius: 1px; }.reference-panel img { border-radius: 1px; }
.result-area { width: auto; margin: 0; padding: 22px 30px; }.result-area > header { display: flex; }
.result-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; background: transparent; }.result-grid.single { grid-template-columns: minmax(0, 320px); max-width: 320px; }
.result-card { border: 1px solid #e7e1dc; border-radius: 1px; }.result-card :deep(.el-image) { aspect-ratio: 1; }.result-card footer { padding: 10px; }
.generating-state,.failed-state { min-height: 260px; border: 1px solid #e7e1dc; border-radius: 1px; }
.iteration-composer { position: static; width: auto; margin: 0 30px; grid-template-columns: 36px minmax(0, 1fr) auto; transform: none; border-radius: 1px; box-shadow: none; }
.iterate-button { display: flex; width: auto; min-height: 38px; padding: 0 13px; border-radius: 1px; }.iterate-button span { display: inline; }
@media (max-width:1100px){.detail-shell{grid-template-columns:1fr}.detail-stage{min-height:0}.iteration-composer{margin:0 28px}.detail-titlebar{padding-right:2px;padding-left:2px}}
@media (max-width:760px){.design-detail-page{padding:14px 0}.detail-titlebar{align-items:flex-start;flex-direction:column}.detail-header,.prompt-panel,.result-area{padding-right:20px;padding-left:20px}.reference-panel,.iteration-composer{margin-right:20px;margin-left:20px}.result-grid,.result-grid.single{grid-template-columns:1fr}.result-card footer{align-items:flex-start;flex-direction:column}.task-preview{width:54px;height:54px}}
</style>
