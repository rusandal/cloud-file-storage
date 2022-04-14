package ru.minikhanov.cloud_storage.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "storage_info")
public class EntityFile {
    @Id
    private Long id;
    private String fileName;
    private int fileSize;
    private LocalDate uploadDate;
}
