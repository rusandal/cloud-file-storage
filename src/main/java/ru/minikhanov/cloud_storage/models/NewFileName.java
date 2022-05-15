package ru.minikhanov.cloud_storage.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class NewFileName {
    @JsonProperty("filename")
    private String fileName;
}
