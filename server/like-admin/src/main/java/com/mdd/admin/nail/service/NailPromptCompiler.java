package com.mdd.admin.nail.service;

import com.mdd.admin.nail.dto.NailGenerateRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NailPromptCompiler {

    /**
     * 模型档案：不同代际的 Seedream 对提示词的敏感度不同，
     * 4.0/4.5 需要更详细的结构化描述，5.0 Pro/Lite 理解力强、偏好精简可视化的锚点。
     */
    public enum Profile {
        SEEDREAM_4, SEEDREAM_4_5, SEEDREAM_5_LITE, SEEDREAM_5_PRO;

        public static Profile resolve(String modelCode, String configured) {
            if (configured != null && !configured.isBlank() && !"auto".equalsIgnoreCase(configured.trim())) {
                switch (configured.trim().toLowerCase()) {
                    case "seedream-4": case "seedream4": case "4": return SEEDREAM_4;
                    case "seedream-4-5": case "seedream-4.5": case "4-5": case "4.5": return SEEDREAM_4_5;
                    case "seedream-5-lite": case "seedream5-lite": case "5-lite": case "lite": return SEEDREAM_5_LITE;
                    case "seedream-5-pro": case "seedream5-pro": case "5-pro": case "pro": return SEEDREAM_5_PRO;
                    default: break;
                }
            }
            String code = modelCode == null ? "" : modelCode.toLowerCase();
            if (code.contains("5-0-pro") || code.contains("5.0-pro") || code.contains("seedream-5-pro")) return SEEDREAM_5_PRO;
            if (code.contains("5-0-lite") || code.contains("5.0-lite") || code.contains("seedream-5-lite")) return SEEDREAM_5_LITE;
            if (code.contains("4-5") || code.contains("4.5") || code.contains("seedream-4-5")) return SEEDREAM_4_5;
            return SEEDREAM_4;
        }
    }

    private static final Map<String, String> SHAPES = Map.ofEntries(
            Map.entry("SHORT_ALMOND", "short almond nails with a refined natural apex"),
            Map.entry("SHORT_SQUOVAL", "short squoval nails with clean balanced sidewalls"),
            Map.entry("ALMOND", "medium almond nails with elegant tapered symmetry"),
            Map.entry("SQUARE", "modern square nails with precise straight edges"),
            Map.entry("COFFIN", "medium coffin nails with a slim architectural silhouette"),
            Map.entry("ROUND", "soft round nails with a gentle even curve"),
            Map.entry("STILETTO", "dramatic stiletto nails with a sharp elongated point"),
            Map.entry("LIPSTICK", "lipstick nails with an asymmetric angular tip"));

    private static final Map<String, String> FINISHES = Map.ofEntries(
            Map.entry("VELVET_CAT_EYE", "multi-dimensional velvet magnetic cat-eye gel with a soft diffused light band"),
            Map.entry("JELLY", "translucent glass-like jelly gel with buildable color depth"),
            Map.entry("CHROME", "finely milled mirror chrome with controlled metallic reflection"),
            Map.entry("MICRO_FRENCH", "ultra-thin precision micro-French detailing"),
            Map.entry("AURA", "layered airbrushed aura haze with seamless tonal diffusion"),
            Map.entry("SCULPTED_GEL", "restrained translucent sculpted gel relief with salon-realistic thickness"),
            Map.entry("GLOSSY_GEL", "high-gloss salon gel with crisp curved specular highlights"),
            Map.entry("FRENCH_TIP", "clean classic French tip with a crisp white smile line"),
            Map.entry("MILK_BATH", "milky translucent milk-bath base with soft floating floral flecks"),
            Map.entry("OMBRE", "smooth two-tone ombre gradient with seamless tonal blending"),
            Map.entry("GLITTER", "dense crystal glitter and fine sparkle with controlled light scatter"),
            Map.entry("PEARL", "delicate pearl beads and soft mother-of-pearl sheen"));

    private static final Map<String, String> STYLES = Map.ofEntries(
            Map.entry("QUIET_LUXURY", "quiet luxury, restrained details, premium editorial taste"),
            Map.entry("KOREAN_CLEAR", "contemporary Korean clear nail art, airy spacing, delicate luminous layers"),
            Map.entry("RUNWAY", "fashion-week editorial direction, directional but commercially wearable"),
            Map.entry("FUTURISTIC", "refined futuristic minimalism, optical depth, precise metallic accents"),
            Map.entry("ROMANTIC", "modern romantic restraint, fine botanical or ribbon-like linework"),
            Map.entry("SWEET_COOL", "polished sweet-cool contrast, playful accents balanced by sophisticated color"),
            Map.entry("MINIMALIST", "clean minimalism, generous negative space, one quiet focal detail"),
            Map.entry("Y2K", "playful Y2K nostalgia, metallic accents, chrome and candy tones"),
            Map.entry("COQUETTE", "soft coquette charm, bows, pearls and ribbon details"),
            Map.entry("OLD_MONEY", "understated old-money polish, muted neutrals, quiet shine"),
            Map.entry("DOPAMINE", "bright dopamine color blocking, high-energy saturated hues"),
            Map.entry("MORANDI", "muted Morandi palette, dusty desaturated tones, soft harmony"));

    private static final Map<String, String> LAYOUTS = Map.ofEntries(
            Map.entry("UNIFIED", "a coherent ten-nail set with one disciplined design language"),
            Map.entry("TWO_ACCENTS", "a coherent set with exactly two intentional accent nails per hand"),
            Map.entry("MICRO_FRENCH_LAYOUT", "a consistent micro-French rhythm with one subtle variation"),
            Map.entry("MISMATCHED", "a curated mismatched set where every nail differs but palette and material stay cohesive"));

    private static final Map<String, String> TRENDS = Map.ofEntries(
            Map.entry("ROSE_VELVET", "rose velvet cat-eye with champagne micro-metal edging"),
            Map.entry("SEA_GLASS", "translucent sea-glass blue jelly layered with watery chrome reflections"),
            Map.entry("BUTTER_MICRO_FRENCH", "butter-yellow micro French over a sheer milky base"),
            Map.entry("MIXED_METAL", "mixed-metal manicure using controlled silver and warm gold details"),
            Map.entry("AURORA_MAGNETIC", "aurora magnetic gel with violet, blue and silver optical shifts"),
            Map.entry("KOREAN_SYRUP", "Korean syrup and blush nails with translucent tonal layering"),
            Map.entry("JADE_CAT_EYE", "jade-green glass bead cat-eye with deep crystal light"),
            Map.entry("MINT_FRENCH", "mint micro French over a cool sheer base"),
            Map.entry("LACE_NAILS", "white lace detailing over a nude milky base, delicate and romantic"),
            Map.entry("REVERSE_FRENCH", "reverse French half-moon with a deep cuticle arch"),
            Map.entry("LEOPARD_PRINT", "refined leopard print accents, muted and unexpectedly chic"),
            Map.entry("METALLIC_FRENCH", "metallic French tips with bands of silver or plum cat-eye"),
            Map.entry("MILKY_WHITE", "milky white moonlight nails, glossy and luminous"),
            Map.entry("SUNSET_OMBRE", "sunset ombre with a warm coral-to-lavender gradient"),
            Map.entry("CUSTOM", "current salon-forward nail design with fresh, original decorative choices"));

    /**
     * 按设计风格收尾的"调性"提示词，替代旧的全局"小红书清透风"后缀。
     * 每种风格给模型一个明确的成品氛围，避免所有设计都被渲染成同一副面孔。
     */
    private static final Map<String, String> TONES = Map.ofEntries(
            Map.entry("QUIET_LUXURY", "Understated quiet-luxury finish, muted sophisticated tones, restrained shine, premium editorial taste. "),
            Map.entry("KOREAN_CLEAR", "Airy contemporary Korean finish, delicate luminous layers, fresh negative space, subtle glassy depth. "),
            Map.entry("RUNWAY", "High-fashion editorial finish, precise composition, modern sophistication, runway-wearable polish. "),
            Map.entry("FUTURISTIC", "Sleek futuristic finish, precise metallic accents, clean optical depth, minimal high-tech elegance. "),
            Map.entry("ROMANTIC", "Soft romantic finish, delicate feminine details, gentle diffused light, tender color harmony. "),
            Map.entry("SWEET_COOL", "Polished sweet-cool finish, playful accents balanced by sophisticated color, fresh and modern. "),
            Map.entry("MINIMALIST", "Clean minimalist finish, generous negative space, one quiet focal detail, calm and refined. "),
            Map.entry("Y2K", "Playful Y2K finish, chrome and candy accents, nostalgic fun with a modern edge. "),
            Map.entry("COQUETTE", "Soft coquette finish, bows, pearls and ribbon details, delicate feminine charm. "),
            Map.entry("OLD_MONEY", "Old-money polish, muted neutrals, quiet understated luster, timeless elegance. "),
            Map.entry("DOPAMINE", "Bold dopamine finish, vivid saturated color energy, joyful confident contrast. "),
            Map.entry("MORANDI", "Dusty Morandi finish, muted desaturated elegance, soft tonal harmony. "));

    public String compile(NailGenerateRequest request, boolean hasReference, Profile profile, String styleEnhance) {
        Profile p = profile == null ? Profile.SEEDREAM_4 : profile;
        StringBuilder prompt = new StringBuilder();
        if ("DESIGN_BOARD".equals(request.getCreativeMode())) {
            prompt.append(boardPreamble(p));
        } else {
            prompt.append(onHandPreamble(p));
        }
        if (hasReference) {
            prompt.append(referenceInstruction(request.getReferenceStrategy()));
        }
        // 创作描述是整张图的"主角"，必须放在靠前的位置；
        // 放在长提示词末尾会被模型忽略，导致结果与用户描述不符。
        prompt.append("Primary design concept: ").append(request.getPrompt().trim()).append(". ");
        prompt.append("Nail shape: ").append(value(SHAPES, request.getNailShape(), "SHORT_ALMOND")).append(". ");
        prompt.append("Material execution: ").append(value(FINISHES, request.getFinish(), "VELVET_CAT_EYE")).append(". ");
        prompt.append("Art direction: ").append(value(STYLES, request.getDesignStyle(), "QUIET_LUXURY")).append(". ");
        prompt.append("Set composition: ").append(value(LAYOUTS, request.getLayoutStyle(), "TWO_ACCENTS")).append(". ");
        prompt.append("Trend direction: ").append(value(TRENDS, request.getTrendPreset(), "ROSE_VELVET")).append(". ");
        if (request.getColorPalette() != null && !request.getColorPalette().isBlank()) {
            prompt.append("Color palette: ").append(request.getColorPalette().trim()).append(". ");
        }
        prompt.append(closing(p));
        if (styleEnhance != null && !styleEnhance.isBlank()) {
            prompt.append(styleEnhance.trim()).append(". ");
        }
        String tone = toneFor(request.getDesignStyle());
        if (tone != null) {
            prompt.append(tone);
        }
        return prompt.toString();
    }

    private String onHandPreamble(Profile p) {
        return switch (p) {
            case SEEDREAM_5_PRO -> "High-end salon campaign close-up of two elegant natural female hands, anatomically correct with exactly five fingers each, natural relaxed pose, the manicure fills the frame as the hero subject, realistic skin texture with visible pores. ";
            case SEEDREAM_5_LITE -> "Close-up salon photo of two natural female hands with exactly five fingers each, realistic anatomy and skin, relaxed pose, the manicure is the main subject. ";
            case SEEDREAM_4_5 -> "High-end salon editorial close-up of two elegant natural female hands with exactly five fingers each, natural anatomy, relaxed graceful pose, manicure as the hero subject, realistic skin. ";
            default -> "Luxury editorial macro photograph of two elegant natural human hands, anatomically correct with exactly five fingers each, relaxed believable pose, realistic skin texture with natural pores, manicure is the dominant subject. ";
        };
    }

    private String boardPreamble(Profile p) {
        return switch (p) {
            case SEEDREAM_5_PRO -> "Professional nail design board, exactly ten detached press-on nail tips laid flat in two even rows of five, bird's-eye flat-lay product shot on a clean ivory background, every nail fully visible, evenly lit and tack-sharp. ";
            case SEEDREAM_5_LITE -> "Professional nail design board, exactly ten press-on nail tips in two even rows of five, clean ivory background, evenly lit. ";
            case SEEDREAM_4_5 -> "Professional nail design board, exactly ten detached press-on nail tips laid flat in two even rows of five, bird's-eye flat-lay product shot, every nail fully visible and tack-sharp. ";
            default -> "Professional nail design board, exactly ten detached press-on nail tips laid flat in two even rows of five, top-down macro product photography, every nail fully visible and equally sharp. ";
        };
    }

    private String closing(Profile p) {
        return switch (p) {
            case SEEDREAM_5_PRO -> "Soft diffused studio light from the upper left, gentle curved specular highlights on the gel surface, warm ivory silk backdrop with a subtle gradient, precise cuticle spacing, clean sidewalls, realistic gel thickness, micro-details in tack-sharp macro focus, true-to-life color grading. ";
            case SEEDREAM_5_LITE -> "Soft diffused light, gentle gel highlights, warm ivory backdrop, precise cuticles, clean sidewalls, realistic gel thickness, tack-sharp micro-details, true-to-life color. ";
            case SEEDREAM_4_5 -> "Soft studio light, gentle specular highlights on the gel, clean ivory backdrop, precise cuticle spacing, clean sidewalls, realistic gel thickness, micro-details in sharp macro focus, accurate color. ";
            default -> "Salon-feasible craftsmanship, precise cuticle spacing, clean sidewalls, realistic gel thickness, controlled highlights, micro-details in sharp focus, premium beauty campaign lighting, neutral stone-gray background, accurate color. ";
        };
    }

    private String toneFor(String designStyle) {
        return TONES.get(designStyle == null ? "" : designStyle);
    }

    private String value(Map<String, String> values, String key, String fallback) {
        return values.getOrDefault(key, values.get(fallback));
    }

    private String referenceInstruction(String strategy) {
        return switch (strategy == null ? "REINTERPRET" : strategy) {
            case "KEEP_PALETTE" -> "Use the reference image only for its color relationships; redesign the nail layout and techniques into a new original set. ";
            case "KEEP_LAYOUT" -> "Preserve the reference image's nail-to-nail layout rhythm, but update colors, materials and details into an original current design. ";
            case "KEEP_TEXTURE" -> "Preserve the reference image's dominant material and optical texture, but create a new palette and nail-to-nail composition. ";
            default -> "Treat the reference as creative direction only; produce an original elevated reinterpretation, never a literal copy. ";
        };
    }
}
