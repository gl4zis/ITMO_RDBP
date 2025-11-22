package ru.itmo.is.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.dto.response.FileResponse;
import ru.itmo.is.entity.bid.BidFile;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.BidFileRepository;
import ru.itmo.is.storage.FileRecord;
import ru.itmo.is.storage.FileStorage;

@Service
@RequiredArgsConstructor
public class FileService {
    private final BidFileRepository bidFileRepository;
    private final FileStorage fileStorage;

    @Transactional
    public String upload(MultipartFile file) {
        FileRecord record = fileStorage.save(file);
        var bidFile = new BidFile();
        bidFile.setKey(record.getKey());
        bidFile.setName(record.getName());
        bidFileRepository.save(bidFile);
        return record.getKey();
    }

    public FileResponse get(String key) {
        return bidFileRepository.findById(key)
                .map(FileRecord::new)
                .map(fileStorage::get)
                .orElseThrow(() -> new NotFoundException("No file with such key"));
    }
}
