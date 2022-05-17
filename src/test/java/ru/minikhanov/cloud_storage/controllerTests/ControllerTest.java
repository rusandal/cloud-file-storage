package ru.minikhanov.cloud_storage.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.FileStorageProperties;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.jwt.JwtUtils;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;
import ru.minikhanov.cloud_storage.service.AuthService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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
    private RoleRepository roleRepository;
    @Autowired
    private StorageRepository storageRepository;
    @MockBean
    private AuthService authServiceMock;
    @MockBean
    private FileStorageProperties rootPathMock;
    /*@MockBean
    private StorageService storageServiceMock;*/
    @Autowired
    private JwtUtils jwtUtils;
    private static User user;
    private static UserDetailsImpl userDetails;
    private static UsernamePasswordAuthenticationToken authentication;
    private static List<EntityFile> entityFileList;
    private static final String ROOT_PATH = "storage_test";

    @BeforeEach
    public void createData() throws IOException {
        Role role = roleRepository.save(new Role(ERole.ROLE_TEST));
        User newUser = User.builder().login("testUser").password("testPassword").enabled(true).build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        newUser.setRole(roles);
        user = userRepository.save(newUser);
        userDetails = UserDetailsImpl.build(user);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        entityFileList = new ArrayList<>();
        EntityFile entityFile1 = EntityFile.builder().fileName("testFileName1.txt").fileSize(1L).hash("1").uploadDate(LocalDate.now()).build();
        EntityFile entityFile2 = EntityFile.builder().fileName("testFileName2.txt").fileSize(2L).hash("2").uploadDate(LocalDate.now()).build();
        EntityFile entityFile3 = EntityFile.builder().fileName("testFileName3.txt").fileSize(3L).hash("3").uploadDate(LocalDate.now()).build();
        entityFileList.add(entityFile1);
        entityFileList.add(entityFile2);
        entityFileList.add(entityFile3);
        Path pathDir = Path.of(ROOT_PATH, user.getLogin());
        if (!Files.exists(pathDir)) {
            Files.createDirectories(pathDir);
        }
    }

    @AfterEach
    public void delDir() throws IOException {
        Path pathDir = Path.of(ROOT_PATH, user.getLogin());
        FileUtils.deleteQuietly(pathDir.toFile());
    }


    @Test
    public void addFile() throws Exception {
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        String token = jwtUtils.generateJwtToken(authentication);
        MockMultipartFile myFile = new MockMultipartFile("file", "test.txt", "text/plane", "Spring Framework".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/file")
                                .file(myFile)
                                .param("filename", myFile.getOriginalFilename())
                                .header("auth-token", "Bearer " + token)
                        //.contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete file")
    public void deleteFileTest() throws Exception {
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        String token = jwtUtils.generateJwtToken(authentication);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        mockMvc.perform(delete("/file")
                        .param("filename", entityFileList.get(0).getFileName())
                        .header("auth-token", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get file")
    public void getFileTest() throws Exception {
        Path pathFile = Files.createFile(Path.of(ROOT_PATH, user.getLogin(), entityFileList.get(0).getFileName()));
        Files.write(pathFile, "test test test".getBytes());
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        String token = jwtUtils.generateJwtToken(authentication);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        mockMvc.perform(get("/file")
                                .param("filename", entityFileList.get(0).getFileName())
                                .header("auth-token", "Bearer " + token)
                        //.contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().bytes("test test test".getBytes()));
    }

    @Test
    @DisplayName("Get files")
    public void getFilesTest() throws Exception {
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        String token = jwtUtils.generateJwtToken(authentication);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        mockMvc.perform(get("/list")
                        .param("limit", "3")
                        .header("auth-token", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rename file")
    public void renameFile() throws Exception {
        Path pathFile = Files.createFile(Path.of(ROOT_PATH, user.getLogin(), entityFileList.get(0).getFileName()));
        Files.write(pathFile, "test test test".getBytes());
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        String token = jwtUtils.generateJwtToken(authentication);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        mockMvc.perform(put("/file")
                        .param("filename", entityFileList.get(0).getFileName())
                        .content("{\"filename\": \"renamed_file.txt\"}")
                        .header("auth-token", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }
}
