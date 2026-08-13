package com.mdd.admin.nail.provider;

public interface NailImageProvider {
    void validateConfiguration();
    GeneratedImage generate(NailGenerationCommand command);
    String modelCode();
}
