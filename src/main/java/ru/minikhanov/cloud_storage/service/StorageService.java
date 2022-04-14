package ru.minikhanov.cloud_storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.repository.StorageRepository;

import java.util.List;

@Service
public class StorageService {
    @Autowired
    private StorageRepository storageRepository;

    public List<EntityFile> getAllFiles(){
        return storageRepository.findAllFiles();
    }
}
