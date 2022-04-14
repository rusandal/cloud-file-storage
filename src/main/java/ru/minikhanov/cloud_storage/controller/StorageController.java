package ru.minikhanov.cloud_storage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.service.StorageService;

import javax.annotation.security.RolesAllowed;
import java.util.List;

@RestController
@RequestMapping("/")
public class StorageController {
    @Autowired
    private StorageService storageService;

    //@RolesAllowed({"ROLE_ADMIN"})
    @GetMapping("/getAllFiles")
    public List<EntityFile> getAllFiles(){
        return storageService.getAllFiles();
    }

}
