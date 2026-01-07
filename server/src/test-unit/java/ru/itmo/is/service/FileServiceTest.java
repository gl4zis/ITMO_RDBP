package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.entity.bid.BidFile;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.BidFileRepository;
import ru.itmo.is.storage.FileData;
import ru.itmo.is.storage.FileRecord;
import ru.itmo.is.storage.FileStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private BidFileRepository bidFileRepository;
    @Mock
    private FileStorage fileStorage;
    @InjectMocks
    private FileService fileService;

    private MultipartFile multipartFile;
    private FileRecord fileRecord;
    private BidFile bidFile;
    private FileData fileData;

    @BeforeEach
    void setUp() {
        multipartFile = mock(MultipartFile.class);

        bidFile = new BidFile();
        bidFile.setKey("test-key-123");
        bidFile.setName("test.pdf");

        fileRecord = new FileRecord(bidFile);
        fileData = new FileData("test.pdf", null);
    }

    @Test
    void testUpload_ShouldSaveFileAndReturnKey() {
        when(fileStorage.save(multipartFile)).thenReturn(fileRecord);
        when(bidFileRepository.save(any(BidFile.class))).thenReturn(bidFile);

        String result = fileService.upload(multipartFile);

        assertNotNull(result);
        assertEquals("test-key-123", result);
        verify(fileStorage).save(multipartFile);
        verify(bidFileRepository).save(any(BidFile.class));
    }

    @Test
    void testGet_WhenFileExists_ShouldReturnFileData() {
        when(bidFileRepository.findById("test-key-123")).thenReturn(Optional.of(bidFile));
        when(fileStorage.get(any(FileRecord.class))).thenReturn(fileData);

        FileData result = fileService.get("test-key-123");

        assertNotNull(result);
        assertEquals("test.pdf", result.name());
        verify(bidFileRepository).findById("test-key-123");
        verify(fileStorage).get(any(FileRecord.class));
    }

    @Test
    void testGet_WhenFileNotFound_ShouldThrowNotFoundException() {
        when(bidFileRepository.findById("non-existent-key")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            fileService.get("non-existent-key");
        });

        verify(bidFileRepository).findById("non-existent-key");
        verify(fileStorage, never()).get(any(FileRecord.class));
    }
}

