package ru.minikhanov.cloud_storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.LoginForm;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import javax.annotation.security.RolesAllowed;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StorageController {
    @Autowired
    private StorageService storageService;
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginForm loginForm){
        System.out.println("post login");
        return authService.getToken(loginForm.getLogin(), loginForm.getPassword());
    }
/*
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(){
    }*/

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addFile(@RequestParam("hash") String hash, @RequestParam("file") MultipartFile multipartFile){
        return storageService.getFileInfo(hash, multipartFile);
        //return ResponseEntity.ok().body("body post file");
    }

    @DeleteMapping("/file")
    public ResponseEntity<Object> deleteFile(){
        return ResponseEntity.ok().body("body delete file");
    }

    @GetMapping("/file")
    public ResponseEntity<Object> getFile(){
        return ResponseEntity.ok().body("body get file");
    }

    @PutMapping("/file")
    public ResponseEntity<Object> putFile(){
        return ResponseEntity.ok().body("body put file");
    }

    @GetMapping("/list")
    public List<EntityFile> getFiles(){
        return storageService.getAllFiles();
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handlerIllegalArgumentException(Exception e){
        return exceptionMessage(e.getMessage());
    }

    @ExceptionHandler({IOException.class, NullPointerException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handlerIOException(Exception e){
        return exceptionMessage(e.getMessage());
    }

    @ExceptionHandler(AuthorizationServiceException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handlerUnauthorized(Exception e){
        return exceptionMessage(e.getMessage());
    }

    private Map<String, Object> exceptionMessage(String message){
        Map<String, Object> answerObject = new HashMap<>();
        answerObject.put("id", 0);
        answerObject.put("message", message);
        return answerObject;
    }
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(100000);
        return multipartResolver;
    }
}
