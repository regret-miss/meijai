package com.mdd.admin.nail.service;

import com.mdd.admin.nail.dto.NailGenerateRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NailPromptCompiler {
    private static final Map<String, String> SHAPES = Map.of(
            "SHORT_ALMOND", "short almond nails with a refined natural apex",
            "SHORT_SQUOVAL", "short squoval nails with clean balanced sidewalls",
            "ALMOND", "medium almond nails with elegant tapered symmetry",
            "SQUARE", "modern square nails with precise straight edges",
            "COFFIN", "medium coffin nails with a slim architectural silhouette");
    private static final Map<String, String> FINISHES = Map.of(
            "VELVET_CAT_EYE", "multi-dimensional velvet magnetic cat-eye gel with a soft diffused light band",
            "JELLY", "translucent glass-like jelly gel with buildable color depth",
            "CHROME", "finely milled mirror chrome with controlled metallic reflection",
            "MICRO_FRENCH", "ultra-thin precision micro-French detailing",
            "AURA", "layered airbrushed aura haze with seamless tonal diffusion",
            "SCULPTED_GEL", "restrained translucent sculpted gel relief with salon-realistic thickness",
            "GLOSSY_GEL", "high-gloss salon gel with crisp curved specular highlights");
    private static final Map<String, String> STYLES = Map.of(
            "QUIET_LUXURY", "quiet luxury, restrained details, premium editorial taste",
            "KOREAN_CLEAR", "contemporary Korean clear nail art, airy spacing, delicate luminous layers",
            "RUNWAY", "fashion-week editorial direction, directional but commercially wearable",
            "FUTURISTIC", "refined futuristic minimalism, optical depth, precise metallic accents",
            "ROMANTIC", "modern romantic restraint, fine botanical or ribbon-like linework",
            "SWEET_COOL", "polished sweet-cool contrast, playful accents balanced by sophisticated color");
    private static final Map<String, String> LAYOUTS = Map.of(
            "UNIFIED", "a coherent ten-nail set with one disciplined design language",
            "TWO_ACCENTS", "a coherent set with exactly two intentional accent nails per hand",
            "MICRO_FRENCH_LAYOUT", "a consistent micro-French rhythm with one subtle variation",
            "MISMATCHED", "a curated mismatched set where every nail differs but palette and material stay cohesive");
    private static final Map<String, String> TRENDS = Map.of(
            "ROSE_VELVET", "2026 rose velvet cat-eye with champagne micro-metal edging",
            "SEA_GLASS", "2026 translucent sea-glass blue jelly layered with watery chrome reflections",
            "BUTTER_MICRO_FRENCH", "2026 butter-yellow micro French over a sheer milky base",
            "MIXED_METAL", "2026 mixed-metal manicure using controlled silver and warm gold details",
            "AURORA_MAGNETIC", "2026 aurora magnetic gel with violet, blue and silver optical shifts",
            "KOREAN_SYRUP", "2026 Korean syrup and blush nails with translucent tonal layering",
            "CUSTOM", "current salon-forward nail design with no generic decorative motifs");

    public String compile(NailGenerateRequest request, boolean hasReference) {
        StringBuilder prompt = new StringBuilder();
        if ("DESIGN_BOARD".equals(request.getCreativeMode())) {
            prompt.append("Professional nail artist design board, exactly ten detached press-on nail tips, no hands, arranged symmetrically in two clean rows of five, top-down macro product photography, every nail fully visible and equally sharp. ");
        } else {
            prompt.append("Luxury editorial macro photograph of two elegant natural human hands, exactly five fingers on each hand, relaxed believable pose, manicure is the dominant subject, skin texture realistic and not over-retouched. ");
        }
        if (hasReference) {
            prompt.append(referenceInstruction(request.getReferenceStrategy()));
        }
        prompt.append("Nail shape: ").append(value(SHAPES, request.getNailShape(), "SHORT_ALMOND")).append(". ");
        prompt.append("Material execution: ").append(value(FINISHES, request.getFinish(), "VELVET_CAT_EYE")).append(". ");
        prompt.append("Art direction: ").append(value(STYLES, request.getDesignStyle(), "QUIET_LUXURY")).append(". ");
        prompt.append("Set composition: ").append(value(LAYOUTS, request.getLayoutStyle(), "TWO_ACCENTS")).append(". ");
        prompt.append("Trend direction: ").append(value(TRENDS, request.getTrendPreset(), "ROSE_VELVET")).append(". ");
        if (request.getColorPalette() != null && !request.getColorPalette().isBlank()) {
            prompt.append("Color palette: ").append(request.getColorPalette().trim()).append(". ");
        }
        prompt.append("Creative brief: ").append(request.getPrompt().trim()).append(". ");
        prompt.append("Salon-feasible craftsmanship, precise cuticle spacing, clean sidewalls, realistic gel thickness, controlled highlights, micro-details in sharp focus, premium beauty campaign lighting, neutral stone-gray background, accurate color, sophisticated and trend-aware rather than generic. ");
        prompt.append("No text, no logo, no watermark, no jewelry blocking the nails, no duplicated nails, no malformed fingers, no plastic skin, no excessive decorations, no random flowers unless requested.");
        return prompt.toString();
    }

    public String negativePrompt() {
        return "多余手指，缺失手指，畸形手部，错误指甲数量，破损甲面，模糊，低清晰度，文字，Logo，水印";
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
