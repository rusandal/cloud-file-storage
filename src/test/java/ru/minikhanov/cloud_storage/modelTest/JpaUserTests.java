package ru.minikhanov.cloud_storage.modelTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.minikhanov.cloud_storage.Utils.MockUserUtils;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;

import javax.transaction.Transactional;
import java.util.Set;
@Transactional
@SpringBootTest
public class JpaUserTests {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private static User user = new MockUserUtils().getMockUser("testUser");

    @BeforeAll
    public static void createUser() {
        //user = new User(-1L, "testUser", "testPassword", false, new HashSet<>());

    }

    @Test
    @DisplayName("Create user in DB")
    public void addUserAndRole() {
        //user = new User(-1L, "testUser", "testPassword", false, new HashSet<>());
        Set<Role> roles = user.getRole();
        Role role = roleRepository.findByName(ERole.ROLE_USER).get();
        roles.add(role);
        User receivedUser = userRepository.save(user);


        Assertions.assertEquals(true, userRepository.existsByLogin(user.getLogin()));
        Assertions.assertEquals(true, userRepository.findByLogin(user.getLogin()).get().getRole().contains(role));
    }

    @Test
    @DisplayName("Find user by login")
    public void findUserByLoginTest() {
        userRepository.save(user);
        User receivedUser = userRepository.findByLogin(user.getLogin()).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(receivedUser.getLogin(), user.getLogin());
    }

    @Test
    @DisplayName("Return throw if user not found")
    public void ifUserNotFound_ReturnThrow() {
        String badLogin = "qwertyasdfgh";
        Throwable throwable = Assertions.assertThrows(RuntimeException.class, () -> {
            userRepository.findByLogin(badLogin).orElseThrow(RuntimeException::new);
        });
        Assertions.assertNotNull(throwable);
    }
}
