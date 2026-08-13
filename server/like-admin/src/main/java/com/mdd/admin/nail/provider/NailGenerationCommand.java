package com.mdd.admin.nail.provider;

public record NailGenerationCommand(
        String prompt,
        String aspectRatio,
        String resolution,
        byte[] referenceImage,
        String referenceMimeType
) {}
