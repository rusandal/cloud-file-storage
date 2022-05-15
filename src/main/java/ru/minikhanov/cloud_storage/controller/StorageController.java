package ru.minikhanov.cloud_storage.controller;

import io.jsonwebtoken.MalformedJwtException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import ru.minikhanov.cloud_storage.CloudStorageApplication;
import ru.minikhanov.cloud_storage.exceptions.StorageException;
import ru.minikhanov.cloud_storage.models.*;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class StorageController {
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);
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
        } catch (IOException e) {
            throw new StorageException("User directory not created or file not saved");
        }
    }

    @DeleteMapping("/file")
    public void deleteFile(@RequestParam(value = "filename", required = false) String filename) {
        storageService.deleteFile(filename);
    }

    @GetMapping("/file")
    public Map<String, String> getFile(@RequestParam("filename") String filename) /*throws IOException*/ {
        return storageService.getFileByName(filename);
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



   /* private Map<String, Object> exceptionMessage(String message){
        Map<String, Object> answerObject = new HashMap<>();
        answerObject.put("id", 0);
        answerObject.put("message", message);
        return answerObject;
    }*/
    /*@Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(100000);
        return multipartResolver;
    }*/
}
