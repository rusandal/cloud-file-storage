package ru.minikhanov.cloud_storage.serviceTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.service.StorageService;

import javax.transaction.Transactional;

@Transactional
@SpringBootTest
public class StorageServiceTests {
    @Autowired
    private StorageService storageService;
    @Autowired
    private StorageRepository storageRepository;

    @Test
    @DisplayName("Get files by user")
    public void getAllFilesTest(){

        Assertions.assertFalse(storageService.getAllFiles().isEmpty());
    }
}
