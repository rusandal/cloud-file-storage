package ru.minikhanov.cloud_storage.modelTest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

/*@DataJpaTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)*/
@Transactional
@SpringBootTest
public class JpaUserTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private static User user;

    @BeforeAll
    public static void createUser(){
        user = new User(-1L, "testUser", "testPassword", false, new HashSet<>());
    }

    @Test
    @DisplayName("Create user in DB")
    public void addUserAndRole(){
        //user = new User(-1L, "testUser", "testPassword", false, new HashSet<>());
        Set<Role> roles = user.getRole();
        Role role = roleRepository.findByName(ERole.ROLE_USER).get();
        roles.add(role);
        userRepository.save(user);

        Assertions.assertEquals(true, userRepository.existsByLogin("testUser"));
        Assertions.assertEquals(true, userRepository.findByLogin("testUser").get().getRole().contains(role));
    }

}
