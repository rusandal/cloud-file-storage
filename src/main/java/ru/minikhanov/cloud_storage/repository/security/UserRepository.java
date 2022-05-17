package ru.minikhanov.cloud_storage.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.minikhanov.cloud_storage.models.security.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    Boolean existsByLogin(String login);
}
