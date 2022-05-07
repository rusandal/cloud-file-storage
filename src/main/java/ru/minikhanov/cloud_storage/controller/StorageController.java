package ru.minikhanov.cloud_storage.controller;

import io.jsonwebtoken.MalformedJwtException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import ru.minikhanov.cloud_storage.CloudStorageApplication;
import ru.minikhanov.cloud_storage.exceptions.StorageException;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.MessageResponse;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class StorageController {
    private final StorageService storageService;
    private final AuthService authService;

    public StorageController(StorageService storageService, AuthService authService) {
        this.storageService = storageService;
        this.authService = authService;
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public void addFile(@RequestParam("hash") String hash, @RequestParam("file") MultipartFile multipartFile){
        User user = authService.getUser();
        if (storageService.checkHex(hash, multipartFile)) {
            EntityFile entityFile = new EntityFile();
            entityFile.setFileName(multipartFile.getOriginalFilename());
            entityFile.setHash(hash);
            entityFile.setUploadDate(LocalDate.now());
            entityFile.setFileSize(multipartFile.getSize());
            entityFile.setUser(user);
            try {
                Path path = Path.of(CloudStorageApplication.PATH + user.getLogin());
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }
                multipartFile.transferTo(Paths.get(CloudStorageApplication.PATH + user.getLogin(), multipartFile.getOriginalFilename()));
                storageService.addFile(entityFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Hash is not correct");
        }

    }

    @DeleteMapping("/file")
    public void deleteFile(@RequestParam("filename") String filename){
        storageService.deleteFile(filename);
    }

    @GetMapping("/file")
    public Map<String, String> getFile(@RequestParam("filename") String filename) throws IOException{
        return storageService.getFileByName(filename);
    }

    @PutMapping("/file")
    public ResponseEntity<Object> putFile(){
        return ResponseEntity.ok().body("body put file");
    }

    @GetMapping("/list")
    public List<EntityFile> getFiles(){
        return storageService.getAllFiles();
    }

    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handlerIllegalArgumentException(Exception e){
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler({IOException.class, NullPointerException.class, ConstraintViolationException.class, StorageException.class, SizeLimitExceededException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponse handlerIOException(Exception e){
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler({AuthorizationServiceException.class, AuthenticationException.class, HttpClientErrorException.Unauthorized.class, MalformedJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handlerUnauthorized(Exception e){
        return new MessageResponse(e.getMessage());
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
