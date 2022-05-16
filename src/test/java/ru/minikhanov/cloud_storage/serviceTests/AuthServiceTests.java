package ru.minikhanov.cloud_storage.serviceTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import ru.minikhanov.cloud_storage.Utils.MockUserUtils;
import ru.minikhanov.cloud_storage.models.LoginRequest;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;
import ru.minikhanov.cloud_storage.service.AuthService;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTests {
    @Autowired
    private AuthService authService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private AuthenticationManager authenticationManager;
    private static User user = MockUserUtils.getMockUser("testUserName");
    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    public void addUserToSecureContext(){
        userDetails = UserDetailsImpl.build(user);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }
    @Test
    @DisplayName("Get userDetails from context")
    public void getUserDetailsFromSecurityContextTest(){
        UserDetailsImpl userDetailsFromContext = authService.getUserAuthDetails();
        Assertions.assertEquals(userDetails, userDetailsFromContext);
    }

    @Test
    @DisplayName("Get token if login was find")
    public void getTokenTest(){
        Mockito.when(userRepository.existsByLogin(user.getLogin())).thenReturn(true);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        Assertions.assertFalse(authService.getToken(user.getLogin(), user.getPassword()).isEmpty());
    }
    @Test
    @DisplayName("New user get token")
    public void getTokenIfLoginNotFoundTest(){
        Role role = new Role();
        role.setId(999);
        role.setName(ERole.ROLE_USER);
        Optional<Role> roleOptional = Optional.of(role);
        Mockito.when(userRepository.existsByLogin(user.getLogin())).thenReturn(false);
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(roleOptional);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        Assertions.assertFalse(authService.getToken(user.getLogin(), user.getPassword()).isEmpty());
    }
}
