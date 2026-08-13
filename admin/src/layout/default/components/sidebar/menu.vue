<template>
    <div
        class="menu flex-1 min-h-0"
        :class="themeClass"
        :style="isCollapsed ? '' : `--aside-width: ${width}px`"
    >
        <el-scrollbar>
            <el-menu
                v-bind="config"
                :default-active="activeMenu"
                :collapse="isCollapsed"
                mode="vertical"
                :unique-opened="uniqueOpened"
                @select="$emit('select')"
            >
                <menu-item
                    v-for="route in routes"
                    :key="route.path"
                    :route="route"
                    :route-path="route.path"
                    :popper-class="themeClass"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'
import MenuItem from './menu-item.vue'
import type { RouteRecordRaw } from 'vue-router'

const props = defineProps({
    routes: {
        type: Object as PropType<RouteRecordRaw[]>
    },
    config: {
        type: Object
    },
    isCollapsed: {
        type: Boolean,
        default: false
    },
    uniqueOpened: {
        type: Boolean,
        default: false
    },
    theme: {
        type: String
    },
    width: {
        type: Number,
        default: 200
    }
})

defineEmits(['select'])

const route = useRoute()
const activeMenu = computed<string>(() => route.meta?.activeMenu || route.path)
const themeClass = computed(() => `theme-${props.theme}`)
</script>

<style lang="scss" scoped>
.menu {
    &.theme-dark {
        .el-menu {
            :deep(.el-menu-item) {
                &.is-active {
                    @apply bg-primary border-primary;
                }
            }
        }
        :deep(.el-menu--collapse) {
            .el-sub-menu.is-active .el-sub-menu__title {
                @apply bg-primary #{!important};
            }
        }
    }
    &.theme-light {
        :deep(.el-menu) {
            padding: 12px 8px;
            background: transparent;
            .el-menu-item {
                border-color: transparent;
                height: 42px;
                margin: 2px 0;
                border-radius: 2px;
                color: #6d6662;
                font-size: 12px;
                &.is-active {
                    border-right: 0;
                    background: #f5e9eb;
                    color: #8f5965;
                    &::before {
                        position: absolute;
                        left: 0;
                        width: 2px;
                        height: 16px;
                        background: #a66d78;
                        content: '';
                    }
                }
            }
            .el-sub-menu__title { height: 42px; margin: 2px 0; border-radius: 2px; color: #6d6662; font-size: 12px; }
            .el-menu-item:hover,
            .el-sub-menu__title:hover {
                background: #f8f4f2;
                color: #8f5965;
            }
        }
    }
    .el-menu {
        border-right: none;
        &:not(.el-menu--collapse) {
            width: var(--aside-width);
        }
    }
}
</style>
