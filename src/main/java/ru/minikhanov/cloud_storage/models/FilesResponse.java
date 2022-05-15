package ru.minikhanov.cloud_storage.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilesResponse {
    @JsonProperty("size")
    private Long size;
    @JsonProperty("filename")
    private String entityFileName;
}
