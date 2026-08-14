package com.mdd.admin.nail.provider;

import java.util.List;

public record NailGenerationCommand(
        String prompt,
        String model,
        String aspectRatio,
        String resolution,
        long seed,
        List<byte[]> referenceImages,
        List<String> referenceMimeTypes
) {}
