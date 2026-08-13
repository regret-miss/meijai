<template>
    <main class="main-wrap h-full bg-page">
        <el-scrollbar>
            <div class="studio-main-content">
                <router-view v-if="isRouteShow" v-slot="{ Component, route }">
                    <keep-alive :include="includeList" :max="20">
                        <component :is="Component" :key="route.fullPath" />
                    </keep-alive>
                </router-view>
            </div>
        </el-scrollbar>
    </main>
</template>

<script setup lang="ts">
import useAppStore from '@/stores/modules/app'
import useTabsStore from '@/stores/modules/multipleTabs'
import useSettingStore from '@/stores/modules/setting'
const appStore = useAppStore()
const tabsStore = useTabsStore()
const settingStore = useSettingStore()
const isRouteShow = computed(() => appStore.isRouteShow)
const includeList = computed(() => (settingStore.openMultipleTabs ? tabsStore.getCacheTabList : []))
</script>

<style lang="scss">
.main-wrap { background: #f4f1ee; }
.studio-main-content { min-height: 100%; padding: 18px 24px 28px; }
@media (max-width: 760px) { .studio-main-content { padding: 14px; } }
</style>
