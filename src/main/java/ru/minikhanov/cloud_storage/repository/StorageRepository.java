package ru.minikhanov.cloud_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.minikhanov.cloud_storage.models.EntityFile;

import java.util.List;

public interface StorageRepository extends JpaRepository<EntityFile, Long> {

    //Boolean insertFile(String fileName, Long id);
    //@Query("select f from EntityFile f")
    //@Query(value = "SELECT * FROM storage_info WHERE user_id=;", nativeQuery = true)
    //List<EntityFile> findAllFiles();

    @Query(value = "SELECT s.file_name FROM storage_info s LEFT JOIN storage_users u ON s.user_id=u.id LEFT JOIN roles_table r ON r.id=u.id WHERE u.login=?1;", nativeQuery = true)
    List<String> findFiles(String login);

    /*@Modifying
    @Query(value = "DELETE FROM storage_info WHERE file_name=?1 and user_id=?2", nativeQuery = true)
    void deleteByFilenameAndUserid(String filename, Long id);*/

}
