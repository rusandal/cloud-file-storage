package ru.minikhanov.cloud_storage.Utils;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import ru.minikhanov.cloud_storage.serviceTests.StorageServiceTests;

public class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=jdbc:mysql://localhost:" + StorageServiceTests.mySQLContainer.getFirstMappedPort() + "/mydb?createDatabaseIfNotExist=true",
                "spring.datasource.username=" + StorageServiceTests.mySQLContainer.getUsername(),
                "spring.datasource.password=" + StorageServiceTests.mySQLContainer.getPassword()
        ).applyTo(configurableApplicationContext.getEnvironment());
    }
}
