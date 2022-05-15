package ru.minikhanov.cloud_storage.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import ru.minikhanov.cloud_storage.models.security.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "storage_info",
        uniqueConstraints = { @UniqueConstraint(name = "FileNameAndIdUser", columnNames = { "fileName", "user_id" } ) })
public class EntityFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String fileName;
    private String hash;
    private Long fileSize;
    private LocalDate uploadDate;
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "storage_users")
    private User user;
}
