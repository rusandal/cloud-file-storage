package ru.minikhanov.cloud_storage.controllerTests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.minikhanov.cloud_storage.Utils.MockUserUtils;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.LoginRequest;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.jwt.JwtUtils;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;
import ru.minikhanov.cloud_storage.service.AuthService;

import javax.transaction.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private MockUserUtils mockUserUtils;
    //private static String token;
    private static LoginRequest loginRequest;
    private static User user;
    private static UserDetailsImpl userDetails;
    private static UsernamePasswordAuthenticationToken authentication;
    private static EntityFile entityFile;

    @BeforeEach
    public void createData(){
        user = mockUserUtils.addTestUser("testUser", "testPassword");
        //token="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTY1MjI4ODkwNywiZXhwIjoxNjUyMzc1MzA3fQ.W06-wKsjh-xXoLA0qqmpD1b1ZJVk7s0FojWc6Zt5JO_VBvdnXtu5s7ul4g5wtT0DyVIH50DSp7SkO0Mb5B-nMQ";
        loginRequest = new LoginRequest(user.getLogin(), user.getPassword());
        userDetails = UserDetailsImpl.build(user);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        entityFile = EntityFile.builder().fileName("testFileName1.txt").fileSize(1L).hash("1").uploadDate(LocalDate.now()).build();

    }

    @BeforeEach
    public void createSecurityContext(){

        //LoginRequest loginRequest = new LoginRequest(user.getLogin(), user.getPassword());

    }

    @Test
    public void userRegistration_whenAdd_returnToken() throws Exception {
        userDetails=null;
        authentication=null;
        userDetails=null;
        user=null;
        SecurityContextHolder.createEmptyContext();
        LoginRequest newLoginRequest = new LoginRequest("login", "password");
        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(newLoginRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("auth_token").isString());

    }
    @Test
    public void userLogout() throws Exception {
        User persistUser = userRepository.save(user);
        String token = jwtUtils.generateJwtToken(authentication);
        System.out.println(persistUser);
        mockMvc.perform(post("/logou")
                        .header("auth_token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void putFile() throws Exception {
        User persistUser = userRepository.save(user);
        String token = jwtUtils.generateJwtToken(authentication);
        MockMultipartFile myFile = new MockMultipartFile("file", "test.txt", "text/plane", "Spring Framework".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/file")
                        .file(myFile)
                        .header("auth-token", token)
                        //.contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }
}
