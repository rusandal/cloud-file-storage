package ru.minikhanov.cloud_storage.models;

import lombok.Data;
import ru.minikhanov.cloud_storage.models.security.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "storage_info",
        uniqueConstraints = { @UniqueConstraint(name = "FileNameAndIdUser", columnNames = { "fileName", "user_id" } ) })
public class EntityFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String fileName;
    @NotNull
    private String hash;
    private Long fileSize;
    private LocalDate uploadDate;
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "storage_users")
    private User user;
}
