<template>
    <aside class="task-rail" aria-label="设计记录">
        <header class="rail-header">
            <div>
                <span>NAIL STUDIO</span>
                <strong>设计档案</strong>
            </div>
            <button type="button" aria-label="刷新设计记录" @click="$emit('refresh')">
                <icon name="el-icon-Refresh" />
            </button>
        </header>

        <button class="new-design" type="button" @click="$emit('create')">
            <span><icon name="el-icon-Plus" /></span>
            <span><b>开始新设计</b><small>从灵感或参考图开始</small></span>
        </button>

        <div v-loading="loading" class="rail-list">
            <button
                v-for="task in tasks"
                :key="task.id"
                type="button"
                :class="['rail-task', { active: activeId === task.id }]"
                @click="$emit('select', task.id)"
            >
                <span class="task-cover" :class="{ 'has-preview': task.coverUrl }">
                    <icon name="el-icon-MagicStick" />
                    <img v-if="task.coverUrl" :src="task.coverUrl" :alt="`${task.title} 的生成预览`" loading="lazy" @error="hideBrokenImage" />
                </span>
                <span class="task-copy">
                    <b>{{ task.title }}</b>
                    <small>{{ task.createTime }} · {{ resultCopy(task) }}</small>
                </span>
                <i :class="['status-dot', statusClass(task.status)]"></i>
            </button>
            <div v-if="!loading && !tasks.length" class="rail-empty">
                <icon name="el-icon-Brush" />
                <p>还没有设计记录</p>
                <small>完成第一张作品后会自动归档</small>
            </div>
        </div>
    </aside>
</template>

<script lang="ts" setup>
import type { NailTaskSummary } from '@/api/nail'

defineProps<{ tasks: NailTaskSummary[]; loading: boolean; activeId?: number }>()
defineEmits<{
    (event: 'refresh'): void
    (event: 'create'): void
    (event: 'select', id: number): void
}>()

const statusClass = (status: string) => status.toLowerCase().replace('_', '-')
const hideBrokenImage = (event: Event) => { (event.target as HTMLImageElement).remove() }
const resultCopy = (task: NailTaskSummary) => {
    if (['QUEUED', 'RUNNING'].includes(task.status)) return '正在生成'
    if (task.status === 'FAILED') return '生成失败'
    return `${task.resultCount || 0} 张方案`
}
</script>

<style lang="scss" scoped>
.task-rail {
    display: flex;
    flex-direction: column;
    min-width: 0;
    height: calc(100vh - 132px);
    padding: 20px 14px 14px;
    border: 1px solid #dfe2e7;
    border-radius: 16px;
    background: #f7f8fa;
}
.rail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 6px 16px;
    div { display: grid; gap: 3px; }
    span { color: #8b9099; font-size: 9px; letter-spacing: .18em; }
    strong { color: #17191d; font-size: 17px; font-weight: 620; }
    button {
        display: grid;
        width: 32px;
        height: 32px;
        place-items: center;
        border: 1px solid #dfe2e7;
        border-radius: 9px;
        background: #fff;
        color: #5e646d;
    }
}
.new-design {
    display: flex;
    align-items: center;
    gap: 11px;
    width: 100%;
    padding: 12px;
    border: 1px solid #24272c;
    border-radius: 12px;
    background: #24272c;
    color: #fff;
    text-align: left;
    > span:first-child {
        display: grid;
        width: 32px;
        height: 32px;
        flex: none;
        place-items: center;
        border-radius: 50%;
        background: #b97987;
    }
    > span:last-child { display: grid; min-width: 0; gap: 2px; }
    b { font-size: 13px; font-weight: 600; }
    small { color: #cdd0d5; font-size: 10px; }
}
.rail-list {
    flex: 1;
    min-height: 120px;
    margin-top: 14px;
    overflow-y: auto;
    scrollbar-width: thin;
}
.rail-task {
    position: relative;
    display: grid;
    grid-template-columns: 44px minmax(0, 1fr) 8px;
    align-items: center;
    gap: 10px;
    width: 100%;
    margin-bottom: 4px;
    padding: 9px;
    border: 1px solid transparent;
    border-radius: 12px;
    background: transparent;
    text-align: left;
    transition: background-color .18s ease, border-color .18s ease;
    &:hover { border-color: #e2d4d8; background: #fff; }
    &.active { border-color: #d8b5bd; background: #fff; box-shadow: 0 6px 22px rgba(45, 49, 56, .06); }
}
.task-cover {
    position: relative;
    display: grid;
    width: 44px;
    height: 44px;
    place-items: center;
    overflow: hidden;
    border-radius: 10px;
    background: #e9e8eb;
    color: #8c7279;
    img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
}
.task-copy {
    display: grid;
    min-width: 0;
    gap: 5px;
    b { overflow: hidden; color: #26292e; font-size: 12px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
    small { overflow: hidden; color: #979ca5; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
}
.status-dot { width: 7px; height: 7px; border-radius: 50%; background: #b8bdc5; }
.status-dot.succeeded { background: #3c966c; }
.status-dot.partial-succeeded, .status-dot.running, .status-dot.queued { background: #c18a42; }
.status-dot.failed { background: #c95d63; }
.rail-empty { padding: 48px 10px; color: #a2a6ae; text-align: center; }
.rail-empty :deep(.icon) { font-size: 28px; }
.rail-empty p { margin: 10px 0 3px; color: #60656e; font-size: 13px; }
.rail-empty small { font-size: 10px; }

@media (max-width: 1100px) {
    .task-rail { height: auto; max-height: 260px; }
    .rail-list { display: flex; gap: 8px; overflow-x: auto; }
    .rail-task { min-width: 230px; }
}
@media (prefers-reduced-motion: reduce) {
    .rail-task { transition: none; }
}

.task-rail {
    height: auto;
    padding: 18px 12px 12px;
    border-color: #e7e1dc;
    border-radius: 1px;
    background: #fbfaf8;
}
.rail-header span { color: #aa858c; font-family: Georgia, serif; }
.rail-header button { border-color: #e7e1dc; border-radius: 1px; }
.new-design { border-color: #332e2b; border-radius: 1px; background: #332e2b; }
.new-design > span:first-child { border-radius: 50%; background: #a66d78; }
.rail-task { border-radius: 1px; }
.rail-task:hover { border-color: #e4d7d8; background: #fff; }
.rail-task.active { border-color: #d6b8bd; background: #f8eeef; box-shadow: none; }
.task-cover { border-radius: 1px; }
.task-cover.has-preview { background: #eeeae7; }
@media (max-width: 1100px) { .task-rail { height: auto; max-height: 260px; } }

/* The rail is a quiet conversation picker, not a second dashboard. */
@media (min-width: 1101px) {
    .task-rail { height: auto; padding: 22px 14px; border: 0; border-right: 1px solid #eaebee; border-radius: 0; background: #fff; }
    .rail-header { min-height: 38px; padding: 0 2px 10px; }
    .rail-header div { display: none; }
    .rail-header::before { color: #15161a; content: '开启创作'; font-size: 15px; font-weight: 650; letter-spacing: -.03em; }
    .rail-header button { width: 28px; height: 28px; border: 0; border-radius: 6px; background: #f4f5f6; color: #555a62; }
    .new-design { min-height: 40px; gap: 9px; padding: 6px; border: 0; border-radius: 8px; background: #fff; color: #202126; }
    .new-design:hover { background: #f2f3f5; }.new-design > span:first-child { width: 28px; height: 28px; border: 1px solid #e1e3e6; border-radius: 7px; background: #fff; color: #373a40; }
    .new-design > span:last-child { display: block; }.new-design b { font-size: 12px; font-weight: 540; }.new-design small { display: none; }
    .rail-list { flex: none; max-height: calc(100vh - 190px); margin-top: 8px; }.rail-task { grid-template-columns: 34px minmax(0, 1fr); min-height: 42px; gap: 8px; margin-bottom: 4px; padding: 5px 6px; border: 0; border-radius: 8px; }
    .rail-task:hover,.rail-task.active { border: 0; background: #f0f1f3; box-shadow: none; }.task-cover { width: 32px; height: 32px; border-radius: 7px; }.task-copy { gap: 0; }.task-copy b { font-size: 12px; font-weight: 520; }.task-copy small,.status-dot { display: none; }
}
</style>
