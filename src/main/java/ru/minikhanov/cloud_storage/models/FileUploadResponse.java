package ru.minikhanov.cloud_storage.models;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class FileUploadResponse {
    private String hash;
    private Resource resource;
}
