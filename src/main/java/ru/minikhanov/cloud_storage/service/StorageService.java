package ru.minikhanov.cloud_storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public StorageService(StorageRepository storageRepository, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.storageRepository = storageRepository;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    public List<?> getFiles(String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        String login = principal.toString();
        List<String> files = storageRepository.findFiles(login);
        return files;
    }

    public List<EntityFile> getAllFiles() {
        return storageRepository.findAll();
    }

    public ResponseEntity getFileInfo(String hash, MultipartFile multipartFile) {
        if (checkHex(hash, multipartFile)) {
            try (InputStream is = multipartFile.getInputStream()) {
                int data;
                while ((data = is.read()) != -1) {
                    System.out.print(data + " ");
                }
                return ResponseEntity.ok("added");
                        /*"Hash:" + hash + ", Filename: " + multipartFile.getOriginalFilename()
                        + ", Name: " + multipartFile.getName() + ", InputStream: " + multipartFile.getInputStream() + " bytes: " + multipartFile.getBytes();
*/
            } catch (IOException ex) {
                ex.getMessage();
            }
        }
        return null;

    }

    public boolean checkHex(String hash, MultipartFile multipartFile) {
        String md5Hex = new String();
        try {
            md5Hex = DigestUtils.md5DigestAsHex(new BufferedInputStream(multipartFile.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (md5Hex.equals(hash)) {

            return true;
        }
        System.out.println("hash file " + md5Hex + ". Hash receive " + hash);
        return false;
    }
    @Transactional
    public void addFile(String hash, MultipartFile multipartFile) {
        if (checkHex(hash, multipartFile)) {
            User user = userRepository.getById(getUserAuthDetails().getId());
            EntityFile entityFile = new EntityFile();
            entityFile.setFileName(multipartFile.getOriginalFilename());
            entityFile.setHash(hash);
            entityFile.setUser(user);
            entityFile.setUploadDate(LocalDate.now());
            entityFile.setFileSize(multipartFile.getSize());
            try {
                Path path = Path.of(CloudStorageApplication.PATH + user.getLogin());
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }
                multipartFile.transferTo(Paths.get(CloudStorageApplication.PATH + user.getLogin(), multipartFile.getOriginalFilename()));
                storageRepository.save(entityFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Hash is not correct");
        }
    }

    public Map<String,String> loadFileResponse(String filename) {
        Path file = Paths.get(CloudStorageApplication.PATH + getUserAuthDetails().getUsername(), filename);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(file)){
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            Resource resource = new UrlResource(file.toUri());
            //InputStream fileInputStream = new FileInputStream(String.valueOf(file));
            Map<String, String> response = new HashMap<>();
            if (resource.exists() || resource.isReadable()) {
                String md5Hex = DigestUtils.md5DigestAsHex(resource.getInputStream());
                response.put("hash", md5Hex);
                response.put("file", sb.toString());
                return response;
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
        Path file = Paths.get(CloudStorageApplication.PATH + getUserAuthDetails().getUsername(), filename);
        try {
            boolean result = Files.deleteIfExists(file);
            System.out.println("file exist: "+result+" file name: "+filename+" user id: "+getUserAuthDetails().getId());
            entityManager.createQuery("DELETE FROM EntityFile WHERE fileName= :fileName and user= :userId")
                    .setParameter("fileName", filename)
                    .setParameter("userId", getUserAuthDetails().getId())
                    .executeUpdate();
            //storageRepository.deleteByFilenameAndUserid(filename, getUserAuthDetails().getId());
            if(!result){
                throw new StorageException("file not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public UserDetailsImpl getUserAuthDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails;
    }
}
