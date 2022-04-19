package ru.minikhanov.cloud_storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.repository.StorageRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class StorageService {
    @Autowired
    private StorageRepository storageRepository;

    public List<EntityFile> getAllFiles(){
        return storageRepository.findAllFiles();
    }

    public String getFileInfo(String hash, MultipartFile multipartFile) {
        try(InputStream is = multipartFile.getInputStream()){
            int data;
            while ((data=is.read())!=-1){
                System.out.print(data + " ");
            }
            return "Hash:" + hash + ", Filename: " + multipartFile.getOriginalFilename()
                    + ", Name: " + multipartFile.getName() + ", InputStream: " + multipartFile.getInputStream() + " bytes: " + multipartFile.getBytes();

        } catch (IOException ex){
            return ex.getMessage();
        }
    }
}
