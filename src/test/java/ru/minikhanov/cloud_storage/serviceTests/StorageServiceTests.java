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
import org.springframework.core.io.Resource;
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
import ru.minikhanov.cloud_storage.models.FileStorageProperties;
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
import java.net.MalformedURLException;
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
    @MockBean
    private AuthService authServiceMock;
    @MockBean
    private FileStorageProperties rootPathMock;
    @Autowired
    private static MockUserUtils mockUserUtils;
    private static List<EntityFile> entityFileList = new ArrayList<>();
    private static User user = new User();
    private static UserDetailsImpl userDetails;
    private static Authentication authentication;
    private static final String ROOT_PATH = "storage_test";


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

    @BeforeEach
    public void createUser(){
        String login = "testUser";
        String password = "testPassword";
        Role role = roleRepository.save(new Role(ERole.ROLE_TEST));
        User newUser=User.builder().login(login).password(password).enabled(false).build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        newUser.setRole(roles);
        //user= userRepository.save(user);
        user = userRepository.save(newUser);
    }

    @Test
    @DisplayName("Get files by user")
    public void getAllFilesTest() {
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        //Assertions.assertEquals(3, storageService.fi.size());
        Assertions.assertEquals(3, storageService.getAllFiles(3).size());
    }

    //@Test
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
            pathDir = Path.of(ROOT_PATH, user.getLogin());
            if (!Files.exists(pathDir)) {
                Files.createDirectories(pathDir);
            }
            pathFile = Files.createFile(Path.of(String.valueOf(pathDir), "myTestFile.tmp"));
            Files.write(pathFile, "test test test".getBytes());
            Mockito.when(authServiceMock.getUser()).thenReturn(user);
            Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
            Resource resource = storageService.getFileByName("myTestFile.tmp");
            Assertions.assertTrue(resource.isFile());
            Assertions.assertEquals("myTestFile.tmp", resource.getFilename());
            Files.deleteIfExists(pathFile);
            Assertions.assertThrows(StorageException.class, ()->{storageService.getFileByName("myTestFile.tmp");});
        } catch (IOException e) {
            throw new RuntimeException(e);
        } /*finally {
            FileUtils.deleteQuietly(pathDir.toFile());
        }*/
    }

    @Test
    @DisplayName("delete file from DB and storage")
    public void deleteFileTest(){
        String deleteFile = "testFileName1.txt";
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        storageRepository.saveAll(entityFileList);
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        Path dir;
        Path file;
        try {
            dir = Files.createDirectories(Path.of(ROOT_PATH, user.getLogin()));
            file = Files.createFile(Paths.get(ROOT_PATH, user.getLogin(), deleteFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        storageService.deleteFile(deleteFile);
        FileUtils.deleteQuietly(dir.toFile());
        Assertions.assertFalse(storageRepository.findById(entityFileList.get(0).getId()).isPresent());
        Assertions.assertFalse(Files.isReadable(file));
    }

    @Test
    @DisplayName("Hex from file")
    public void getHexTest() throws IOException {
        Path dir = Files.createDirectories(Path.of(ROOT_PATH, user.getLogin()));
        Path file = Files.createFile(Paths.get(ROOT_PATH, user.getLogin(), "myTestFile.tmp"));
        Files.write(file, "test test test".getBytes());
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        Assertions.assertEquals(DigestUtils.md5DigestAsHex(Files.readAllBytes(file)), storageService.getHexFromFile("myTestFile.tmp"));
        Files.deleteIfExists(file);
        Assertions.assertThrows(StorageException.class, ()->{storageService.getHexFromFile("myTestFile.tmp");});
    }

    @Test
    @DisplayName("Rename File")
    public void renameFileTest() throws IOException {
        String newName = "newName.txt";
        for (EntityFile file : entityFileList) {
            file.setUser(user);
        }
        List<EntityFile> filesFromDb = storageRepository.saveAll(entityFileList);
        String oldName = filesFromDb.get(1).getFileName();
        Mockito.when(authServiceMock.getUser()).thenReturn(user);
        Mockito.when(rootPathMock.getUploadDir()).thenReturn(ROOT_PATH);
        FileUtils.deleteDirectory(new File(ROOT_PATH, user.getLogin()));
        Path dir = Files.createDirectories(Path.of(ROOT_PATH, user.getLogin()));
        Path file = Files.createFile(Paths.get(ROOT_PATH, user.getLogin(), oldName));
        Files.write(file, "test test test".getBytes());
        storageService.renameFile(oldName, newName);
        System.out.println(storageRepository.findAll());
        Assertions.assertEquals(newName, file.getFileName().toString());
        System.out.println(storageRepository.findAll());
        Assertions.assertEquals(newName, storageRepository.getById(filesFromDb.get(1).getId()).getFileName());
        Files.deleteIfExists(file);
    }
}
