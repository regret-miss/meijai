<template>
    <div class="nail-task-page">
        <header class="page-heading">
            <div><span>PRODUCTION CONTROL</span><h1>任务与审阅</h1><p>跟踪生成状态、待审结果与资产采纳，所有作品均可回到完整设计档案。</p></div>
            <router-link to="/nail/ai"><el-button type="primary"><icon name="el-icon-Plus" /> 新建设计</el-button></router-link>
        </header>

        <section class="stats-grid">
            <article :class="{ active: !query.status }" @click="filterByStatus('')"><span>全部设计</span><b>{{ stats.totalTasks || 0 }}</b><small>累计任务</small></article>
            <article :class="{ active: query.status === 'QUEUED' || query.status === 'RUNNING' }" @click="filterByStatus('QUEUED')"><span>正在制作</span><b>{{ stats.runningTasks || 0 }}</b><small>队列与运行中</small></article>
            <article :class="{ active: query.status === 'SUCCEEDED' || query.status === 'PARTIAL_SUCCEEDED' }" @click="filterByStatus('SUCCEEDED')"><span>待审结果</span><b>{{ stats.pendingResults || 0 }}</b><small>需要设计确认</small></article>
            <article @click="goToAssets"><span>正式资产</span><b>{{ stats.activeAssets || 0 }}</b><small>当前可用</small></article>
        </section>

        <section class="control-panel">
            <el-input v-model="query.keyword" clearable placeholder="搜索设计名称或创作描述" @keyup.enter="resetPage"><template #prefix><icon name="el-icon-Search" /></template></el-input>
            <el-select v-model="query.status" clearable placeholder="全部状态"><el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" /></el-select>
            <el-select v-model="query.taskType" clearable placeholder="全部模式"><el-option label="文字创作" value="TEXT_TO_IMAGE" /><el-option label="参考图改款" value="IMAGE_TO_IMAGE" /></el-select>
            <el-button type="primary" @click="resetPage">筛选</el-button><el-button @click="resetParams">重置</el-button>
        </section>

        <section class="task-table-wrap">
            <el-table v-loading="pager.loading" :data="pager.lists" row-key="id">
                <el-table-column label="设计记录" min-width="310">
                    <template #default="{ row }"><div class="design-cell"><span class="cover"><img v-if="row.coverUrl" :src="row.coverUrl" :alt="row.title" /><icon v-else name="el-icon-MagicStick" /></span><div><b>{{ row.title }}</b><small>{{ row.prompt }}</small></div></div></template>
                </el-table-column>
                <el-table-column label="状态" width="120"><template #default="{ row }"><span :class="['status', statusClass(row.status)]">{{ statusLabel(row.status) }}</span></template></el-table-column>
                <el-table-column label="输出" width="120"><template #default="{ row }"><span class="count-copy">{{ row.resultCount }} / {{ row.outputCount }} 张</span></template></el-table-column>
                <el-table-column label="已采纳" width="100"><template #default="{ row }"><span class="count-copy">{{ row.adoptedCount }} 张</span></template></el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="170" />
                <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openTask(row.id)">查看与审阅</el-button><el-button link type="danger" @click="removeTask(row)">删除</el-button></template></el-table-column>
            </el-table>
            <el-empty v-if="!pager.loading && !pager.lists.length" description="没有匹配的设计任务" />
        </section>
        <div class="pagination-row"><pagination v-model="pager" @change="getLists" /></div>
    </div>
</template>

<script lang="ts" setup name="nailTasks">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { nailTaskDelete, nailTaskList, nailTaskStats } from '@/api/nail'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'

const router = useRouter()
const query = reactive({ keyword: '', status: '', taskType: '' })
const stats = reactive<Record<string, number>>({})
const { pager, getLists, resetPage, resetParams } = usePaging({ fetchFun: nailTaskList, params: query, size: 12, firstLoading: true })
const statuses = [
    { value: 'QUEUED', label: '等待生成' }, { value: 'RUNNING', label: '正在生成' },
    { value: 'SUCCEEDED', label: '生成完成' }, { value: 'PARTIAL_SUCCEEDED', label: '部分完成' }, { value: 'FAILED', label: '生成失败' }
]
const statusClass = (status: string) => status.toLowerCase().replace('_', '-')
const statusLabel = (status: string) => statuses.find((item) => item.value === status)?.label || status
const openTask = (id: number) => router.push({ path: '/nail/ai/detail', query: { id } })
const filterByStatus = (status: string) => { query.status = status; resetPage() }
const goToAssets = () => router.push('/nail/assets')
const removeTask = async (row: any) => {
    await feedback.confirm(`确定删除设计记录「${row.title}」？其生成结果与已采纳资产将一并回收。`)
    await nailTaskDelete({ id: row.id })
    feedback.msgSuccess('设计记录已删除')
    await Promise.all([getLists(), Object.assign(stats, await nailTaskStats())])
}
onMounted(async () => { getLists(); Object.assign(stats, await nailTaskStats()) })
</script>

<style lang="scss" scoped>
.nail-task-page { min-height: calc(100vh - 94px); color: #25282d; }
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; padding: 8px 4px 22px; }.page-heading span { color: #9b6b76; font-size: 9px; letter-spacing: .18em; }.page-heading h1 { margin: 6px 0 4px; font-family: 'Noto Serif SC','Songti SC',serif; font-size: 27px; font-weight: 560; }.page-heading p { margin: 0; color: #858a93; font-size: 12px; }
.stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 10px; margin-bottom: 12px; }.stats-grid article { padding: 17px 19px; border: 1px solid #dfe2e7; border-radius: 12px; background: #fff; cursor: pointer; transition: border-color .18s ease, transform .18s ease, box-shadow .18s ease, background .18s ease; }.stats-grid article:hover { border-color: #b56872; transform: translateY(-2px); box-shadow: 0 6px 18px rgba(45,42,37,.08); }.stats-grid article.active { border-color: #b56872; background: #fdf6f7; }.stats-grid span,.stats-grid small { display: block; color: #9398a1; font-size: 10px; }.stats-grid b { display: block; margin: 8px 0 3px; color: #2a2d32; font-family: Georgia,serif; font-size: 26px; font-weight: 500; }
.control-panel { display: grid; grid-template-columns: minmax(260px,1fr) 160px 160px auto auto; gap: 8px; padding: 13px; border: 1px solid #dfe2e7; border-radius: 12px 12px 0 0; background: #f7f8fa; }
.task-table-wrap { min-height: 300px; padding: 0 13px 10px; border: 1px solid #dfe2e7; border-top: 0; border-radius: 0 0 12px 12px; background: #fff; }.task-table-wrap :deep(.el-table th.el-table__cell) { background: #fff; color: #777c85; font-size: 11px; font-weight: 500; }.task-table-wrap :deep(.el-table td.el-table__cell) { padding: 12px 0; }
.design-cell { display: grid; grid-template-columns: 54px minmax(0,1fr); align-items: center; gap: 11px; }.cover { display: grid; width: 54px; height: 54px; place-items: center; overflow: hidden; border-radius: 9px; background: #eceef1; color: #9a7079; }.cover img { width: 100%; height: 100%; object-fit: cover; }.design-cell > div { display: grid; min-width: 0; gap: 5px; }.design-cell b,.design-cell small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.design-cell b { color: #30343a; font-size: 12px; }.design-cell small { color: #979ca4; font-size: 10px; }
.status { display: inline-block; padding: 5px 8px; border-radius: 6px; background: #edf0f2; color: #66707a; font-size: 10px; }.status.succeeded { background: #e8f3ed; color: #397c5b; }.status.running,.status.queued,.status.partial-succeeded { background: #f6eee0; color: #936d35; }.status.failed { background: #f8e9ea; color: #a44f55; }.count-copy { color: #626871; font-size: 11px; }.pagination-row { display: flex; justify-content: flex-end; margin-top: 14px; }
@media(max-width:1000px){.stats-grid{grid-template-columns:repeat(2,1fr)}.control-panel{grid-template-columns:1fr 1fr}.control-panel :deep(.el-input){grid-column:1/-1}}
</style>
