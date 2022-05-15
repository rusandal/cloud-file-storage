package ru.minikhanov.cloud_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.DigestUtils;
import ru.minikhanov.cloud_storage.models.FileStorageProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class CloudStorageApplication {
	public static HashSet<String> hashSetBadToken = new HashSet<>();
	//public static final String PATH = "storage";

	public static void main(String[] args) {
		SpringApplication.run(CloudStorageApplication.class, args);

		try (Scanner sc = new Scanner(new File("badtoken.txt"))){
			while (sc.hasNextLine()) {
				hashSetBadToken.add(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			String md5Hex = DigestUtils.md5DigestAsHex(new FileInputStream("badtoken.txt"));
			System.out.println(md5Hex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
