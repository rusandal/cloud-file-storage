package ru.minikhanov.cloud_storage.repository;

import org.springframework.data.domain.PageRequest;
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
    @Modifying
    @Query("update EntityFile ef set ef.fileName = :newFileName where ef.fileName = :fileName AND ef.user.id = :user_id")
    void updateFileName(@Param("fileName") String filename, @Param("newFileName") String name, @Param("user_id") Long user_id);
    //Boolean insertFile(String fileName, Long id);
    //@Query("select f from EntityFile f")
    //@Query(value = "SELECT * FROM storage_info WHERE user_id=;", nativeQuery = true)
    //List<EntityFile> findAllFiles();

    //@Query(value = "SELECT s.file_name FROM storage_info s LEFT JOIN storage_users u ON s.user_id=u.id LEFT JOIN roles_table r ON r.id=u.id WHERE u.login=?1;", nativeQuery = true)
    //List<String> findFiles(String login);


    //List<EntityFile> findAllByLo();
    /*@Modifying
    @Query(value = "DELETE FROM storage_info WHERE file_name=?1 and user_id=?2", nativeQuery = true)
    void deleteByFilenameAndUserid(String filename, Long id);*/

}
