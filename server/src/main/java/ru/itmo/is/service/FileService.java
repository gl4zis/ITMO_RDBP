package ru.itmo.is.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.storage.FileData;
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
        FileRecord fr = fileStorage.save(file);
        var bidFile = new BidFile();
        bidFile.setKey(fr.getKey());
        bidFile.setName(fr.getName());
        bidFileRepository.save(bidFile);
        return fr.getKey();
    }

    public FileData get(String key) {
        return bidFileRepository.findById(key)
                .map(FileRecord::new)
                .map(fileStorage::get)
                .orElseThrow(() -> new NotFoundException("No file with such key"));
    }
}
