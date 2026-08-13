package com.mdd.admin.nail.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface NailAssetStorage {
    StoredImage store(MultipartFile file, String namespace) throws IOException;
    StoredImage store(byte[] bytes, String mimeType, String namespace) throws IOException;
    byte[] read(String uri) throws IOException;
}
