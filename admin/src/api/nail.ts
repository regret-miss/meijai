import request from '@/utils/request'

export type NailTaskStatus =
    | 'QUEUED'
    | 'RUNNING'
    | 'SUCCEEDED'
    | 'PARTIAL_SUCCEEDED'
    | 'FAILED'

export interface NailGeneratePayload {
    taskType: 'TEXT_TO_IMAGE' | 'IMAGE_TO_IMAGE'
    prompt: string
    creativeMode: 'DESIGN_BOARD' | 'ON_HAND'
    nailShape: 'SHORT_ALMOND' | 'SHORT_SQUOVAL' | 'ALMOND' | 'SQUARE' | 'COFFIN'
    finish:
        | 'VELVET_CAT_EYE'
        | 'JELLY'
        | 'CHROME'
        | 'MICRO_FRENCH'
        | 'AURA'
        | 'SCULPTED_GEL'
        | 'GLOSSY_GEL'
    designStyle:
        | 'QUIET_LUXURY'
        | 'KOREAN_CLEAR'
        | 'RUNWAY'
        | 'FUTURISTIC'
        | 'ROMANTIC'
        | 'SWEET_COOL'
    layoutStyle: 'UNIFIED' | 'TWO_ACCENTS' | 'MICRO_FRENCH_LAYOUT' | 'MISMATCHED'
    trendPreset:
        | 'ROSE_VELVET'
        | 'SEA_GLASS'
        | 'BUTTER_MICRO_FRENCH'
        | 'MIXED_METAL'
        | 'AURORA_MAGNETIC'
        | 'KOREAN_SYRUP'
        | 'CUSTOM'
    referenceStrategy: 'REINTERPRET' | 'KEEP_PALETTE' | 'KEEP_LAYOUT' | 'KEEP_TEXTURE'
    colorPalette: string
    aspectRatio: '1:1' | '16:9' | '9:16' | '4:3' | '3:4' | '3:2' | '2:3' | '21:9'
    resolution: '1.5K' | '2K' | '4K'
    outputCount: number
    referenceAssetId?: number
}

export interface NailTaskSummary {
    id: number
    taskType: string
    title: string
    status: NailTaskStatus
    prompt: string
    aspectRatio: string
    resolution: string
    outputCount: number
    resultCount: number
    adoptedCount: number
    referenceAssetId?: number
    creativeMode: 'DESIGN_BOARD' | 'ON_HAND'
    modelCode: string
    errorMessage: string
    createTime: string
    coverUrl: string
}

export interface NailResult {
    id: number
    url: string
    width: number
    height: number
    reviewStatus: 'PENDING' | 'ADOPTED' | 'REJECTED'
    reviewNote: string
    reviewTime: string
    adoptedAssetId?: number
    sort: number
    createTime: string
}

export interface NailAsset {
    id: number
    name: string
    url: string
    smallUrl: string
    originalUrl: string
    downloadUrl: string
    mimeType: string
    fileSize: number
    width: number
    height: number
    source: string
    copyrightStatus: string
    aiUsable: number
    status: string
    failureReason: string
    category: string
    style: string
    colorFamily: string
    nailShape: string
    craft: string
    tags: string[]
    originalFilename: string
    sha256: string
    prompt: string
    createTime: string
    sourceTaskId?: number
    sourceResultId?: number
}

export interface NailAssetOption { value: string; label: string }
export interface NailAssetOptions {
    categories: NailAssetOption[]
    styles: NailAssetOption[]
    colors: NailAssetOption[]
    shapes: NailAssetOption[]
    crafts: NailAssetOption[]
    tags: string[]
}

export interface NailTaskDetail extends NailTaskSummary {
    provider: string
    templateVersion: string
    startedTime: string
    finishedTime: string
    designSpec: Pick<
        NailGeneratePayload,
        | 'creativeMode'
        | 'nailShape'
        | 'finish'
        | 'designStyle'
        | 'layoutStyle'
        | 'trendPreset'
        | 'referenceStrategy'
        | 'colorPalette'
    >
    referenceAsset?: NailAsset
    results: NailResult[]
}

export const nailAssetList = (params?: any) => request.get({ url: '/nail/asset/list', params })
export const nailAssetOptions = () => request.get({ url: '/nail/asset/options' })
export const nailAssetDetail = (params: { id: number }) => request.get({ url: '/nail/asset/detail', params })
export const nailAssetDelete = (data: { ids: number[] }) => request.post({ url: '/nail/asset/delete', data })
export const nailAssetBatchDelete = (data: { ids: number[] }) => request.post({ url: '/nail/asset/batch-delete', data })
export const nailAssetUpdate = (params: {
    id: number
    name: string
    copyrightStatus: string
    aiUsable: number
    category: string
    style: string
    colorFamily: string
    nailShape: string
    craft: string
    tags: string
}) => request.post({ url: '/nail/asset/update', data: params })
export const nailAssetUpload = (data: FormData) =>
    request.post({
        url: '/nail/asset/upload',
        data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })

export const nailTaskCreate = (params: NailGeneratePayload) =>
    request.post({ url: '/nail/ai/task/create', params })
export const nailTaskList = (params?: any) => request.get({ url: '/nail/ai/task/list', params })
export const nailTaskDetail = (params: { id: number }) =>
    request.get({ url: '/nail/ai/task/detail', params })
export const nailTaskRename = (params: { id: number; title: string }) =>
    request.post({ url: '/nail/ai/task/rename', params })
export const nailTaskStats = () => request.get({ url: '/nail/ai/stats' })
export const nailResultAdopt = (params: { id: number }) =>
    request.post({ url: '/nail/ai/result/adopt', params })
export const nailResultReject = (params: { id: number; note: string }) =>
    request.post({ url: '/nail/ai/result/reject', params })
