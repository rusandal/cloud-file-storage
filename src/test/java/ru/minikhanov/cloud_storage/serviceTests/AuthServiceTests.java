package ru.minikhanov.cloud_storage.serviceTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.service.AuthService;

import javax.transaction.Transactional;
import java.util.HashSet;

@Transactional
@SpringBootTest
public class AuthServiceTests {
    @Autowired
    private AuthService authService;
    @MockBean
    private StorageRepository storageRepositoryMock;
    @MockBean
    private AuthService authServiceBean;
    private static User user;
    private static Role role;

    @BeforeAll
    public void createAuth(){
        user=User.builder().login("testUser").password("testPassword").enabled(false).id(99999999L).build();
        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    }

    @Test
    @DisplayName("Get user from DB by auth")
    private void getUserTest(){
        user
        User user = Mockito(userRepository.findByLogin(login)
                .orElseThrow(()-> new UsernameNotFoundException("User not found: "+login));
        Mockito.when(authServiceBean.getUserAuthDetails().getId()).thenReturn()
        User user = authService.getUser();
        //Assertions.assertFalse();
    }
}
