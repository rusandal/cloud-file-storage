package ru.minikhanov.cloud_storage.serviceTests;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.minikhanov.cloud_storage.CloudStorageApplication;
import ru.minikhanov.cloud_storage.Utils.MockUserUtils;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@SpringBootTest
public class StorageServiceTests {
    @Autowired
    private StorageService storageService;
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @MockBean
    private AuthService authServiceMock;
    private static List<EntityFile> entityFileList = new ArrayList<>();
    private static User user = MockUserUtils.getMockUser("testUserStorageServiceTest");
    private static UserDetailsImpl userDetails;
    private static Authentication authentication;


    @BeforeAll
    public static void createData() {
        EntityFile entityFile1 = EntityFile.builder().fileName("testFileName1").fileSize(1L).hash("1").uploadDate(LocalDate.now()).build();
        EntityFile entityFile2 = EntityFile.builder().fileName("testFileName2").fileSize(2L).hash("2").uploadDate(LocalDate.now()).build();
        EntityFile entityFile3 = EntityFile.builder().fileName("testFileName3").fileSize(3L).hash("3").uploadDate(LocalDate.now()).build();
        entityFileList.add(entityFile1);
        entityFileList.add(entityFile2);
        entityFileList.add(entityFile3);
        userDetails = UserDetailsImpl.build(user);
        //LoginRequest loginRequest = new LoginRequest(user.getLogin(), user.getPassword());
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Get files by user")
    public void getAllFilesTest() {
        User userFromDb = userRepository.save(user);
        for (EntityFile file : entityFileList) {
            file.setUser(userFromDb);
        }
        storageRepository.saveAll(entityFileList);
        Assertions.assertEquals(3, storageService.getAllFiles().size());
    }

    @Test
    @DisplayName("Check file hex")
    public void checkHexTest() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plane", "Spring Framework".getBytes());
        String md5Hex;
        try {
            md5Hex = DigestUtils.md5DigestAsHex(new BufferedInputStream(multipartFile.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(storageService.checkHex(md5Hex, multipartFile));
    }

    @Test
    @DisplayName("Get file by name")
    public void getFileByNameTest() throws IOException {
        Path pathDir = null;
        Path pathFile;
        try {
            pathDir = Path.of(CloudStorageApplication.PATH + user.getLogin());
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            pathFile = Files.createFile(Path.of(String.valueOf(pathDir), "myTestFile.tmp"));
            Files.write(pathFile, "test test test".getBytes());
            Mockito.when(authServiceMock.getUser()).thenReturn(user);
            Map<String, String> fileInfo = storageService.getFileByName("myTestFile.tmp");
            System.out.println(fileInfo);
            Assertions.assertTrue(fileInfo.containsKey("file"));
            Assertions.assertTrue(fileInfo.containsKey("hash"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteDirectory(pathDir.toFile());
        }
    }
}
