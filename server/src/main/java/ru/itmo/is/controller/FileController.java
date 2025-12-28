package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.is.api.FileApi;
import ru.itmo.is.dto.OneFieldString;
import ru.itmo.is.dto.response.FileResponse;
import ru.itmo.is.service.FileService;

@Log4j2
@RestController
@RequiredArgsConstructor
public class FileController implements FileApi {
    private final FileService fileService;

    @Override
    public ResponseEntity<Resource> downloadFile(String key) {
        FileResponse file = fileService.get(key);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.name() + "\"")
                .body(file.data());
    }

    @Override
    public ResponseEntity<OneFieldString> uploadFile(MultipartFile file) {
        return ResponseEntity.ok(new OneFieldString(fileService.upload(file)));
    }
}
