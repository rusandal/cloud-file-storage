package ru.minikhanov.cloud_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.minikhanov.cloud_storage.models.FileStorageProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;

@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class CloudStorageApplication {
    public static HashSet<String> hashSetBadToken = new HashSet<>();

    public static void main(String[] args) throws IOException {
        SpringApplication.run(CloudStorageApplication.class, args);

        Path badtoken = Path.of("badtoken.txt");
        if(!Files.exists(badtoken)){
            Files.createFile(badtoken);
        }

        try (Scanner sc = new Scanner(new File("badtoken.txt"))) {
            while (sc.hasNextLine()) {
                hashSetBadToken.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
