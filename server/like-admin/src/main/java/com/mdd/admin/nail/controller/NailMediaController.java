package com.mdd.admin.nail.controller;

import com.mdd.admin.nail.service.NailMediaService;
import com.mdd.common.aop.NotLogin;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/nail/media")
public class NailMediaController {
    @Resource private NailMediaService mediaService;

    @NotLogin
    @GetMapping("/{type}/{id}")
    public ResponseEntity<byte[]> media(@PathVariable String type,
                                        @PathVariable Long id,
                                        @RequestParam(defaultValue = "600") String variant,
                                        @RequestParam(defaultValue = "false") boolean download,
                                        @RequestParam Long expires,
                                        @RequestParam String signature) {
        NailMediaService.MediaFile file = mediaService.read(type, id, variant, download, expires, signature);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(file.fileName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .cacheControl(CacheControl.maxAge(9, TimeUnit.MINUTES).cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.bytes());
    }
}
