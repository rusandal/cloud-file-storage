package ru.minikhanov.cloud_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.minikhanov.cloud_storage.models.EntityFile;

import java.util.List;

public interface StorageRepository extends JpaRepository<EntityFile, Long> {

    //@Query("select f from EntityFile f")
    @Query(value = "select * from storage_info;", nativeQuery = true)
    List<EntityFile> findAllFiles();
}
