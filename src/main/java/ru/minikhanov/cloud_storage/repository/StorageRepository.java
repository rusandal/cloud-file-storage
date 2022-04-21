package ru.minikhanov.cloud_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.minikhanov.cloud_storage.models.EntityFile;

import java.util.List;

public interface StorageRepository extends JpaRepository<EntityFile, Long> {

    //Boolean insertFile(String fileName, Long id);
    //@Query("select f from EntityFile f")
    @Query(value = "select * from storage_info where user_id=;", nativeQuery = true)
    List<EntityFile> findAllFiles();

    @Query(value = "select s.file_name from storage_info s left join storage_users u on s.user_id=u.id left join roles_table r on r.id=u.id where u.login=?1;", nativeQuery = true)
    List<String> findFiles(String login);
}
