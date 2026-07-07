package com.passaaqui.backend.infra.integration.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("dev")
@Primary
public class InMemoryStorageService implements StorageService {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        String id = UUID.randomUUID().toString();
        String key = folder + "/" + id + "_" + file.getOriginalFilename();
        store.put(key, "stored");
        return key;
    }

    @Override
    public void deleteFile(String fileName) {
        store.remove(fileName);
    }

    @Override
    public String getFileUrl(String fileName) {
        return "http://localhost:8080/files/" + fileName;
    }
}
