package ru.minikhanov.cloud_storage.models;

import lombok.Data;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Data
public class File {
    private Long id;
    private String fileName;
    private int fileSize;
    private LocalDate uploadDate;
}
