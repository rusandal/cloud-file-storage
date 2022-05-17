package ru.minikhanov.cloud_storage.modelTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    public static void createUser() {
        user = User.builder().login("testUser").password("testPassword").enabled(false).build();
        fileInfo = EntityFile.builder().fileName("testfile").fileSize(1L).hash("123").uploadDate(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Create file in DB")
    public void addFileInDB() {
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(role);
        user.setRole(roles);
        User user1 = userRepository.save(user);
        fileInfo.setUser(user1);
        storageRepository.save(fileInfo);

        Assertions.assertEquals(true, storageRepository.existsByFileName(fileInfo.getFileName()));
    }


}
