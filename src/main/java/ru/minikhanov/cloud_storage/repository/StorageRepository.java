package ru.minikhanov.cloud_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.minikhanov.cloud_storage.models.EntityFile;
import ru.minikhanov.cloud_storage.models.security.User;

import javax.transaction.Transactional;
import java.util.List;

public interface StorageRepository extends JpaRepository<EntityFile, Long> {

    Boolean existsByFileName(String fileName);

    List<EntityFile> findEntityFilesByUser(User user/*, PageRequest limit*/);

    @Transactional
    void deleteByFileNameAndUser(String filename, User user);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update EntityFile ef set ef.fileName = :newFileName where ef.fileName = :fileName AND ef.user.id = :user_id")
    void updateFileName(@Param("fileName") String filename, @Param("newFileName") String name, @Param("user_id") Long user_id);
}
