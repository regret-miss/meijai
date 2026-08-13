export interface PublicNailGeneratePayload {
    taskType: 'TEXT_TO_IMAGE' | 'IMAGE_TO_IMAGE'
    prompt: string
    creativeMode: 'DESIGN_BOARD' | 'ON_HAND'
    nailShape: 'SHORT_ALMOND' | 'SHORT_SQUOVAL' | 'ALMOND' | 'SQUARE' | 'COFFIN'
    finish: 'VELVET_CAT_EYE' | 'JELLY' | 'CHROME' | 'MICRO_FRENCH' | 'AURA' | 'SCULPTED_GEL' | 'GLOSSY_GEL'
    designStyle: 'QUIET_LUXURY' | 'KOREAN_CLEAR' | 'RUNWAY' | 'FUTURISTIC' | 'ROMANTIC' | 'SWEET_COOL'
    layoutStyle: 'UNIFIED' | 'TWO_ACCENTS' | 'MICRO_FRENCH_LAYOUT' | 'MISMATCHED'
    trendPreset: 'ROSE_VELVET' | 'SEA_GLASS' | 'BUTTER_MICRO_FRENCH' | 'MIXED_METAL' | 'AURORA_MAGNETIC' | 'KOREAN_SYRUP' | 'CUSTOM'
    referenceStrategy: 'REINTERPRET' | 'KEEP_PALETTE' | 'KEEP_LAYOUT' | 'KEEP_TEXTURE'
    colorPalette: string
    aspectRatio: '1:1' | '16:9' | '9:16' | '4:3' | '3:4'
    resolution: '1.5K' | '2K' | '4K'
    outputCount: number
    referenceAssetId?: number
}

export const uploadNailReference = (file: File) =>
    $request.uploadFile({ url: '/nail/public/reference' }, { file, name: 'file' })

export const createPublicNailTask = (params: PublicNailGeneratePayload) =>
    $request.post({ url: '/nail/public/task/create', params }, { withToken: false })

export const getPublicNailTask = (id: number, token: string) =>
    $request.get({ url: '/nail/public/task/detail', params: { id, token } }, { withToken: false })
