package ru.minikhanov.cloud_storage.serviceTests;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.minikhanov.cloud_storage.CloudStorageApplication;
import ru.minikhanov.cloud_storage.Utils.MockUserUtils;
import ru.minikhanov.cloud_storage.exceptions.StorageException;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.StorageRepository;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;
import ru.minikhanov.cloud_storage.security.services.UserDetailsImpl;
import ru.minikhanov.cloud_storage.service.AuthService;
import ru.minikhanov.cloud_storage.service.StorageService;

import javax.persistence.EntityManager;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@ContextConfiguration(initializers = {StorageServiceTests.Initializer.class})
@Testcontainers
@Transactional
public class StorageServiceTests {
    @Container
    private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8")
            .withDatabaseName("mydb")
            .withUsername("user")
            .withPassword("pass");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:mysql://localhost:"+ mySQLContainer.getFirstMappedPort()+"/mydb?createDatabaseIfNotExist=true",
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.password=" + mySQLContainer.getPassword()
                    //"spring.datasource.url=jdbc:mysql://localhost:3306/testdb?createDatabaseIfNotExist=true"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private StorageService storageService;
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EntityManager entityManager;
    @MockBean
    private AuthService authServiceMock;
    @Autowired
    private static MockUserUtils mockUserUtils;
    private static List<EntityFile> entityFileList = new ArrayList<>();
    private static User user = new User();
    private static UserDetailsImpl userDetails;
    private static Authentication authentication;


    @BeforeAll
    public static void createData() {

        //user=mockUserUtils.addTestUser("testUser", "testPassword");
        EntityFile entityFile1 = EntityFile.builder().fileName("testFileName1.txt").fileSize(1L).hash("1").uploadDate(LocalDate.now()).build();
        EntityFile entityFile2 = EntityFile.builder().fileName("testFileName2.txt").fileSize(2L).hash("2").uploadDate(LocalDate.now()).build();
        EntityFile entityFile3 = EntityFile.builder().fileName("testFileName3.txt").fileSize(3L).hash("3").uploadDate(LocalDate.now()).build();
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
        String login = "testUser";
        String password = "testPassword";
        Role role = roleRepository.save(new Role(ERole.ROLE_TEST));
        User user=User.builder().login(login).password(password).enabled(false).build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roles);
        //user= userRepository.save(user);
        User userFromDb = userRepository.save(user);
        for (EntityFile file : entityFileList) {
            file.setUser(userFromDb);
        }
        storageRepository.saveAll(entityFileList);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        //Assertions.assertEquals(3, storageService.fi.size());
        Assertions.assertEquals(3, storageService.getAllFiles(3).size());
    }

    @Test
    /*@DisplayName("Check file hex")
    public void checkHexTest() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plane", "Spring Framework".getBytes());
        String md5Hex;
        String badmd5Hex = "a";
        try {
            md5Hex = DigestUtils.md5DigestAsHex(new BufferedInputStream(multipartFile.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Throwable throwable = Assertions.assertThrows(StorageException.class, ()->{storageService.checkHex(badmd5Hex, multipartFile);});
        Assertions.assertTrue(storageService.checkHex(md5Hex, multipartFile));
        Assertions.assertNotNull(throwable);
    }*/

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

    @Test
    @DisplayName("delete file if not exist in storage")
    public void deleteFileIfNotInStorageTest(){
        String login = "testUser";
        String password = "testPassword";
        Role role = roleRepository.save(new Role(ERole.ROLE_TEST));
        User user=User.builder().login(login).password(password).enabled(false).build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roles);
        user= userRepository.save(user);
        User receiveUser = userRepository.save(user);
        for (EntityFile file : entityFileList) {
            file.setUser(receiveUser);
        }
        storageRepository.saveAll(entityFileList);
        Mockito.when(authServiceMock.getUser()).thenReturn(receiveUser);
        Assertions.assertThrows(StorageException.class, ()->{storageService.deleteFile("testFileName1.txt");});

        //Assertions.assertThrows(StorageException.class, ()->{storageService.deleteFile("testFileName1.txt");});
    }
    @Test
    @DisplayName("delete file if exist in storage")
    public void deleteFileIfInStorageTest(){
        String login = "testUser";
        String password = "testPassword";
        Role role = roleRepository.save(new Role(ERole.ROLE_TEST));
        User user=User.builder().login(login).password(password).enabled(false).build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roles);
        user= userRepository.save(user);
        String deleteFile = "testFileName1.txt";
        //User receiveUser = userRepository.save(user);
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Path dir;
        Path file;
        try {
            dir = Files.createDirectories(Path.of(CloudStorageApplication.PATH, user.getLogin()));
            file = Files.createFile(Paths.get(CloudStorageApplication.PATH, user.getLogin(), deleteFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(user);
        storageService.deleteFile(deleteFile);
        FileUtils.deleteQuietly(dir.toFile());
        Assertions.assertFalse(storageRepository.findById(entityFileList.get(1).getId()).get().getFileName().equals(deleteFile));
        /*Assertions.assertTrue(Files.exists());*/
        System.out.println(mySQLContainer);
        Assertions.assertThrows(StorageException.class, ()->{storageService.deleteFile("testFileName1.txt");});

    }
}
