package ru.minikhanov.cloud_storage.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.minikhanov.cloud_storage.exceptions.StorageException;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.FileStorageProperties;
import ru.minikhanov.cloud_storage.models.FilesResponse;
import ru.minikhanov.cloud_storage.models.NewFileName;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Log4j2
public class StorageController {
    private final StorageService storageService;
    private final AuthService authService;
    private final FileStorageProperties rootPath;

    public StorageController(StorageService storageService, AuthService authService, FileStorageProperties fileStorageProperties) {
        this.storageService = storageService;
        this.authService = authService;
        this.rootPath = fileStorageProperties;
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public void addFile(@RequestParam("file") MultipartFile multipartFile) {
        if (!multipartFile.getOriginalFilename().contains(".")) {
            throw new StorageException("The file must contain a '.'");
        }
        User user = authService.getUser();
        EntityFile entityFile = new EntityFile();
        entityFile.setFileName(multipartFile.getOriginalFilename());
        entityFile.setUploadDate(LocalDate.now());
        entityFile.setFileSize(multipartFile.getSize());
        entityFile.setUser(user);
        try {
            Path path = Path.of(rootPath.getUploadDir(), user.getLogin());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            multipartFile.transferTo(Paths.get(rootPath.getUploadDir(), user.getLogin(), multipartFile.getOriginalFilename()));
            storageService.addFile(entityFile);
            log.info("User: " + authService.getUser().getLogin() + " File " + entityFile.getFileName() + " was saved");
        } catch (IOException e) {
            log.warn("User directory not created or file not saved");
            throw new StorageException("User directory not created or file not saved");
        }
    }

    @DeleteMapping("/file")
    public void deleteFile(@RequestParam(value = "filename", required = false) String filename) {
        storageService.deleteFile(filename);
    }

    @GetMapping(value = "/file")
    public ResponseEntity<Resource> getFile(@RequestParam("filename") String filename) /*throws IOException*/ {
        Resource resource = storageService.getFileByName(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(resource);

    }

    @PutMapping("/file")
    @ResponseStatus(code = HttpStatus.OK)
    public void putFile(@RequestParam(value = "filename") String filename, @RequestBody NewFileName newFileName) {
        storageService.renameFile(filename, newFileName.getFileName());
    }

    @GetMapping("/list")
    public ResponseEntity<List<FilesResponse>> getFiles(@RequestParam("limit") Integer limit) {
        List<EntityFile> entityFileList = storageService.getAllFiles(limit);
        List<FilesResponse> filesResponseList = new ArrayList<>();
        for (EntityFile entityFile : entityFileList) {
            filesResponseList.add(new FilesResponse(entityFile.getFileSize(), entityFile.getFileName()));
        }
        return ResponseEntity.ok(filesResponseList);
    }
}
