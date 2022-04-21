package ru.minikhanov.cloud_storage.controller;

import io.jsonwebtoken.MalformedJwtException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import ru.minikhanov.cloud_storage.exceptions.StorageException;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.LoginForm;
import ru.minikhanov.cloud_storage.models.MessageResponse;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import java.io.*;
import java.util.List;

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

    @PostMapping("/logou")
    public String logout(@RequestHeader("auth_token") String token){
        System.out.println("post logout");
        authService.deleteToken(token);
        return "Ok";
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.OK)
    public void addFile(@RequestParam("hash") String hash, @RequestParam("file") MultipartFile multipartFile){
        storageService.addFile(hash, multipartFile);
    }

    @DeleteMapping("/file")
    public ResponseEntity<Object> deleteFile(){
        return ResponseEntity.ok().body("body delete file");
    }

    @GetMapping("/file")
    public ResponseEntity<Object> getFile(@RequestParam("filename") String filename) throws IOException{
        return ResponseEntity.ok().body(storageService.loadFileResponse(filename));
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
    public MessageResponse handlerIllegalArgumentException(Exception e){
        return new MessageResponse(e.getMessage());
    }

    @ExceptionHandler({IOException.class, NullPointerException.class, ConstraintViolationException.class, StorageException.class})
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
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(100000);
        return multipartResolver;
    }
}
