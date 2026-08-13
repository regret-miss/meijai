<template>
    <main class="nail-login">
        <section class="nail-login__story" aria-labelledby="login-title">
            <span class="story-seal" aria-hidden="true">湘</span>
            <p>指尖湘韵 · AI 创作台</p>
            <h1 id="login-title">一念落纸<br />灵感续作</h1>
            <span>登录后，首页留下的美甲设计意图会自动回到 AI 创作工作台。</span>
        </section>

        <section class="nail-login__card" aria-labelledby="form-title">
            <header>
                <span class="card-seal" aria-hidden="true">甲</span>
                <div>
                    <h2 id="form-title">入席创作</h2>
                    <p>输入账号完成验证，系统会自动识别您的访问身份。</p>
                </div>
            </header>

            <div v-if="creationTicket" class="creation-note">
                <b>本次设计意图已留存</b>
                <span>认证完成后会自动回到 AI 创作台。</span>
            </div>

            <el-form ref="formRef" :model="formData" :rules="rules" class="nail-login__form" label-position="top" size="large">
                <el-form-item prop="account" label="">
                    <el-input v-model.trim="formData.account" placeholder="请输入账号" autocomplete="username" @keyup.enter="handleEnter">
                        <template #prefix><icon name="el-icon-User" /></template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password" label="">
                    <el-input ref="passwordRef" v-model="formData.password" show-password placeholder="请输入登录密码" autocomplete="current-password" @keyup.enter="handleLogin">
                        <template #prefix><icon name="el-icon-Lock" /></template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="code" label="">
                    <div class="captcha-row">
                        <el-input v-model.trim="formData.code" placeholder="请输入验证码" autocomplete="off" @keyup.enter="handleLogin">
                            <template #prefix><icon name="local-icon-anquan" /></template>
                        </el-input>
                        <button type="button" class="captcha-image" aria-label="刷新验证码" @click="getLoginCaptcha"><img :src="codeImg" alt="登录验证码" /></button>
                    </div>
                </el-form-item>
            </el-form>

            <el-checkbox v-model="remAccount" label="记住账号" class="remember-account" />
            <el-button class="login-submit" type="primary" size="large" :loading="isLock" @click="lockLogin">{{ isLock ? '正在核验身份' : '登录并继续创作' }}</el-button>
            <a class="back-link" :href="publicHomeUrl">返回首页</a>
        </section>
    </main>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, shallowRef } from 'vue'
import type { InputInstance, FormInstance } from 'element-plus'
import cache from '@/utils/cache'
import request from '@/utils/request'
import { ACCOUNT_KEY } from '@/enums/cacheEnums'
import { useLockFn } from '@/hooks/useLockFn'

const passwordRef = shallowRef<InputInstance>()
const formRef = shallowRef<FormInstance>()
const route = useRoute()
const remAccount = ref(false)
const codeImg = ref('')
const creationTicket = computed(() => typeof route.query.creation_ticket === 'string' ? route.query.creation_ticket : '')
const publicHomeUrl = computed(() => `${location.protocol}//${location.hostname}:8082/nail-site/首页.html`)
const formData = reactive({ account: '', password: '', code: '', uuid: '' })
const rules = computed(() => ({
    account: [{ required: true, message: '请输入账号', trigger: ['blur', 'change'] }],
    password: [{ required: true, message: '请输入密码', trigger: ['blur', 'change'] }],
    code: [{ required: true, message: '请输入验证码', trigger: ['blur', 'change'] }]
}))

const getLoginCaptcha = async () => {
    const data = await request.get({ url: '/nail/auth/captcha' }, { withToken: false })
    formData.uuid = data.uuid
    codeImg.value = data.img
}

const handleEnter = () => {
    if (!formData.password) return passwordRef.value?.focus()
    void handleLogin()
}

const finishBridge = async () => {
    if (!creationTicket.value) return false
    const ticket = encodeURIComponent(creationTicket.value)
    const data = await request.post({ url: `/nail/creation-bridge/complete-session?ticket=${ticket}` }, { withToken: false })
    if (!data?.returnUrl) throw new Error('登录成功，但未取得创作回填地址')
    window.location.replace(data.returnUrl)
    return true
}

const handleLogin = async () => {
    await formRef.value?.validate()
    cache.set(ACCOUNT_KEY, { remember: remAccount.value, account: remAccount.value ? formData.account : '' })
    try {
        await request.post({
        url: '/nail/auth/login',
            params: { username: formData.account, password: formData.password, code: formData.code, uuid: formData.uuid }
        }, { withToken: false })
        if (await finishBridge()) return
        window.location.assign(`${location.protocol}//${location.hostname}:8082/nail-site/AI.html`)
    } catch (error) {
        formData.code = ''
        await getLoginCaptcha()
        throw error
    }
}

const { isLock, lockFn: lockLogin } = useLockFn(handleLogin)

onMounted(() => {
    const value = cache.get(ACCOUNT_KEY)
    if (value?.remember) {
        remAccount.value = value.remember
        formData.account = value.account
    }
    void getLoginCaptcha()
})
</script>

<style lang="scss" scoped>
.nail-login {
    --paper: #f6f1e7;
    --paper-light: #fffdf8;
    --ink: #2a2823;
    --muted: #6d6559;
    --cinnabar: #9e3027;
    --cinnabar-dark: #74221d;
    --jade: #456357;
    --line: rgba(86, 70, 50, 0.18);
    min-height: 100dvh;
    position: relative;
    isolation: isolate;
    overflow: hidden;
    display: grid;
    grid-template-columns: minmax(340px, .94fr) minmax(440px, .78fr);
    align-items: center;
    gap: clamp(64px, 10vw, 164px);
    padding: clamp(44px, 7vw, 88px) max(7.5vw, 52px);
    color: var(--ink);
    background:
        linear-gradient(90deg, rgba(246, 241, 231, .98) 0 45%, rgba(246, 241, 231, .78) 66%, rgba(246, 241, 231, .58)),
        url('./images/login_bg.png') center / cover;

    &::before {
        position: absolute;
        z-index: -1;
        top: 11%;
        left: 5.8%;
        width: min(31vw, 460px);
        aspect-ratio: 1;
        border: 1px solid rgba(158, 48, 39, .14);
        border-radius: 50%;
        content: '';
        box-shadow: 0 0 0 28px rgba(158, 48, 39, .025), 0 0 0 62px rgba(158, 48, 39, .018);
    }

    &::after {
        position: absolute;
        z-index: -1;
        right: 5%;
        bottom: 7%;
        width: min(38vw, 520px);
        height: 1px;
        background: rgba(86, 70, 50, .16);
        content: '';
        transform: rotate(-31deg);
        transform-origin: right;
    }

    &__story { max-width: 510px; padding: 22px 0 22px 38px; border-left: 1px solid rgba(158, 48, 39, .34); }
    &__story > p { margin: 0 0 24px; color: var(--cinnabar); font-family: Georgia, 'Times New Roman', serif; font-size: 11px; font-weight: 600; letter-spacing: .17em; }
    &__story h1 { max-width: 6.1em; margin: 0; font-family: STKaiti, KaiTi, serif; font-size: clamp(52px, 5vw, 76px); font-weight: 600; line-height: 1.04; letter-spacing: -.04em; text-wrap: balance; }
    &__story > span:last-child { display: block; max-width: 31em; margin-top: 28px; color: var(--muted); font-family: 'Microsoft YaHei', sans-serif; font-size: 14px; line-height: 2; }

    &__card { position: relative; width: min(100%, 520px); justify-self: end; padding: 42px; border: 1px solid var(--line); border-radius: 16px; background: rgba(255, 253, 248, .94); box-shadow: 0 30px 72px rgba(76, 57, 41, .15), 0 2px 3px rgba(76, 57, 41, .04); }
    &__card::before { position: absolute; top: 0; left: 42px; width: 86px; height: 3px; border-radius: 0 0 4px 4px; background: var(--cinnabar); content: ''; }
    &__card > header { display: grid; grid-template-columns: 48px 1fr; gap: 15px; align-items: center; margin-bottom: 27px; }
    &__card h2 { margin: 0; font-family: STKaiti, KaiTi, serif; font-size: 32px; font-weight: 600; line-height: 1.15; letter-spacing: .02em; }
    &__card header p { margin: 6px 0 0; color: var(--muted); font-size: 13px; line-height: 1.6; }
    &__form :deep(.el-form-item) { margin-bottom: 18px; }
    &__form :deep(.el-form-item.is-error) { margin-bottom: 35px; }
    &__form :deep(.el-form-item__label) { display: flex; align-items: center; gap: 6px; padding-bottom: 8px; color: #413b32; font-size: 13px; font-weight: 700; line-height: 1.3; }
    &__form :deep(.el-form-item__label::before) { color: var(--cinnabar); font-size: 11px; }
    &__form :deep(.el-form-item__error) { padding-top: 5px; color: var(--cinnabar); font-size: 11px; line-height: 1.35; }
    &__form :deep(.el-input__wrapper) { min-height: 50px; padding: 1px 14px; border: 1px solid var(--line); border-radius: 9px; background: rgba(255, 253, 248, .78); box-shadow: none; transition: border-color .18s ease, background .18s ease, box-shadow .18s ease; }
    &__form :deep(.el-input__wrapper.is-focus) { border-color: var(--cinnabar); box-shadow: 0 0 0 3px rgba(158, 48, 39, .13); }
    &__form :deep(.el-input__inner) { color: var(--ink); }
    &__form :deep(.el-input__prefix) { color: var(--cinnabar); }
    &__form :deep(.el-input__inner::placeholder) { color: #9a9388; }
}

.story-seal, .card-seal { display: grid; place-items: center; color: var(--paper-light); background: var(--cinnabar); font-family: STKaiti, KaiTi, serif; }
.story-seal { width: 56px; height: 56px; margin: 0 0 26px -67px; border: 5px solid var(--paper); border-radius: 50%; font-size: 31px; box-shadow: 0 7px 18px rgba(116, 34, 29, .12); }
.card-seal { width: 48px; height: 48px; border-radius: 10px; font-size: 25px; box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .24); }
.creation-note { display: grid; gap: 4px; margin: 0 0 19px; padding: 12px 14px; border: 1px solid rgba(80, 107, 96, .3); border-radius: 9px; color: var(--jade); background: rgba(80, 107, 96, .065); font-size: 12px; line-height: 1.45; }.creation-note span { color: #61786f; font-size: 11px; }
.captcha-row { display: grid; grid-template-columns: minmax(0, 1fr) 118px; gap: 12px; width: 100%; }.captcha-image { position: relative; overflow: hidden; min-height: 50px; border: 1px solid var(--line); border-radius: 9px; background: var(--paper); cursor: pointer; transition: border-color .18s ease, transform .18s ease, box-shadow .18s ease; }.captcha-image::after { position: absolute; right: 7px; bottom: 5px; padding: 2px 5px; border-radius: 3px; color: rgba(42, 40, 35, .68); background: rgba(255, 253, 248, .78); content: '换一张'; font-size: 10px; opacity: 0; transform: translateY(3px); transition: opacity .18s ease, transform .18s ease; }.captcha-image:hover { border-color: var(--cinnabar); box-shadow: 0 5px 12px rgba(76, 57, 41, .1); transform: translateY(-1px); }.captcha-image:hover::after, .captcha-image:focus-visible::after { opacity: 1; transform: translateY(0); }.captcha-image img { display: block; width: 100%; height: 50px; object-fit: cover; }
.remember-account { margin: 2px 0 20px; color: var(--muted); }.login-submit { width: 100%; min-height: 52px; border: 0; border-radius: 9px; background: var(--cinnabar); box-shadow: 0 10px 18px rgba(116, 34, 29, .15); font-weight: 700; letter-spacing: .04em; transition: background .18s ease, transform .18s ease, box-shadow .18s ease; }.login-submit:hover { background: var(--cinnabar-dark); box-shadow: 0 13px 23px rgba(116, 34, 29, .2); transform: translateY(-1px); }.login-submit:active { transform: translateY(0); }.back-link { display: block; width: max-content; margin: 17px auto 0; color: var(--muted); font-size: 12px; text-decoration: none; text-underline-offset: 4px; }.back-link:hover { color: var(--cinnabar); text-decoration: underline; }
.nail-login :focus-visible { outline: 2px solid var(--cinnabar); outline-offset: 3px; }
.nail-login ::selection { color: var(--paper-light); background: var(--cinnabar); }

@media (max-width: 980px) { .nail-login { grid-template-columns: 1fr; gap: 34px; padding: 46px 28px; }.nail-login__story { max-width: 590px; }.nail-login__card { justify-self: start; width: min(100%, 560px); }.nail-login::before { top: 4%; left: auto; right: -10%; width: 380px; } }
@media (max-width: 560px) { .nail-login { gap: 25px; padding: 28px 16px; }.nail-login__story { padding: 0 0 0 20px; }.nail-login__story h1 { font-size: clamp(42px, 13vw, 58px); }.nail-login__story > span:last-child { margin-top: 20px; font-size: 13px; }.nail-login__card { padding: 30px 21px 26px; }.nail-login__card::before { left: 21px; }.nail-login__card h2 { font-size: 29px; }.story-seal { width: 48px; height: 48px; margin: 0 0 21px -45px; border-width: 4px; font-size: 27px; }.card-seal { width: 44px; height: 44px; }.captcha-row { grid-template-columns: minmax(0, 1fr) 104px; gap: 10px; }.nail-login::after { display: none; } }

@media (prefers-reduced-motion: reduce) { .nail-login *, .nail-login *::before, .nail-login *::after { scroll-behavior: auto !important; transition-duration: .01ms !important; } }
</style>
