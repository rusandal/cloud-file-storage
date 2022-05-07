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
    private StorageRepository storageRepositoryMock;
    @MockBean
    private AuthenticationManager authenticationManager;
    private static User user = MockUserUtils.getMockUser("testUserName");
    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken authentication;

    /*@BeforeAll
    public void createUser(){
        user=User.builder().login("testUser").password("testPassword").enabled(false).id(99999999L).build();
        role = new Role(ERole.ROLE_USER);
        roles.add(role);
        user.setRole(roles);
                *//*roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    }*/

    @BeforeEach
    public void addUserToSecureContext(){
        userDetails = UserDetailsImpl.build(user);
        //LoginRequest loginRequest = new LoginRequest(user.getLogin(), user.getPassword());
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }
    @Test
    @DisplayName("Get userDetails from context")
    public void getUserDetailsFromSecurityContextTest(){
        UserDetailsImpl userDetailsFromContext = authService.getUserAuthDetails();
        //Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        //Mockito.when(authServiceBean.getUserAuthDetails()).thenReturn(userDetails);
        //Mockito.when(userRepository.getById(user.getId())).thenReturn()
        Assertions.assertEquals(userDetails, userDetailsFromContext);

        /*User user = Mockito(userRepository.findByLogin(login)
                .orElseThrow(()-> new UsernameNotFoundException("User not found: "+login));*/
        /*Mockito.when(authServiceBean.getUserAuthDetails().getId()).thenReturn(user);
        User user = authService.getUser();*/
        //Assertions.assertFalse();
    }

    @Test
    @DisplayName("Get token if login was find")
    public void getTokenTest(){
        Mockito.when(userRepository.existsByLogin(user.getLogin())).thenReturn(true);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        ResponseEntity<?> responseEntity = authService.getToken(user.getLogin(), user.getPassword());
        Assertions.assertEquals(responseEntity.getStatusCodeValue(), 200);
        Assertions.assertTrue(responseEntity.getBody().toString().contains("auth_token"));

    }
    @Test
    @DisplayName("Get token if login is not find")
    public void getTokenIfLoginNotFindTest(){
        Role role = new Role();
        role.setId(999);
        role.setName(ERole.ROLE_USER);
        Optional<Role> roleOptional = Optional.of(role);
        Mockito.when(userRepository.existsByLogin(user.getLogin())).thenReturn(false);
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(roleOptional);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        ResponseEntity<?> responseEntity = authService.getToken(user.getLogin(), user.getPassword());
        System.out.println(responseEntity.getBody());
        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(responseEntity.getBody().toString().contains("auth_token"));
    }
}
