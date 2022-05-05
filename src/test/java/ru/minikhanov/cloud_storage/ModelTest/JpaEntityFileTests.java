package ru.minikhanov.cloud_storage.ModelTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;

import static ru.minikhanov.cloud_storage.models.security.ERole.ROLE_USER;

@Transactional
@SpringBootTest
public class JpaEntityFileTests {
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private static User user;
    private static EntityFile fileInfo;

    @BeforeAll
    public static void createUser(){
        user = new User(-1L, "testUser", "testPassword", false, new HashSet<>());
        fileInfo = new EntityFile(-1L, "testfile.xxx", "123", 1L, LocalDate.now(), user);
    }

    @Test
    @DisplayName("Create file in DB")
    public void addFileInDB(){
        user.getRole().add(roleRepository.findByName(ROLE_USER).get());
        userRepository.save(user);
        fileInfo.setUser(user);
        storageRepository.save(fileInfo);


        Assertions.assertEquals(true, storageRepository.existsByFileName(fileInfo.getFileName()));

    }
}
