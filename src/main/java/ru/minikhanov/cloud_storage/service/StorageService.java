package ru.minikhanov.cloud_storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.minikhanov.cloud_storage.CloudStorageApplication;
import ru.minikhanov.cloud_storage.exceptions.StorageException;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.FileStorageProperties;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.auth.message.AuthException;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorageService {
    private final StorageRepository storageRepository;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AuthService authService;
    private FileStorageProperties rootPath;
    static final Logger log = LoggerFactory.getLogger(StorageService.class);
    @Autowired
    public StorageService(StorageRepository storageRepository, UserDetailsService userDetailsService, UserRepository userRepository, AuthService authService, FileStorageProperties fileStorageProperties) {
        this.storageRepository = storageRepository;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.authService = authService;
        this.rootPath = fileStorageProperties;
    }

    @PersistenceContext
    private EntityManager entityManager;

    /*public List<?> getFiles(String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        String login = principal.toString();
        List<String> files = storageRepository.findFiles(login);
        return files;
    }*/

    public List<EntityFile> getAllFiles(Integer limit) {
        String login = authService.getUser().getLogin();
        User user = userRepository.findByLogin(login).orElseThrow(()->{throw new RuntimeException("User not found");});
        //PageRequest pageRequest = PageRequest.of(0,limit);
        System.out.println(storageRepository.findEntityFilesByUser(user/*, PageRequest.of(0,limit))*/));

        return storageRepository.findEntityFilesByUser(user/*, PageRequest.of(0,limit)*/);
    }

    /*public ResponseEntity getFileInfo(String hash, MultipartFile multipartFile) {
        if (checkHex(hash, multipartFile)) {
            try (InputStream is = multipartFile.getInputStream()) {
                int data;
                while ((data = is.read()) != -1) {
                    System.out.print(data + " ");
                }
                return ResponseEntity.ok("added");
                        *//*"Hash:" + hash + ", Filename: " + multipartFile.getOriginalFilename()
                        + ", Name: " + multipartFile.getName() + ", InputStream: " + multipartFile.getInputStream() + " bytes: " + multipartFile.getBytes();
*//*
            } catch (IOException ex) {
                ex.getMessage();
            }
        }
        return null;

    }*/

    public String getHexFromFile(String fileName){
        User user = authService.getUser();
        Path file = Paths.get(rootPath.getUploadDir(), user.getLogin(), fileName);
        if(!Files.exists(file) || !Files.isReadable(file)){
            throw new StorageException("File "+file+" is not exist");
        }
        String md5Hex;
        try {
            md5Hex = DigestUtils.md5DigestAsHex(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new StorageException("Can not get hash from file "+fileName);
        }
        return md5Hex;
    }

    /*public boolean checkHex(String hash, MultipartFile multipartFile) {
        String md5Hex;
        try {
            md5Hex = DigestUtils.md5DigestAsHex(new BufferedInputStream(multipartFile.getInputStream()));
            if (md5Hex.equals(hash)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new StorageException("Bad hash");
    }*/
    @Transactional
    public void addFile(EntityFile entityFile) {
        storageRepository.save(entityFile);
    }

    public Resource getFileByName(String filename) {
        Path file = Paths.get(rootPath.getUploadDir(), authService.getUser().getLogin(), filename);
        /*Path file = Paths.get(String.valueOf(path), filename);

        if (!Files.isReadable(file)){
            throw new StorageException("File is not readable");
        }
        try (InputStream inputStream = new UrlResource(file.toUri()).getInputStream();){
            Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e){
            throw new StorageException("Failed to store file "+filename);
        }*/
        try {
            Resource resource = new UrlResource(file.toUri());
            //InputStream fileInputStream = new FileInputStream(String.valueOf(file));
            /*Map<String, Resource> response = new HashMap<>();*/
            if (resource.exists() || resource.isReadable()) {
                InputStream inputStream = resource.getInputStream();
                //String md5Hex = DigestUtils.md5DigestAsHex(inputStream);
                /*response.put("hash", md5Hex);
                response.put("file", file.getFileName().toString());*/
                inputStream.close();
                return resource;
            } else {
                throw new StorageException(
                        "Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageException("Could not read file: " + filename, e);
        } catch (IOException e) {
            throw new StorageException("Could not get hash: " + filename, e);
        }

    }
    @Transactional
    public void deleteFile(String filename) {
        User user = authService.getUser();
        Path file = Paths.get(rootPath.getUploadDir(), user.getLogin(), filename);
        try {
            boolean result = Files.deleteIfExists(file);
            System.out.println("file exist: "+result+" file name: "+filename+" user id: "+ user.getId());
            storageRepository.deleteByFileNameAndUser(filename,user);
            /*entityManager.createQuery("DELETE FROM EntityFile WHERE fileName= :fileName and user= :user")
                    .setParameter("fileName", filename)
                    .setParameter("user", user)
                    .executeUpdate();*/
        } catch (IOException e) {
            log.error("IOException", IOException.class);
            throw new StorageException("File not found:", e);
        }
    }

    public void renameFile(String filename, String newFileName) {
        User user = authService.getUser();
        Path file = Paths.get(rootPath.getUploadDir(), user.getLogin(), filename);
        Path newFile = Paths.get(rootPath.getUploadDir(), user.getLogin(), newFileName);
        try {
            Files.move(file,newFile);
        } catch (IOException e) {
            storageRepository.deleteByFileNameAndUser(filename, user);
            throw new StorageException("file not found");
        }
        storageRepository.updateFileName(filename, newFileName, user.getId());
    }
}
