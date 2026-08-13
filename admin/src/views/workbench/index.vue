<template>
    <div class="nail-workbench">
        <header class="workbench-heading">
            <div>
                <p>STUDIO OPERATIONS</p>
                <h1>美甲工作台</h1>
                <span>今天从灵感、生成到资产审阅，都在这里完成。</span>
            </div>
            <div class="operator-note">
                <span>个人工作区</span>
                <strong>{{ userName }}</strong>
                <button type="button" aria-label="刷新工作台" :disabled="loading" @click="loadWorkbench">
                    <icon name="el-icon-Refresh" />
                </button>
            </div>
        </header>

        <section class="metric-strip" aria-label="美甲工作室数据概览">
            <article v-for="item in metrics" :key="item.label">
                <div class="metric-index">{{ item.index }}</div>
                <div class="metric-value">{{ item.value }}</div>
                <div class="metric-copy">
                    <strong>{{ item.label }}</strong>
                    <span>{{ item.note }}</span>
                </div>
                <icon :name="item.icon" />
            </article>
        </section>

        <div class="workbench-grid">
            <section class="creation-block">
                <header class="section-heading">
                    <div>
                        <p>CREATE</p>
                        <h2>开启新一轮创作</h2>
                    </div>
                    <span>AI CREATIVE SUITE</span>
                </header>
                <div class="creation-actions">
                    <router-link to="/nail/ai" class="creation-action creation-action--rose">
                        <span class="action-icon"><icon name="el-icon-EditPen" /></span>
                        <span><strong>描述生成</strong><small>用场景、色彩与工艺描述新的美甲方案</small></span>
                        <icon name="el-icon-ArrowRight" />
                    </router-link>
                    <router-link to="/nail/ai" class="creation-action creation-action--sand">
                        <span class="action-icon"><icon name="el-icon-Picture" /></span>
                        <span><strong>参考图创作</strong><small>从已授权资产提取配色、材质或构图语言</small></span>
                        <icon name="el-icon-ArrowRight" />
                    </router-link>
                </div>
            </section>

            <section class="latest-run">
                <header class="section-heading">
                    <div><p>LAST RUN</p><h2>最近生成记录</h2></div>
                    <router-link to="/nail/tasks">全部记录</router-link>
                </header>
                <button v-if="latestTask" type="button" class="latest-task" @click="openTask(latestTask.id)">
                    <span class="latest-cover">
                        <img v-if="latestTask.coverUrl" :src="latestTask.coverUrl" :alt="latestTask.title" />
                        <icon v-else name="el-icon-MagicStick" />
                    </span>
                    <span class="latest-copy">
                        <strong>{{ latestTask.title }}</strong>
                        <small>{{ latestTask.createTime }} · {{ statusLabel(latestTask.status) }}</small>
                        <em>打开设计档案 <icon name="el-icon-ArrowRight" /></em>
                    </span>
                </button>
                <div v-else class="quiet-empty">
                    <icon name="el-icon-Brush" />
                    <span><strong>尚无生成记录</strong><small>从一条清晰的美甲设计意图开始。</small></span>
                </div>
            </section>

            <section class="asset-library">
                <header class="section-heading">
                    <div><p>LIBRARY</p><h2>最近入库</h2></div>
                    <router-link to="/nail/assets">全部资产</router-link>
                </header>
                <div v-if="assets.length" class="asset-row">
                    <router-link v-for="asset in assets" :key="asset.id" to="/nail/assets" class="asset-item">
                        <img :src="asset.url" :alt="asset.name" loading="lazy" />
                        <span><strong>{{ asset.name }}</strong><small>{{ asset.width }} × {{ asset.height }}</small></span>
                    </router-link>
                </div>
                <div v-else class="quiet-empty quiet-empty--wide">
                    <icon name="el-icon-FolderOpened" />
                    <span><strong>资产库还没有作品</strong><small>采纳 AI 结果后会自动出现在这里。</small></span>
                    <router-link to="/nail/assets">进入资产库</router-link>
                </div>
            </section>

            <section class="today-tasks">
                <header class="section-heading">
                    <div><p>TO DO</p><h2>今日待办</h2></div>
                    <span>{{ pendingCount }}</span>
                </header>
                <div v-if="pendingCount" class="todo-list">
                    <router-link to="/nail/tasks">
                        <span class="todo-mark"></span>
                        <span><strong>{{ stats.pendingResults || 0 }} 张结果等待审阅</strong><small>确认可用作品并采纳到资产库</small></span>
                    </router-link>
                    <router-link v-if="stats.runningTasks" to="/nail/tasks">
                        <span class="todo-mark todo-mark--sand"></span>
                        <span><strong>{{ stats.runningTasks }} 个任务正在制作</strong><small>生成完成后可进入设计档案查看</small></span>
                    </router-link>
                </div>
                <div v-else class="quiet-empty">
                    <icon name="el-icon-CircleCheck" />
                    <span><strong>当前没有待办</strong><small>工作室已经整理完毕。</small></span>
                </div>
            </section>
        </div>

        <section class="recent-activity">
            <header class="section-heading">
                <div><p>ACTIVITY</p><h2>最近任务</h2></div>
                <router-link to="/nail/tasks">进入任务中心</router-link>
            </header>
            <div v-if="tasks.length" class="activity-list">
                <button v-for="task in tasks.slice(0, 5)" :key="task.id" type="button" @click="openTask(task.id)">
                    <span class="activity-cover">
                        <img v-if="task.coverUrl" :src="task.coverUrl" :alt="task.title" loading="lazy" />
                        <icon v-else name="el-icon-MagicStick" />
                    </span>
                    <span class="activity-copy"><strong>{{ task.title }}</strong><small>{{ task.prompt }}</small></span>
                    <span class="activity-output">{{ task.resultCount }} / {{ task.outputCount }} 张</span>
                    <span :class="['activity-status', statusClass(task.status)]">{{ statusLabel(task.status) }}</span>
                    <icon name="el-icon-ArrowRight" />
                </button>
            </div>
            <div v-else class="quiet-empty quiet-empty--activity">
                <icon name="el-icon-MagicStick" />
                <span><strong>输入描述开始第一条创作</strong><small>设计任务会在这里持续沉淀为可追溯档案。</small></span>
            </div>
        </section>
    </div>
</template>

<script lang="ts" setup name="workbench">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { nailAssetList, nailTaskList, nailTaskStats, type NailAsset, type NailTaskSummary } from '@/api/nail'
import useUserStore from '@/stores/modules/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const stats = reactive<Record<string, number>>({})
const tasks = ref<NailTaskSummary[]>([])
const assets = ref<NailAsset[]>([])

const userName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || 'admin')
const latestTask = computed(() => tasks.value[0])
const pendingCount = computed(() => Number(stats.pendingResults || 0) + Number(stats.runningTasks || 0))
const todayKey = new Date().toLocaleDateString('zh-CN').replaceAll('/', '-')
const todayTaskCount = computed(() => tasks.value.filter((task) => task.createTime?.startsWith(todayKey)).length)
const failedTaskCount = computed(() => tasks.value.filter((task) => task.status === 'FAILED').length)
const metrics = computed(() => [
    { index: '01', label: '待审结果', note: '需要确认的设计提案', value: stats.pendingResults || 0, icon: 'el-icon-DocumentChecked' },
    { index: '02', label: '今日生成', note: '今日建立的创作任务', value: todayTaskCount.value, icon: 'el-icon-MagicStick' },
    { index: '03', label: '可用资产', note: '版权与状态均可用', value: stats.activeAssets || 0, icon: 'el-icon-FolderOpened' },
    { index: '04', label: '失败任务', note: '等待重新调整设计', value: failedTaskCount.value, icon: 'el-icon-Warning' }
])

const statusLabel = (status: string) => ({
    QUEUED: '等待生成', RUNNING: '正在生成', SUCCEEDED: '生成完成',
    PARTIAL_SUCCEEDED: '部分完成', FAILED: '生成失败'
} as Record<string, string>)[status] || status
const statusClass = (status: string) => status.toLowerCase().replace('_', '-')
const openTask = (id: number) => router.push({ path: '/nail/ai/detail', query: { id } })
const loadWorkbench = async () => {
    loading.value = true
    try {
        const [statsData, taskData, assetData] = await Promise.all([
            nailTaskStats(),
            nailTaskList({ pageNo: 1, pageSize: 8 }),
            nailAssetList({ pageNo: 1, pageSize: 5 })
        ])
        Object.assign(stats, statsData || {})
        tasks.value = taskData?.lists || []
        assets.value = assetData?.lists || []
    } finally {
        loading.value = false
    }
}

onMounted(loadWorkbench)
</script>

<style lang="scss" scoped>
.nail-workbench {
    --studio-ink: #24211f;
    --studio-muted: #8f8984;
    --studio-line: #e7e1dc;
    --studio-rose: #a66d78;
    --studio-blush: #f7eeef;
    --studio-sand: #f5f0e7;
    max-width: 1240px;
    min-height: calc(100vh - 110px);
    margin: 0 auto;
    color: var(--studio-ink);
}
.workbench-heading {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 24px;
    padding: 20px 2px 18px;
    p { margin: 0 0 5px; color: #a48288; font-size: 9px; letter-spacing: .18em; }
    h1 { margin: 0; font-size: 27px; font-weight: 640; letter-spacing: -.035em; }
    > div:first-child > span { display: block; margin-top: 5px; color: var(--studio-muted); font-size: 12px; }
}
.operator-note {
    display: grid;
    grid-template-columns: auto 34px;
    align-items: center;
    column-gap: 10px;
    text-align: right;
    span { color: #a39c97; font-size: 9px; }
    strong { font-size: 11px; font-weight: 600; }
    button { grid-row: 1 / 3; grid-column: 2; display: grid; width: 34px; height: 34px; place-items: center; border: 1px solid var(--studio-line); background: #fff; color: #6e6864; }
}
.metric-strip {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    border: 1px solid var(--studio-line);
    background: #fff;
    article { position: relative; min-height: 110px; padding: 14px 18px 16px; border-right: 1px solid var(--studio-line); }
    article:last-child { border-right: 0; }
    article > :deep(.icon) { position: absolute; top: 16px; right: 16px; color: #b8afa9; }
}
.metric-index { color: #b5aca6; font-family: Georgia, serif; font-size: 9px; }
.metric-value { margin-top: 13px; font-family: Georgia, 'Times New Roman', serif; font-size: 30px; line-height: 1; }
.metric-copy { display: flex; align-items: baseline; justify-content: space-between; gap: 8px; margin-top: 9px; strong { font-size: 11px; } span { overflow: hidden; color: #a29b96; font-size: 9px; text-overflow: ellipsis; white-space: nowrap; } }
.workbench-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(290px, .72fr); gap: 10px; margin-top: 10px; }
.creation-block, .latest-run, .asset-library, .today-tasks, .recent-activity { border: 1px solid var(--studio-line); background: #fff; }
.creation-block, .latest-run, .asset-library, .today-tasks { padding: 18px; }
.section-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; p { margin: 0 0 4px; color: #987f84; font-family: Georgia, serif; font-size: 9px; letter-spacing: .12em; } h2 { margin: 0; font-size: 14px; font-weight: 640; } > span, > a { color: #8d8580; font-size: 10px; letter-spacing: .08em; } > a { color: #8e626b; letter-spacing: 0; } }
.creation-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 16px; }
.creation-action { display: grid; grid-template-columns: 38px minmax(0, 1fr) auto; align-items: center; gap: 12px; min-height: 76px; padding: 12px 14px; border: 1px solid transparent; color: #332f2d; &:hover { border-color: #d8c4c7; } > span:nth-child(2) { display: grid; gap: 4px; } strong { font-size: 12px; } small { color: #928a85; font-size: 9px; } > :deep(.icon) { color: #a58e92; } }
.creation-action--rose { background: var(--studio-blush); }
.creation-action--sand { background: var(--studio-sand); }
.action-icon { display: grid; width: 36px; height: 36px; place-items: center; border: 1px solid rgba(134, 102, 106, .14); border-radius: 50%; background: rgba(255,255,255,.58); color: #9f6c76; }
.latest-run { border-top-color: #c6929c; }
.latest-task { display: grid; grid-template-columns: 58px minmax(0,1fr); gap: 12px; width: 100%; margin-top: 14px; padding: 0; border: 0; background: transparent; text-align: left; }
.latest-cover { display: grid; width: 58px; height: 58px; place-items: center; overflow: hidden; background: #f2efed; color: #a67b83; img { width: 100%; height: 100%; object-fit: cover; } }
.latest-copy { display: grid; min-width: 0; gap: 4px; strong, small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } strong { font-size: 11px; } small { color: #9b948f; font-size: 9px; } em { display: flex; align-items: center; gap: 4px; margin-top: 5px; color: #8e626b; font-size: 9px; font-style: normal; } }
.asset-library { min-height: 155px; }
.asset-row { display: grid; grid-template-columns: repeat(5, minmax(0,1fr)); gap: 8px; margin-top: 14px; }
.asset-item { display: grid; grid-template-columns: 58px minmax(0,1fr); align-items: center; gap: 9px; min-width: 0; padding: 6px; border: 1px solid #eee9e5; color: var(--studio-ink); img { width: 58px; height: 58px; object-fit: cover; } span { display: grid; min-width: 0; gap: 4px; } strong, small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } strong { font-size: 10px; } small { color: #817a76; font-size: 9px; } }
.today-tasks { min-height: 155px; }
.todo-list { display: grid; gap: 8px; margin-top: 14px; a { display: grid; grid-template-columns: 8px minmax(0,1fr); gap: 10px; padding: 10px 0; border-top: 1px solid #eee9e5; color: var(--studio-ink); } a span:last-child { display: grid; gap: 3px; } strong { font-size: 10px; } small { color: #9b938e; font-size: 9px; } }
.todo-mark { width: 7px; height: 7px; margin-top: 3px; border-radius: 50%; background: #b97c87; }.todo-mark--sand { background: #c8a86c; }
.quiet-empty { display: flex; align-items: center; justify-content: center; gap: 12px; min-height: 80px; color: #b0a9a4; text-align: left; > span { display: grid; gap: 3px; } strong { color: #6d6763; font-size: 10px; } small { font-size: 9px; } }
.quiet-empty--wide { min-height: 95px; a { padding: 6px 9px; border: 1px solid var(--studio-line); color: #766f6b; font-size: 9px; } }
.recent-activity { grid-column: 1 / -1; padding: 18px; }
.activity-list { margin-top: 12px; border-top: 1px solid #ece7e3; }
.activity-list button { display: grid; grid-template-columns: 42px minmax(0,1fr) 70px 76px 16px; align-items: center; gap: 12px; width: 100%; padding: 9px 2px; border: 0; border-bottom: 1px solid #f0ece9; background: transparent; color: var(--studio-ink); text-align: left; &:hover { background: #fbf9f7; } }
.activity-cover { display: grid; width: 42px; height: 42px; place-items: center; overflow: hidden; background: #f0edeb; color: #aa7b84; img { width: 100%; height: 100%; object-fit: cover; } }
.activity-copy { display: grid; min-width: 0; gap: 3px; strong, small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } strong { font-size: 10px; } small { color: #9c9590; font-size: 9px; } }
.activity-output { color: #8e8782; font-variant-numeric: tabular-nums; font-size: 9px; }
.activity-status { justify-self: start; padding: 4px 6px; background: #f0eeec; color: #625c58; font-size: 9px; }.activity-status.succeeded { background: #eaf2ed; color: #4c775f; }.activity-status.running, .activity-status.queued, .activity-status.partial-succeeded { background: #f4eee3; color: #8b6d3b; }.activity-status.failed { background: #f7e9ea; color: #9c535b; }
.quiet-empty--activity { min-height: 110px; }
button, a { transition: border-color .18s ease, background-color .18s ease, color .18s ease; }
button:focus-visible, a:focus-visible { outline: 2px solid #a66d78; outline-offset: 2px; }
@media (max-width: 1100px) { .asset-row { grid-template-columns: repeat(3, minmax(0,1fr)); }.metric-copy span { display: none; } }
@media (max-width: 820px) { .workbench-heading { align-items: flex-start; flex-direction: column; }.operator-note { align-self: flex-end; }.metric-strip { grid-template-columns: repeat(2,1fr); }.metric-strip article:nth-child(2) { border-right: 0; }.metric-strip article:nth-child(-n+2) { border-bottom: 1px solid var(--studio-line); }.workbench-grid { grid-template-columns: 1fr; }.recent-activity { grid-column: auto; }.asset-row { grid-template-columns: repeat(2,minmax(0,1fr)); }.activity-list button { grid-template-columns: 42px minmax(0,1fr) 70px 16px; }.activity-output { display: none; } }
@media (max-width: 560px) { .creation-actions { grid-template-columns: 1fr; }.metric-strip article { min-height: 96px; }.asset-row { grid-template-columns: 1fr; }.activity-status { display: none; }.activity-list button { grid-template-columns: 42px minmax(0,1fr) 16px; } }
@media (prefers-reduced-motion: reduce) { button, a { transition: none; } }
</style>
