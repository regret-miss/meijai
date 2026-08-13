<template>
    <div class="nail-public-page">
        <header class="site-header">
            <a class="brand" href="#top"><span>N</span><div><b>NAILFORM</b><small>AI NAIL ATELIER</small></div></a>
            <nav><a href="#directions">趋势方向</a><a href="#studio">AI 创作</a><a href="#craft">专业标准</a></nav>
            <a class="start-link" href="#studio">开始设计 <span>↗</span></a>
        </header>

        <main id="top">
            <section class="editorial-hero">
                <div class="hero-copy">
                    <span class="eyebrow">NAIL DESIGN INTELLIGENCE · 2026</span>
                    <h1>不是随机出图<br />是懂美甲的设计提案</h1>
                    <p>把流行趋势、甲型比例、材质工艺与真实佩戴效果组织成可执行的美甲方案。每一次生成，都能继续改款和沉淀为你的设计资产。</p>
                    <div class="hero-actions"><a href="#studio">进入 AI 创作台</a><span>专业美甲场景提示系统 · Seedream 图像模型</span></div>
                </div>
                <div class="hero-gallery" aria-label="美甲设计示例">
                    <figure v-for="(image, index) in sampleImages" :key="image" :class="`sample-${index + 1}`"><img :src="image" alt="AI 美甲设计示例" /><figcaption>0{{ index + 1 }} · {{ sampleTitles[index] }}</figcaption></figure>
                </div>
            </section>

            <section id="directions" class="direction-strip">
                <div><span>CURATED NOW</span><b>本季趋势胶囊</b></div>
                <button v-for="preset in presets" :key="preset.value" type="button" :class="{ active: form.trendPreset === preset.value }" @click="selectPreset(preset)">
                    <span class="palette"><i v-for="color in preset.colors" :key="color" :style="{ background: color }"></i></span>
                    <span><b>{{ preset.label }}</b><small>{{ preset.note }}</small></span>
                </button>
            </section>

            <section id="studio" class="studio-section">
                <header class="section-title"><div><span>CREATE WITH NAILFORM</span><h2>AI 美甲创作台</h2></div><p>清楚描述你想要的气质和细节，其余专业参数由系统协同完成。</p></header>
                <div class="studio-grid">
                    <form class="composer-card" @submit.prevent="generate">
                        <div class="mode-switch">
                            <button type="button" :class="{ active: form.creativeMode === 'ON_HAND' }" @click="form.creativeMode = 'ON_HAND'"><b>真人上手</b><small>适合展示佩戴效果</small></button>
                            <button type="button" :class="{ active: form.creativeMode === 'DESIGN_BOARD' }" @click="form.creativeMode = 'DESIGN_BOARD'"><b>明档设计稿</b><small>适合确认十指款式</small></button>
                        </div>

                        <div class="prompt-field"><label for="nail-prompt">DESIGN BRIEF</label><span>{{ form.prompt.length }} / 1000</span><textarea id="nail-prompt" v-model="form.prompt" maxlength="1000" placeholder="例如：短杏仁甲，烟粉猫眼与冷银细线，两枚重点甲保留干净留白，整体克制、有精品店陈列感。"></textarea></div>

                        <div class="reference-field">
                            <label v-if="!reference" class="reference-upload">
                                <input type="file" accept="image/png,image/jpeg" :disabled="uploading" @change="uploadReference" />
                                <span>＋</span><span><b>{{ uploading ? '正在上传参考图' : '加入参考图' }}</b><small>有授权的款式、配色或材质图片</small></span>
                            </label>
                            <div v-else class="reference-ready"><img :src="reference.url" :alt="reference.name" /><span><b>{{ reference.name }}</b><small>已加入参考 · {{ form.referenceStrategy === 'REINTERPRET' ? '提取语言重新设计' : '保留指定特征' }}</small></span><button type="button" @click="removeReference">移除</button></div>
                        </div>

                        <div class="parameter-grid">
                            <label><span>甲型</span><select v-model="form.nailShape"><option value="SHORT_ALMOND">短杏仁</option><option value="SHORT_SQUOVAL">短方圆</option><option value="ALMOND">杏仁</option><option value="SQUARE">方形</option><option value="COFFIN">芭蕾</option></select></label>
                            <label><span>材质</span><select v-model="form.finish"><option value="VELVET_CAT_EYE">丝绒猫眼</option><option value="JELLY">果冻透色</option><option value="CHROME">镜面铬光</option><option value="MICRO_FRENCH">微法式</option><option value="AURA">晕染光圈</option><option value="SCULPTED_GEL">立体凝胶</option></select></label>
                            <label><span>气质</span><select v-model="form.designStyle"><option value="QUIET_LUXURY">克制高级</option><option value="KOREAN_CLEAR">韩系清透</option><option value="RUNWAY">秀场前卫</option><option value="FUTURISTIC">未来机能</option><option value="ROMANTIC">细腻浪漫</option></select></label>
                            <label><span>规格</span><select v-model="form.aspectRatio"><option value="1:1">1:1 方形</option><option value="4:3">4:3 横向</option><option value="3:4">3:4 竖向</option><option value="16:9">16:9 横屏</option><option value="9:16">9:16 竖屏</option></select></label>
                        </div>

                        <button class="generate-action" type="submit" :disabled="form.prompt.trim().length < 2 || generating"><span><small>{{ form.resolution }} · 1 张设计提案</small><b>{{ generating ? '正在生成专业提案' : '生成我的美甲设计' }}</b></span><i>{{ generating ? '···' : '↗' }}</i></button>
                        <p v-if="errorMessage" class="error-copy">{{ errorMessage }}</p>
                    </form>

                    <section class="result-stage" aria-live="polite">
                        <header><div><span>DESIGN OUTPUT</span><b>{{ task ? task.title || '本次设计提案' : '等待你的灵感' }}</b></div><small v-if="task">{{ task.aspectRatio }} · {{ task.resolution }}</small></header>
                        <div v-if="generating" class="public-generating"><div class="nail-loader"><i v-for="item in 5" :key="item"></i></div><b>正在研色、构图与模拟材质</b><p>请保留此页面，完成后会自动展示结果。</p></div>
                        <div v-else-if="task?.results?.length" class="public-results"><figure v-for="result in task.results" :key="result.id"><img :src="result.url" alt="AI 生成的美甲设计" /><figcaption><span>NAILFORM PROPOSAL · {{ result.width }} × {{ result.height }}</span><a :href="result.url" target="_blank" rel="noopener">查看原图 ↗</a></figcaption></figure></div>
                        <div v-else class="result-placeholder"><div class="placeholder-grid"><figure v-for="image in sampleImages.slice(0, 2)" :key="image"><img :src="image" alt="美甲设计示例" /></figure></div><div><span>YOUR DESIGN WILL APPEAR HERE</span><b>专业提案会以高清图片呈现</b><p>你可以使用纯文字创作，也可以上传有权使用的参考图片进行改款。</p></div></div>
                    </section>
                </div>
            </section>

            <section id="craft" class="craft-section">
                <div><span>01</span><h3>专精美甲语义</h3><p>提示系统理解甲型、甲面结构、猫眼磁吸、微法式、透色凝胶与真实手部比例。</p></div>
                <div><span>02</span><h3>紧跟设计趋势</h3><p>趋势胶囊不是简单换色，会同步调整材质、留白、重点甲与拍摄呈现方式。</p></div>
                <div><span>03</span><h3>可持续迭代</h3><p>后台完整保留创作记录、参考资产与生成结果，方便审阅、采纳和继续改款。</p></div>
            </section>
        </main>

        <footer class="site-footer"><a class="brand" href="#top"><span>N</span><div><b>NAILFORM</b><small>AI NAIL ATELIER</small></div></a><p>AI 提供设计灵感，实际落地请由专业美甲师确认工艺。</p><span>© 2026 NAILFORM</span></footer>
    </div>
</template>

<script lang="ts" setup>
import { createPublicNailTask, getPublicNailTask, uploadNailReference, type PublicNailGeneratePayload } from '@/api/nail'

definePageMeta({ layout: 'blank' })
useHead({ title: 'NAILFORM · AI 美甲设计工作室', meta: [{ name: 'description', content: '专精美甲设计与趋势的 AI 创作平台' }] })

const runtimeConfig = useRuntimeConfig()
const apiOrigin = String(runtimeConfig.public.apiUrl || 'http://127.0.0.1:8082').replace(/\/$/, '')
const sampleImages = [
    `${apiOrigin}/api/uploads/nail/results/20260812/ee008525-30e6-41b2-a3d2-5d04e2cfc751.jpg`,
    `${apiOrigin}/api/uploads/nail/results/20260812/cd3134d5-417b-41b2-b120-5d7b017258a4.jpg`,
    `${apiOrigin}/api/uploads/nail/results/20260812/6d9b3d91-6899-44d0-bc2e-fcb5394bf637.jpg`
]
const sampleTitles = ['ROSE VELVET', 'EDITORIAL HAND', 'QUIET LUXURY']
const presets = [
    { value: 'ROSE_VELVET' as const, label: '玫瑰丝绒', note: '烟粉猫眼 · 冷银细线', colors: ['#9f6673', '#d7bec4', '#c2c4c8'], palette: '烟粉、冷银、奶白', finish: 'VELVET_CAT_EYE' as const },
    { value: 'SEA_GLASS' as const, label: '海盐玻璃', note: '雾蓝透色 · 冰玻璃光', colors: ['#779fa4', '#d5e1e1', '#b6c1c4'], palette: '雾蓝、海盐白、透明玻璃', finish: 'JELLY' as const },
    { value: 'BUTTER_MICRO_FRENCH' as const, label: '奶油微法式', note: '柔奶黄 · 极细法式边', colors: ['#d1b65c', '#eee8d8', '#cbb88e'], palette: '柔奶黄、象牙白、浅沙色', finish: 'MICRO_FRENCH' as const },
    { value: 'MIXED_METAL' as const, label: '混合金属', note: '冷银交错 · 镜面细节', colors: ['#a9adb0', '#82734f', '#2a2c2f'], palette: '冷银、钛灰、少量香槟金', finish: 'CHROME' as const },
    { value: 'AURORA_MAGNETIC' as const, label: '极光磁场', note: '雾紫偏光 · 深层磁吸', colors: ['#6e678e', '#7894a2', '#c8c6d4'], palette: '雾紫、冰蓝、珍珠白', finish: 'VELVET_CAT_EYE' as const }
]
const form = reactive<PublicNailGeneratePayload>({ taskType: 'TEXT_TO_IMAGE', prompt: '', creativeMode: 'ON_HAND', nailShape: 'SHORT_ALMOND', finish: 'VELVET_CAT_EYE', designStyle: 'QUIET_LUXURY', layoutStyle: 'TWO_ACCENTS', trendPreset: 'ROSE_VELVET', referenceStrategy: 'REINTERPRET', colorPalette: '烟粉、冷银、奶白', aspectRatio: '1:1', resolution: '2K', outputCount: 1 })
const reference = ref<any>()
const task = ref<any>()
const uploading = ref(false)
const generating = ref(false)
const errorMessage = ref('')
let pollTimer: number | undefined

const selectPreset = (preset: typeof presets[number]) => { form.trendPreset = preset.value; form.colorPalette = preset.palette; form.finish = preset.finish }
const uploadReference = async (event: Event) => {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) return
    uploading.value = true
    errorMessage.value = ''
    try { reference.value = await uploadNailReference(file); form.referenceAssetId = reference.value.id; form.taskType = 'IMAGE_TO_IMAGE' }
    catch (error: any) { errorMessage.value = String(error || '参考图上传失败') }
    finally { uploading.value = false; input.value = '' }
}
const removeReference = () => { reference.value = undefined; delete form.referenceAssetId; form.taskType = 'TEXT_TO_IMAGE' }
const generate = async () => {
    if (form.prompt.trim().length < 2 || generating.value) return
    errorMessage.value = ''
    generating.value = true
    task.value = undefined
    try { const access = await createPublicNailTask({ ...form, prompt: form.prompt.trim() }); await pollTask(Number(access.id), access.accessToken) }
    catch (error: any) { generating.value = false; errorMessage.value = String(error || '生成失败，请稍后重试') }
}
const pollTask = async (id: number, token: string) => {
    const refresh = async () => {
        task.value = await getPublicNailTask(id, token)
        if (!['QUEUED', 'RUNNING'].includes(task.value.status)) {
            generating.value = false
            if (pollTimer) window.clearInterval(pollTimer)
            pollTimer = undefined
            if (!task.value.results?.length) errorMessage.value = task.value.errorMessage || '没有生成可用结果，请调整描述后再试'
        }
    }
    await refresh()
    if (generating.value) pollTimer = window.setInterval(refresh, 2500)
}
onBeforeUnmount(() => { if (pollTimer) window.clearInterval(pollTimer) })
</script>

<style lang="scss" scoped>
.nail-public-page { min-height: 100vh; background: #111316; color: #eceff2; font-family: Inter, 'PingFang SC', 'Microsoft YaHei', sans-serif; }
.site-header { position: sticky; top: 0; z-index: 20; display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; min-height: 70px; padding: 0 clamp(22px,4vw,68px); border-bottom: 1px solid #2b2e33; background: rgba(17,19,22,.96); backdrop-filter: blur(14px); }
.brand { display: flex; align-items: center; gap: 10px; width: max-content; color: #f2f3f5; }.brand > span { display: grid; width: 34px; height: 34px; place-items: center; border: 1px solid #d69ba8; border-radius: 50%; color: #d69ba8; font-family: Georgia,serif; font-size: 17px; }.brand div { display: grid; gap: 1px; }.brand b { font-family: Georgia,serif; font-size: 14px; font-weight: 500; letter-spacing: .12em; }.brand small { color: #777e87; font-size: 7px; letter-spacing: .16em; }
.site-header nav { display: flex; gap: 32px; }.site-header nav a { color: #aeb3ba; font-size: 12px; }.start-link { justify-self: end; color: #e8e9ec; font-size: 12px; }.start-link span { margin-left: 7px; color: #d69ba8; }
.editorial-hero { display: grid; grid-template-columns: .85fr 1.15fr; gap: clamp(28px,5vw,86px); max-width: 1480px; min-height: 720px; margin: 0 auto; padding: 70px clamp(22px,4vw,68px) 76px; }
.hero-copy { display: flex; flex-direction: column; justify-content: center; max-width: 580px; }.eyebrow { color: #d09aa6; font-size: 9px; letter-spacing: .22em; }.hero-copy h1 { margin: 18px 0 24px; color: #f3f4f6; font-family: 'Noto Serif SC','Songti SC',Georgia,serif; font-size: clamp(36px,4.2vw,58px); font-weight: 480; line-height: 1.24; letter-spacing: -.035em; }.hero-copy > p { max-width: 520px; margin: 0; color: #a0a6ae; font-size: 14px; line-height: 2; }.hero-actions { display: flex; align-items: center; gap: 22px; margin-top: 34px; }.hero-actions a { padding: 13px 17px; border: 1px solid #d69ba8; border-radius: 8px; background: #d69ba8; color: #17191c; font-size: 12px; font-weight: 650; }.hero-actions span { max-width: 190px; color: #727981; font-size: 9px; line-height: 1.6; }
.hero-gallery { display: grid; grid-template-columns: 1.1fr .9fr; grid-template-rows: 1fr 1fr; gap: 10px; min-width: 0; }.hero-gallery figure { position: relative; min-height: 0; margin: 0; overflow: hidden; border-radius: 10px; background: #1d2024; }.hero-gallery .sample-1 { grid-row: 1 / 3; }.hero-gallery img { width: 100%; height: 100%; object-fit: cover; }.hero-gallery figcaption { position: absolute; right: 9px; bottom: 9px; left: 9px; padding: 8px 10px; border-radius: 6px; background: rgba(17,19,22,.78); color: #d4d7dc; font-size: 8px; letter-spacing: .12em; }
.direction-strip { display: grid; grid-template-columns: 190px repeat(5,minmax(145px,1fr)); gap: 8px; max-width: 1480px; margin: 0 auto; padding: 20px clamp(22px,4vw,68px); border-top: 1px solid #292c31; border-bottom: 1px solid #292c31; }.direction-strip > div { display: grid; align-content: center; gap: 4px; }.direction-strip > div span { color: #d09aa6; font-size: 8px; letter-spacing: .16em; }.direction-strip > div b { font-family: 'Noto Serif SC','Songti SC',serif; font-size: 16px; font-weight: 500; }.direction-strip button { display: grid; grid-template-columns: 42px minmax(0,1fr); align-items: center; gap: 9px; padding: 9px; border: 1px solid #34383e; border-radius: 8px; background: #191b1f; color: #dfe1e5; text-align: left; }.direction-strip button.active { border-color: #c48d99; }.palette { display: flex; width: 42px; height: 34px; overflow: hidden; border-radius: 6px; }.palette i { flex: 1; }.direction-strip button > span:last-child { display: grid; min-width: 0; gap: 3px; }.direction-strip b,.direction-strip small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.direction-strip b { font-size: 10px; }.direction-strip small { color: #777e86; font-size: 8px; }
.studio-section { max-width: 1480px; margin: 0 auto; padding: 96px clamp(22px,4vw,68px); }.section-title { display: flex; align-items: flex-end; justify-content: space-between; gap: 30px; margin-bottom: 28px; }.section-title span { color: #d09aa6; font-size: 9px; letter-spacing: .18em; }.section-title h2 { margin: 6px 0 0; font-family: 'Noto Serif SC','Songti SC',serif; font-size: 31px; font-weight: 500; }.section-title p { max-width: 420px; margin: 0; color: #858c94; font-size: 12px; line-height: 1.8; }
.studio-grid { display: grid; grid-template-columns: 440px minmax(0,1fr); min-height: 680px; overflow: hidden; border: 1px solid #34383e; border-radius: 14px; background: #191b1f; }.composer-card { padding: 26px; border-right: 1px solid #34383e; }.mode-switch { display: grid; grid-template-columns: 1fr 1fr; margin-bottom: 24px; border: 1px solid #363a40; border-radius: 9px; }.mode-switch button { display: grid; gap: 3px; padding: 11px; border: 0; border-right: 1px solid #363a40; background: transparent; color: #a9afb7; text-align: left; }.mode-switch button:last-child { border-right: 0; }.mode-switch button.active { background: #24272c; color: #f0f1f3; }.mode-switch b { font-size: 11px; }.mode-switch small { color: #747b84; font-size: 8px; }
.prompt-field { position: relative; }.prompt-field label { color: #d09aa6; font-size: 8px; letter-spacing: .16em; }.prompt-field > span { position: absolute; top: 0; right: 0; color: #697079; font-size: 8px; }.prompt-field textarea { width: 100%; min-height: 158px; margin-top: 10px; padding: 14px; resize: vertical; border: 1px solid #363a40; border-radius: 9px; outline: 0; background: #141619; color: #e5e7ea; font-family: inherit; font-size: 12px; line-height: 1.8; }.prompt-field textarea::placeholder { color: #5f666f; }
.reference-field { margin-top: 12px; }.reference-upload { display: grid; grid-template-columns: 34px minmax(0,1fr); align-items: center; gap: 10px; padding: 11px; border: 1px dashed #3c4147; border-radius: 9px; color: #9ea4ac; cursor: pointer; }.reference-upload input { position: absolute; width: 1px; height: 1px; opacity: 0; }.reference-upload > span:first-of-type { display: grid; width: 34px; height: 34px; place-items: center; border-radius: 8px; background: #25282d; color: #d09aa6; }.reference-upload > span:last-child { display: grid; gap: 2px; }.reference-upload b { color: #cfd2d7; font-size: 10px; }.reference-upload small { color: #686f77; font-size: 8px; }.reference-ready { display: grid; grid-template-columns: 48px minmax(0,1fr) auto; align-items: center; gap: 10px; padding: 8px; border: 1px solid #4b3a3f; border-radius: 9px; background: #211b1e; }.reference-ready img { width: 48px; height: 48px; border-radius: 7px; object-fit: cover; }.reference-ready span { display: grid; min-width: 0; gap: 3px; }.reference-ready b,.reference-ready small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.reference-ready b { font-size: 10px; }.reference-ready small { color: #7f777c; font-size: 8px; }.reference-ready button { border: 0; background: transparent; color: #c78391; font-size: 9px; }
.parameter-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 11px; margin-top: 20px; }.parameter-grid label { display: grid; gap: 5px; }.parameter-grid label span { color: #858c94; font-size: 9px; }.parameter-grid select { width: 100%; padding: 9px; border: 1px solid #363a40; border-radius: 8px; outline: 0; background: #141619; color: #d7dae0; font-family: inherit; font-size: 10px; }
.generate-action { display: flex; align-items: center; justify-content: space-between; width: 100%; margin-top: 22px; padding: 13px 15px; border: 1px solid #d39aa6; border-radius: 9px; background: #d39aa6; color: #181a1d; text-align: left; }.generate-action:disabled { cursor: not-allowed; opacity: .38; }.generate-action > span { display: grid; gap: 1px; }.generate-action small { font-size: 8px; }.generate-action b { font-size: 12px; }.generate-action i { font-size: 17px; font-style: normal; }.error-copy { margin: 10px 0 0; color: #df8e96; font-size: 10px; line-height: 1.6; }
.result-stage { min-width: 0; padding: 22px; background: #15171a; }.result-stage > header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 14px; }.result-stage > header div { display: grid; gap: 3px; }.result-stage > header span { color: #d09aa6; font-size: 8px; letter-spacing: .14em; }.result-stage > header b { font-family: 'Noto Serif SC','Songti SC',serif; font-size: 16px; font-weight: 500; }.result-stage > header small { color: #767d85; font-size: 9px; }
.result-placeholder { display: grid; grid-template-rows: minmax(0,1fr) auto; min-height: 590px; overflow: hidden; border: 1px solid #30343a; border-radius: 10px; background: #191b1f; }.placeholder-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1px; min-height: 0; background: #30343a; }.placeholder-grid figure { margin: 0; min-height: 0; background: #22252a; }.placeholder-grid img { width: 100%; height: 100%; object-fit: cover; opacity: .72; filter: saturate(.8); }.result-placeholder > div:last-child { padding: 18px; }.result-placeholder > div:last-child span { color: #b8828e; font-size: 8px; letter-spacing: .14em; }.result-placeholder > div:last-child b { display: block; margin: 6px 0; font-size: 12px; }.result-placeholder > div:last-child p { margin: 0; color: #747b83; font-size: 9px; }
.public-generating { display: grid; min-height: 590px; place-content: center; place-items: center; border: 1px solid #30343a; border-radius: 10px; background: #191b1f; }.nail-loader { display: flex; align-items: flex-end; gap: 6px; height: 66px; margin-bottom: 20px; }.nail-loader i { width: 17px; height: 46px; border: 1px solid #c88f9b; border-radius: 10px 10px 6px 6px; background: #35282c; animation: nailPulse 1.2s ease-in-out infinite alternate; }.nail-loader i:nth-child(2),.nail-loader i:nth-child(4){height:58px;animation-delay:.15s}.nail-loader i:nth-child(3){height:64px;animation-delay:.3s}@keyframes nailPulse{to{background:#a56e7a;transform:translateY(-5px)}}.public-generating b { font-size: 12px; }.public-generating p { margin: 6px 0 0; color: #727981; font-size: 9px; }
.public-results { display: grid; gap: 12px; }.public-results figure { margin: 0; overflow: hidden; border: 1px solid #30343a; border-radius: 10px; background: #191b1f; }.public-results img { display: block; width: 100%; max-height: 650px; object-fit: contain; }.public-results figcaption { display: flex; justify-content: space-between; padding: 12px 14px; color: #7b828a; font-size: 8px; letter-spacing: .08em; }.public-results a { color: #d09aa6; }
.craft-section { display: grid; grid-template-columns: repeat(3,1fr); max-width: 1480px; margin: 0 auto; padding: 0 clamp(22px,4vw,68px) 100px; }.craft-section > div { padding: 30px; border-top: 1px solid #34383e; border-right: 1px solid #34383e; }.craft-section > div:last-child { border-right: 0; }.craft-section span { color: #d09aa6; font-family: Georgia,serif; font-size: 11px; }.craft-section h3 { margin: 15px 0 9px; font-family: 'Noto Serif SC','Songti SC',serif; font-size: 18px; font-weight: 500; }.craft-section p { margin: 0; color: #858c94; font-size: 11px; line-height: 1.8; }
.site-footer { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; min-height: 110px; padding: 0 clamp(22px,4vw,68px); border-top: 1px solid #2b2e33; }.site-footer p { margin: 0; color: #666d75; font-size: 9px; }.site-footer > span { justify-self: end; color: #5d646c; font-size: 8px; }
a,button { transition: border-color .18s ease,background-color .18s ease,opacity .18s ease; }.direction-strip button:hover,.mode-switch button:hover { border-color: #8e6871; }.nail-public-page :focus-visible { outline: 2px solid #d69ba8; outline-offset: 2px; }
@media(max-width:1100px){.site-header{grid-template-columns:1fr auto}.site-header nav{display:none}.editorial-hero{grid-template-columns:1fr;min-height:auto}.hero-copy{max-width:720px}.hero-gallery{min-height:560px}.direction-strip{grid-template-columns:repeat(5,minmax(160px,1fr));overflow-x:auto}.direction-strip>div{display:none}.studio-grid{grid-template-columns:380px minmax(0,1fr)}}
@media(max-width:760px){.site-header{padding:0 18px}.start-link{font-size:0}.start-link span{font-size:16px}.editorial-hero,.studio-section{padding-right:18px;padding-left:18px}.hero-gallery{min-height:430px}.hero-actions{align-items:flex-start;flex-direction:column}.section-title{align-items:flex-start;flex-direction:column}.studio-grid{grid-template-columns:1fr}.composer-card{border-right:0;border-bottom:1px solid #34383e}.result-stage{padding:14px}.craft-section{grid-template-columns:1fr;padding-right:18px;padding-left:18px}.craft-section>div{border-right:0}.site-footer{grid-template-columns:1fr;gap:18px;padding:28px 18px}.site-footer>span{justify-self:start}}
@media(prefers-reduced-motion:reduce){*{scroll-behavior:auto!important}.nail-loader i{animation:none}a,button{transition:none}}
</style>
