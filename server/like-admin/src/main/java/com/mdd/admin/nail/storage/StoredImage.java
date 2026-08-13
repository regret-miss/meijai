package com.mdd.admin.nail.storage;

public record StoredImage(String uri, String mimeType, long fileSize, int width, int height,
                          String sha256, String thumb200Uri, String thumb600Uri) {
    public StoredImage(String uri, String mimeType, long fileSize, int width, int height) {
        this(uri, mimeType, fileSize, width, height, "", "", "");
    }
}
