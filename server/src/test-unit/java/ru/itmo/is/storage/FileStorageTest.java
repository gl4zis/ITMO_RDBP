package ru.itmo.is.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.exception.InternalServerErrorException;
import ru.itmo.is.exception.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageTest {

    private FileStorage fileStorage;
    private MultipartFile multipartFile;
    private String testStorageDir;

    @BeforeEach
    void setUp() throws IOException {
        testStorageDir = Files.createTempDirectory("test-storage").toString();
        fileStorage = new FileStorage(testStorageDir);

        multipartFile = mock(MultipartFile.class);
        lenient().when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
    }

    @Test
    void testSave_ShouldSaveFileAndReturnRecord() throws IOException {
        doAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            Files.createFile(path);
            return null;
        }).when(multipartFile).transferTo(any(Path.class));

        FileRecord result = fileStorage.save(multipartFile);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertEquals("test.pdf", result.getName());
        verify(multipartFile).transferTo(any(Path.class));
    }

    @Test
    void testSave_WhenDirectoryNotExists_ShouldCreateDirectory() throws IOException {
        String newDir = testStorageDir + "/new";
        ReflectionTestUtils.setField(fileStorage, "storageDir", newDir);
        doAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            return null;
        }).when(multipartFile).transferTo(any(Path.class));

        FileRecord result = fileStorage.save(multipartFile);

        assertNotNull(result);
        assertTrue(Files.exists(Paths.get(newDir)));
    }

    @Test
    void testSave_WhenIOException_ShouldThrowInternalServerErrorException() throws IOException {
        doThrow(new IOException("IO Error")).when(multipartFile).transferTo(any(Path.class));

        assertThrows(InternalServerErrorException.class, () -> {
            fileStorage.save(multipartFile);
        });
    }

    @Test
    void testGet_WhenFileExists_ShouldReturnFileData() throws IOException {
        String key = "test-key";
        Path filePath = Paths.get(testStorageDir, key);
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);

        FileRecord record = new FileRecord("test.pdf", key);
        FileData result = fileStorage.get(record);

        assertNotNull(result);
        assertEquals("test.pdf", result.name());
        assertNotNull(result.data());
    }

    @Test
    void testGet_WhenFileNotExists_ShouldThrowNotFoundException() {
        FileRecord record = new FileRecord("test.pdf", "non-existent-key");

        assertThrows(NotFoundException.class, () -> {
            fileStorage.get(record);
        });
    }

    @Test
    void testGet_WhenFileNotReadable_ShouldThrowNotFoundException() throws IOException {
        String key = "test-key";
        Path filePath = Paths.get(testStorageDir, key);
        Files.createDirectories(filePath.getParent());
        File file = Files.createFile(filePath).toFile();
        file.setReadable(false);

        FileRecord record = new FileRecord("test.pdf", key);

        assertThrows(NotFoundException.class, () -> {
            fileStorage.get(record);
        });

        file.setReadable(true);
    }

    @Test
    void testGenerateKey_ShouldGenerateUniqueKey() throws IOException {
        doAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            Files.createFile(path);
            return null;
        }).when(multipartFile).transferTo(any(Path.class));

        FileRecord result = fileStorage.save(multipartFile);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertEquals(8, result.getKey().length());
    }
}

