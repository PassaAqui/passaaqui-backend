package com.passaaqui.backend.infra.integration.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String folder);
    void deleteFile(String fileName);
    String getFileUrl(String fileName);
}
