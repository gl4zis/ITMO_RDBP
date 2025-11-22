package ru.itmo.is.storage;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.dto.response.FileResponse;
import ru.itmo.is.exception.InternalServerErrorException;
import ru.itmo.is.exception.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorage {
    private final static int MAX_KEY_GEN_ATTEMPTS = 10;
    @Value("${file.storage.dir}")
    private String storageDir;

    public FileRecord save(MultipartFile file) {
        try {
            Path dir = Paths.get(storageDir);
            if (dir.toFile().exists()) {
                dir.toFile().mkdirs();
            }
            String key = generateKey();
            Path newFile = dir.resolve(key);
            file.transferTo(newFile);
            return new FileRecord(file.getOriginalFilename(), key);
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while file saving", e);
        }
    }

    public FileResponse get(FileRecord record) {
        Path path = Paths.get(storageDir, record.getKey());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists() || !resource.isReadable()) {
            throw new NotFoundException("File not found");
        }

        return new FileResponse(record.getName(), resource);
    }

    private String generateKey() {
        String key;
        File newFile;
        int counter = 0;
        do {
            key = RandomStringUtils.random(8, true, true);
            newFile = new File(storageDir, key);
            counter++;
            if (counter > MAX_KEY_GEN_ATTEMPTS) {
                throw new InternalServerErrorException("Cannot generate unique key for file");
            }
        } while (newFile.exists() && !newFile.isFile());
        return key;
    }
}
