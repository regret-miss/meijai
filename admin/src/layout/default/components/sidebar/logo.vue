<template>
    <div class="logo">
        <image-contain :width="szie" :height="szie" :src="config.webLogo" />
        <transition name="title-width">
            <div
                v-show="showTitle"
                class="logo-title overflow-hidden whitespace-nowrap"
                :class="{ 'text-white': theme == ThemeEnum.DARK }"
                :style="{ left: `${szie + 16}px` }"
            >
                <strong>{{ title || '美甲运营台' }}</strong>
                <small>NAIL STUDIO</small>
            </div>
        </transition>
    </div>
</template>

<script setup lang="ts">
import useAppStore from '@/stores/modules/app'
import { ThemeEnum } from '@/enums/appEnums'
defineProps({
    szie: { type: Number, default: 34 },
    title: { type: String },
    theme: { type: String },
    showTitle: { type: Boolean, default: true }
})
const appStore = useAppStore()
const config = computed(() => appStore.config)
</script>
<style lang="scss" scoped>
.logo {
    height: 68px;
    overflow: hidden;
    @apply flex items-center relative;
    padding: 10px 12px;
    border-bottom: 1px solid #eee9e5;
    .logo-title {
        display: grid;
        width: calc(100% - 58px);
        position: absolute;
        gap: 2px;
        strong { overflow: hidden; color: #312c29; font-size: 13px; font-weight: 650; text-overflow: ellipsis; }
        small { color: #aa8b91; font-family: Georgia, serif; font-size: 7px; letter-spacing: .13em; }
    }

    .title-width-enter-active {
        opacity: 0;
        transition: all 0.3s ease-out;
    }

    .title-width-leave-active {
        transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
    }

    .title-width-enter-from,
    .title-width-leave-to {
        width: 0;
        opacity: 0;
    }
}
</style>
